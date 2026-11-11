import type { ErrorEnvelope, FieldError } from "./types";

// The built dashboard is served by the API server, so it calls the API on its own
// origin. `npm run dev` runs on another port and points here instead.
export const API_BASE = (process.env.NEXT_PUBLIC_AIRAVATA_API_URL ?? "").replace(/\/$/, "");

const TOKEN_KEY = "airavata.token";
// Set only for a CILogon sign-in: its access tokens are short-lived, so the dashboard
// keeps the refresh token and renews the access token before it expires.
const REFRESH_KEY = "airavata.refreshToken";
const EXPIRES_KEY = "airavata.tokenExpiresAt";
// Renew this long before the access token expires.
const REFRESH_MARGIN_MS = 60_000;

function read(key: string): string | null {
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string | null) {
  try {
    if (value) window.localStorage.setItem(key, value);
    else window.localStorage.removeItem(key);
  } catch {
    // Storage unavailable (private mode); the token lives only as long as the page.
  }
}

export function getToken(): string | null {
  return read(TOKEN_KEY);
}

// setToken stores a pasted token, which is not renewed, and drops any CILogon refresh
// token. null signs out.
export function setToken(token: string | null) {
  write(TOKEN_KEY, token);
  write(REFRESH_KEY, null);
  write(EXPIRES_KEY, null);
}

// Tokens as the server's CILogon sign-in routes return them (internal/auth/signin.go).
export interface SignInTokens {
  accessToken: string;
  refreshToken?: string;
  expiresIn?: number;
}

// TOKEN_EVENT fires when a renewal replaced the access token, so holders of it (the
// session, and the sockets it opens) pick up the new one.
export const TOKEN_EVENT = "airavata:token";

export function setSignInTokens(tokens: SignInTokens) {
  write(TOKEN_KEY, tokens.accessToken);
  // A refresh may not rotate the refresh token; keep the one held.
  if (tokens.refreshToken) write(REFRESH_KEY, tokens.refreshToken);
  write(EXPIRES_KEY, tokens.expiresIn ? String(Date.now() + tokens.expiresIn * 1000) : null);
}

// One renewal at a time: concurrent requests share it.
let renewing: Promise<string | null> | null = null;

function renew(): Promise<string | null> {
  renewing ??= (async () => {
    const refreshToken = read(REFRESH_KEY);
    if (!refreshToken) return null;
    try {
      // No Authorization header: the access token is expired, and the server would
      // refuse the request for it before reaching the route.
      const res = await fetch(API_BASE + "/api/v1/auth/cilogon/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });
      if (!res.ok) return null;
      const tokens = (await res.json()) as SignInTokens;
      setSignInTokens(tokens);
      window.dispatchEvent(new Event(TOKEN_EVENT));
      return tokens.accessToken;
    } catch {
      return null;
    }
  })().finally(() => {
    renewing = null;
  });
  return renewing;
}

// currentToken is the access token to send, renewed first when it is about to expire.
async function currentToken(): Promise<string | null> {
  const expiresAt = Number(read(EXPIRES_KEY) || 0);
  if (expiresAt && read(REFRESH_KEY) && Date.now() > expiresAt - REFRESH_MARGIN_MS) {
    return (await renew()) ?? getToken();
  }
  return getToken();
}

export class ApiError extends Error {
  status: number;
  fieldErrors: FieldError[];

  constructor(status: number, message: string, fieldErrors: FieldError[] = []) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  describe(): string {
    if (this.fieldErrors.length === 0) return this.message;
    return `${this.message}: ${this.fieldErrors.map((f) => `${f.field} — ${f.message}`).join("; ")}`;
  }
}

// A 401 anywhere means the stored token is no longer good; the shell listens for this
// and sends the user back to sign in.
export const UNAUTHORIZED_EVENT = "airavata:unauthorized";

async function send(method: string, path: string, init: RequestInit, token: string | null): Promise<Response> {
  const headers = new Headers(init.headers);
  if (token) headers.set("Authorization", `Bearer ${token}`);
  try {
    return await fetch(API_BASE + path, { ...init, method, headers });
  } catch {
    throw new ApiError(0, `Cannot reach the Airavata server at ${API_BASE || window.location.origin}`);
  }
}

async function raw(method: string, path: string, init: RequestInit = {}): Promise<Response> {
  let res = await send(method, path, init, await currentToken());
  // A CILogon token can be revoked or expire early; renew once and retry before
  // treating the 401 as a sign-out.
  if (res.status === 401 && read(REFRESH_KEY)) {
    const renewed = await renew();
    if (renewed) res = await send(method, path, init, renewed);
  }
  if (res.ok) return res;

  let envelope: Partial<ErrorEnvelope> = {};
  try {
    envelope = await res.json();
  } catch {
    // Not the JSON envelope (a proxy error page, say); fall back to the status text.
  }
  if (res.status === 401) window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
  throw new ApiError(res.status, envelope.message || res.statusText || `HTTP ${res.status}`, envelope.fieldErrors);
}

async function json<T>(method: string, path: string, body?: unknown): Promise<T> {
  const init: RequestInit = {};
  if (body !== undefined) {
    init.body = JSON.stringify(body);
    init.headers = { "Content-Type": "application/json" };
  }
  const res = await raw(method, path, init);
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  get: <T>(path: string) => json<T>("GET", path),
  post: <T>(path: string, body?: unknown) => json<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => json<T>("PUT", path, body),
  del: (path: string) => json<void>("DELETE", path),

  // Saves a file product's content through the browser. The download needs the bearer
  // token, so it is fetched here and handed over as a blob rather than linked to.
  async download(path: string, fallbackName: string) {
    const res = await raw("GET", path);
    const disposition = res.headers.get("Content-Disposition") ?? "";
    const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposition);
    const name = match ? decodeURIComponent(match[1]) : fallbackName;
    const url = URL.createObjectURL(await res.blob());
    const a = document.createElement("a");
    a.href = url;
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10_000);
  },

  async upload<T>(path: string, file: File): Promise<T> {
    const form = new FormData();
    form.append("file", file);
    const res = await raw("PUT", path, { body: form });
    return (await res.json()) as T;
  },
};

// tryGet reads a listing the caller may not be allowed to see, answering null on 403
// rather than failing the page: admin-only listings are offered to admins only.
export async function tryGet<T>(path: string): Promise<T | null> {
  try {
    return await api.get<T>(path);
  } catch (e) {
    if (e instanceof ApiError && e.status === 403) return null;
    throw e;
  }
}

export function errorText(e: unknown): string {
  if (e instanceof ApiError) return e.describe();
  if (e instanceof Error) return e.message;
  return String(e);
}

// The websocket URL for an API path, on the same origin as every other call.
export function wsURL(path: string): string {
  const base = API_BASE || window.location.origin;
  return base.replace(/^http/, "ws") + path;
}

// A browser WebSocket cannot send an Authorization header, so the token rides as a
// subprotocol the server reads (internal/auth/middleware.go), beside the one it selects.
export function wsProtocols(token: string): string[] {
  const bytes = new TextEncoder().encode(token);
  let bin = "";
  bytes.forEach((b) => (bin += String.fromCharCode(b)));
  const encoded = btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  return ["airavata.v1", `base64url.bearer.${encoded}`];
}
