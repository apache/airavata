import { NextResponse } from "next/server";
import { auth, getKeycloakIssuer } from "@/lib/auth";

/**
 * Validates the current session's access token against Keycloak (userinfo via proxy).
 * After cold-start / realm wipe the token is from the old realm; Keycloak rejects it.
 * We then signal the client to clear session and redirect to login (self-recovery).
 */
export async function GET() {
  const session = await auth();
  if (!session?.accessToken) {
    return NextResponse.json({ ok: true }, { status: 200 });
  }

  const issuer = getKeycloakIssuer();
  const userinfoUrl = `${issuer}/protocol/openid-connect/userinfo`;

  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    const res = await fetch(userinfoUrl, {
      method: "GET",
      headers: { Authorization: `Bearer ${session.accessToken}` },
      cache: "no-store",
      signal: controller.signal,
    });
    clearTimeout(timeout);

    if (res.status === 401 || res.status === 403) {
      return NextResponse.json({ error: "Session invalid" }, { status: 401 });
    }
    return NextResponse.json({ ok: true }, { status: 200 });
  } catch {
    return NextResponse.json({ ok: true }, { status: 200 });
  }
}
