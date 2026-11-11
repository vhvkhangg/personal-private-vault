import "server-only";
import { serverConfig } from "./config";
import { BffException, trustedUpstreamError } from "./bff-errors";
import { sessionStore, type SessionRecord } from "./session-store";

const GLOBAL_TRANSFERS_KEY = Symbol.for("ppv.transfers.count");
type GlobalTransferScope = typeof globalThis & {
  [GLOBAL_TRANSFERS_KEY]?: number;
};
const gTransferScope = globalThis as GlobalTransferScope;
if (typeof gTransferScope[GLOBAL_TRANSFERS_KEY] !== "number") {
  gTransferScope[GLOBAL_TRANSFERS_KEY] = 0;
}

export function acquireTransferAdmission(): () => void {
  const current = gTransferScope[GLOBAL_TRANSFERS_KEY] ?? 0;
  if (current >= serverConfig.maxConcurrentTransfers) {
    throw new BffException(
      "TRANSFER_CAPACITY_EXHAUSTED",
      503,
      "Maximum concurrent binary transfers reached"
    );
  }
  gTransferScope[GLOBAL_TRANSFERS_KEY] = current + 1;
  let released = false;
  return () => {
    if (!released) {
      released = true;
      const count = gTransferScope[GLOBAL_TRANSFERS_KEY] ?? 1;
      gTransferScope[GLOBAL_TRANSFERS_KEY] = Math.max(0, count - 1);
    }
  };
}

export function getActiveTransferCount(): number {
  return gTransferScope[GLOBAL_TRANSFERS_KEY] ?? 0;
}

export function resetActiveTransfersForTest(): void {
  gTransferScope[GLOBAL_TRANSFERS_KEY] = 0;
}

/**
 * Creates a counting, bounded TransformStream that ensures total bytes do not exceed maxBytes.
 */
export function createByteLimitingStream(
  maxBytes: number,
  onProgress?: () => void
): {
  stream: TransformStream<Uint8Array, Uint8Array>;
  getBytesRead: () => number;
} {
  let bytesRead = 0;
  const stream = new TransformStream<Uint8Array, Uint8Array>({
    transform(chunk, controller) {
      onProgress?.();
      bytesRead += chunk.byteLength;
      if (bytesRead > maxBytes) {
        controller.error(
          new BffException("PAYLOAD_TOO_LARGE", 413, `Payload exceeds limit of ${maxBytes} bytes`)
        );
        return;
      }
      controller.enqueue(chunk);
    },
  });
  return { stream, getBytesRead: () => bytesRead };
}

/** Bounded bytes and time, including a stalled body. Never treat empty/malformed JSON as success. */
export async function readBoundedResponseJson<T = Record<string, unknown>>(
  res: Response,
  maxBytes: number = serverConfig.maxJsonBodyBytes
): Promise<T> {
  const length = res.headers.get("content-length");
  if (
    length !== null &&
    (!/^(0|[1-9][0-9]*)$/.test(length) ||
      !Number.isSafeInteger(Number(length)) ||
      Number(length) > maxBytes)
  ) {
    void res.body?.cancel().catch(() => {});
    throw new BffException("UPSTREAM_PAYLOAD_TOO_LARGE", 502);
  }
  if (!res.body) throw new BffException("UPSTREAM_INVALID_JSON", 502);
  const reader = res.body.getReader();
  const chunks: Uint8Array[] = [];
  let totalBytes = 0;
  let timer: ReturnType<typeof setTimeout> | undefined;
  const expired = new Promise<never>((_, reject) => {
    timer = setTimeout(() => {
      reject(new BffException("UPSTREAM_TIMEOUT", 504));
      void reader.cancel().catch(() => {});
    }, 10_000);
  });
  try {
    while (true) {
      const { done, value } = await Promise.race([reader.read(), expired]);
      if (done) break;
      totalBytes += value.byteLength;
      if (totalBytes > maxBytes) throw new BffException("UPSTREAM_PAYLOAD_TOO_LARGE", 502);
      chunks.push(value);
    }
    const bytes = new Uint8Array(totalBytes);
    let offset = 0;
    for (const chunk of chunks) {
      bytes.set(chunk, offset);
      offset += chunk.length;
    }
    const json: unknown = JSON.parse(new TextDecoder("utf-8", { fatal: true }).decode(bytes));
    if (!json || typeof json !== "object" || Array.isArray(json))
      throw new Error("Invalid envelope");
    return json as T;
  } catch (err) {
    void reader.cancel().catch(() => {});
    if (err instanceof BffException) throw err;
    throw new BffException("UPSTREAM_INVALID_JSON", 502);
  } finally {
    clearTimeout(timer);
    reader.releaseLock();
  }
}

/** Owned transfer timers and abort chain, including a pending upstream read. */
export function createTransferScope(record: SessionRecord, clientSignal: AbortSignal) {
  const release = acquireTransferAdmission();
  const controller = new AbortController();
  const generation = record.generation;
  let closed = false;
  let bodyTimer: ReturnType<typeof setTimeout> | undefined;
  let idleTimer: ReturnType<typeof setTimeout> | undefined;
  let unwatch = () => {};
  const clear = () => {
    clearTimeout(headerTimer);
    clearTimeout(bodyTimer);
    clearTimeout(idleTimer);
    clearTimeout(totalTimer);
    clearInterval(sessionTimer);
    unwatch();
    clientSignal.removeEventListener("abort", clientAbort);
    release();
  };
  const abort = (reason = new BffException("TRANSFER_ABORTED", 502)) => {
    if (closed) return;
    closed = true;
    controller.abort(reason);
    clear();
  };
  const clientAbort = () => abort(new BffException("TRANSFER_CANCELLED", 499));
  const checkSession = () => {
    const active = sessionStore.getSessionByIdDigest(record.idDigest);
    if (!active || active.generation !== generation) {
      abort(new BffException("SESSION_INVALIDATED", 401));
    }
  };
  unwatch = sessionStore.watchInvalidation(record, () =>
    abort(new BffException("SESSION_INVALIDATED", 401))
  );
  clientSignal.addEventListener("abort", clientAbort, { once: true });
  const headerTimer = setTimeout(
    () => abort(new BffException("TRANSFER_HEADER_TIMEOUT", 504)),
    10_000
  );
  const totalTimer = setTimeout(
    () => abort(new BffException("TRANSFER_TOTAL_TIMEOUT", 504)),
    180_000
  );
  const sessionTimer = setInterval(checkSession, 250);
  if (clientSignal.aborted) clientAbort();
  const touch = () => {
    clearTimeout(idleTimer);
    idleTimer = setTimeout(() => abort(new BffException("TRANSFER_IDLE_TIMEOUT", 504)), 15_000);
  };
  return {
    signal: controller.signal,
    abort,
    touch,
    checkSession,
    startBody() {
      clearTimeout(headerTimer);
      bodyTimer = setTimeout(() => abort(new BffException("TRANSFER_BODY_TIMEOUT", 504)), 120_000);
      touch();
    },
    close() {
      if (!closed) {
        closed = true;
        clear();
        controller.abort();
      }
    },
  };
}

/** One read per demand, <=64 KiB emitted chunks, exact framing and prompt abort. */
export function forwardDownload(
  body: ReadableStream<Uint8Array>,
  expectedBytes: number,
  scope: ReturnType<typeof createTransferScope>
): ReadableStream<Uint8Array> {
  const reader = body.getReader();
  let emitted = 0;
  let finished = false;
  let streamController: ReadableStreamDefaultController<Uint8Array>;
  const stop = (error: unknown) => {
    if (finished) return;
    finished = true;
    void reader.cancel(error).catch(() => {});
    reader.releaseLock();
    scope.signal.removeEventListener("abort", onAbort);
    streamController.error(error);
    scope.abort(
      error instanceof BffException ? error : new BffException("DRIVE_DOWNLOAD_FAILED", 502)
    );
  };
  const onAbort = () => stop(scope.signal.reason);
  return new ReadableStream<Uint8Array>(
    {
      start(controller) {
        streamController = controller;
        scope.signal.addEventListener("abort", onAbort, { once: true });
        if (scope.signal.aborted) onAbort();
      },
      async pull(controller) {
        try {
          scope.checkSession();
          if (finished) return;
          const { done, value } = await reader.read();
          scope.checkSession();
          if (finished) return;
          if (done) {
            if (emitted !== expectedBytes) throw new BffException("DRIVE_DOWNLOAD_TRUNCATED", 502);
            finished = true;
            scope.signal.removeEventListener("abort", onAbort);
            reader.releaseLock();
            scope.close();
            controller.close();
            return;
          }
          scope.touch();
          if (emitted + value.byteLength > expectedBytes)
            throw new BffException("DRIVE_DOWNLOAD_EXCESS", 502);
          emitted += value.byteLength;
          // Node fetch returns bounded network chunks; reject an unexpected oversized application chunk.
          if (value.byteLength > serverConfig.streamChunkSize) {
            throw new BffException("UPSTREAM_CHUNK_TOO_LARGE", 502);
          }
          controller.enqueue(value);
        } catch (error) {
          stop(error);
        }
      },
      cancel(reason) {
        if (finished) return;
        finished = true;
        scope.signal.removeEventListener("abort", onAbort);
        void reader.cancel(reason).catch(() => {});
        reader.releaseLock();
        scope.abort();
      },
    },
    { highWaterMark: 0 }
  );
}

/** One fixed Spring origin, no redirects, finite header and body budgets. */
export async function fetchSpringJson(
  path: string,
  init: RequestInit,
  maxBytes: number = serverConfig.maxJsonBodyBytes
): Promise<{ response: Response; data: Record<string, unknown> }> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 10_000);
  const headers = new Headers(init.headers);
  headers.set("Host", new URL(serverConfig.springOrigin).host);
  try {
    const response = await fetch(`${serverConfig.springOrigin}${path}`, {
      ...init,
      redirect: "error",
      cache: "no-store",
      signal: controller.signal,
      headers,
    });
    const json = await readBoundedResponseJson<{ data?: unknown; error?: unknown }>(
      response,
      maxBytes
    );
    if (!response.ok) throw trustedUpstreamError(json, response.status);
    if (!json.data || typeof json.data !== "object" || Array.isArray(json.data) || json.error) {
      throw new BffException("UPSTREAM_INVALID_RESPONSE", 502);
    }
    return { response, data: json.data as Record<string, unknown> };
  } catch (err) {
    if (err instanceof BffException) throw err;
    throw new BffException(
      controller.signal.aborted ? "UPSTREAM_TIMEOUT" : "UPSTREAM_UNAVAILABLE",
      controller.signal.aborted ? 504 : 502
    );
  } finally {
    clearTimeout(timer);
    controller.abort();
  }
}
