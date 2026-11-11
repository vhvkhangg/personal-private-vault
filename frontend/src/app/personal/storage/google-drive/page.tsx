import { headers } from "next/headers";
import { redirect } from "next/navigation";
import { sessionStore } from "@/lib/server/session-store";
import { extractSidFromCookieHeader } from "@/lib/server/route-guards";
import { PrivateShell } from "@/components/layout/private-shell";
import { Explorer } from "@/components/drive/explorer";

export const dynamic = "force-dynamic";

export default async function GoogleDrivePage() {
  const reqHeaders = await headers();
  const cookieHeader = reqHeaders.get("cookie");
  let sid: string | undefined;
  try {
    sid = extractSidFromCookieHeader(cookieHeader);
  } catch {
    redirect("/login");
  }
  const session = sessionStore.getSession(sid);

  if (!session || session.purpose !== "AUTHENTICATED") {
    redirect("/login");
  }

  return (
    <PrivateShell>
      <Explorer />
    </PrivateShell>
  );
}
