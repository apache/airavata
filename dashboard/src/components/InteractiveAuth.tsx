"use client";

import { useEffect, useState } from "react";
import { errorText } from "@/lib/api";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import type { InteractiveAuth } from "@/lib/types";
import { Alert, Modal } from "./ui";

// AnswerForm shows what the host asked and collects one response per prompt. Secret
// prompts (echo: false) are masked; answers are sent once and kept nowhere.
export function AnswerForm({ auth, onDone }: { auth: InteractiveAuth; onDone?: () => void }) {
  const { answer, cancel } = useInteractiveAuth();
  const [responses, setResponses] = useState<string[]>(() => auth.prompts.map(() => ""));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setResponses(auth.prompts.map(() => ""));
    setError(null);
  }, [auth.authId, auth.prompts]);

  const run = async (fn: () => Promise<void>) => {
    setBusy(true);
    setError(null);
    try {
      await fn();
      onDone?.();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault();
        void run(() => answer(auth.authId, responses));
      }}
    >
      <div className="small muted">
        <code>
          {auth.loginUser}@{auth.hostName}:{auth.port}
        </code>{" "}
        · waiting since {new Date(auth.createdAt).toLocaleTimeString()}
      </div>
      {auth.banner.trim() && <pre className="block">{auth.banner.trim()}</pre>}
      {(auth.name || auth.instruction) && (
        <div>
          {auth.name && <strong>{auth.name} </strong>}
          {auth.instruction}
        </div>
      )}
      {auth.prompts.length === 0 && <div className="muted">The host asked no questions; submit to continue.</div>}
      {auth.prompts.map((p, i) => (
        <label className="field" key={i}>
          <span>{p.text.trim() || `Response ${i + 1}`}</span>
          <input
            type={p.echo ? "text" : "password"}
            autoComplete={p.echo ? "off" : "one-time-code"}
            autoFocus={i === 0}
            value={responses[i] ?? ""}
            onChange={(e) => setResponses((r) => r.map((v, j) => (j === i ? e.target.value : v)))}
          />
        </label>
      ))}
      <Alert>{error}</Alert>
      <div className="row">
        <button type="submit" className="primary" disabled={busy}>
          {busy ? "Sending…" : "Submit"}
        </button>
        <button type="button" className="danger" disabled={busy} onClick={() => void run(() => cancel(auth.authId))}>
          Cancel login
        </button>
      </div>
    </form>
  );
}

// InteractiveAuthDialog pops the oldest waiting login over whatever page is open. "Later"
// hides that one auth until a different one arrives; the navigation rail still flags it.
export function InteractiveAuthDialog() {
  const { current } = useInteractiveAuth();
  const [snoozed, setSnoozed] = useState<string | null>(null);

  if (!current || current.authId === snoozed) return null;
  return (
    <Modal title="SSH login needs your answer" onClose={() => setSnoozed(current.authId)}>
      <AnswerForm auth={current} />
      <div className="row" style={{ marginTop: 12 }}>
        <span className="spacer" />
        <button type="button" className="link" onClick={() => setSnoozed(current.authId)}>
          Answer later
        </button>
      </div>
    </Modal>
  );
}
