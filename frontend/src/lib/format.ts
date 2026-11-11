/**
 * Formatting utilities for file sizes, dates and safe string handling.
 */

export function formatBytes(bytes: number | null | undefined): string {
  if (bytes === null || bytes === undefined) {
    return "—";
  }
  if (bytes < 0) {
    return "—";
  }
  if (bytes === 0) {
    return "0 B";
  }

  const k = 1024;
  const sizes = ["B", "KiB", "MiB", "GiB", "TiB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const safeI = Math.min(i, sizes.length - 1);
  const value = bytes / Math.pow(k, safeI);

  return `${value.toFixed(safeI === 0 ? 0 : 1)} ${sizes[safeI]}`;
}

/**
 * Format ISO timestamp into Vietnam timezone (Asia/Ho_Chi_Minh).
 */
export function formatDateTime(isoString: string | null | undefined): string {
  if (!isoString) {
    return "—";
  }
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) {
      return "—";
    }
    return new Intl.DateTimeFormat("vi-VN", {
      timeZone: "Asia/Ho_Chi_Minh",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false,
    }).format(d);
  } catch {
    return "—";
  }
}

/**
 * Format ISO timestamp into date-only string in Asia/Ho_Chi_Minh.
 */
export function formatDateOnly(isoString: string | null | undefined): string {
  if (!isoString) {
    return "—";
  }
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) {
      return "—";
    }
    return new Intl.DateTimeFormat("vi-VN", {
      timeZone: "Asia/Ho_Chi_Minh",
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(d);
  } catch {
    return "—";
  }
}

/**
 * Generate an action key matching Spring's required pattern:
 * yyyyMMddTHHmmssZ.lowercase-UUIDv4
 */
export function generateActionKey(): string {
  const now = new Date();
  const y = now.getUTCFullYear();
  const m = String(now.getUTCMonth() + 1).padStart(2, "0");
  const d = String(now.getUTCDate()).padStart(2, "0");
  const hh = String(now.getUTCHours()).padStart(2, "0");
  const mm = String(now.getUTCMinutes()).padStart(2, "0");
  const ss = String(now.getUTCSeconds()).padStart(2, "0");

  const timestamp = `${y}${m}${d}T${hh}${mm}${ss}Z`;
  const uuid = crypto.randomUUID().toLowerCase();

  return `${timestamp}.${uuid}`;
}
