"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { bffClient } from "@/lib/client/bff-client";
import { Button } from "@/components/ui/button";
import type { DriveConnectionDto } from "@/lib/contracts";
import { CheckCircle2, AlertTriangle, XCircle, Link2, Unlink } from "lucide-react";

interface AccountsProps {
  connections: DriveConnectionDto[];
  activeSlot: 1 | 2;
  onSelectSlot: (slot: 1 | 2) => void;
  onRefreshConnections: () => void;
}

export function Accounts({
  connections,
  activeSlot,
  onSelectSlot,
  onRefreshConnections,
}: AccountsProps) {
  const [actionLoading, setActionLoading] = React.useState<number | null>(null);

  const getSlotConnection = (slot: 1 | 2): DriveConnectionDto => {
    const found = connections.find((c) => c.slot === slot);
    return found || { slot, status: "DISCONNECTED", displayEmail: null, remoteRevocation: null };
  };

  const handleAuthorize = async (slot: 1 | 2) => {
    setActionLoading(slot);
    try {
      const data = await bffClient.fetchJson<{ data: { authorizationUrl: string } }>(
        `/api/bff/drive/connections/${slot}/authorize`,
        { method: "POST" }
      );
      window.location.assign(data.data.authorizationUrl);
    } catch (err) {
      console.error(`Authorize failed for slot ${slot}:`, err);
      setActionLoading(null);
    }
  };

  const handleDisconnect = async (slot: 1 | 2) => {
    setActionLoading(slot);
    try {
      await bffClient.fetchJson(`/api/bff/drive/connections/${slot}`, {
        method: "DELETE",
      });
      onRefreshConnections();
    } catch (err) {
      console.error(`Disconnect failed for slot ${slot}:`, err);
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="flex flex-wrap items-center gap-4">
      {[1, 2].map((slotNum) => {
        const slot = slotNum as 1 | 2;
        const conn = getSlotConnection(slot);
        const isActive = activeSlot === slot;
        const isLoading = actionLoading === slot;

        return (
          <div
            key={slot}
            className={`flex max-sm:flex-col max-sm:items-start items-center justify-between gap-4 rounded-xl border p-3.5 transition-all w-full min-w-0 sm:w-auto sm:min-w-[280px] ${
              isActive
                ? "border-blue-500 bg-slate-900/90 shadow-lg shadow-blue-500/10 ring-1 ring-blue-500/50"
                : "border-slate-800 bg-slate-900/40 hover:border-slate-700 hover:bg-slate-900/60"
            }`}
          >
            <button
              type="button"
              onClick={() => onSelectSlot(slot)}
              aria-pressed={isActive}
              className="flex flex-col gap-1 text-left cursor-pointer rounded-sm"
            >
              <div className="flex items-center gap-2">
                <span className="font-semibold text-sm text-slate-100">
                  {slot === 1 ? vi.drive.slot1 : vi.drive.slot2}
                </span>
                {conn.status === "CONNECTED" && (
                  <span className="flex items-center gap-1 rounded-full bg-emerald-500/10 px-2 py-0.5 text-xs font-medium text-emerald-400 border border-emerald-500/20">
                    <CheckCircle2 className="h-3 w-3" />
                    {vi.drive.status.connected}
                  </span>
                )}
                {conn.status === "RECONNECT_REQUIRED" && (
                  <span className="flex items-center gap-1 rounded-full bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-400 border border-amber-500/20">
                    <AlertTriangle className="h-3 w-3" />
                    {vi.drive.status.reconnectRequired}
                  </span>
                )}
                {conn.status === "DISCONNECTED" && (
                  <span className="flex items-center gap-1 rounded-full bg-slate-500/10 px-2 py-0.5 text-xs font-medium text-slate-400 border border-slate-700">
                    <XCircle className="h-3 w-3" />
                    {vi.drive.status.disconnected}
                  </span>
                )}
              </div>
              <span className="text-xs text-slate-400 truncate max-w-[180px]">
                {conn.displayEmail || "—"}
              </span>
            </button>

            <div className="flex items-center gap-1.5" onClick={(e) => e.stopPropagation()}>
              {conn.status === "CONNECTED" ? (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDisconnect(slot)}
                  disabled={isLoading}
                  className="h-8 text-xs text-slate-400 hover:bg-red-950/40 hover:text-red-400"
                >
                  <Unlink className="mr-1 h-3.5 w-3.5" />
                  {vi.drive.actions.disconnect}
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleAuthorize(slot)}
                  disabled={isLoading}
                  className="h-8 text-xs border-slate-700 bg-slate-800 text-blue-400 hover:bg-slate-700"
                >
                  <Link2 className="mr-1 h-3.5 w-3.5" />
                  {conn.status === "RECONNECT_REQUIRED"
                    ? vi.drive.actions.reconnect
                    : vi.drive.actions.connect}
                </Button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
