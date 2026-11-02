"use client";

import { useState } from "react";
import { Alert, Card, ConfirmButton, Field, Id, Modal, numOrNull, PageHead, Table } from "@/components/ui";
import { api, errorText, tryGet } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { SCPDataStorage, SSHCredential } from "@/lib/types";

export default function StoragesPage() {
  const { isAdmin } = useSession();
  const mine = useResource(() => api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/me"));
  const shared = useResource(() => api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/shared-with-me"));
  const all = useResource(() => (isAdmin ? tryGet<SCPDataStorage[]>("/api/v1/scp-data-storages") : Promise.resolve(null)), [isAdmin]);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const base = [
    { header: "Name", cell: (s: SCPDataStorage) => <strong>{s.dataName || "Unnamed"}</strong> },
    { header: "ID", cell: (s: SCPDataStorage) => <Id value={s.dataId} /> },
    { header: "Host", cell: (s: SCPDataStorage) => <code>{`${s.hostName}:${s.port ?? 22}`}</code> },
    { header: "Login", cell: (s: SCPDataStorage) => <code>{s.loginUser}</code> },
    { header: "Credential", cell: (s: SCPDataStorage) => s.sshCredential?.sshCredentialName ?? <Id value={s.sshCredentialId} /> },
  ];

  return (
    <div className="stack">
      <PageHead title="Data storages" sub="SCP hosts that data products live on and that runs stage data through.">
        <button className="primary" onClick={() => setCreating(true)}>
          Add storage
        </button>
      </PageHead>
      <Alert>{mine.error || shared.error || error}</Alert>
      <Card title="My storages" flush>
        <Table
          rows={mine.data}
          rowKey={(s) => s.dataId}
          empty="No storages yet."
          columns={[
            ...base,
            {
              header: "",
              className: "actions",
              cell: (s) => (
                <ConfirmButton
                  onConfirm={async () => {
                    setError(null);
                    try {
                      await api.del(`/api/v1/scp-data-storages/${s.dataId}`);
                      await mine.reload();
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
      <Card title="Shared with me" flush>
        <Table
          rows={shared.data}
          rowKey={(s) => s.dataId}
          empty="Nothing shared with you."
          columns={[...base, { header: "Permission", cell: (s) => <span className="badge">{s.permission ?? "READ"}</span> }]}
        />
      </Card>
      {all.data && (
        <Card title="All storages (admin)" flush>
          <Table
            rows={all.data}
            rowKey={(s) => s.dataId}
            empty="No storages."
            columns={[...base, { header: "Owner", cell: (s) => <code>{s.ownerId}</code> }]}
          />
        </Card>
      )}
      {creating && (
        <CreateStorage
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false);
            void mine.reload();
            void all.reload();
          }}
        />
      )}
    </div>
  );
}

function CreateStorage({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const creds = useResource(() => api.get<SSHCredential[]>("/api/v1/ssh-credentials"));
  const [f, setF] = useState({ dataName: "", hostName: "", port: "22", loginUser: "", sshCredentialId: "" });
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  const save = useAction(async () => {
    await api.post("/api/v1/scp-data-storages", { ...f, port: numOrNull(f.port) });
    onCreated();
  });

  return (
    <Modal
      title="Add SCP data storage"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Add storage"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Name">
          <input value={f.dataName} onChange={(e) => set({ dataName: e.target.value })} />
        </Field>
        <div className="form-grid">
          <Field label="Host name">
            <input value={f.hostName} onChange={(e) => set({ hostName: e.target.value })} placeholder="data.lab.edu" />
          </Field>
          <Field label="Port">
            <input type="number" value={f.port} onChange={(e) => set({ port: e.target.value })} />
          </Field>
          <Field label="Login user">
            <input value={f.loginUser} onChange={(e) => set({ loginUser: e.target.value })} />
          </Field>
        </div>
        <Field label="SSH credential" hint={creds.data?.length === 0 ? "register one on the SSH credentials page first" : undefined}>
          <select value={f.sshCredentialId} onChange={(e) => set({ sshCredentialId: e.target.value })}>
            <option value="">Select a credential…</option>
            {creds.data?.map((c) => (
              <option key={c.sshCredentialId} value={c.sshCredentialId}>
                {c.sshCredentialName}
                {c.fullInteractive ? " (interactive)" : ""}
              </option>
            ))}
          </select>
        </Field>
        <Alert>{save.error || creds.error}</Alert>
      </div>
    </Modal>
  );
}
