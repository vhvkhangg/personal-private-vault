import { headers } from "next/headers";
import { redirect } from "next/navigation";
import { sessionStore } from "@/lib/server/session-store";
import { extractSidFromCookieHeader } from "@/lib/server/route-guards";

export const dynamic = "force-dynamic";

export default async function IndexPage() {
  const reqHeaders = await headers();
  const cookieHeader = reqHeaders.get("cookie");
  let sid: string | undefined;
  try {
    sid = extractSidFromCookieHeader(cookieHeader);
  } catch {
    redirect("/login");
  }
  const session = sessionStore.getSession(sid);

  if (session && session.purpose === "AUTHENTICATED") {
    redirect("/personal/storage/google-drive");
  } else {
    redirect("/login");
  }
}
