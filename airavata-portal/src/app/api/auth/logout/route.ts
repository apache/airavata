import { NextRequest, NextResponse } from "next/server";
import { buildKeycloakLogoutUrl } from "@/lib/auth";

/**
 * Server-side logout endpoint that handles complete federated OIDC logout.
 *
 * Clears the NextAuth session and redirects to Keycloak's end_session endpoint
 * with only client_id and post_logout_redirect_uri (no id_token_hint) to avoid
 * sending stale tokens after realm wipe (e.g. cold-start).
 */
export async function GET(request: NextRequest) {
  const postLogoutRedirectUri = `${request.nextUrl.origin}/login`;
  const keycloakLogoutUrl = buildKeycloakLogoutUrl(postLogoutRedirectUri);
  
  // Create the redirect response
  const response = NextResponse.redirect(keycloakLogoutUrl, { status: 302 });
  
  // Properly clear NextAuth v5 session cookies
  // NextAuth v5 uses "authjs." prefix, but we clear both for compatibility
  // The cookie name depends on whether HTTPS is used (__Secure- prefix)
  const isSecure = request.url.startsWith('https://');
  
  const cookieOptions = {
    expires: new Date(0),
    maxAge: 0,
    path: '/',
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: isSecure,
  };
  
  // NextAuth v5 cookie names
  if (isSecure) {
    response.cookies.set('__Secure-authjs.session-token', '', { ...cookieOptions, secure: true });
    response.cookies.set('__Host-authjs.csrf-token', '', { ...cookieOptions, secure: true, path: '/' });
  } else {
    response.cookies.set('authjs.session-token', '', cookieOptions);
    response.cookies.set('authjs.csrf-token', '', cookieOptions);
  }
  
  // Also clear NextAuth v4 cookie names for backwards compatibility
  if (isSecure) {
    response.cookies.set('__Secure-next-auth.session-token', '', { ...cookieOptions, secure: true });
    response.cookies.set('__Secure-next-auth.csrf-token', '', { ...cookieOptions, secure: true });
    response.cookies.set('__Host-next-auth.csrf-token', '', { ...cookieOptions, secure: true, path: '/' });
  } else {
    response.cookies.set('next-auth.session-token', '', cookieOptions);
    response.cookies.set('next-auth.csrf-token', '', cookieOptions);
  }
  
  // Clear callback URL cookie if present
  response.cookies.set('authjs.callback-url', '', cookieOptions);
  response.cookies.set('next-auth.callback-url', '', cookieOptions);

  return response;
}

/**
 * POST handler for programmatic logout (e.g., from fetch calls)
 * Returns JSON with the logout URL instead of redirecting
 */
export async function POST(request: NextRequest) {
  const postLogoutRedirectUri = `${request.nextUrl.origin}/login`;
  const keycloakLogoutUrl = buildKeycloakLogoutUrl(postLogoutRedirectUri);
  
  // Create response with logout URL
  const response = NextResponse.json({ 
    logoutUrl: keycloakLogoutUrl,
    success: true 
  });
  
  // Clear cookies (same as GET handler)
  const isSecure = request.url.startsWith('https://');
  
  const cookieOptions = {
    expires: new Date(0),
    maxAge: 0,
    path: '/',
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: isSecure,
  };
  
  if (isSecure) {
    response.cookies.set('__Secure-authjs.session-token', '', { ...cookieOptions, secure: true });
  } else {
    response.cookies.set('authjs.session-token', '', cookieOptions);
  }
  
  return response;
}
