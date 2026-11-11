import { headers } from "next/headers";
import { redirect } from "next/navigation";
import { sessionStore } from "@/lib/server/session-store";
import { extractSidFromCookieHeader } from "@/lib/server/route-guards";
import { LoginPanel } from "@/components/auth/login-panel";

export const dynamic = "force-dynamic";

interface LoginPageProps {
  searchParams: Promise<{ error?: string }>;
}

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const reqHeaders = await headers();
  const cookieHeader = reqHeaders.get("cookie");
  let sid: string | undefined;
  try {
    sid = extractSidFromCookieHeader(cookieHeader);
  } catch {
    // If duplicate cookies or invalid, stay on login page
  }
  const session = sessionStore.getSession(sid);

  if (session && session.purpose === "AUTHENTICATED") {
    redirect("/personal/storage/google-drive");
  }

  const resolvedParams = await searchParams;
  return <LoginPanel initialError={resolvedParams?.error} />;
}
