import { NextRequest, NextResponse } from "next/server";

export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const error = searchParams.get("error") || "Unknown";
  
  // Redirect to login page with error parameter
  const loginUrl = new URL("/login", request.url);
  loginUrl.searchParams.set("error", error);
  
  return NextResponse.redirect(loginUrl);
}
