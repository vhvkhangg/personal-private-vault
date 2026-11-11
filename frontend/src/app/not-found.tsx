import Link from "next/link";
import { Button } from "@/components/ui/button";
import { FileQuestion } from "lucide-react";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 p-6 text-slate-100">
      <div className="flex max-w-md flex-col items-center gap-4 text-center">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-900 text-slate-400 border border-slate-800">
          <FileQuestion className="h-6 w-6" />
        </div>
        <h2 className="text-xl font-bold">404 - Không tìm thấy trang</h2>
        <p className="text-xs text-slate-400">
          Đường dẫn yêu cầu không tồn tại trong hệ thống Personal Private Vault.
        </p>
        <Button asChild className="mt-2 bg-blue-600 hover:bg-blue-700 text-white">
          <Link href="/personal/storage/google-drive">Về Google Drive</Link>
        </Button>
      </div>
    </div>
  );
}
