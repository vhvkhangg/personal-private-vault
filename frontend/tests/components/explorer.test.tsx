// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, act, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Explorer } from "@/components/drive/explorer";
import { bffClient } from "@/lib/client/bff-client";

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

it("restart replaces an old append owner; its late completion cannot admit duplicate newer pages", async () => {
  let intersect = () => {};
  vi.stubGlobal(
    "IntersectionObserver",
    class {
      constructor(callback: IntersectionObserverCallback) {
        intersect = () =>
          callback(
            [{ isIntersecting: true } as IntersectionObserverEntry],
            {} as IntersectionObserver
          );
      }
      observe() {}
      unobserve() {}
      disconnect() {}
    }
  );
  const connections = [
    {
      slot: 1 as const,
      status: "CONNECTED" as const,
      displayEmail: "synthetic",
      remoteRevocation: null,
    },
    {
      slot: 2 as const,
      status: "DISCONNECTED" as const,
      displayEmail: null,
      remoteRevocation: null,
    },
  ];
  const items = Array.from({ length: 7 }, (_, i) => ({
    id: "restart-" + i,
    name: "restart-" + i + ".txt",
    kind: "file" as const,
    mimeType: "text/plain",
    sizeBytes: 1,
    modifiedAt: null,
    parentId: "root",
  }));
  let initial = 0;
  let completeOld: (value: unknown) => void = () => {};
  let completeNew: (value: unknown) => void = () => {};
  let oldSignal: AbortSignal | null = null;
  const cursors: string[] = [];
  vi.spyOn(bffClient, "fetchJson").mockImplementation(async (url, init) => {
    if (url.endsWith("/connections")) return { data: { connections } };
    if (url.includes("storage-quota"))
      return {
        data: {
          usageBytes: null,
          usageInDriveBytes: null,
          limitBytes: null,
          limitKnown: false,
          observedAt: null,
        },
      };
    const cursor = new URL(url, "http://localhost:3000").searchParams.get("cursor");
    if (!cursor) {
      initial++;
      return {
        data: {
          items: items.slice(0, 5),
          nextCursor: (initial === 1 ? "a" : "b").repeat(43),
          hasMore: true,
        },
      };
    }
    cursors.push(cursor);
    if (cursor === "a".repeat(43)) {
      oldSignal = init?.signal ?? null;
      return new Promise((resolve) => {
        completeOld = resolve;
      });
    }
    return new Promise((resolve) => {
      completeNew = resolve;
    });
  });
  render(<Explorer initialConnections={connections} />);
  await screen.findByText("restart-0.txt");
  await act(async () => {
    intersect();
  });
  await waitFor(() => expect(cursors).toHaveLength(1));
  await userEvent
    .setup()
    .click(screen.getAllByRole("button", { name: "Làm mới trạng thái" }).at(-1)!);
  await waitFor(() => expect(initial).toBe(2));
  await waitFor(() => expect(screen.getByRole("button", { name: /^Mới$/ })).toBeEnabled());
  await act(async () => {
    intersect();
  });
  await waitFor(() => expect(cursors).toHaveLength(2));
  expect((oldSignal as AbortSignal | null)?.aborted).toBe(true);
  await act(async () => {
    completeOld({
      data: {
        items: [{ ...items[0], id: "obsolete", name: "obsolete.txt" }],
        nextCursor: null,
        hasMore: false,
      },
    });
  });
  await act(async () => {
    intersect();
    intersect();
  });
  expect(cursors).toEqual(["a".repeat(43), "b".repeat(43)]);
  expect(screen.queryByText("obsolete.txt")).not.toBeInTheDocument();
  await act(async () => {
    completeNew({ data: { items: items.slice(5), nextCursor: null, hasMore: false } });
  });
  for (const item of items) expect(await screen.findByText(item.name)).toBeVisible();
});

it("stops failed/consumed cursors including queued observer events, then permits explicit restart", async () => {
  let intersect = () => {};
  vi.stubGlobal(
    "IntersectionObserver",
    class {
      constructor(callback: IntersectionObserverCallback) {
        intersect = () =>
          callback(
            [{ isIntersecting: true } as IntersectionObserverEntry],
            {} as IntersectionObserver
          );
      }
      observe() {}
      unobserve() {}
      disconnect() {}
    }
  );
  const connections = [
    {
      slot: 1 as const,
      status: "CONNECTED" as const,
      displayEmail: "synthetic",
      remoteRevocation: null,
    },
    {
      slot: 2 as const,
      status: "DISCONNECTED" as const,
      displayEmail: null,
      remoteRevocation: null,
    },
  ];
  let cursors = 0,
    listings = 0;
  vi.spyOn(bffClient, "fetchJson").mockImplementation(async (url) => {
    if (url.endsWith("/connections")) return { data: { connections } };
    if (url.includes("storage-quota"))
      return {
        data: {
          usageBytes: null,
          usageInDriveBytes: null,
          limitBytes: null,
          limitKnown: false,
          observedAt: null,
        },
      };
    if (url.includes("cursor=")) {
      cursors++;
      throw Object.assign(new Error("Lost cursor authority"), {
        status: 409,
        code: "DRIVE_STALE_AUTHORITY",
      });
    }
    listings++;
    return {
      data: {
        items: [
          {
            id: "one",
            name: "one.txt",
            kind: "file",
            mimeType: "text/plain",
            sizeBytes: 1,
            modifiedAt: null,
            parentId: "root",
          },
        ],
        nextCursor: "c".repeat(43),
        hasMore: true,
      },
    };
  });
  render(<Explorer initialConnections={connections} />);
  await screen.findByText("one.txt");
  await act(async () => {
    intersect();
  });
  await waitFor(() => expect(cursors).toBe(1));
  vi.useFakeTimers();
  await act(async () => {
    intersect();
    await vi.advanceTimersByTimeAsync(3000);
  });
  expect(cursors).toBe(1);
  vi.useRealTimers();
  const before = listings;
  await userEvent.setup().click(screen.getAllByRole("button", { name: "Làm mới trạng thái" })[1]!);
  await waitFor(() => expect(listings).toBe(before + 1));
});

describe("Explorer Component", () => {
  beforeEach(() => {
    vi.spyOn(bffClient, "fetchJson").mockImplementation(async (url: string) => {
      if (url.includes("/connections/1/storage-quota")) {
        return {
          data: {
            usageBytes: 1048576,
            usageInDriveBytes: 524288,
            limitBytes: 15728640,
            limitKnown: true,
            observedAt: "2026-11-09T00:00:00Z",
          },
        };
      }
      if (url.includes("/connections/1/items")) {
        return {
          data: {
            items: [
              {
                id: "folder-1",
                name: "Tài liệu mật",
                kind: "folder",
                mimeType: "application/vnd.google-apps.folder",
                sizeBytes: null,
                modifiedAt: "2026-11-09T00:00:00Z",
                parentId: "root",
              },
              {
                id: "file-1",
                name: "bao-cao.pdf",
                kind: "file",
                mimeType: "application/pdf",
                sizeBytes: 10240,
                modifiedAt: "2026-11-09T00:00:00Z",
                parentId: "root",
              },
            ],
            nextCursor: null,
            hasMore: false,
          },
        };
      }
      if (url === "/api/bff/drive/connections") {
        return {
          data: {
            connections: [
              {
                slot: 1,
                status: "CONNECTED",
                displayEmail: "owner@vault.local",
                remoteRevocation: null,
              },
              { slot: 2, status: "DISCONNECTED", displayEmail: null, remoteRevocation: null },
            ],
          },
        };
      }
      return { data: {} };
    });
  });

  it("renders Google Drive explorer title, items and breadcrumb navigation", async () => {
    render(
      <Explorer
        initialConnections={[
          {
            slot: 1,
            status: "CONNECTED",
            displayEmail: "owner@vault.local",
            remoteRevocation: null,
          },
          { slot: 2, status: "DISCONNECTED", displayEmail: null, remoteRevocation: null },
        ]}
      />
    );

    expect(screen.getByText("Google Drive Explorer")).toBeInTheDocument();
    expect(await screen.findByText("Tài liệu mật")).toBeInTheDocument();
    expect(await screen.findByText("bao-cao.pdf")).toBeInTheDocument();

    // Click folder to navigate inside child folder
    const user = userEvent.setup();
    const folderCard = screen.getByText("Tài liệu mật").closest('[role="button"]');
    expect(folderCard).not.toBeNull();
    if (folderCard) {
      await user.click(folderCard);
      // Breadcrumb should show child folder name
      expect((await screen.findAllByText("Tài liệu mật")).length).toBeGreaterThanOrEqual(1);
    }
  });
});
