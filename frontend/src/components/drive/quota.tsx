"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { formatBytes, formatDateTime } from "@/lib/format";
import { Progress } from "@/components/ui/progress";
import type { StorageQuotaDto } from "@/lib/contracts";
import { HardDrive } from "lucide-react";

interface QuotaProps {
  quota: StorageQuotaDto | null;
  loading: boolean;
}

export function Quota({ quota, loading }: QuotaProps) {
  if (loading) {
    return (
      <div className="flex items-center gap-3 rounded-xl border border-slate-800/80 bg-slate-900/50 p-3 animate-pulse min-w-[260px]">
        <div className="h-7 w-7 rounded-lg bg-slate-800" />
        <div className="flex-1 space-y-1.5">
          <div className="h-3 w-24 rounded bg-slate-800" />
          <div className="h-1.5 w-full rounded bg-slate-800" />
        </div>
      </div>
    );
  }

  if (!quota) {
    return null;
  }

  const driveUsage = quota.usageInDriveBytes;
  const totalUsage = quota.usageBytes;
  const limit = quota.limitBytes;
  const hasKnownLimit = quota.limitKnown && limit !== null && limit > 0;

  const hasDriveUsage = driveUsage !== null && driveUsage !== undefined;
  const hasTotalUsage = totalUsage !== null && totalUsage !== undefined;

  const percent =
    hasKnownLimit && hasDriveUsage ? Math.min(100, Math.round((driveUsage / limit) * 100)) : 0;

  return (
    <div className="flex flex-col gap-1.5 rounded-xl border border-slate-800/80 bg-slate-950/60 backdrop-blur-sm p-3 text-xs min-w-[260px] shadow-sm">
      <div className="flex items-center justify-between text-slate-300">
        <div className="flex flex-wrap items-center gap-1.5">
          <HardDrive className="h-3.5 w-3.5 text-blue-400 shrink-0" />
          <span className="font-medium text-slate-300">{vi.drive.quota.title}:</span>
          <span className="font-semibold text-white">
            {hasDriveUsage ? formatBytes(driveUsage) : vi.drive.quota.unknown}
          </span>
          {hasKnownLimit ? (
            <span className="text-slate-400 text-[11px]">
              {vi.drive.quota.of} {formatBytes(limit)} ({percent}%)
            </span>
          ) : (
            <span className="text-slate-400 text-[11px]">
              (
              {quota.limitKnown && limit === null
                ? vi.drive.quota.unlimited
                : vi.drive.quota.unknown}
              )
            </span>
          )}
        </div>
      </div>

      {hasKnownLimit && (
        <Progress
          value={percent}
          className="h-1.5 bg-slate-800 [&>div]:bg-gradient-to-r [&>div]:from-blue-600 [&>div]:to-indigo-500"
          aria-label={`${vi.drive.quota.title} ${percent}%`}
        />
      )}

      {/* Account total usage footnote */}
      <div className="flex flex-wrap gap-1 items-center justify-between text-[10px] text-slate-400">
        <span>
          {vi.drive.quota.poolUsed}:{" "}
          {hasTotalUsage ? formatBytes(totalUsage) : vi.drive.quota.unknown}
        </span>
        {quota.observedAt && <span>{formatDateTime(quota.observedAt)}</span>}
      </div>
    </div>
  );
}
