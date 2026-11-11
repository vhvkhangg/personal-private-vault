"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import type { DriveItemTypeFilter } from "@/lib/contracts";
import { Plus, UploadCloud, FolderPlus, Search, RotateCcw } from "lucide-react";

interface ToolbarProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  typeFilter: DriveItemTypeFilter;
  onTypeFilterChange: (type: DriveItemTypeFilter) => void;
  fromDate: string;
  onFromDateChange: (from: string) => void;
  toDate: string;
  onToDateChange: (to: string) => void;
  onResetFilters: () => void;
  onOpenFileUpload: () => void;
  onOpenNewFolder: () => void;
  isInsideChildFolder: boolean;
  disabled?: boolean;
  newMenuTriggerRef?: React.RefObject<HTMLButtonElement | null>;
}

export function Toolbar({
  searchQuery,
  onSearchChange,
  typeFilter,
  onTypeFilterChange,
  fromDate,
  onFromDateChange,
  toDate,
  onToDateChange,
  onResetFilters,
  onOpenFileUpload,
  onOpenNewFolder,
  isInsideChildFolder,
  disabled = false,
  newMenuTriggerRef,
}: ToolbarProps) {
  const hasActiveFilters = Boolean(
    searchQuery.trim() || typeFilter !== "all" || fromDate || toDate
  );

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-800/80 bg-slate-950/60 backdrop-blur-md p-3 shadow-inner">
      {/* Search-First: Left side contains search input and query filters */}
      <div className="flex flex-wrap items-center gap-2.5 flex-1 basis-full sm:basis-auto min-w-0 sm:min-w-[280px]">
        {/* Search input */}
        <div className="relative w-full min-w-0 sm:min-w-[240px] flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            disabled={disabled}
            placeholder={vi.drive.search.placeholder}
            aria-label={vi.drive.search.placeholder}
            className="h-9 pl-9 pr-3 text-xs bg-slate-900/90 border-slate-700/80 text-slate-100 placeholder:text-slate-400 focus-visible:ring-blue-500"
          />
        </div>

        {/* Type select filter */}
        <div className="w-[130px]">
          <Select
            value={typeFilter}
            onValueChange={(val) => onTypeFilterChange(val as DriveItemTypeFilter)}
            disabled={disabled}
          >
            <SelectTrigger
              aria-label={vi.drive.search.typeLabel}
              className="h-9 text-xs bg-slate-900/90 border-slate-700/80 text-slate-200"
            >
              <SelectValue placeholder={vi.drive.search.typeLabel} />
            </SelectTrigger>
            <SelectContent className="border-slate-800 bg-slate-900 text-slate-200">
              <SelectItem value="all">{vi.drive.search.types.all}</SelectItem>
              <SelectItem value="folder">{vi.drive.search.types.folder}</SelectItem>
              <SelectItem value="image">{vi.drive.search.types.image}</SelectItem>
              <SelectItem value="pdf">{vi.drive.search.types.pdf}</SelectItem>
              <SelectItem value="document">{vi.drive.search.types.document}</SelectItem>
              <SelectItem value="text">{vi.drive.search.types.text}</SelectItem>
              <SelectItem value="other">{vi.drive.search.types.other}</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Date From */}
        <div className="flex items-center gap-1.5 text-xs text-slate-300">
          <span className="text-slate-400 font-medium text-[11px] whitespace-nowrap">
            {vi.drive.search.fromDate}:
          </span>
          <Input
            type="date"
            value={fromDate}
            onChange={(e) => onFromDateChange(e.target.value)}
            disabled={disabled}
            aria-label={vi.drive.search.fromDate}
            title={vi.drive.search.fromDate}
            className="h-9 w-[125px] text-xs bg-slate-900/90 border-slate-700/80 text-slate-200"
          />
        </div>

        {/* Date To */}
        <div className="flex items-center gap-1.5 text-xs text-slate-300">
          <span className="text-slate-400 font-medium text-[11px] whitespace-nowrap">
            {vi.drive.search.toDate}:
          </span>
          <Input
            type="date"
            value={toDate}
            onChange={(e) => onToDateChange(e.target.value)}
            disabled={disabled}
            aria-label={vi.drive.search.toDate}
            title={vi.drive.search.toDate}
            className="h-9 w-[125px] text-xs bg-slate-900/90 border-slate-700/80 text-slate-200"
          />
        </div>

        {/* Reset filters button */}
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onResetFilters}
            aria-label={vi.drive.search.resetFilters}
            title={vi.drive.search.resetFilters}
            className="h-9 px-2 text-xs text-slate-400 hover:text-white"
          >
            <RotateCcw className="h-3.5 w-3.5" />
          </Button>
        )}
      </div>

      {/* Right side: "Mới" (New) Action Menu */}
      <div className="flex items-center gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              ref={newMenuTriggerRef}
              disabled={disabled}
              className="bg-blue-600 hover:bg-blue-700 text-white font-medium shadow-md shadow-blue-600/20 cursor-pointer h-9 px-4 text-xs"
            >
              <Plus className="mr-1.5 h-4 w-4" />
              {vi.drive.actions.newMenu}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            align="end"
            className="w-48 border-slate-800 bg-slate-900 text-slate-100 shadow-xl"
          >
            <DropdownMenuItem
              onClick={onOpenFileUpload}
              className="flex items-center gap-2 text-sm text-slate-200 hover:bg-slate-800 hover:text-white cursor-pointer"
            >
              <UploadCloud className="h-4 w-4 text-blue-400" />
              {vi.drive.actions.uploadFile}
            </DropdownMenuItem>

            <DropdownMenuItem
              onClick={onOpenNewFolder}
              disabled={isInsideChildFolder}
              className="flex items-center gap-2 text-sm text-slate-200 hover:bg-slate-800 hover:text-white cursor-pointer disabled:opacity-40"
            >
              <FolderPlus className="h-4 w-4 text-amber-400" />
              {vi.drive.actions.newFolder}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
}
