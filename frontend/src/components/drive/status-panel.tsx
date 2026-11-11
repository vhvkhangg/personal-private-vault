"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { Button } from "@/components/ui/button";
import { FolderKanban, Link2, AlertTriangle, Loader2 } from "lucide-react";

interface StatusPanelProps {
  type: "DISCONNECTED" | "RECONNECT_REQUIRED" | "ROOT_NOT_READY";
  slot: 1 | 2;
  onAction: () => void;
  loading?: boolean;
}

export function StatusPanel({ type, slot: _slot, onAction, loading = false }: StatusPanelProps) {
  if (type === "ROOT_NOT_READY") {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-800 bg-slate-900/30 p-12 text-center">
        <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600/10 text-blue-400 border border-blue-500/20">
          <FolderKanban className="h-7 w-7" />
        </div>
        <h3 className="text-lg font-semibold text-white">{vi.drive.explorer.rootNotReady}</h3>
        <p className="mt-1 max-w-sm text-sm text-slate-400">{vi.drive.explorer.ensurePrompt}</p>
        <Button
          onClick={onAction}
          disabled={loading}
          className="mt-6 bg-blue-600 hover:bg-blue-700 text-white font-medium"
        >
          {loading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Đang khởi tạo...
            </>
          ) : (
            vi.drive.actions.ensureRoot
          )}
        </Button>
      </div>
    );
  }

  if (type === "RECONNECT_REQUIRED") {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-amber-800/40 bg-amber-950/20 p-12 text-center">
        <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-600/10 text-amber-400 border border-amber-500/20">
          <AlertTriangle className="h-7 w-7" />
        </div>
        <h3 className="text-lg font-semibold text-amber-200">
          {vi.drive.status.reconnectRequired}
        </h3>
        <p className="mt-1 max-w-sm text-sm text-slate-400">{vi.drive.explorer.reconnectPrompt}</p>
        <Button
          onClick={onAction}
          disabled={loading}
          className="mt-6 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold"
        >
          <Link2 className="mr-2 h-4 w-4" />
          {vi.drive.actions.reconnect}
        </Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-800 bg-slate-900/30 p-12 text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-800 text-slate-400">
        <Link2 className="h-7 w-7" />
      </div>
      <h3 className="text-lg font-semibold text-white">{vi.drive.status.disconnected}</h3>
      <p className="mt-1 max-w-sm text-sm text-slate-400">{vi.drive.explorer.disconnectedPrompt}</p>
      <Button
        onClick={onAction}
        disabled={loading}
        className="mt-6 bg-blue-600 hover:bg-blue-700 text-white font-medium"
      >
        <Link2 className="mr-2 h-4 w-4" />
        {vi.drive.actions.connect}
      </Button>
    </div>
  );
}
