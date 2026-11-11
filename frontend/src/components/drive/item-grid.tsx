"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { ItemCard } from "./item-card";
import { Button } from "@/components/ui/button";
import type { DriveItemDto } from "@/lib/contracts";
import { FolderOpen, SearchX, Loader2, RotateCcw, AlertCircle } from "lucide-react";

interface ItemGridProps {
  items: DriveItemDto[];
  slot: 1 | 2;
  loading: boolean;
  loadingMore: boolean;
  hasMore: boolean;
  onLoadMore: () => void;
  onOpenFolder: (folder: DriveItemDto) => void;
  isSearchActive: boolean;
  cursorError?: string | null;
  onRetryListing?: () => void;
}

export function ItemGrid({
  items,
  slot,
  loading,
  loadingMore,
  hasMore,
  onLoadMore,
  onOpenFolder,
  isSearchActive,
  cursorError,
  onRetryListing,
}: ItemGridProps) {
  const sentinelRef = React.useRef<HTMLDivElement>(null);

  // IntersectionObserver for cursor-backed infinite scroll
  React.useEffect(() => {
    if (!hasMore || loading || loadingMore || cursorError) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && document.visibilityState !== "hidden") {
          onLoadMore();
        }
      },
      { rootMargin: "200px" }
    );

    const el = sentinelRef.current;
    if (el) {
      observer.observe(el);
    }

    return () => {
      if (el) observer.unobserve(el);
    };
  }, [hasMore, loading, loadingMore, cursorError, onLoadMore]);

  if (loading && items.length === 0) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 animate-pulse">
        {Array.from({ length: 10 }).map((_, i) => (
          <div
            key={i}
            className="flex flex-col gap-2 rounded-xl border border-slate-800 bg-slate-900/40 p-4"
          >
            <div className="h-24 w-full rounded-lg bg-slate-800/60" />
            <div className="h-4 w-3/4 rounded bg-slate-800/60" />
            <div className="h-3 w-1/2 rounded bg-slate-800/40" />
          </div>
        ))}
      </div>
    );
  }

  if (items.length === 0 && cursorError) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-red-900/30 bg-red-950/20 p-16 text-center">
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-red-950/40 text-red-400">
          <AlertCircle className="h-6 w-6" />
        </div>
        <p className="text-sm font-medium text-red-300 mb-3">{cursorError}</p>
        <Button
          size="sm"
          variant="outline"
          onClick={onRetryListing}
          className="border-red-500/40 text-red-300 hover:bg-red-950/40 text-xs"
        >
          {vi.drive.actions.retry}
        </Button>
      </div>
    );
  }

  if (items.length === 0 && hasMore) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-slate-800/80 bg-slate-900/20 p-16 text-center">
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-slate-800 text-blue-400">
          <Loader2 className="h-6 w-6 animate-spin" />
        </div>
        <p className="text-sm font-medium text-slate-300">{vi.drive.search.searching}</p>
        <div ref={sentinelRef} className="h-4 w-full" />
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-slate-800/80 bg-slate-900/20 p-16 text-center">
        {isSearchActive ? (
          <>
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-slate-800 text-slate-400">
              <SearchX className="h-6 w-6" />
            </div>
            <p className="text-sm font-medium text-slate-300">{vi.drive.explorer.emptySearch}</p>
          </>
        ) : (
          <>
            <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-slate-800 text-slate-400">
              <FolderOpen className="h-6 w-6" />
            </div>
            <p className="text-sm font-medium text-slate-300">{vi.drive.explorer.emptyRoot}</p>
          </>
        )}
      </div>
    );
  }

  // Deduplicate items by ID
  const uniqueItems = Array.from(new Map(items.map((it) => [it.id, it])).values());

  return (
    <div className="flex flex-col gap-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {uniqueItems.map((item) => (
          <ItemCard key={item.id} item={item} slot={slot} onOpenFolder={onOpenFolder} />
        ))}
      </div>

      {/* Sentinel for infinite scroll */}
      {hasMore && !cursorError && (
        <div ref={sentinelRef} className="flex items-center justify-center py-6">
          {loadingMore && (
            <div className="flex items-center gap-2 text-xs text-slate-400">
              <Loader2 className="h-4 w-4 animate-spin text-blue-400" />
              <span>{vi.drive.explorer.loadingMore}</span>
            </div>
          )}
        </div>
      )}

      {/* Cursor error state */}
      {cursorError && (
        <div className="flex items-center justify-center gap-3 py-6 text-xs text-amber-300">
          <span>{cursorError}</span>
          {onRetryListing && (
            <Button
              variant="outline"
              size="sm"
              onClick={onRetryListing}
              className="h-7 text-xs border-amber-500/40 text-amber-300 hover:bg-amber-950/40"
            >
              <RotateCcw className="mr-1 h-3 w-3" />
              {vi.drive.actions.refreshStatus}
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
