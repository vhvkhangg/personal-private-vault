import "server-only";
import { type NextRequest } from "next/server";
import { handleGoogleAuthComplete } from "@/lib/server/auth-completion";

export const dynamic = "force-dynamic";

export async function GET(req: NextRequest) {
  return handleGoogleAuthComplete(req);
}
