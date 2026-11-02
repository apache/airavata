"use client";

import { useState } from "react";
import { Alert, Card, ConfirmButton, Field, Id, Modal, PageHead, Table } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import type { SSHCredential, SSHCredentialRequest } from "@/lib/types";

export default function CredentialsPage() {
  const creds = useResource(() => api.get<SSHCredential[]>("/api/v1/ssh-credentials"));
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  return (
    <div className="stack">
      <PageHead
        title="SSH credentials"
        sub="Keypairs that cluster configs and data storages present when they log in. Private keys are write-only."
      >
        <button className="primary" onClick={() => setCreating(true)}>
          Register credential
        </button>
      </PageHead>
      <Alert>{creds.error || error}</Alert>
      <Card flush>
        <Table
          rows={creds.data}
          rowKey={(c) => c.sshCredentialId}
          empty="No credentials registered yet."
          columns={[
            { header: "Name", cell: (c) => <strong>{c.sshCredentialName}</strong> },
            { header: "ID", cell: (c) => <Id value={c.sshCredentialId} /> },
            {
              header: "Public key",
              cell: (c) => (
                <code className="truncate" style={{ maxWidth: 360 }} title={c.publicKey}>
                  {c.publicKey}
                </code>
              ),
            },
            {
              header: "Login",
              cell: (c) =>
                c.fullInteractive ? <span className="badge warn">key + interactive</span> : <span className="badge">key</span>,
            },
            {
              header: "",
              className: "actions",
              cell: (c) => (
                <ConfirmButton
                  onConfirm={async () => {
                    setError(null);
                    try {
                      await api.del(`/api/v1/ssh-credentials/${c.sshCredentialId}`);
                      await creds.reload();
                    } catch (e) {
                      setError(errorText(e));
                    }
                  }}
                >
                  Delete
                </ConfirmButton>
              ),
            },
          ]}
        />
      </Card>
      {creating && (
        <CreateCredential
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false);
            void creds.reload();
          }}
        />
      )}
    </div>
  );
}

function CreateCredential({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState<SSHCredentialRequest>({
    sshCredentialName: "",
    publicKey: "",
    privateKey: "",
    passphrase: null,
    fullInteractive: false,
  });
  const save = useAction(async () => {
    await api.post("/api/v1/ssh-credentials", { ...form, passphrase: form.passphrase || null });
    onCreated();
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
