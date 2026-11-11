"use client";

import * as React from "react";
import Link from "next/link";
import { vi } from "@/i18n/vi";
import { bffClient } from "@/lib/client/bff-client";
import { Button } from "@/components/ui/button";
import { User, HardDrive, Home, Shield, LogOut } from "lucide-react";

interface PrivateShellProps {
  children: React.ReactNode;
}

export function PrivateShell({ children }: PrivateShellProps) {
  const [loggingOut, setLoggingOut] = React.useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await bffClient.logout();
    } catch (err: unknown) {
      setLoggingOut(false);
      const status = (err as { status?: number })?.status;
      if (status === 401) {
        window.location.href = "/login?error=session_expired";
      }
    }
  };

  return (
    <div className="flex h-screen w-full overflow-hidden bg-slate-950 text-slate-100">
      {/* Skip to main content link for keyboard navigation / a11y */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:rounded-md focus:bg-blue-600 focus:px-4 focus:py-2 focus:text-white focus:outline-none focus:ring-2 focus:ring-white"
      >
        Chuyển đến nội dung chính
      </a>

      {/* Primary Left Icon Rail */}
      <nav
        aria-label="Điều hướng chính"
        className="hidden sm:flex w-16 flex-col items-center justify-between border-r border-slate-800/80 bg-slate-950/90 py-4"
      >
        <div className="flex flex-col items-center gap-6">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 text-white shadow-lg shadow-blue-500/20">
            <Shield className="h-5 w-5" />
          </div>

          <div className="flex flex-col items-center gap-3">
            {/* Home (Placeholder - disabled per handoff) */}
            <button
              type="button"
              disabled
              aria-label={`${vi.nav.home} ${vi.nav.unavailableContext}`}
              title={`${vi.nav.home} ${vi.nav.unavailableContext}`}
              className="flex h-10 w-10 items-center justify-center rounded-lg text-slate-600 transition-colors hover:text-slate-500 disabled:opacity-40"
            >
              <Home className="h-5 w-5" />
            </button>

            {/* Personal (Selected) */}
            <button
              type="button"
              aria-label={vi.nav.personal}
              aria-current="page"
              className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-600/20 text-blue-400 border border-blue-500/30 transition-colors"
            >
              <User className="h-5 w-5" />
            </button>
          </div>
        </div>

        <Button
          variant="ghost"
          size="icon"
          onClick={handleLogout}
          disabled={loggingOut}
          aria-label={vi.nav.logout}
          title={vi.nav.logout}
          className="text-slate-400 hover:bg-slate-900 hover:text-red-400"
        >
          <LogOut className="h-5 w-5" />
        </Button>
      </nav>

      {/* Secondary Contextual Sidebar */}
      <aside
        aria-label="Cá nhân"
        className="hidden md:flex w-64 flex-col border-r border-slate-800/80 bg-slate-900/40 backdrop-blur-md p-4"
      >
        <div className="mb-6 px-2">
          <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            {vi.nav.personal}
          </h2>
        </div>

        <nav className="flex flex-col gap-1 text-sm">
          {/* Overview placeholder */}
          <span className="flex items-center gap-3 rounded-lg px-3 py-2 text-slate-400 select-none">
            {vi.nav.overview}
            <span className="text-xs text-slate-400"> {vi.nav.unavailableContext}</span>
          </span>

          {/* Profile placeholder */}
          <span className="flex items-center gap-3 rounded-lg px-3 py-2 text-slate-400 select-none">
            {vi.nav.profile}
            <span className="text-xs text-slate-400"> {vi.nav.unavailableContext}</span>
          </span>

          {/* Storage group */}
          <div className="mt-4">
            <span className="px-3 text-xs font-medium uppercase tracking-wider text-slate-400">
              {vi.nav.storage}
            </span>
            <div className="mt-1 flex flex-col gap-1">
              <Link
                href="/personal/storage/google-drive"
                className="flex items-center gap-3 rounded-lg bg-blue-600/10 px-3 py-2 font-medium text-blue-400 border border-blue-500/20 transition-all hover:bg-blue-600/20"
              >
                <HardDrive className="h-4 w-4" />
                {vi.nav.googleDrive}
              </Link>
            </div>
          </div>
        </nav>
      </aside>

      {/* Main Content Surface */}
      <main
        id="main-content"
        tabIndex={-1}
        className="min-w-0 flex-1 overflow-y-auto bg-slate-950/60 p-4 sm:p-6"
      >
        <Button
          variant="ghost"
          onClick={handleLogout}
          disabled={loggingOut}
          className="mb-3 sm:hidden"
          aria-label={vi.nav.logout}
        >
          <LogOut className="mr-2 h-4 w-4" />
          {vi.nav.logout}
        </Button>
        {children}
      </main>
    </div>
  );
}
