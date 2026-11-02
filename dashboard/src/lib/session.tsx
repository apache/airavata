"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, ApiError, getToken, setToken, UNAUTHORIZED_EVENT } from "./api";

interface Session {
  // undefined until the stored token has been read, so the shell does not flash the
  // sign-in page on every load.
  token: string | null | undefined;
  isAdmin: boolean;
  signIn: (token: string) => Promise<void>;
  signOut: () => void;
}

const SessionContext = createContext<Session | null>(null);

// probeAdmin asks a listing only admins may read — the user list, the lightest of them.
// There is no "who am I" route, and this decides whether admin-only views are offered.
async function probeAdmin(): Promise<boolean> {
  try {
    await api.get("/api/v1/users");
    return true;
  } catch {
    return false;
  }
}

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null | undefined>(undefined);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const stored = getToken();
    setTokenState(stored);
    if (stored) void probeAdmin().then(setIsAdmin);
  }, []);

  const signOut = useCallback(() => {
    setToken(null);
    setTokenState(null);
    setIsAdmin(false);
  }, []);

  useEffect(() => {
    window.addEventListener(UNAUTHORIZED_EVENT, signOut);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, signOut);
  }, [signOut]);

  const signIn = useCallback(async (candidate: string) => {
    const trimmed = candidate.trim();
    setToken(trimmed);
    try {
      // Any authenticated-only route will do; this one needs no users row.
      await api.get("/api/v1/processes/me");
    } catch (e) {
      setToken(null);
      if (e instanceof ApiError && e.status === 401) throw new ApiError(401, "That token was not accepted");
      throw e;
    }
    setTokenState(trimmed);
    setIsAdmin(await probeAdmin());
  }, []);

  return <SessionContext.Provider value={{ token, isAdmin, signIn, signOut }}>{children}</SessionContext.Provider>;
}

export function useSession(): Session {
  const s = useContext(SessionContext);
  if (!s) throw new Error("useSession outside SessionProvider");
  return s;
}
