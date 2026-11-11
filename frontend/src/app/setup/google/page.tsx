import { redirect } from "next/navigation";
import { serverConfig } from "@/lib/server/config";
import { OwnerSetup } from "@/components/auth/owner-setup";

export const dynamic = "force-dynamic";

export default function SetupGooglePage() {
  if (!serverConfig.ownerSetupEnabled) {
    redirect("/login");
  }

  return <OwnerSetup />;
}
