"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { formatBytes } from "@/lib/format";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { type PendingUploadAction, executeUpload } from "@/lib/client/upload-action";
import {
  UploadCloud,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  X,
  File as FileIcon,
} from "lucide-react";

interface UploadPanelProps {
  actions: PendingUploadAction[];
  onActionUpdated: () => void;
  onDismissAction: (key: string) => void;
}

export function UploadPanel({ actions, onActionUpdated, onDismissAction }: UploadPanelProps) {
  if (actions.length === 0) {
    return null;
  }

  const handleRetry = async (action: PendingUploadAction) => {
    try {
      await executeUpload(action, () => {
        onActionUpdated();
      });
      onActionUpdated();
    } catch {
      onActionUpdated();
    }
  };

  return (
    <div
      role="region"
      aria-label={vi.drive.upload.panelTitle}
      aria-live="polite"
      className="fixed bottom-6 right-6 z-40 w-96 max-w-[calc(100vw-3rem)] rounded-xl border border-slate-800 bg-slate-900/95 shadow-2xl backdrop-blur-md p-4"
    >
      <div className="mb-3 flex items-center justify-between border-b border-slate-800/80 pb-2">
        <div className="flex items-center gap-2 text-sm font-semibold text-slate-100">
          <UploadCloud className="h-4 w-4 text-blue-400" />
          <span>{vi.drive.upload.panelTitle}</span>
        </div>
        <span className="text-xs text-slate-400">{actions.length} mục</span>
      </div>

      <div className="flex max-h-72 flex-col gap-3 overflow-y-auto pr-1">
        {actions.map((act) => {
          const isDone = act.status === "SUCCESS" || act.status === "RECONCILED";
          const isError = act.status === "ERROR";
          const isUncertain = act.status === "UNCERTAIN" || act.status === "IN_PROGRESS";
          const isUploading = act.status === "UPLOADING";

          return (
            <div
              key={act.idempotencyKey}
              className="flex flex-col gap-1.5 rounded-lg border border-slate-800/80 bg-slate-950/60 p-2.5 text-xs"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5 truncate">
                  <FileIcon className="h-3.5 w-3.5 shrink-0 text-slate-400" />
                  <span className="truncate font-medium text-slate-200" title={act.file.name}>
                    {act.file.name}
                  </span>
                </div>
                <span className="shrink-0 text-[11px] text-slate-400">
                  {formatBytes(act.file.size)}
                </span>
              </div>

              {/* Progress bar while uploading */}
              {isUploading && (
                <div className="space-y-1">
                  <Progress value={act.progressPercent} className="h-1.5 bg-slate-800" />
                  <div className="flex justify-between text-[10px] text-slate-400">
                    <span>{vi.drive.upload.uploading}</span>
                    <span>{act.progressPercent}%</span>
                  </div>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      act.cancel?.();
                      onActionUpdated();
                    }}
                  >
                    Hủy tải lên
                  </Button>
                </div>
              )}

              {/* Status outcome */}
              {isDone && (
                <div className="flex items-center gap-1.5 text-emerald-400">
                  <CheckCircle2 className="h-3.5 w-3.5" />
                  <span>
                    {act.status === "SUCCESS"
                      ? vi.drive.upload.success
                      : vi.drive.upload.reconciled}
                  </span>
                  <Button
                    variant="ghost"
                    size="icon"
                    aria-label={`Đóng ${act.file.name}`}
                    onClick={() => onDismissAction(act.idempotencyKey)}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </div>
              )}

              {isUncertain && (
                <div className="flex flex-col gap-1 text-amber-300">
                  <div className="flex items-center gap-1.5">
                    <AlertTriangle className="h-3.5 w-3.5 shrink-0" />
                    <span>{act.errorMessage || vi.drive.upload.unknownOutcome}</span>
                  </div>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleRetry(act)}
                    className="mt-1 h-7 text-xs border-amber-500/40 bg-amber-950/40 text-amber-200 hover:bg-amber-900/50"
                  >
                    <RotateCcw className="mr-1.5 h-3 w-3" />
                    {vi.drive.upload.retrySameAction}
                  </Button>
                </div>
              )}

              {isError && (
                <div className="flex items-center justify-between text-red-400">
                  <span className="truncate">{act.errorMessage || vi.drive.errors.generic}</span>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onDismissAction(act.idempotencyKey)}
                    className="h-5 w-5 text-slate-500 hover:text-slate-300"
                    aria-label={`Đóng ${act.file.name}`}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
