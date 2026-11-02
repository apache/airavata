"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { useAction } from "@/lib/hooks";
import type { SSHCredential, SSHCredentialRequest } from "@/lib/types";
import { Alert, Field, Modal } from "./ui";

export function CreateCredential({ onClose, onCreated }: { onClose: () => void; onCreated: (c: SSHCredential) => void }) {
  const [form, setForm] = useState<SSHCredentialRequest>({
    sshCredentialName: "",
    publicKey: "",
    privateKey: "",
    passphrase: null,
    fullInteractive: false,
  });
  const save = useAction(async () => {
    onCreated(await api.post<SSHCredential>("/api/v1/ssh-credentials", { ...form, passphrase: form.passphrase || null }));
  });
  const set = (patch: Partial<SSHCredentialRequest>) => setForm((f) => ({ ...f, ...patch }));

  return (
    <Modal
      title="Register SSH credential"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Register"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Name">
          <input value={form.sshCredentialName} onChange={(e) => set({ sshCredentialName: e.target.value })} />
        </Field>
        <Field label="Public key">
          <textarea
            style={{ minHeight: 60 }}
            value={form.publicKey}
            onChange={(e) => set({ publicKey: e.target.value })}
            placeholder="ssh-ed25519 AAAA… user@host"
          />
        </Field>
        <Field label="Private key">
          <textarea
            value={form.privateKey ?? ""}
            onChange={(e) => set({ privateKey: e.target.value })}
            placeholder="-----BEGIN OPENSSH PRIVATE KEY-----"
          />
        </Field>
        <Field label="Passphrase" hint="optional">
          <input
            type="password"
            autoComplete="new-password"
            value={form.passphrase ?? ""}
            onChange={(e) => set({ passphrase: e.target.value })}
          />
        </Field>
        <label className="check">
          <input
            type="checkbox"
            checked={form.fullInteractive}
            onChange={(e) => set({ fullInteractive: e.target.checked })}
          />
          Host also asks keyboard-interactive questions (password, OTP, Duo) — answer them from this dashboard
        </label>
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}
