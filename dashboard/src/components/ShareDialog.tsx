"use client";

import { useState } from "react";
import { Alert, Card, ConfirmButton, Field, Id, Modal, Table } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { Group, Permission, PrincipalType, ResourceShare, UserAccount } from "@/lib/types";

// ShareDialog manages who, besides the owner, may reach one record. Every shareable
// record exposes the same {resourcePath}/shares routes, so the dialog is the same for
// cluster configs, storages, data products and datasets; only the path and the
// wording of what each permission allows differ.
//
// Only the owner (or an admin) may read or change a share list, so the dialog belongs
// on records the caller owns.
export function ShareDialog({
  title,
  resourcePath,
  permissionHelp,
  onClose,
}: {
  title: string;
  // The record itself, e.g. /api/v1/slurm-cluster-configs/{id}.
  resourcePath: string;
  // What READ and WRITE let a grantee do with this kind of record.
  permissionHelp: Record<Permission, string>;
  onClose: () => void;
}) {
  const { can } = useSession();
  const canListUsers = can("user:list");
  const sharesPath = `${resourcePath}/shares`;
  const shares = useResource(() => api.get<ResourceShare[]>(sharesPath), [sharesPath]);
  // Groups the caller owns or belongs to are the ones they can name. Everyone may list
  // their own; the full user list needs user:list, so others type a user id.
  const groups = useResource(() => api.get<Group[]>("/api/v1/groups/me"));
  const users = useResource(
    () => (canListUsers ? api.get<UserAccount[]>("/api/v1/users") : Promise.resolve([] as UserAccount[])),
    [canListUsers],
  );

  const [principalType, setPrincipalType] = useState<PrincipalType>("USER");
  const [principalId, setPrincipalId] = useState("");
  const [permission, setPermission] = useState<Permission>("READ");
  const [rowError, setRowError] = useState<string | null>(null);

  const groupName = (id: string) => groups.data?.find((g) => g.groupId === id)?.groupName;

  const add = useAction(async () => {
    await api.post(sharesPath, { principalType, principalId: principalId.trim(), permission });
    setPrincipalId("");
    await shares.reload();
  });

  const change = async (s: ResourceShare, next: Permission) => {
    setRowError(null);
    try {
      await api.put(`${sharesPath}/${s.resourceSharingId}`, { permission: next });
      await shares.reload();
    } catch (e) {
      setRowError(errorText(e));
    }
  };

  const revoke = async (s: ResourceShare) => {
    setRowError(null);
    try {
      await api.del(`${sharesPath}/${s.resourceSharingId}`);
      await shares.reload();
    } catch (e) {
      setRowError(errorText(e));
    }
  };

  return (
    <Modal title={`Share ${title}`} onClose={onClose} wide footer={<button onClick={onClose}>Done</button>}>
      <div className="stack">
        <form
          className="form"
          onSubmit={(e) => {
            e.preventDefault();
            void add.run();
          }}
        >
          <div className="form-grid" style={{ alignItems: "end" }}>
            <Field label="Share with">
              <select
                value={principalType}
                onChange={(e) => {
                  setPrincipalType(e.target.value as PrincipalType);
                  setPrincipalId("");
                }}
              >
                <option value="USER">A user</option>
                <option value="GROUP">A group</option>
              </select>
            </Field>
            {principalType === "USER" ? (
              <Field label="User id">
                <input
                  value={principalId}
                  onChange={(e) => setPrincipalId(e.target.value)}
                  placeholder="jdoe@example.edu"
                  list={users.data?.length ? "share-user-ids" : undefined}
                />
              </Field>
            ) : (
              <Field
                label="Group"
                hint={groups.data?.length === 0 ? "you are not in any groups" : undefined}
              >
                <select value={principalId} onChange={(e) => setPrincipalId(e.target.value)}>
                  <option value="">Select a group…</option>
                  {groups.data?.map((g) => (
                    <option key={g.groupId} value={g.groupId}>
                      {g.groupName || g.groupId}
                    </option>
                  ))}
                </select>
              </Field>
            )}
            <Field label="Permission">
              <select value={permission} onChange={(e) => setPermission(e.target.value as Permission)}>
                <option value="READ">Read</option>
                <option value="WRITE">Write</option>
              </select>
            </Field>
            <button type="submit" className="primary" disabled={add.busy || principalId.trim() === ""}>
              {add.busy ? "Sharing…" : "Share"}
            </button>
          </div>
          {users.data && users.data.length > 0 && (
            <datalist id="share-user-ids">
              {users.data.map((u) => (
                <option key={u.userId} value={u.userId}>
                  {[u.firstName, u.lastName].filter(Boolean).join(" ") || u.email || ""}
                </option>
              ))}
            </datalist>
          )}
          <span className="small muted">
            <strong>Read</strong>: {permissionHelp.READ} <strong>Write</strong>: {permissionHelp.WRITE}
          </span>
          <Alert>{add.error}</Alert>
        </form>

        <Card title="Shared with" flush>
          <Table
            rows={shares.data}
            rowKey={(s) => s.resourceSharingId}
            empty="Not shared with anyone yet."
            columns={[
              {
                header: "Who",
                cell: (s) =>
                  s.principalType === "GROUP" ? (
                    <>
                      <span className="badge">Group</span> {groupName(s.principalId) ?? <Id value={s.principalId} />}
                    </>
                  ) : (
                    <>
                      <span className="badge">User</span> <code>{s.principalId}</code>
                    </>
                  ),
              },
              {
                header: "Permission",
                cell: (s) => (
                  <select
                    value={s.permission}
                    onChange={(e) => void change(s, e.target.value as Permission)}
                    aria-label={`Permission for ${s.principalId}`}
                  >
                    <option value="READ">Read</option>
                    <option value="WRITE">Write</option>
                  </select>
                ),
              },
              {
                header: "",
                className: "actions",
                cell: (s) => <ConfirmButton onConfirm={() => revoke(s)}>Revoke</ConfirmButton>,
              },
            ]}
          />
        </Card>
        <Alert>{shares.error || groups.error || users.error || rowError}</Alert>
      </div>
    </Modal>
  );
}
