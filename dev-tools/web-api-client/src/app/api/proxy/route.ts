import { NextRequest, NextResponse } from "next/server";
import { getSession } from "@/lib/session";

const METHODS_WITH_BODY = new Set(["POST", "PUT", "PATCH"]);

export async function POST(req: NextRequest) {
  const session = await getSession();
  if (!session?.accessToken) {
    return NextResponse.json({ error: "Not authenticated with CILogon" }, { status: 401 });
  }

  const payload = await req.json().catch(() => null);
  if (!payload?.url || !payload?.method) {
    return NextResponse.json({ error: "Missing url or method" }, { status: 400 });
  }

  const { url, method, headers, body } = payload as {
    url: string;
    method: string;
    headers?: Record<string, string>;
    body?: string;
  };

  let target: URL;
  try {
    target = new URL(url);
  } catch {
    return NextResponse.json({ error: "Invalid URL" }, { status: 400 });
  }
  if (target.protocol !== "http:" && target.protocol !== "https:") {
    return NextResponse.json({ error: "Only http/https URLs are allowed" }, { status: 400 });
  }

  console.log(`Access token: ${session.accessToken}`);

  const outgoingHeaders: Record<string, string> = {
    ...(headers ?? {}),
    Authorization: `Bearer ${session.accessToken}`,
  };

  const init: RequestInit = { method, headers: outgoingHeaders };
  if (METHODS_WITH_BODY.has(method.toUpperCase()) && body) {
    init.body = body;
  }

  const start = Date.now();
  try {
    const upstream = await fetch(target, init);
    const text = await upstream.text();
    const responseHeaders: Record<string, string> = {};
    upstream.headers.forEach((value, key) => {
      responseHeaders[key] = value;
    });

    return NextResponse.json({
      status: upstream.status,
      statusText: upstream.statusText,
      headers: responseHeaders,
      body: text,
      durationMs: Date.now() - start,
    });
  } catch (err) {
    return NextResponse.json(
      {
        error: "Request failed",
        message: err instanceof Error ? err.message : String(err),
        durationMs: Date.now() - start,
      },
      { status: 502 }
    );
  }
}
