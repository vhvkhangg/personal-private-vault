"use client";

import * as React from "react";
import Image from "next/image";
import { formatBytes, formatDateTime } from "@/lib/format";
import { Card } from "@/components/ui/card";
import { DownloadLink } from "./download-link";
import type { DriveItemDto } from "@/lib/contracts";
import {
  Folder,
  FileText,
  FileImage,
  FileCode,
  FileSpreadsheet,
  FileArchive,
  File as FileGeneric,
} from "lucide-react";

interface ItemCardProps {
  item: DriveItemDto;
  slot: 1 | 2;
  onOpenFolder?: (folder: DriveItemDto) => void;
}

export function ItemCard({ item, slot, onOpenFolder }: ItemCardProps) {
  const [thumbError, setThumbError] = React.useState(false);

  const isFolder = item.kind === "folder";
  const isImage = item.mimeType?.startsWith("image/");
  const canShowThumbnail = isImage && !thumbError;

  const thumbnailUrl = `/api/bff/drive/connections/${slot}/files/${item.id}/thumbnail`;

  const getFileIcon = () => {
    if (isFolder) {
      return <Folder className="h-8 w-8 text-amber-400 fill-amber-400/20" />;
    }
    const mime = item.mimeType || "";
    if (mime.includes("pdf")) return <FileText className="h-8 w-8 text-red-400" />;
    if (mime.includes("image")) return <FileImage className="h-8 w-8 text-emerald-400" />;
    if (mime.includes("sheet") || mime.includes("excel") || mime.includes("csv"))
      return <FileSpreadsheet className="h-8 w-8 text-green-400" />;
    if (mime.includes("zip") || mime.includes("tar") || mime.includes("rar"))
      return <FileArchive className="h-8 w-8 text-purple-400" />;
    if (mime.includes("text") || mime.includes("json") || mime.includes("xml"))
      return <FileCode className="h-8 w-8 text-cyan-400" />;
    return <FileGeneric className="h-8 w-8 text-slate-400" />;
  };

  const handleClick = () => {
    if (isFolder && onOpenFolder) {
      onOpenFolder(item);
    }
  };

  return (
    <Card
      onClick={handleClick}
      role={isFolder ? "button" : undefined}
      tabIndex={isFolder ? 0 : undefined}
      onKeyDown={(e) => {
        if (isFolder && (e.key === "Enter" || e.key === " ")) {
          e.preventDefault();
          handleClick();
        }
      }}
      className={`group relative flex flex-col justify-between overflow-hidden rounded-xl border border-slate-800 bg-slate-900/50 p-3.5 transition-all hover:border-slate-700 hover:bg-slate-900/80 hover:shadow-lg ${
        isFolder ? "cursor-pointer hover:border-amber-500/40" : ""
      }`}
    >
      {/* Top Preview / Icon Area */}
      <div className="flex h-24 w-full items-center justify-center rounded-lg bg-slate-950/60 overflow-hidden relative mb-2">
        {canShowThumbnail ? (
          <Image
            src={thumbnailUrl}
            alt={item.name}
            fill
            sizes="200px"
            className="object-cover transition-transform group-hover:scale-105"
            onError={() => setThumbError(true)}
            unoptimized
          />
        ) : (
          getFileIcon()
        )}
      </div>

      {/* Info Area */}
      <div className="flex flex-col gap-1">
        <span
          className="font-medium text-xs text-slate-200 truncate group-hover:text-white"
          title={item.name}
        >
          {item.name}
        </span>

        <div className="flex items-center justify-between text-[11px] text-slate-400">
          <span>{isFolder ? "Thư mục" : formatBytes(item.sizeBytes)}</span>
          <span>{formatDateTime(item.modifiedAt)}</span>
        </div>
      </div>

      {/* Download Action for File */}
      {!isFolder && (
        <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 focus-within:opacity-100 transition-opacity">
          <DownloadLink slot={slot} fileId={item.id} fileName={item.name} />
        </div>
      )}
    </Card>
  );
}
