import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { getToken } from "next-auth/jwt";

export async function middleware(request: NextRequest) {
  const token = await getToken({ req: request, secret: process.env.NEXTAUTH_SECRET });
  const { pathname } = request.nextUrl;

  // Auth API routes should always be allowed (login/logout flow)
  if (pathname.startsWith("/api/auth")) {
    return NextResponse.next();
  }

  // Login page handling
  if (pathname === "/login" || pathname.startsWith("/login")) {
    // Session recovery: we redirected here after session-check 401 (stale token).
    // Do NOT redirect to dashboard — let the user land on login so we can clear the stale session.
    if (request.nextUrl.searchParams.get("session_expired") === "1") {
      return NextResponse.next();
    }
    // If user is already authenticated, redirect them to the dashboard
    if (token) {
      const gatewayId = (token as { gatewayId?: string }).gatewayId || "default";
      const dashboardUrl = new URL(`/${gatewayId}`, request.url);
      return NextResponse.redirect(dashboardUrl);
    }
    return NextResponse.next();
  }

  // Protected routes - require authentication
  if (!token) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Token exists, allow the request
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    "/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)",
  ],
};
