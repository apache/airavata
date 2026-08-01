import { SignJWT, jwtVerify } from "jose";
import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const SESSION_COOKIE = "airavata_session";
const SESSION_MAX_AGE_SECONDS = 60 * 60 * 24;

function secretKey() {
  return new TextEncoder().encode(process.env.SESSION_SECRET);
}

export interface SessionData {
  accessToken: string;
  accessTokenExpires?: number;
  email?: string;
  name?: string;
}

export async function createSession(res: NextResponse, data: SessionData) {
  const jwt = await new SignJWT({ ...data })
    .setProtectedHeader({ alg: "HS256" })
    .setIssuedAt()
    .setExpirationTime(`${SESSION_MAX_AGE_SECONDS}s`)
    .sign(secretKey());

  res.cookies.set(SESSION_COOKIE, jwt, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
    maxAge: SESSION_MAX_AGE_SECONDS,
  });
}

export function clearSession(res: NextResponse) {
  res.cookies.delete(SESSION_COOKIE);
}

export async function getSession(): Promise<SessionData | null> {
  const token = cookies().get(SESSION_COOKIE)?.value;
  if (!token) return null;

  try {
    const { payload } = await jwtVerify(token, secretKey());
    return payload as unknown as SessionData;
  } catch {
    return null;
  }
}
