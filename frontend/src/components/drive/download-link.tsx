"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { Button } from "@/components/ui/button";
import { Download, Loader2 } from "lucide-react";

interface DownloadLinkProps {
  slot: 1 | 2;
  fileId: string;
  fileName: string;
}

export function DownloadLink({ slot, fileId, fileName }: DownloadLinkProps) {
  const [downloading, setDownloading] = React.useState(false);

  const handleDownload = (e: React.MouseEvent) => {
    e.stopPropagation();
    setDownloading(true);

    try {
      const url = `/api/bff/drive/connections/${slot}/files/${fileId}/download`;
      // Direct native browser attachment delivery (no res.blob() memory buffering)
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      setTimeout(() => {
        if (a.parentNode) {
          a.parentNode.removeChild(a);
        }
      }, 1000);
    } catch (err) {
      console.error("Download failed:", err);
    } finally {
      setTimeout(() => setDownloading(false), 1500);
    }
  };

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={handleDownload}
      disabled={downloading}
      aria-label={`${vi.drive.actions.download} ${fileName}`}
      title={`${vi.drive.actions.download} ${fileName}`}
      className="h-8 w-8 text-slate-400 hover:bg-slate-800 hover:text-white focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:outline-none focus:opacity-100"
    >
      {downloading ? (
        <Loader2 className="h-4 w-4 animate-spin text-blue-400" />
      ) : (
        <Download className="h-4 w-4" />
      )}
    </Button>
  );
}
