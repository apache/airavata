import { NextRequest, NextResponse } from "next/server";
import { createSession } from "@/lib/session";
import {
  CILOGON_TOKEN_ENDPOINT,
  CILOGON_USERINFO_ENDPOINT,
  OAUTH_STATE_COOKIE,
  OAUTH_VERIFIER_COOKIE,
  redirectUriFor,
} from "@/lib/cilogon";

export const dynamic = "force-dynamic";

interface TokenResponse {
  access_token: string;
  id_token?: string;
  expires_in?: number;
}

interface UserInfo {
  email?: string;
  name?: string;
}

export async function GET(req: NextRequest) {
  const code = req.nextUrl.searchParams.get("code");
  const state = req.nextUrl.searchParams.get("state");
  const oauthError = req.nextUrl.searchParams.get("error");

  const expectedState = req.cookies.get(OAUTH_STATE_COOKIE)?.value;
  const codeVerifier = req.cookies.get(OAUTH_VERIFIER_COOKIE)?.value;

  if (oauthError) {
    return NextResponse.redirect(new URL(`/?error=${encodeURIComponent(oauthError)}`, req.url));
  }
  if (!code || !state || !expectedState || state !== expectedState || !codeVerifier) {
    return NextResponse.redirect(new URL("/?error=invalid_state", req.url));
  }

  const tokenRes = await fetch(CILOGON_TOKEN_ENDPOINT, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      code,
      redirect_uri: redirectUriFor(req.nextUrl.origin),
      client_id: process.env.CILOGON_CLIENT_ID!,
      client_secret: process.env.CILOGON_CLIENT_SECRET!,
      code_verifier: codeVerifier,
    }),
  });

  if (!tokenRes.ok) {
    return NextResponse.redirect(new URL("/?error=token_exchange_failed", req.url));
  }

  const tokens = (await tokenRes.json()) as TokenResponse;

  let profile: UserInfo = {};
  try {
    const userinfoRes = await fetch(CILOGON_USERINFO_ENDPOINT, {
      headers: { Authorization: `Bearer ${tokens.access_token}` },
    });
    if (userinfoRes.ok) {
      profile = (await userinfoRes.json()) as UserInfo;
    }
  } catch {
    // best-effort; the console still works without a display name
  }

  const res = NextResponse.redirect(new URL("/", req.url));
  res.cookies.delete(OAUTH_STATE_COOKIE);
  res.cookies.delete(OAUTH_VERIFIER_COOKIE);
  await createSession(res, {
    accessToken: tokens.access_token,
    accessTokenExpires: tokens.expires_in ? Date.now() + tokens.expires_in * 1000 : undefined,
    email: profile.email,
    name: profile.name,
  });
  return res;
}
