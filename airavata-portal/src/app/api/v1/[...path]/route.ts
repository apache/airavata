import { NextRequest, NextResponse } from "next/server";

const API_URL = process.env.API_URL || "http://localhost:8090";

function buildBackendUrl(path: string[], request: NextRequest): string {
  const segment = path.join("/");
  const base = API_URL.replace(/\/$/, "");
  const url = new URL(`/api/v1/${segment}`, base);
  request.nextUrl.searchParams.forEach((value, key) => {
    url.searchParams.set(key, value);
  });
  return url.toString();
}

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const url = buildBackendUrl(path, request);
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (
      key.toLowerCase() !== "host" &&
      key.toLowerCase() !== "connection" &&
      key.toLowerCase() !== "content-length"
    ) {
      headers.set(key, value);
    }
  });
  try {
    const res = await fetch(url, { method: "GET", headers });
    const data = await res.text();
    return new NextResponse(data, {
      status: res.status,
      statusText: res.statusText,
      headers: {
        "Content-Type": res.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (e) {
    return NextResponse.json(
      { error: "Backend unreachable", message: String(e) },
      { status: 502 }
    );
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const url = buildBackendUrl(path, request);
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (
      key.toLowerCase() !== "host" &&
      key.toLowerCase() !== "connection" &&
      key.toLowerCase() !== "content-length"
    ) {
      headers.set(key, value);
    }
  });
  const body = await request.text();
  if (body) headers.set("Content-Type", request.headers.get("Content-Type") ?? "application/json");
  try {
    const res = await fetch(url, { method: "POST", headers, body: body || undefined });
    const data = await res.text();
    return new NextResponse(data, {
      status: res.status,
      statusText: res.statusText,
      headers: {
        "Content-Type": res.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (e) {
    return NextResponse.json(
      { error: "Backend unreachable", message: String(e) },
      { status: 502 }
    );
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const url = buildBackendUrl(path, request);
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (
      key.toLowerCase() !== "host" &&
      key.toLowerCase() !== "connection" &&
      key.toLowerCase() !== "content-length"
    ) {
      headers.set(key, value);
    }
  });
  const body = await request.text();
  if (body) headers.set("Content-Type", request.headers.get("Content-Type") ?? "application/json");
  try {
    const res = await fetch(url, { method: "PUT", headers, body: body || undefined });
    const data = await res.text();
    return new NextResponse(data, {
      status: res.status,
      statusText: res.statusText,
      headers: {
        "Content-Type": res.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (e) {
    return NextResponse.json(
      { error: "Backend unreachable", message: String(e) },
      { status: 502 }
    );
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const url = buildBackendUrl(path, request);
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (
      key.toLowerCase() !== "host" &&
      key.toLowerCase() !== "connection" &&
      key.toLowerCase() !== "content-length"
    ) {
      headers.set(key, value);
    }
  });
  const body = await request.text();
  if (body) headers.set("Content-Type", request.headers.get("Content-Type") ?? "application/json");
  try {
    const res = await fetch(url, { method: "PATCH", headers, body: body || undefined });
    const data = await res.text();
    return new NextResponse(data, {
      status: res.status,
      statusText: res.statusText,
      headers: {
        "Content-Type": res.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (e) {
    return NextResponse.json(
      { error: "Backend unreachable", message: String(e) },
      { status: 502 }
    );
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const url = buildBackendUrl(path, request);
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (
      key.toLowerCase() !== "host" &&
      key.toLowerCase() !== "connection" &&
      key.toLowerCase() !== "content-length"
    ) {
      headers.set(key, value);
    }
  });
  try {
    const res = await fetch(url, { method: "DELETE", headers });
    const data = await res.text();
    return new NextResponse(data, {
      status: res.status,
      statusText: res.statusText,
      headers: {
        "Content-Type": res.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (e) {
    return NextResponse.json(
      { error: "Backend unreachable", message: String(e) },
      { status: 502 }
    );
  }
}
