"use client";

import { Fragment, useEffect, useId, useState } from "react";
import { errorText } from "@/lib/api";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import type { InteractiveAuth } from "@/lib/types";
import { Alert, Modal } from "./ui";

// Lines made of Unicode block elements (U+2580–U+259F) are terminal-drawn QR codes or
// similar art; they only scan when kept on one line, in a monospace font, at line-height 1.
const BLOCK_ART = /[\u2580-\u259f]/;
const URL_RE = /(https?:\/\/[^\s<>"'`]+[^\s<>"'`.,;:!?)\]}])/g;

function isArtLine(line: string) {
  const chars = line.replace(/\s/g, "");
  if (!chars) return false;
  let art = 0;
  for (const c of chars) if (BLOCK_ART.test(c)) art++;
  return art / chars.length >= 0.6;
}

// Linkify turns bare http(s) URLs into links that open in a new tab.
function Linkify({ text }: { text: string }) {
  const parts = text.split(URL_RE);
  return (
    <>
      {parts.map((part, i) =>
        i % 2 === 1 ? (
          <a key={i} href={part} target="_blank" rel="noopener noreferrer">
            {part}
          </a>
        ) : (
          <Fragment key={i}>{part}</Fragment>
        ),
      )}
    </>
  );
}

// PromptText renders a keyboard-interactive prompt as the host formatted it: line breaks
// kept, URLs clickable, and block-art runs (QR codes) drawn like a terminal would.
export function PromptText({ text }: { text: string }) {
  const segments: { art: boolean; lines: string[] }[] = [];
  for (const line of text.replace(/\r\n?/g, "\n").trim().split("\n")) {
    const art = isArtLine(line);
    const last = segments[segments.length - 1];
    if (last && last.art === art) last.lines.push(line);
    else segments.push({ art, lines: [line] });
  }
  return (
    <div className="prompt-text">
      {segments.map((seg, i) =>
        seg.art ? (
          <pre key={i} className="qr-art" aria-label="QR code">
            {seg.lines.join("\n")}
          </pre>
        ) : (
          <div key={i} className="prompt-lines">
            <Linkify text={seg.lines.join("\n").trim()} />
          </div>
        ),
      )}
    </div>
  );
}

// AnswerForm shows what the host asked and collects one response per prompt. Secret
// prompts (echo: false) are masked; answers are sent once and kept nowhere.
export function AnswerForm({ auth, onDone }: { auth: InteractiveAuth; onDone?: () => void }) {
  const { answer, cancel } = useInteractiveAuth();
  const [responses, setResponses] = useState<string[]>(() => auth.prompts.map(() => ""));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const baseId = useId();

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
      {auth.banner.trim() && (
        <pre className="block">
          <Linkify text={auth.banner.trim()} />
        </pre>
      )}
      {(auth.name || auth.instruction) && (
        <div>
          {auth.name && <strong>{auth.name} </strong>}
          {auth.instruction && <PromptText text={auth.instruction} />}
        </div>
      )}
      {auth.prompts.length === 0 && <div className="muted">The host asked no questions; submit to continue.</div>}
      {auth.prompts.map((p, i) => (
        <div className="field" key={i}>
          <label htmlFor={`${baseId}-${i}`}>
            {p.text.trim() ? <PromptText text={p.text} /> : `Response ${i + 1}`}
          </label>
          <input
            id={`${baseId}-${i}`}
            type={p.echo ? "text" : "password"}
            autoComplete={p.echo ? "off" : "one-time-code"}
            autoFocus={i === 0}
            value={responses[i] ?? ""}
            onChange={(e) => setResponses((r) => r.map((v, j) => (j === i ? e.target.value : v)))}
          />
        </div>
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
