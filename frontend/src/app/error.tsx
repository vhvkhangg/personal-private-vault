"use client";

import * as React from "react";
import { vi } from "@/i18n/vi";
import { Button } from "@/components/ui/button";
import { AlertCircle } from "lucide-react";

export default function ErrorPage({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  React.useEffect(() => {
    console.error("Application error:", error);
  }, [error]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 p-6 text-slate-100">
      <div className="flex max-w-md flex-col items-center gap-4 text-center">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-red-950/60 text-red-400 border border-red-500/30">
          <AlertCircle className="h-6 w-6" />
        </div>
        <h2 className="text-xl font-bold">{vi.drive.errors.generic}</h2>
        <p className="text-xs text-slate-400">
          {error.message || "Đã xảy ra lỗi không mong muốn trong quá trình xử lý giao diện."}
        </p>
        <Button onClick={() => reset()} className="mt-2 bg-blue-600 hover:bg-blue-700 text-white">
          {vi.drive.actions.retry}
        </Button>
      </div>
    </div>
  );
}
