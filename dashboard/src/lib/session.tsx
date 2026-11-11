"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, ApiError, getToken, setSignInTokens, setToken, TOKEN_EVENT, UNAUTHORIZED_EVENT } from "./api";
import type { SignInTokens } from "./api";
import type { CurrentUser } from "./types";

interface Session {
  // undefined until the stored token has been read, so the shell does not flash the
  // sign-in page on every load.
  token: string | null | undefined;
  // me is the signed-in caller, null until it has been read.
  me: CurrentUser | null;
  isAdmin: boolean;
  // active is false for an account an administrator has not activated, or has
  // suspended. The server refuses such an account everything but /api/v1/users/me.
  active: boolean;
  // refreshMe reads the caller again, for a user waiting to be activated.
  refreshMe: () => Promise<void>;
  signIn: (token: string) => Promise<void>;
  // signInWithCILogon keeps the tokens the server's CILogon sign-in returned, refresh
  // token included, so the access token is renewed before it expires.
  signInWithCILogon: (tokens: SignInTokens) => Promise<void>;
  signOut: () => void;
}

const SessionContext = createContext<Session | null>(null);

const ME = "/api/v1/users/me";

// loadMe reads the caller, or null when the server can't say. That is never because
// the token was refused: a refused token signs the browser out instead.
async function loadMe(): Promise<CurrentUser | null> {
  try {
    return await api.get<CurrentUser>(ME);
  } catch {
    return null;
  }
}

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null | undefined>(undefined);
  const [me, setMe] = useState<CurrentUser | null>(null);

  useEffect(() => {
    const stored = getToken();
    setTokenState(stored);
    if (stored) void loadMe().then(setMe);
  }, []);

  const signOut = useCallback(() => {
    setToken(null);
    setTokenState(null);
    setMe(null);
  }, []);

  const refreshMe = useCallback(async () => {
    setMe(await loadMe());
  }, []);

  useEffect(() => {
    window.addEventListener(UNAUTHORIZED_EVENT, signOut);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, signOut);
  }, [signOut]);

  // A renewal replaced the access token: hold the new one.
  useEffect(() => {
    const onRenewed = () => setTokenState(getToken());
    window.addEventListener(TOKEN_EVENT, onRenewed);
    return () => window.removeEventListener(TOKEN_EVENT, onRenewed);
  }, []);

  // verify keeps the stored token if the server accepts it, and forgets it otherwise.
  // It reads the caller, the one route open even to an account awaiting activation.
  const verify = useCallback(async () => {
    let caller: CurrentUser;
    try {
      caller = await api.get<CurrentUser>(ME);
    } catch (e) {
      setToken(null);
      if (e instanceof ApiError && e.status === 401) throw new ApiError(401, "That token was not accepted");
      throw e;
    }
    setMe(caller);
    setTokenState(getToken());
  }, []);

  const signIn = useCallback(
    async (candidate: string) => {
      setToken(candidate.trim());
      await verify();
    },
    [verify],
  );

  const signInWithCILogon = useCallback(
    async (tokens: SignInTokens) => {
      setSignInTokens(tokens);
      await verify();
    },
    [verify],
  );

  const isAdmin = !!me?.admin;
  const active = !me || me.status === "ACTIVE";

  return (
    <SessionContext.Provider value={{ token, me, isAdmin, active, refreshMe, signIn, signInWithCILogon, signOut }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession(): Session {
  const s = useContext(SessionContext);
  if (!s) throw new Error("useSession outside SessionProvider");
  return s;
}
