"use client";

import { useState } from "react";
import { PermissionPicker, sameSet } from "@/components/PermissionPicker";
import { Alert, Card, ConfirmButton, Field, formatTime, Modal, numOrNull, PageHead, StatusBadge, strOrNull, Table } from "@/components/ui";
import { api, API_BASE, errorText } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { ServiceAccount, ServiceAccountCreate, ServiceAccountToken, ServiceAccountUpdate, UserStatus } from "@/lib/types";

// Bounds the server enforces (api/iam/dto/serviceaccount.go).
const DEFAULT_VALIDITY_DAYS = 90;
const MAX_VALIDITY_DAYS = 365;
const NAME_PATTERN = /^[a-z0-9][a-z0-9._-]{1,63}$/;

// Tokens this close to expiry are flagged, so an admin renews them before a pipeline breaks.
const EXPIRY_WARNING_DAYS = 14;
const DAY_MS = 24 * 60 * 60 * 1000;

const path = (sa: ServiceAccount) => `/api/v1/service-accounts/${encodeURIComponent(sa.serviceAccountId)}`;

type Dialog =
  | { kind: "create" }
  | { kind: "edit"; sa: ServiceAccount }
  | { kind: "renew"; sa: ServiceAccount }
  | { kind: "regenerate"; sa: ServiceAccount }
  | { kind: "token"; minted: ServiceAccountToken; regenerated: boolean };

export default function ServiceAccountsPage() {
  const { can } = useSession();
  const canRead = can("serviceaccount:read");
  const canManage = can("serviceaccount:manage");
  const accounts = useResource(
    () => (canRead ? api.get<ServiceAccount[]>("/api/v1/service-accounts") : Promise.resolve([])),
    [canRead],
  );
  const [dialog, setDialog] = useState<Dialog | null>(null);
  const [error, setError] = useState<string | null>(null);

  const close = () => setDialog(null);
  const done = () => {
    setDialog(null);
    void accounts.reload();
  };

  if (!canRead) {
    return (
      <div className="stack">
        <PageHead title="Service accounts" />
        <Alert kind="info">You don&apos;t have permission to view service accounts.</Alert>
      </div>
    );
  }

  return (
    <div className="stack">
      <PageHead
        title="Service accounts"
        sub="Identities for automation such as CI pipelines and gateways. They act as ordinary users, can never be administrators, and can do more only where you grant them a permission. Each has one token, which expires."
      >
        {canManage && (
          <button className="primary" onClick={() => setDialog({ kind: "create" })}>
            Create service account
          </button>
        )}
      </PageHead>
      <Alert>{accounts.error || error}</Alert>
      <Card flush>
        <Table
          rows={accounts.data}
          rowKey={(sa) => sa.serviceAccountId}
          empty="No service accounts yet."
          columns={[
            {
              header: "Name",
              cell: (sa) => (
                <div>
                  <strong>{sa.displayName || sa.name}</strong>
                  <div>
                    <code className="small">{sa.serviceAccountId}</code>
                  </div>
                </div>
              ),
            },
            { header: "Status", cell: (sa) => <StatusBadge status={sa.status} /> },
            { header: "Permissions", cell: (sa) => <PermissionSummary permissions={sa.permissions} /> },
            { header: "Token", cell: (sa) => <TokenExpiry expiresAt={sa.tokenExpiresAt} /> },
            {
              header: "Description",
              cell: (sa) =>
                sa.description ? (
                  <span className="truncate" title={sa.description}>
                    {sa.description}
                  </span>
                ) : (
                  <span className="muted">—</span>
                ),
            },
            {
              header: "Created",
              cell: (sa) => (
                <span className="small" title={formatTime(sa.createdAt)}>
                  by <code>{sa.createdBy}</code>
                </span>
              ),
            },
            {
              header: "",
              className: "actions",
              cell: (sa) =>
                !canManage ? null : (
                <div className="row" style={{ justifyContent: "flex-end" }}>
                  <button className="small" onClick={() => setDialog({ kind: "edit", sa })}>
                    Edit
                  </button>
                  <button className="small" onClick={() => setDialog({ kind: "renew", sa })}>
                    Renew
                  </button>
                  <button className="small" onClick={() => setDialog({ kind: "regenerate", sa })}>
                    Regenerate
                  </button>
                  <ConfirmButton
                    confirmText="Delete?"
                    onConfirm={async () => {
                      setError(null);
                      try {
                        await api.del(path(sa));
                        await accounts.reload();
                      } catch (e) {
                        setError(errorText(e));
                      }
                    }}
                  >
                    Delete
                  </ConfirmButton>
                </div>
                ),
            },
          ]}
        />
      </Card>

      {dialog?.kind === "create" && (
        <CreateDialog onClose={close} onCreated={(minted) => setDialog({ kind: "token", minted, regenerated: false })} />
      )}
      {dialog?.kind === "edit" && <EditDialog sa={dialog.sa} onClose={close} onSaved={done} />}
      {dialog?.kind === "renew" && <RenewDialog sa={dialog.sa} onClose={close} onRenewed={done} />}
      {dialog?.kind === "regenerate" && (
        <RegenerateDialog
          sa={dialog.sa}
          onClose={close}
          onRegenerated={(minted) => setDialog({ kind: "token", minted, regenerated: true })}
        />
      )}
      {dialog?.kind === "token" && <TokenDialog minted={dialog.minted} regenerated={dialog.regenerated} onClose={done} />}
    </div>
  );
}

// PermissionSummary shows how many permissions an account holds, and which on hover.
function PermissionSummary({ permissions }: { permissions: string[] }) {
  if (permissions.length === 0) return <span className="muted">None</span>;
  return (
    <span className="badge info" title={permissions.join("\n")}>
      {permissions.length} {permissions.length === 1 ? "permission" : "permissions"}
    </span>
  );
}

function TokenExpiry({ expiresAt }: { expiresAt: number }) {
  const left = expiresAt - Date.now();
  const when = formatTime(expiresAt);
  if (left <= 0)
    return (
      <span className="badge danger" title={`Expired ${when}`}>
        Expired
      </span>
    );
  const days = Math.ceil(left / DAY_MS);
  if (days <= EXPIRY_WARNING_DAYS)
    return (
      <span className="badge warn" title={`Expires ${when}`}>
        Expires in {days} {days === 1 ? "day" : "days"}
      </span>
    );
  return <span className="small">Expires {new Date(expiresAt).toLocaleDateString()}</span>;
}

function ValidityField({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const days = numOrNull(value);
  const preview = days && days >= 1 && days <= MAX_VALIDITY_DAYS ? new Date(Date.now() + days * DAY_MS).toLocaleDateString() : null;
  return (
    <Field label="Token validity (days)" hint={preview ? `expires ${preview}` : `1–${MAX_VALIDITY_DAYS}`}>
      <input type="number" min={1} max={MAX_VALIDITY_DAYS} value={value} onChange={(e) => onChange(e.target.value)} />
    </Field>
  );
}

function CreateDialog({ onClose, onCreated }: { onClose: () => void; onCreated: (t: ServiceAccountToken) => void }) {
  const [f, setF] = useState({ name: "", displayName: "", description: "", email: "", validityDays: String(DEFAULT_VALIDITY_DAYS) });
  const [permissions, setPermissions] = useState<string[]>([]);
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  const nameOk = f.name === "" || NAME_PATTERN.test(f.name);

  const save = useAction(async () => {
    const body: ServiceAccountCreate = {
      name: f.name,
      displayName: strOrNull(f.displayName),
      description: strOrNull(f.description),
      email: strOrNull(f.email),
      validityDays: numOrNull(f.validityDays),
      permissions,
    };
    onCreated(await api.post<ServiceAccountToken>("/api/v1/service-accounts", body));
  });

  return (
    <Modal
      title="Create service account"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy || !f.name || !nameOk} onClick={() => void save.run()}>
            {save.busy ? "Creating…" : "Create"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Name" hint={f.name ? `user id sa:${f.name}` : "lower-case letters, digits, . _ -"}>
          <input value={f.name} onChange={(e) => set({ name: e.target.value.trim() })} placeholder="nightly-ci" autoFocus />
        </Field>
        {!nameOk && (
          <Alert kind="warn">2–64 characters: lower-case letters, digits, “.”, “_” or “-”, starting with a letter or digit.</Alert>
        )}
        <Field label="Display name" hint="optional">
          <input value={f.displayName} onChange={(e) => set({ displayName: e.target.value })} placeholder="Nightly CI" />
        </Field>
        <Field label="Description" hint="optional">
          <input value={f.description} onChange={(e) => set({ description: e.target.value })} placeholder="What uses this account" />
        </Field>
        <Field label="Contact email" hint="optional">
          <input type="email" value={f.email} onChange={(e) => set({ email: e.target.value })} />
        </Field>
        <ValidityField value={f.validityDays} onChange={(validityDays) => set({ validityDays })} />
        <PermissionPicker value={permissions} onChange={setPermissions} />
        <p className="muted small" style={{ margin: 0 }}>
          The account always has <code>USER</code> authority. Share records with it, or add it to groups, by its user id.
        </p>
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

function EditDialog({ sa, onClose, onSaved }: { sa: ServiceAccount; onClose: () => void; onSaved: () => void }) {
  const [f, setF] = useState({
    displayName: sa.displayName ?? "",
    description: sa.description ?? "",
    email: sa.email ?? "",
    status: (sa.status ?? "ACTIVE") as UserStatus,
  });
  const [permissions, setPermissions] = useState<string[]>(sa.permissions);
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));

  const save = useAction(async () => {
    // Only changed fields are sent. The server leaves absent fields alone, so an
    // unchanged field is never overwritten.
    const body: ServiceAccountUpdate = {};
    if (f.displayName !== (sa.displayName ?? "")) body.displayName = f.displayName;
    if (f.description !== (sa.description ?? "")) body.description = f.description;
    if (f.email !== (sa.email ?? "")) body.email = f.email;
    if (f.status !== sa.status) body.status = f.status;
    // The list replaces the account's whole set on the server, so it is sent only when
    // it changed, and then in full.
    if (!sameSet(permissions, sa.permissions)) body.permissions = permissions;
    await api.put(path(sa), body);
    onSaved();
  });

  return (
    <Modal
      title={`Edit ${sa.serviceAccountId}`}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Save"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Display name">
          <input value={f.displayName} onChange={(e) => set({ displayName: e.target.value })} />
        </Field>
        <Field label="Description">
          <input value={f.description} onChange={(e) => set({ description: e.target.value })} />
        </Field>
        <Field label="Contact email">
          <input type="email" value={f.email} onChange={(e) => set({ email: e.target.value })} />
        </Field>
        <Field label="Status">
          <select value={f.status} onChange={(e) => set({ status: e.target.value as UserStatus })}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </Field>
        {f.status !== "ACTIVE" && (
          <Alert kind="warn">
            The token stops working as soon as you save. It is kept, so setting the account back to Active restores access.
          </Alert>
        )}
        <PermissionPicker value={permissions} onChange={setPermissions} initial={sa.permissions} />
        {!sameSet(permissions, sa.permissions) && (
          <Alert kind="info">Permission changes apply to the account&apos;s next request.</Alert>
        )}
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

function RenewDialog({ sa, onClose, onRenewed }: { sa: ServiceAccount; onClose: () => void; onRenewed: () => void }) {
  const [days, setDays] = useState(String(DEFAULT_VALIDITY_DAYS));
  const save = useAction(async () => {
    await api.post(`${path(sa)}/token/renew`, { validityDays: numOrNull(days) });
    onRenewed();
  });

  return (
    <Modal
      title={`Renew token for ${sa.serviceAccountId}`}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Renewing…" : "Renew"}
          </button>
        </>
      }
    >
      <div className="form">
        <p style={{ margin: 0 }}>
          Extends the current token. The token itself does not change, so whatever uses it keeps working.
        </p>
        <dl className="kv">
          <dt>Current expiry</dt>
          <dd>
            {formatTime(sa.tokenExpiresAt)} <TokenExpiry expiresAt={sa.tokenExpiresAt} />
          </dd>
        </dl>
        <ValidityField value={days} onChange={setDays} />
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

function RegenerateDialog({
  sa,
  onClose,
  onRegenerated,
}: {
  sa: ServiceAccount;
  onClose: () => void;
  onRegenerated: (t: ServiceAccountToken) => void;
}) {
  const [days, setDays] = useState(String(DEFAULT_VALIDITY_DAYS));
  const save = useAction(async () => {
    onRegenerated(await api.post<ServiceAccountToken>(`${path(sa)}/token/regenerate`, { validityDays: numOrNull(days) }));
  });

  return (
    <Modal
      title={`Regenerate token for ${sa.serviceAccountId}`}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="danger" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Regenerating…" : "Regenerate token"}
          </button>
        </>
      }
    >
      <div className="form">
        <Alert kind="warn">
          The current token stops working immediately. Anything still using it gets 401 until you give it the new token. Use
          this if the token may have leaked.
        </Alert>
        <ValidityField value={days} onChange={setDays} />
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

// TokenDialog shows a freshly minted token. The server keeps only its digest, so this
// is the one chance to copy it; closing without copying asks first.
function TokenDialog({ minted, regenerated, onClose }: { minted: ServiceAccountToken; regenerated: boolean; onClose: () => void }) {
  const [copied, setCopied] = useState(false);
  const [copyError, setCopyError] = useState<string | null>(null);
  const sa = minted.serviceAccount;

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(minted.token);
      setCopied(true);
      setCopyError(null);
    } catch {
      setCopyError("Copying failed. Select the token and copy it manually.");
    }
  };

  const tryClose = () => {
    if (copied || window.confirm("You haven't copied the token. It can't be shown again. Close anyway?")) onClose();
  };

  return (
    <Modal
      title={regenerated ? "New token" : "Service account created"}
      onClose={tryClose}
      wide
      footer={
        <>
          <button onClick={() => void copy()}>{copied ? "Copied" : "Copy token"}</button>
          <button className="primary" onClick={tryClose}>
            Done
          </button>
        </>
      }
    >
      <div className="form">
        <Alert kind="warn">
          Copy this token now and store it in a secret store. <strong>It won&apos;t be shown again.</strong> If it is lost,
          regenerate it.
        </Alert>
        <pre className="block" onClick={(e) => window.getSelection()?.selectAllChildren(e.currentTarget)}>
          {minted.token}
        </pre>
        <Alert>{copyError}</Alert>
        <dl className="kv">
          <dt>Account</dt>
          <dd>
            <code>{sa.serviceAccountId}</code>
          </dd>
          <dt>Expires</dt>
          <dd>{formatTime(sa.tokenExpiresAt)}</dd>
        </dl>
        <div className="stack" style={{ gap: 4 }}>
          <span className="muted small">Use it as a bearer token:</span>
          <pre className="block">{`curl -H "Authorization: Bearer $AIRAVATA_TOKEN" ${API_BASE || window.location.origin}/api/v1/processes/me`}</pre>
        </div>
      </div>
    </Modal>
  );
}
