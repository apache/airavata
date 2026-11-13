import { api } from "./api";
import type { SignInTokens } from "./api";

// Signing in with CILogon: the authorization code flow with PKCE. The browser goes to
// CILogon and comes back to /login/callback/ with a code, which the Airavata server
// redeems with the client secret it holds (internal/auth/signin.go).

export interface CILogonConfig {
  enabled: boolean;
  authorizationEndpoint?: string;
  clientId?: string;
  scope?: string;
}

// What the callback needs from the request that left: kept for this tab only.
const PENDING_KEY = "airavata.cilogon.pending";

interface Pending {
  state: string;
  verifier: string;
  redirectUri: string;
}

// The page CILogon returns to. It must be registered as a redirect URI of the client.
export function callbackUri(): string {
  return window.location.origin + "/dashboard/login/callback/";
}

export function getCILogonConfig(): Promise<CILogonConfig> {
  return api.get<CILogonConfig>("/api/v1/auth/cilogon");
}

function base64url(bytes: Uint8Array): string {
  let bin = "";
  bytes.forEach((b) => (bin += String.fromCharCode(b)));
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function randomString(size: number): string {
  return base64url(crypto.getRandomValues(new Uint8Array(size)));
}

// beginCILogon sends the browser to CILogon. It does not return.
export async function beginCILogon(config: CILogonConfig): Promise<void> {
  if (!config.enabled || !config.authorizationEndpoint || !config.clientId) {
    throw new Error("CILogon sign-in is not configured on this server");
  }
  const verifier = randomString(32);
  const challenge = base64url(new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier))));
  const pending: Pending = { state: randomString(16), verifier, redirectUri: callbackUri() };
  sessionStorage.setItem(PENDING_KEY, JSON.stringify(pending));

  const url = new URL(config.authorizationEndpoint);
  url.search = new URLSearchParams({
    response_type: "code",
    client_id: config.clientId,
    redirect_uri: pending.redirectUri,
    scope: config.scope ?? "openid email profile",
    state: pending.state,
    code_challenge: challenge,
    code_challenge_method: "S256",
  }).toString();
  window.location.assign(url.toString());
}

// finishCILogon redeems the code CILogon returned with, once: the pending request is
// forgotten whatever the outcome.
export async function finishCILogon(params: URLSearchParams): Promise<SignInTokens> {
  let pending: Pending | null = null;
  try {
    pending = JSON.parse(sessionStorage.getItem(PENDING_KEY) ?? "null");
  } catch {}
  sessionStorage.removeItem(PENDING_KEY);

  const error = params.get("error");
  if (error) throw new Error(params.get("error_description") || `CILogon refused the sign-in (${error})`);
  const code = params.get("code");
  if (!pending || !code) throw new Error("This sign-in was not started from this tab. Start it again.");
  if (params.get("state") !== pending.state) throw new Error("The sign-in response did not match its request. Start it again.");

  return api.post<SignInTokens>("/api/v1/auth/cilogon/token", {
    code,
    codeVerifier: pending.verifier,
    redirectUri: pending.redirectUri,
  });
}
