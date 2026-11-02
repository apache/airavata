"use client";

import { useState } from "react";
import { CreateCredential } from "@/components/CreateCredential";
import { Alert, Card, ConfirmButton, Id, PageHead, Table } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import type { SSHCredential } from "@/lib/types";

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
