"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { bffClient } from "@/lib/client/bff-client";
import { Accounts } from "./accounts";
import { Quota } from "./quota";
import { Toolbar } from "./toolbar";
import { ItemGrid } from "./item-grid";
import { StatusPanel } from "./status-panel";
import { FolderDialog } from "./folder-dialog";
import { UploadPanel } from "./upload-panel";
import {
  type PendingUploadAction,
  createUploadAction,
  executeUpload,
} from "@/lib/client/upload-action";
import type {
  DriveConnectionDto,
  DriveItemDto,
  DriveItemPageDto,
  StorageQuotaDto,
  DriveItemTypeFilter,
} from "@/lib/contracts";
import { ChevronRight, Home, RefreshCw } from "lucide-react";

interface ExplorerProps {
  initialConnections?: DriveConnectionDto[];
}

export function Explorer({ initialConnections = [] }: ExplorerProps) {
  const [connections, setConnections] = React.useState<DriveConnectionDto[]>(initialConnections);
  const [activeSlot, setActiveSlot] = React.useState<1 | 2>(1);

  // Folder navigation & breadcrumbs
  const [currentFolderId, setCurrentFolderId] = React.useState<string | null>(null);
  const [currentFolderName, setCurrentFolderName] = React.useState<string>("Google Drive");

  // Filter & Search
  const [searchQuery, setSearchQuery] = React.useState("");
  const [typeFilter, setTypeFilter] = React.useState<DriveItemTypeFilter>("all");
  const [fromDate, setFromDate] = React.useState("");
  const [toDate, setToDate] = React.useState("");

  // Items and paging
  const [items, setItems] = React.useState<DriveItemDto[]>([]);
  const [nextCursor, setNextCursor] = React.useState<string | null>(null);
  const [hasMore, setHasMore] = React.useState(false);
  const [loading, setLoading] = React.useState(false);
  const [loadingMore, setLoadingMore] = React.useState(false);
  const [cursorError, setCursorError] = React.useState<string | null>(null);

  // Status & Quota
  const [quota, setQuota] = React.useState<StorageQuotaDto | null>(null);
  const [quotaLoading, setQuotaLoading] = React.useState(false);
  const [rootNotReady, setRootNotReady] = React.useState(false);
  const [ensuringRoot, setEnsuringRoot] = React.useState(false);

  // Modals & Panels
  const [folderDialogOpen, setFolderDialogOpen] = React.useState(false);
  const [uploadActions, setUploadActions] = React.useState<PendingUploadAction[]>([]);
  const [uploadAdmissionError, setUploadAdmissionError] = React.useState<string | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const newMenuTriggerRef = React.useRef<HTMLButtonElement>(null);
  const retainedUploadsRef = React.useRef(uploadActions);
  retainedUploadsRef.current = uploadActions;
  React.useEffect(
    () => () => {
      for (const action of retainedUploadsRef.current) action.cancel?.();
    },
    []
  );

  // Request generation counters to prevent race conditions when switching slots/filters
  const requestGenRef = React.useRef(0);
  const quotaGenRef = React.useRef(0);
  const activeSlotRef = React.useRef(activeSlot);
  React.useEffect(() => {
    activeSlotRef.current = activeSlot;
  }, [activeSlot]);
  const inFlightCursorRef = React.useRef<AbortController | null>(null);
  const currentCursorRef = React.useRef<string | null>(null);
  const cursorBlockedRef = React.useRef(false);
  const listingAbortRef = React.useRef<AbortController | null>(null);
  const listedViewRef = React.useRef<string | null>(null);
  const viewKey = JSON.stringify([
    activeSlot,
    currentFolderId,
    searchQuery.trim(),
    typeFilter,
    fromDate,
    toDate,
  ]);

  const activeConnection = connections.find((c) => c.slot === activeSlot) || {
    slot: activeSlot,
    status: "DISCONNECTED",
    displayEmail: null,
    remoteRevocation: null,
  };

  const isSearchActive = Boolean(searchQuery.trim() || typeFilter !== "all" || fromDate || toDate);

  // 1. Fetch connections
  const fetchConnections = React.useCallback(async () => {
    try {
      const data = await bffClient.fetchJson<{ data: { connections: DriveConnectionDto[] } }>(
        "/api/bff/drive/connections"
      );
      setConnections(data.data.connections);
    } catch (e) {
      console.error("Failed to fetch drive connections:", e);
    }
  }, []);

  // 2. Fetch Quota
  const fetchQuota = React.useCallback(async function doFetchQuota(slot: 1 | 2, retry = true) {
    const currentGen = ++quotaGenRef.current;
    await Promise.resolve();
    setQuotaLoading(true);
    try {
      const data = await bffClient.fetchJson<{ data: StorageQuotaDto }>(
        `/api/bff/drive/connections/${slot}/storage-quota`
      );
      if (quotaGenRef.current === currentGen && activeSlotRef.current === slot) {
        setQuota(data.data);
      }
    } catch (e: unknown) {
      const status = (e as { status?: number })?.status;
      if (retry && status === 409) {
        setTimeout(() => {
          if (activeSlotRef.current === slot && quotaGenRef.current === currentGen) {
            void doFetchQuota(slot, false);
          }
        }, 350);
        return;
      }
    } finally {
      if (quotaGenRef.current === currentGen && activeSlotRef.current === slot) {
        setQuotaLoading(false);
      }
    }
  }, []);

  // 3. Fetch Items
  const fetchItems = React.useCallback(
    async (cursor: string | null = null, append = false) => {
      if (append) {
        if (
          cursorBlockedRef.current ||
          listedViewRef.current !== viewKey ||
          cursor === null ||
          cursor !== currentCursorRef.current ||
          inFlightCursorRef.current !== null
        ) {
          return;
        }
      } else {
        // Explicit replacement releases obsolete admission; late completion retains no ownership.
        inFlightCursorRef.current = null;
        cursorBlockedRef.current = false;
        currentCursorRef.current = null;
        listedViewRef.current = null;
      }
      listingAbortRef.current?.abort();
      const requestAbort = new AbortController();
      listingAbortRef.current = requestAbort;
      if (append) inFlightCursorRef.current = requestAbort;
      const currentGen = ++requestGenRef.current;

      await Promise.resolve();
      if (!append) {
        setLoading(true);
        setLoadingMore(false);
        setCursorError(null);
      } else {
        setLoadingMore(true);
      }

      const params = new URLSearchParams();
      params.set("limit", "25");
      if (cursor) {
        params.set("cursor", cursor);
      }

      if (isSearchActive) {
        params.set("scope", "all");
        if (searchQuery.trim()) params.set("name", searchQuery.trim());
        if (typeFilter !== "all") params.set("type", typeFilter);
        if (fromDate) params.set("from", fromDate);
        if (toDate) params.set("to", toDate);
        params.set("timeZone", "Asia/Ho_Chi_Minh");
      } else {
        params.set("scope", "parent");
        params.set("parentId", currentFolderId || "root");
      }

      try {
        const res = await bffClient.fetchJson<{ data: DriveItemPageDto }>(
          `/api/bff/drive/connections/${activeSlot}/items?${params.toString()}`,
          { signal: requestAbort.signal }
        );

        if (requestGenRef.current !== currentGen) {
          return; // Stale request discarded
        }

        setRootNotReady(false);
        setNextCursor(res.data.nextCursor);
        currentCursorRef.current = res.data.nextCursor;
        listedViewRef.current = viewKey;
        setHasMore(res.data.hasMore);

        if (append) {
          setItems((prev) => [...prev, ...res.data.items]);
        } else {
          setItems(res.data.items);
        }
      } catch (err: unknown) {
        if (requestGenRef.current !== currentGen) {
          return;
        }

        const code = (err as { code?: string })?.code;
        const status = (err as { status?: number })?.status;
        // A failed cursor may already have been consumed. Even a queued old observer cannot replay it.
        cursorBlockedRef.current = true;
        currentCursorRef.current = null;
        if (code === "DRIVE_ROOT_NOT_READY") {
          setRootNotReady(true);
          setItems([]);
        } else if (code === "DRIVE_RECONNECT_REQUIRED") {
          setConnections((prev) =>
            prev.map((c) =>
              c.slot === activeSlot ? { ...c, status: "RECONNECT_REQUIRED" as const } : c
            )
          );
        } else if (code === "DRIVE_CURSOR_INVALID" || status === 409) {
          setNextCursor(null);
          setHasMore(false);
          setCursorError(vi.drive.explorer.cursorError);
        } else if (code === "DRIVE_SEARCH_INCOMPLETE" || code === "DRIVE_INCOMPLETE_SEARCH") {
          setCursorError(vi.drive.search.incompleteSearch);
        } else {
          setCursorError(vi.drive.errors.generic);
        }
      } finally {
        if (inFlightCursorRef.current === requestAbort) {
          inFlightCursorRef.current = null;
        }
        if (requestGenRef.current === currentGen) {
          setLoading(false);
          setLoadingMore(false);
        }
      }
    },
    [
      activeSlot,
      currentFolderId,
      isSearchActive,
      searchQuery,
      typeFilter,
      fromDate,
      toDate,
      viewKey,
    ]
  );

  const fetchItemsRef = React.useRef(fetchItems);
  fetchItemsRef.current = fetchItems;

  // Initial load on activeSlot change or connection change
  React.useEffect(() => {
    let ignore = false;
    void (async () => {
      try {
        const data = await bffClient.fetchJson<{ data: { connections: DriveConnectionDto[] } }>(
          "/api/bff/drive/connections"
        );
        if (!ignore) {
          setConnections(data.data.connections);
        }
      } catch (e) {
        console.error("Failed to fetch drive connections:", e);
      }
    })();
    return () => {
      ignore = true;
    };
  }, []);

  React.useEffect(() => {
    const generation = requestGenRef;
    let ignore = false;
    void (async () => {
      if (activeConnection.status === "CONNECTED") {
        await fetchItems();
        if (!ignore) {
          await fetchQuota(activeSlot);
        }
      } else {
        await Promise.resolve();
        if (!ignore) {
          setItems([]);
          setQuota(null);
          setRootNotReady(false);
        }
      }
    })();
    return () => {
      ignore = true;
      generation.current++;
      listingAbortRef.current?.abort();
      inFlightCursorRef.current = null;
    };
  }, [activeSlot, activeConnection.status, fetchQuota, fetchItems]);

  const handleSelectSlot = (slot: 1 | 2) => {
    if (slot !== activeSlot) {
      requestGenRef.current++;
      quotaGenRef.current++;
      setActiveSlot(slot);
      setCurrentFolderId(null);
      setCurrentFolderName("Google Drive");
      setSearchQuery("");
      setTypeFilter("all");
      setFromDate("");
      setToDate("");
      setItems([]);
      setQuota(null);
      setNextCursor(null);
      setHasMore(false);
      setCursorError(null);
    }
  };

  const handleOpenFolder = (folder: DriveItemDto) => {
    setCurrentFolderId(folder.id);
    setCurrentFolderName(folder.name);
    setSearchQuery("");
    setTypeFilter("all");
    setFromDate("");
    setToDate("");
  };

  const handleBackToRoot = () => {
    setCurrentFolderId(null);
    setCurrentFolderName("Google Drive");
  };

  const handleEnsureRoot = async () => {
    const targetSlot = activeSlot;
    setEnsuringRoot(true);
    try {
      await bffClient.fetchJson(`/api/bff/drive/connections/${targetSlot}/root-folder`, {
        method: "POST",
      });
      if (activeSlotRef.current !== targetSlot) return;
      setRootNotReady(false);
      await fetchItems();
      await fetchQuota(targetSlot);
    } catch (e) {
      console.error("Ensure root failed:", e);
    } finally {
      if (activeSlotRef.current === targetSlot) {
        setEnsuringRoot(false);
      }
    }
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const file = files[0];
    if (!file) return;

    // Reset file input value so same file can be selected again
    e.target.value = "";
    if (uploadActions.length >= 8) {
      setUploadAdmissionError(
        "Tối đa 8 hành động được giữ trong trang. Đóng mục đã hoàn tất trước khi chọn thêm tệp."
      );
      return;
    }
    setUploadAdmissionError(null);

    const targetSlot = activeSlot;
    const action = createUploadAction(file, targetSlot, currentFolderId);
    setUploadActions((prev) => [action, ...prev]);

    try {
      await executeUpload(action, () => {
        setUploadActions((prev) => [...prev]);
      });
      setUploadActions((prev) => [...prev]);
      if (activeSlotRef.current !== targetSlot) return;
      await fetchItemsRef.current();
      await fetchQuota(targetSlot);
    } catch {
      setUploadActions((prev) => [...prev]);
    }
  };

  const handleDismissUploadAction = (key: string) => {
    setUploadActions((prev) => prev.filter((a) => a.idempotencyKey !== key));
  };

  return (
    <div className="flex flex-col gap-6 max-w-7xl mx-auto">
      {/* Hidden file input for file upload */}
      <input
        ref={fileInputRef}
        type="file"
        className="hidden"
        onChange={handleFileSelect}
        aria-hidden="true"
      />

      {/* Top Header: Title, Accounts & Quota */}
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-xl font-bold text-white tracking-tight">{vi.drive.title}</h1>
          <p className="text-xs text-slate-400">Quản lý và sao lưu tài liệu an toàn trong Vault</p>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          <Accounts
            connections={connections}
            activeSlot={activeSlot}
            onSelectSlot={handleSelectSlot}
            onRefreshConnections={fetchConnections}
          />
          {activeConnection.status === "CONNECTED" && !rootNotReady && (
            <Quota quota={quota} loading={quotaLoading} />
          )}
        </div>
      </div>

      {/* Secondary States: DISCONNECTED / RECONNECT_REQUIRED / ROOT_NOT_READY */}
      {activeConnection.status === "DISCONNECTED" && (
        <StatusPanel
          type="DISCONNECTED"
          slot={activeSlot}
          onAction={() => {
            void bffClient
              .fetchJson<{ data: { authorizationUrl: string } }>(
                `/api/bff/drive/connections/${activeSlot}/authorize`,
                { method: "POST" }
              )
              .then((d) => (window.location.href = d.data.authorizationUrl));
          }}
        />
      )}

      {activeConnection.status === "RECONNECT_REQUIRED" && (
        <StatusPanel
          type="RECONNECT_REQUIRED"
          slot={activeSlot}
          onAction={() => {
            void bffClient
              .fetchJson<{ data: { authorizationUrl: string } }>(
                `/api/bff/drive/connections/${activeSlot}/authorize`,
                { method: "POST" }
              )
              .then((d) => (window.location.href = d.data.authorizationUrl));
          }}
        />
      )}

      {activeConnection.status === "CONNECTED" && rootNotReady && (
        <StatusPanel
          type="ROOT_NOT_READY"
          slot={activeSlot}
          loading={ensuringRoot}
          onAction={handleEnsureRoot}
        />
      )}

      {/* Main Explorer View when connected & root is ready */}
      {activeConnection.status === "CONNECTED" && !rootNotReady && (
        <div className="rounded-2xl border border-slate-800/80 bg-slate-900/60 backdrop-blur-xl shadow-2xl p-6 flex flex-col gap-6">
          {/* Breadcrumb Bar */}
          <div className="flex items-center justify-between text-xs text-slate-400">
            <div className="flex items-center gap-1.5 font-medium">
              <button
                type="button"
                onClick={handleBackToRoot}
                className="flex items-center gap-1 text-slate-300 hover:text-white transition-colors cursor-pointer"
              >
                <Home className="h-3.5 w-3.5" />
                <span>Google Drive</span>
              </button>

              {currentFolderId && (
                <>
                  <ChevronRight className="h-3.5 w-3.5 text-slate-600" />
                  <span className="text-white font-semibold">{currentFolderName}</span>
                </>
              )}
            </div>

            <button
              type="button"
              onClick={() => fetchItems()}
              disabled={loading}
              className="flex items-center gap-1 text-slate-400 hover:text-slate-200 transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3 w-3 ${loading ? "animate-spin" : ""}`} />
              <span>{vi.drive.actions.refreshStatus}</span>
            </button>
          </div>

          {/* Action & Filter Toolbar */}
          <Toolbar
            newMenuTriggerRef={newMenuTriggerRef}
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            typeFilter={typeFilter}
            onTypeFilterChange={setTypeFilter}
            fromDate={fromDate}
            onFromDateChange={setFromDate}
            toDate={toDate}
            onToDateChange={setToDate}
            onResetFilters={() => {
              setSearchQuery("");
              setTypeFilter("all");
              setFromDate("");
              setToDate("");
            }}
            onOpenFileUpload={() => fileInputRef.current?.click()}
            onOpenNewFolder={() => setFolderDialogOpen(true)}
            isInsideChildFolder={Boolean(currentFolderId)}
            disabled={loading}
          />

          {/* Items Card Grid */}
          <ItemGrid
            items={items}
            slot={activeSlot}
            loading={loading}
            loadingMore={loadingMore}
            hasMore={hasMore}
            onLoadMore={() => fetchItems(nextCursor, true)}
            onOpenFolder={handleOpenFolder}
            isSearchActive={isSearchActive}
            cursorError={cursorError}
            onRetryListing={() => fetchItems()}
          />
        </div>
      )}

      {/* New Folder Creation Dialog */}
      <FolderDialog
        returnFocusRef={newMenuTriggerRef}
        open={folderDialogOpen}
        onOpenChange={setFolderDialogOpen}
        slot={activeSlot}
        onFolderCreated={(createdSlot) => {
          if (createdSlot && activeSlotRef.current !== createdSlot) return;
          fetchItemsRef.current();
        }}
      />

      {/* Floating Upload Actions Panel */}
      {uploadAdmissionError && (
        <p role="alert" className="text-sm text-amber-300">
          {uploadAdmissionError}
        </p>
      )}
      <UploadPanel
        actions={uploadActions}
        onActionUpdated={() => setUploadActions((prev) => [...prev])}
        onDismissAction={handleDismissUploadAction}
      />
    </div>
  );
}
