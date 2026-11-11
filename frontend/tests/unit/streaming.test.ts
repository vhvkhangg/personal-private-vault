import { describe, it, expect, beforeEach } from "vitest";
import {
  acquireTransferAdmission,
  getActiveTransferCount,
  resetActiveTransfersForTest,
  createByteLimitingStream,
} from "@/lib/server/streaming";
import { BffException } from "@/lib/server/bff-errors";

describe("streaming", () => {
  beforeEach(() => {
    resetActiveTransfersForTest();
  });

  it("admits up to 2 concurrent binary transfers", () => {
    expect(getActiveTransferCount()).toBe(0);
    const release1 = acquireTransferAdmission();
    expect(getActiveTransferCount()).toBe(1);
    const release2 = acquireTransferAdmission();
    expect(getActiveTransferCount()).toBe(2);

    try {
      acquireTransferAdmission();
      expect.fail("Should have thrown BffException");
    } catch (err) {
      expect(err).toBeInstanceOf(BffException);
      expect((err as BffException).code).toBe("TRANSFER_CAPACITY_EXHAUSTED");
      expect((err as BffException).httpStatus).toBe(503);
    }

    release1();
    expect(getActiveTransferCount()).toBe(1);
    release2();
    expect(getActiveTransferCount()).toBe(0);
  });

  it("limits bytes passing through createByteLimitingStream", async () => {
    const { stream, getBytesRead } = createByteLimitingStream(10);
    const writer = stream.writable.getWriter();
    const reader = stream.readable.getReader();

    const writePromise = writer.write(new Uint8Array([1, 2, 3, 4, 5]));
    const read1 = await reader.read();
    await writePromise;

    expect(read1.value?.length).toBe(5);
    expect(getBytesRead()).toBe(5);

    // Exceed limit -> stream errors
    const errorWrite = writer.write(new Uint8Array([6, 7, 8, 9, 10, 11, 12]));
    await expect(Promise.all([errorWrite, reader.read()])).rejects.toThrow();
  });
});
