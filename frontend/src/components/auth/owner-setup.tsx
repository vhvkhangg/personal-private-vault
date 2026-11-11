"use client";

import * as React from "react";
import Link from "next/link";
import { vi } from "@/i18n/vi";
import { bffClient } from "@/lib/client/bff-client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { ShieldAlert, AlertCircle, Loader2, ArrowLeft } from "lucide-react";

export function OwnerSetup() {
  const [step, setStep] = React.useState<"LOGIN" | "LINK">("LOGIN");
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  // Form references - native form inputs, not retained React state
  const usernameRef = React.useRef<HTMLInputElement>(null);
  const passwordRef = React.useRef<HTMLInputElement>(null);
  const freshPasswordRef = React.useRef<HTMLInputElement>(null);

  React.useEffect(() => {
    bffClient.getSessionStatus().catch(() => {});
  }, []);

  const handleLegacyLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    const identifier = usernameRef.current?.value || "";
    const password = passwordRef.current?.value || "";

    if (!identifier || !password) {
      setError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await bffClient.fetchJson("/api/bff/setup/legacy/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });
      // Clear password field immediately
      if (passwordRef.current) passwordRef.current.value = "";
      // Bootstrap the new CSRF token for the new SETUP_ONLY session generation
      await bffClient.getSessionStatus().catch(() => {});
      setStep("LINK");
    } catch (err: unknown) {
      if (passwordRef.current) passwordRef.current.value = "";
      const code = (err as { code?: string })?.code;
      if (code === "AUTH_INVALID_CREDENTIALS") {
        setError("Tên đăng nhập hoặc mật khẩu không chính xác.");
      } else {
        setError(vi.auth.forbidden);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleStartLink = async (e: React.FormEvent) => {
    e.preventDefault();
    const freshPassword = freshPasswordRef.current?.value || "";

    if (!freshPassword) {
      setError("Vui lòng nhập mật khẩu xác nhận.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await bffClient.fetchJson<{ data: { authorizationUrl: string } }>(
        "/api/bff/setup/google/link/start",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ password: freshPassword }),
        }
      );
      if (freshPasswordRef.current) freshPasswordRef.current.value = "";
      window.location.href = data.data.authorizationUrl;
    } catch (err: unknown) {
      if (freshPasswordRef.current) freshPasswordRef.current.value = "";
      const code = (err as { code?: string })?.code;
      if (code === "AUTH_INVALID_CREDENTIALS") {
        setError("Mật khẩu xác nhận không chính xác.");
      } else {
        setError(vi.auth.forbidden);
      }
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 p-4">
      <Card className="w-full max-w-md border-slate-800 bg-slate-900/80 backdrop-blur-md shadow-2xl">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-600 to-orange-600 text-white shadow-xl shadow-amber-500/20">
            <ShieldAlert className="h-8 w-8" />
          </div>
          <CardTitle
            role="heading"
            aria-level={1}
            className="text-2xl font-bold tracking-tight text-white"
          >
            {vi.auth.setupTitle}
          </CardTitle>
          <CardDescription className="text-slate-400">{vi.auth.setupSubtitle}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {error && (
            <div
              role="alert"
              className="flex items-center gap-3 rounded-lg border border-red-500/30 bg-red-950/40 p-3 text-sm text-red-300"
            >
              <AlertCircle className="h-5 w-5 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="rounded-lg border border-amber-500/30 bg-amber-950/30 p-3 text-xs text-amber-200">
            {vi.auth.emergencyNotice}
          </div>

          {step === "LOGIN" ? (
            <form onSubmit={handleLegacyLogin} className="flex flex-col gap-4">
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="setup-username">{vi.auth.usernameLabel}</Label>
                <Input
                  id="setup-username"
                  ref={usernameRef}
                  type="text"
                  required
                  autoComplete="username"
                  placeholder="vaultowner"
                  className="bg-slate-950/60 border-slate-700"
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="setup-password">{vi.auth.passwordLabel}</Label>
                <Input
                  id="setup-password"
                  ref={passwordRef}
                  type="password"
                  required
                  autoComplete="current-password"
                  className="bg-slate-950/60 border-slate-700"
                />
              </div>

              <Button
                type="submit"
                disabled={loading}
                className="w-full bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold"
              >
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Đang xác thực...
                  </>
                ) : (
                  vi.auth.loginButton
                )}
              </Button>
            </form>
          ) : (
            <form onSubmit={handleStartLink} className="flex flex-col gap-4">
              <div className="rounded-md bg-blue-950/30 border border-blue-500/30 p-3 text-xs text-blue-200">
                {vi.auth.setupOnlyNotice}
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="setup-fresh-password">{vi.auth.freshPasswordLabel}</Label>
                <Input
                  id="setup-fresh-password"
                  ref={freshPasswordRef}
                  type="password"
                  required
                  autoComplete="current-password"
                  placeholder="Nhập lại mật khẩu để xác nhận liên kết..."
                  className="bg-slate-950/60 border-slate-700"
                />
              </div>

              <Button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium"
              >
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Đang chuẩn bị liên kết...
                  </>
                ) : (
                  vi.auth.linkButton
                )}
              </Button>
            </form>
          )}

          <div className="mt-2 text-center">
            <Link
              href="/login"
              className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-300"
            >
              <ArrowLeft className="h-3.5 w-3.5" />
              {vi.auth.cancel}
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
