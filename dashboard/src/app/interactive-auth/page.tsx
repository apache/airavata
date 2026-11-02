"use client";

import { useState } from "react";
import { AnswerForm } from "@/components/InteractiveAuth";
import { Alert, Card, PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import type { InteractiveAuth } from "@/lib/types";

const DESCRIPTION: Record<string, string> = {
  live: "Subscribed over a WebSocket: new requests appear the moment a login stops to ask.",
  polling: "The WebSocket is unavailable, so the dashboard is checking for requests every few seconds.",
  connecting: "Connecting to the server…",
  off: "Not subscribed.",
};

export default function InteractiveAuthPage() {
  const { connection, revision } = useInteractiveAuth();
  // Every waiting login, not only the oldest the subscription presents. Re-read whenever
  // the subscription reports a change.
  const pending = useResource(() => api.get<InteractiveAuth[]>("/api/v1/ssh-interactive-auths"), [revision], 5000);
  const [selected, setSelected] = useState<string | null>(null);

  const auths = pending.data ?? [];
  const active = auths.find((a) => a.authId === selected) ?? auths[0];

  return (
    <div className="stack">
      <PageHead
        title="Interactive auth requests"
        sub="SSH logins made with a key that also needs a password, OTP or Duo stop here until you answer."
      />
      <Alert kind={connection === "live" ? "info" : "warn"}>{DESCRIPTION[connection]}</Alert>
      <Alert>{pending.error}</Alert>
      {auths.length === 0 ? (
        <Card>
          <div className="empty">
            Nothing is waiting for you. Requests appear here, and as a pop-up on any page, when a transfer or job
            submission logs in with an interactive credential.
          </div>
        </Card>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "minmax(220px, 300px) 1fr", gap: 16 }}>
          <Card title={`Waiting (${auths.length})`} flush>
            <div className="nav" style={{ padding: 8 }}>
              {auths.map((a) => (
                <a
                  key={a.authId}
                  href="#"
                  className={a.authId === active?.authId ? "active" : ""}
                  onClick={(e) => {
                    e.preventDefault();
                    setSelected(a.authId);
                  }}
                >
                  <span>
                    <code>
                      {a.loginUser}@{a.hostName}
                    </code>
                    <div className="small muted">{new Date(a.createdAt).toLocaleTimeString()}</div>
                  </span>
                </a>
              ))}
            </div>
          </Card>
          {active && (
            <Card title="Answer">
              <AnswerForm key={active.authId} auth={active} onDone={() => void pending.reload()} />
            </Card>
          )}
        </div>
      )}
    </div>
  );
}
