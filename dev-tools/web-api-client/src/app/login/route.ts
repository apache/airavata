import { NextRequest, NextResponse } from "next/server";
import crypto from "crypto";
import {
  CILOGON_AUTHORIZATION_ENDPOINT,
  CILOGON_SCOPE,
  OAUTH_STATE_COOKIE,
  OAUTH_VERIFIER_COOKIE,
  redirectUriFor,
} from "@/lib/cilogon";

export const dynamic = "force-dynamic";

function base64url(input: Buffer) {
  return input.toString("base64").replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

export async function GET(req: NextRequest) {
  const state = base64url(crypto.randomBytes(24));
  const codeVerifier = base64url(crypto.randomBytes(32));
  const codeChallenge = base64url(crypto.createHash("sha256").update(codeVerifier).digest());

  const authorizeUrl = new URL(CILOGON_AUTHORIZATION_ENDPOINT);
  authorizeUrl.searchParams.set("response_type", "code");
  authorizeUrl.searchParams.set("client_id", process.env.CILOGON_CLIENT_ID!);
  authorizeUrl.searchParams.set("redirect_uri", redirectUriFor(req.nextUrl.origin));
  authorizeUrl.searchParams.set("scope", CILOGON_SCOPE);
  authorizeUrl.searchParams.set("state", state);
  authorizeUrl.searchParams.set("code_challenge", codeChallenge);
  authorizeUrl.searchParams.set("code_challenge_method", "S256");

  const res = NextResponse.redirect(authorizeUrl);
  const cookieOpts = { httpOnly: true, sameSite: "lax" as const, path: "/", maxAge: 600 };
  res.cookies.set(OAUTH_STATE_COOKIE, state, cookieOpts);
  res.cookies.set(OAUTH_VERIFIER_COOKIE, codeVerifier, cookieOpts);
  return res;
}
