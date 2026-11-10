"use client";

import { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import { api, wsProtocols, wsURL } from "./api";
import type { InteractiveAuth, InteractiveAuthMessage } from "./types";
import { useSession } from "./session";

export type Connection = "connecting" | "live" | "polling" | "off";

interface InteractiveAuthState {
  // The oldest auth waiting on this user, which is the one the server's subscribe
  // socket presents; null when nothing is waiting.
  current: InteractiveAuth | null;
  connection: Connection;
  answer: (authId: string, responses: string[]) => Promise<void>;
  cancel: (authId: string) => Promise<void>;
  // Bumped whenever the pending set may have changed, for views listing all of them.
  revision: number;
}

const Ctx = createContext<InteractiveAuthState | null>(null);

const POLL_MS = 4000;
const RECONNECT_MS = 3000;

// InteractiveAuthProvider subscribes to the caller's keyboard-interactive SSH logins for
// as long as the dashboard is open, so a login waiting on a password or OTP surfaces on
// whichever page the user is on.
//
// It listens on the subscribe socket (/api/v1/ssh-interactive-auths/ws) and falls back to
// polling the REST listing when the socket cannot be opened — as under `npm run dev`,
// where the socket's origin check sees a different port. Answers always go over REST:
// the socket only announces, and moves on by itself once an auth is answered anywhere.
export function InteractiveAuthProvider({ children }: { children: React.ReactNode }) {
  const session = useSession();
  // An account awaiting activation can't read interactive auths, so it doesn't ask.
  const token = session.active ? session.token : null;
  const [current, setCurrent] = useState<InteractiveAuth | null>(null);
  const [connection, setConnection] = useState<Connection>("off");
  const [revision, setRevision] = useState(0);
  const pollRef = useRef<() => Promise<void>>(async () => {});

  useEffect(() => {
    if (!token) {
      setCurrent(null);
      setConnection("off");
      return;
    }

    let disposed = false;
    let socket: WebSocket | null = null;
    let pollTimer: ReturnType<typeof setInterval> | null = null;
    let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

    const poll = async () => {
      try {
        const pending = await api.get<InteractiveAuth[]>("/api/v1/ssh-interactive-auths");
        if (!disposed) {
          setCurrent(pending[0] ?? null);
          setRevision((r) => r + 1);
        }
      } catch {
        // Transient; the next tick tries again.
      }
    };
    pollRef.current = poll;

    const startPolling = () => {
      if (pollTimer || disposed) return;
      setConnection("polling");
      void poll();
      pollTimer = setInterval(poll, POLL_MS);
    };
    const stopPolling = () => {
      if (pollTimer) clearInterval(pollTimer);
      pollTimer = null;
    };

    const connect = () => {
      if (disposed) return;
      let opened = false;
      setConnection((c) => (c === "polling" ? c : "connecting"));
      try {
        socket = new WebSocket(wsURL("/api/v1/ssh-interactive-auths/ws"), wsProtocols(token));
      } catch {
        startPolling();
        return;
      }
      socket.onopen = () => {
        opened = true;
        stopPolling();
        setConnection("live");
      };
      socket.onmessage = (ev) => {
        let msg: InteractiveAuthMessage;
        try {
          msg = JSON.parse(ev.data);
        } catch {
          return;
        }
        if (msg.type === "challenge" && msg.auth) setCurrent(msg.auth);
        if (msg.type === "idle") setCurrent(null);
        setRevision((r) => r + 1);
      };
      socket.onclose = () => {
        socket = null;
        if (disposed) return;
        // Poll while the socket is down. A socket that never opened is retried less
        // eagerly: it most likely failed its handshake and will again.
        startPolling();
        reconnectTimer = setTimeout(connect, opened ? RECONNECT_MS : 30_000);
      };
    };

    connect();
    return () => {
      disposed = true;
      stopPolling();
      if (reconnectTimer) clearTimeout(reconnectTimer);
      socket?.close();
    };
  }, [token]);

  const answer = useCallback(async (authId: string, responses: string[]) => {
    await api.post(`/api/v1/ssh-interactive-auths/${encodeURIComponent(authId)}/response`, { responses });
    setCurrent((c) => (c?.authId === authId ? null : c));
    await pollRef.current();
  }, []);

  const cancel = useCallback(async (authId: string) => {
    await api.post(`/api/v1/ssh-interactive-auths/${encodeURIComponent(authId)}/response`, {
      responses: [],
      cancel: true,
    });
    setCurrent((c) => (c?.authId === authId ? null : c));
    await pollRef.current();
  }, []);

  return <Ctx.Provider value={{ current, connection, answer, cancel, revision }}>{children}</Ctx.Provider>;
}

export function useInteractiveAuth(): InteractiveAuthState {
  const s = useContext(Ctx);
  if (!s) throw new Error("useInteractiveAuth outside InteractiveAuthProvider");
  return s;
}
