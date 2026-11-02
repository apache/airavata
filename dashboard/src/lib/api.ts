import type { ErrorEnvelope, FieldError } from "./types";

// The built dashboard is served by the API server, so it calls the API on its own
// origin. `npm run dev` runs on another port and points here instead.
export const API_BASE = (process.env.NEXT_PUBLIC_AIRAVATA_API_URL ?? "").replace(/\/$/, "");

const TOKEN_KEY = "airavata.token";

export function getToken(): string | null {
  try {
    return window.localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string | null) {
  try {
    if (token) window.localStorage.setItem(TOKEN_KEY, token);
    else window.localStorage.removeItem(TOKEN_KEY);
  } catch {
    // Storage unavailable (private mode); the token lives only as long as the page.
  }
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

async function raw(method: string, path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let res: Response;
  try {
    res = await fetch(API_BASE + path, { ...init, method, headers });
  } catch {
    throw new ApiError(0, `Cannot reach the Airavata server at ${API_BASE || window.location.origin}`);
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
