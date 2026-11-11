"use client";

import type { SessionStatusDto } from "../contracts";

class BffClient {
  private currentCsrfToken: string | null = null;

  constructor() {
    if (typeof window !== "undefined") {
      window.addEventListener("storage", (e) => {
        if (e.key === "ppv_logout_signal") {
          this.currentCsrfToken = null;
          if (!window.location.pathname.startsWith("/login")) {
            window.location.href = "/login";
          }
        }
      });
    }
  }

  public setCsrfToken(token: string): void {
    this.currentCsrfToken = token;
  }

  public getCsrfToken(): string | null {
    return this.currentCsrfToken;
  }

  public async getSessionStatus(): Promise<SessionStatusDto> {
    const res = await fetch("/api/bff/session", {
      method: "GET",
      cache: "no-store",
    });
    if (!res.ok) {
      throw new Error(`Failed to get session status: ${res.status}`);
    }
    const dto: SessionStatusDto = await res.json();
    if (dto.csrfToken) {
      this.currentCsrfToken = dto.csrfToken;
    }
    return dto;
  }

  public async fetchJson<T>(url: string, init: RequestInit = {}): Promise<T> {
    const headers = new Headers(init.headers || {});
    const method = (init.method || "GET").toUpperCase();

    if (method !== "GET" && method !== "HEAD") {
      if (!this.currentCsrfToken) {
        await this.getSessionStatus().catch(() => {});
      }
      if (this.currentCsrfToken) {
        headers.set("X-PPV-CSRF", this.currentCsrfToken);
      }
    }

    const res = await fetch(url, {
      ...init,
      headers,
      cache: "no-store",
    });

    if (!res.ok) {
      const errorJson = await res.json().catch(() => ({}));
      const code = errorJson.error?.code || `HTTP_${res.status}`;
      const message = errorJson.error?.message || res.statusText;
      const err = new Error(message);
      (err as unknown as { code: string; status: number }).code = code;
      (err as unknown as { code: string; status: number }).status = res.status;

      if (
        res.status === 401 &&
        code !== "AUTH_INVALID_CREDENTIALS" &&
        typeof window !== "undefined" &&
        !window.location.pathname.startsWith("/login") &&
        !window.location.pathname.startsWith("/setup")
      ) {
        try {
          window.localStorage.setItem("ppv_logout_signal", String(Date.now()));
        } catch {
          // Ignore localStorage access failures
        }
        window.location.href = "/login?error=session_expired";
      }

      throw err;
    }

    return res.json();
  }

  public async logout(): Promise<void> {
    await this.fetchJson("/api/bff/auth/logout", {
      method: "POST",
    });
    this.currentCsrfToken = null;
    if (typeof window !== "undefined") {
      try {
        window.localStorage.setItem("ppv_logout_signal", String(Date.now()));
      } catch {
        // Ignore localStorage access failures in private browsing
      }
      window.location.href = "/login";
    }
  }
}

export const bffClient = new BffClient();
