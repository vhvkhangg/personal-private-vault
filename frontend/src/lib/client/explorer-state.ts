"use client";

import type { DriveItemTypeFilter } from "../contracts";

export interface BreadcrumbItem {
  id: string | null; // null represents root
  name: string;
}

export interface ExplorerState {
  slot: 1 | 2;
  currentFolder: BreadcrumbItem;
  breadcrumbs: BreadcrumbItem[];
  searchQuery: string;
  typeFilter: DriveItemTypeFilter;
  fromDate: string;
  toDate: string;
  requestGeneration: number;
}

export function createInitialExplorerState(initialSlot: 1 | 2 = 1): ExplorerState {
  return {
    slot: initialSlot,
    currentFolder: { id: null, name: "Google Drive" },
    breadcrumbs: [{ id: null, name: "Google Drive" }],
    searchQuery: "",
    typeFilter: "all",
    fromDate: "",
    toDate: "",
    requestGeneration: 1,
  };
}
