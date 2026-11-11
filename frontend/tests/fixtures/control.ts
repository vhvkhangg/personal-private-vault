export interface ControlStats {
  refreshTokensInDb: number;
  googleTokenRequests: number;
  oauthTokenExchanges: number;
  driveCalls: number;
  thumbnailDispatches: number;
  pendingTickets: number;
  fileCreates: number;
  childFolderCreates: number;
  refreshRequests: number;
  activeRefreshTokens: number;
  activeOperations: number;
  activeSpools: number;
  springRequests: number;
  responseHeld: boolean;
}

export interface ThumbnailConfig {
  status?: number;
  contentType?: string;
  useWebp?: boolean;
  usePng?: boolean;
  useJpeg?: boolean;
}

export class FixtureControlClient {
  private readonly controlOrigin: string;

  constructor(controlPort: number | string) {
    this.controlOrigin = `http://127.0.0.1:${controlPort}`;
  }

  async reset(): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/reset`, {
      method: "POST",
    });
    if (!res.ok) {
      throw new Error(`Control reset failed with status ${res.status}`);
    }
    try {
      await fetch("http://localhost:3000/api/bff/session", {
        headers: { "x-ppv-test-reset-sessions": "true" },
      });
    } catch {
      // ignore
    }
  }

  async seedOwnerGoogle(
    sub = "browser-owner-sub-1001",
    issuer = "https://accounts.google.com"
  ): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/seed-owner-google`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sub, issuer }),
    });
    if (!res.ok) {
      throw new Error(`Control seedOwnerGoogle failed with status ${res.status}`);
    }
  }

  async issueGoogleCode(
    code: string,
    nonce = "test-nonce",
    sub = "browser-owner-sub-1001"
  ): Promise<string> {
    const res = await fetch(`${this.controlOrigin}/api/test/issue-google-code`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, nonce, sub }),
    });
    if (!res.ok) {
      throw new Error(`Control issueGoogleCode failed with status ${res.status}`);
    }
    const data = (await res.json()) as { code: string };
    return data.code;
  }

  async connectSlot(slot: 1 | 2, sub = `google-sub-${slot}`): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/connect-slot`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ slot, sub }),
    });
    if (!res.ok) {
      throw new Error(`Control connectSlot failed with status ${res.status}`);
    }
  }

  async configure(
    action:
      | "issue-drive-code"
      | "advance-clock"
      | "drive-behavior"
      | "update-remote"
      | "hold-response"
      | "release-response"
      | "refresh-state",
    data: unknown
  ): Promise<void> {
    const response = await fetch(`${this.controlOrigin}/api/test/${action}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    if (!response.ok) throw new Error("Fixture control rejected " + action);
  }

  async setThumbnail(config: ThumbnailConfig): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/set-thumbnail`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(config),
    });
    if (!res.ok) {
      throw new Error(`Control setThumbnail failed with status ${res.status}`);
    }
  }

  async pauseRefresh(): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/pause-refresh`, {
      method: "POST",
    });
    if (!res.ok) {
      throw new Error(`Control pauseRefresh failed with status ${res.status}`);
    }
  }

  async waitRefreshEntered(): Promise<boolean> {
    const res = await fetch(`${this.controlOrigin}/api/test/wait-refresh-entered`, {
      method: "POST",
    });
    if (!res.ok) return false;
    const data = (await res.json()) as { entered: boolean };
    return data.entered;
  }

  async releaseRefresh(): Promise<void> {
    const res = await fetch(`${this.controlOrigin}/api/test/release-refresh`, {
      method: "POST",
    });
    if (!res.ok) {
      throw new Error(`Control releaseRefresh failed with status ${res.status}`);
    }
  }

  async getStats(): Promise<ControlStats> {
    const res = await fetch(`${this.controlOrigin}/api/test/stats`);
    if (!res.ok) {
      throw new Error(`Control getStats failed with status ${res.status}`);
    }
    return (await res.json()) as ControlStats;
  }

  async shutdown(): Promise<void> {
    try {
      await fetch(`${this.controlOrigin}/api/test/shutdown`, {
        method: "POST",
      });
    } catch {
      // Ignored
    }
  }
}
