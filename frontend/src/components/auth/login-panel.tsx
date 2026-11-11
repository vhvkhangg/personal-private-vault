"use client";

import * as React from "react";
import Link from "next/link";
import { vi } from "@/i18n/vi";
import { bffClient } from "@/lib/client/bff-client";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Shield, AlertCircle, Loader2 } from "lucide-react";

interface LoginPanelProps {
  initialError?: string | undefined;
}

export function LoginPanel({ initialError }: LoginPanelProps) {
  const [loading, setLoading] = React.useState(false);
  const [bootstrapping, setBootstrapping] = React.useState(true);
  const [error, setError] = React.useState<string | null>(initialError || null);

  React.useEffect(() => {
    bffClient
      .getSessionStatus()
      .then(() => setBootstrapping(false))
      .catch((err) => {
        console.error("Session bootstrap failed:", err);
        setBootstrapping(false);
      });
  }, []);

  const handleGoogleSignIn = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await bffClient.fetchJson<{ data: { authorizationUrl: string } }>(
        "/api/bff/auth/google/start",
        {
          method: "POST",
        }
      );
      window.location.href = data.data.authorizationUrl;
    } catch (err: unknown) {
      const code = (err as { code?: string })?.code;
      if (code === "GOOGLE_SIGNIN_RATE_LIMITED") {
        setError(vi.auth.rateLimited);
      } else if (code === "GOOGLE_IDENTITY_CONFLICT") {
        setError(vi.auth.identityConflict);
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
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-600 text-white shadow-xl shadow-blue-500/20">
            <Shield className="h-8 w-8" />
          </div>
          <CardTitle
            role="heading"
            aria-level={1}
            className="text-2xl font-bold tracking-tight text-white"
          >
            {vi.auth.loginTitle}
          </CardTitle>
          <CardDescription className="text-slate-400">{vi.auth.loginSubtitle}</CardDescription>
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

          <Button
            type="button"
            onClick={handleGoogleSignIn}
            disabled={loading || bootstrapping}
            className="w-full h-11 bg-blue-600 hover:bg-blue-700 text-white font-medium shadow-md shadow-blue-600/30 transition-all cursor-pointer"
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Đang chuyển hướng Google...
              </>
            ) : (
              vi.auth.signInWithGoogle
            )}
          </Button>

          <div className="mt-4 border-t border-slate-800 pt-4 text-center">
            <Link
              href="/setup/google"
              className="text-xs text-slate-400 hover:text-blue-400 underline underline-offset-4 transition-colors"
            >
              {vi.auth.setupTitle}
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
