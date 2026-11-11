import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Personal Private Vault",
  description: "Personal Private Vault - Google Drive Explorer",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#020617",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="vi" className="dark h-full">
      <body className="h-full bg-slate-950 text-slate-100 antialiased">{children}</body>
    </html>
  );
}
