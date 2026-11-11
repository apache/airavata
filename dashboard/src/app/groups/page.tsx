"use client";

import { useState } from "react";
import { Alert, Card, ConfirmButton, Field, formatTime, Id, Modal, PageHead, StatusBadge, Table, Tabs } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { Group, GroupMember, GroupMemberStatus, GroupRole, UserAccount } from "@/lib/types";

type Tab = "mine" | "all";

const ROLE_LABEL: Record<GroupRole, string> = { ADMIN: "Admin", MODERATOR: "Moderator", MEMBER: "Member" };
const ROLES: GroupRole[] = ["MEMBER", "MODERATOR", "ADMIN"];

const groupLabel = (g: Group) => g.groupName || g.groupId;
const groupPath = (g: Group) => `/api/v1/groups/${encodeURIComponent(g.groupId)}`;

export default function GroupsPage() {
  const { isAdmin } = useSession();
  const [tab, setTab] = useState<Tab>("mine");
  // The full list is admin-only; everyone else sees the groups they own or belong to.
  const groups = useResource(
    () => api.get<Group[]>(tab === "all" && isAdmin ? "/api/v1/groups" : "/api/v1/groups/me"),
    [tab, isAdmin],
  );
  const [creating, setCreating] = useState(false);
  const [managing, setManaging] = useState<Group | null>(null);

  return (
    <div className="stack">
      <PageHead
        title="Groups"
        sub="Share records with a group to reach every active member at once. Anyone can create a group."
      >
        <button className="primary" onClick={() => setCreating(true)}>
          New group
        </button>
      </PageHead>
      {isAdmin && (
        <Tabs
          value={tab}
          onChange={setTab}
          tabs={[
            { key: "mine", label: "My groups" },
            { key: "all", label: "All groups" },
          ]}
        />
      )}
      <Alert>{groups.error}</Alert>
      <Card flush>
        <Table
          rows={groups.data}
          rowKey={(g) => g.groupId}
          empty={tab === "all" ? "No groups exist yet." : "You are not in any groups yet."}
          columns={[
            {
              header: "Name",
              cell: (g) => (
                <button className="link" onClick={() => setManaging(g)}>
                  {groupLabel(g)}
                </button>
              ),
            },
            { header: "ID", cell: (g) => <Id value={g.groupId} /> },
            { header: "Owner", cell: (g) => (g.ownerId ? <code>{g.ownerId}</code> : "—") },
            { header: "Created", cell: (g) => formatTime(g.createdAt) },
            {
              header: "",
              className: "actions",
              cell: (g) => (
                <button className="small" onClick={() => setManaging(g)}>
                  Manage
                </button>
              ),
            },
          ]}
        />
      </Card>
      {creating && (
        <CreateGroup
          onClose={() => setCreating(false)}
          onCreated={(g) => {
            setCreating(false);
            void groups.reload();
            setManaging(g);
          }}
        />
      )}
      {managing && (
        <GroupDetail
          group={managing}
          onClose={() => setManaging(null)}
          onChanged={(g) => {
            void groups.reload();
            if (g) setManaging(g);
            else setManaging(null);
          }}
        />
      )}
    </div>
  );
}

function CreateGroup({ onClose, onCreated }: { onClose: () => void; onCreated: (g: Group) => void }) {
  const [name, setName] = useState("");
  const save = useAction(async () => {
    onCreated(await api.post<Group>("/api/v1/groups", { groupName: name.trim() }));
  });

  return (
    <Modal
      title="New group"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy || !name.trim()} onClick={() => void save.run()}>
            {save.busy ? "Creating…" : "Create group"}
          </button>
        </>
      }
    >
      <form
        className="form"
        onSubmit={(e) => {
          e.preventDefault();
          if (name.trim()) void save.run();
        }}
      >
        <Field label="Group name">
          <input autoFocus value={name} onChange={(e) => setName(e.target.value)} placeholder="lab-team" />
        </Field>
        <span className="small muted">You will own the group and be added to it as an admin.</span>
        <Alert>{save.error}</Alert>
      </form>
    </Modal>
  );
}

// GroupDetail shows a group's members and offers only what the server will allow the
// caller: the owner and platform admins rename and delete; they and members holding
// ADMIN or MODERATOR manage memberships; any member other than the owner may leave.
//
// onChanged reports a rename with the updated group, and a delete or leave with null.
function GroupDetail({
  group,
  onClose,
  onChanged,
}: {
  group: Group;
  onClose: () => void;
  onChanged: (g: Group | null) => void;
}) {
  const { me, isAdmin } = useSession();
  const base = groupPath(group);
  const members = useResource(() => api.get<GroupMember[]>(`${base}/members`), [base]);
  // Admins can pick from the user list; everyone else types a user id.
  const users = useResource(
    () => (isAdmin ? api.get<UserAccount[]>("/api/v1/users") : Promise.resolve([] as UserAccount[])),
    [isAdmin],
  );
  const [rowError, setRowError] = useState<string | null>(null);

  const myId = me?.userId;
  const isOwner = !!myId && group.ownerId === myId;
  const mine = members.data?.find((m) => m.userId === myId);
  const canControl = isAdmin || isOwner;
  const canManage =
    canControl || (!!mine && mine.groupMemberStatus === "ACTIVE" && (mine.groupRole === "ADMIN" || mine.groupRole === "MODERATOR"));

  // The owner's own membership is reserved to the owner and platform admins.
  const canEditRow = (m: GroupMember) => canManage && (m.userId !== group.ownerId || canControl);

  const update = async (m: GroupMember, patch: { groupRole?: GroupRole; groupMemberStatus?: GroupMemberStatus }) => {
    setRowError(null);
    try {
      await api.put(`${base}/members/${encodeURIComponent(m.userId)}`, patch);
      await members.reload();
    } catch (e) {
      setRowError(errorText(e));
    }
  };

  const remove = async (userId: string) => {
    setRowError(null);
    try {
      await api.del(`${base}/members/${encodeURIComponent(userId)}`);
      if (userId === myId) onChanged(null);
      else await members.reload();
    } catch (e) {
      setRowError(errorText(e));
    }
  };

  const deleteGroup = async () => {
    setRowError(null);
    try {
      await api.del(base);
      onChanged(null);
    } catch (e) {
      setRowError(errorText(e));
    }
  };

  return (
    <Modal title={groupLabel(group)} onClose={onClose} wide footer={<button onClick={onClose}>Done</button>}>
      <div className="stack">
        <dl className="kv">
          <dt>ID</dt>
          <dd>
            <code>{group.groupId}</code>
          </dd>
          <dt>Owner</dt>
          <dd>{group.ownerId ? <code>{group.ownerId}</code> : "—"}</dd>
          <dt>Created</dt>
          <dd>{formatTime(group.createdAt)}</dd>
          {mine && (
            <>
              <dt>Your role</dt>
              <dd>
                {ROLE_LABEL[mine.groupRole]} <StatusBadge status={mine.groupMemberStatus} />
              </dd>
            </>
          )}
        </dl>

        {canControl && <RenameGroup group={group} onRenamed={onChanged} />}
        {canManage && <AddMember base={base} users={users.data} onAdded={() => void members.reload()} />}

        <Card title="Members" flush>
          {members.error && !members.data ? (
            // A suspended member still sees the group listed, but may not read through it.
            <div className="empty">
              {!canControl
                ? "Your membership is inactive, so the member list is hidden. You can still leave the group."
                : members.error}
            </div>
          ) : (
            <Table
              rows={members.data}
              rowKey={(m) => m.userId}
              empty="No members."
              columns={[
                {
                  header: "User",
                  cell: (m) => (
                    <>
                      <code>{m.userId}</code>
                      {m.userId === group.ownerId && <span className="badge info"> owner</span>}
                      {m.userId === myId && <span className="small muted"> (you)</span>}
                    </>
                  ),
                },
                {
                  header: "Role",
                  cell: (m) =>
                    canEditRow(m) ? (
                      <select
                        value={m.groupRole}
                        aria-label={`Role of ${m.userId}`}
                        onChange={(e) => void update(m, { groupRole: e.target.value as GroupRole })}
                      >
                        {ROLES.map((r) => (
                          <option key={r} value={r}>
                            {ROLE_LABEL[r]}
                          </option>
                        ))}
                      </select>
                    ) : (
                      ROLE_LABEL[m.groupRole]
                    ),
                },
                {
                  header: "Status",
                  cell: (m) =>
                    canEditRow(m) ? (
                      <select
                        value={m.groupMemberStatus}
                        aria-label={`Status of ${m.userId}`}
                        onChange={(e) => void update(m, { groupMemberStatus: e.target.value as GroupMemberStatus })}
                      >
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                      </select>
                    ) : (
                      <StatusBadge status={m.groupMemberStatus} />
                    ),
                },
                {
                  header: "",
                  className: "actions",
                  cell: (m) =>
                    // The owner's membership cannot be removed while they own the group.
                    m.userId !== group.ownerId &&
                    (m.userId === myId ? (
                      <ConfirmButton onConfirm={() => remove(m.userId)}>Leave</ConfirmButton>
                    ) : (
                      canManage && <ConfirmButton onConfirm={() => remove(m.userId)}>Remove</ConfirmButton>
                    )),
                },
              ]}
            />
          )}
        </Card>

        <Alert>{rowError || users.error}</Alert>

        <div className="row">
          {/* A member whose membership is inactive cannot see the list, so leaving is offered here too. */}
          {members.error && !members.data && !isOwner && myId && (
            <ConfirmButton onConfirm={() => remove(myId)}>
              Leave group
            </ConfirmButton>
          )}
          {canControl && (
            <>
              <span className="spacer" />
              <span className="small muted">Deleting the group also revokes everything shared with it.</span>
              <ConfirmButton onConfirm={deleteGroup} confirmText="Delete group and its shares?">
                Delete group
              </ConfirmButton>
            </>
          )}
        </div>
      </div>
    </Modal>
  );
}

function RenameGroup({ group, onRenamed }: { group: Group; onRenamed: (g: Group) => void }) {
  const [name, setName] = useState(group.groupName ?? "");
  const save = useAction(async () => {
    onRenamed(await api.put<Group>(groupPath(group), { groupName: name.trim() }));
  });
  const unchanged = name.trim() === (group.groupName ?? "");

  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault();
        if (!unchanged && name.trim()) void save.run();
      }}
    >
      <div className="row" style={{ alignItems: "end", flexWrap: "nowrap" }}>
        <Field label="Group name">
          <input value={name} onChange={(e) => setName(e.target.value)} />
        </Field>
        <button type="submit" disabled={save.busy || unchanged || !name.trim()}>
          {save.busy ? "Saving…" : "Rename"}
        </button>
      </div>
      <Alert>{save.error}</Alert>
    </form>
  );
}

function AddMember({
  base,
  users,
  onAdded,
}: {
  base: string;
  users: UserAccount[] | undefined;
  onAdded: () => void;
}) {
  const [userId, setUserId] = useState("");
  const [role, setRole] = useState<GroupRole>("MEMBER");
  const add = useAction(async () => {
    await api.post(`${base}/members`, { userId: userId.trim(), groupRole: role });
    setUserId("");
    setRole("MEMBER");
    onAdded();
  });

  return (
    <form
      className="form"
      onSubmit={(e) => {
        e.preventDefault();
        if (userId.trim()) void add.run();
      }}
    >
      <div className="form-grid" style={{ alignItems: "end" }}>
        <Field label="Add member" hint="user id">
          <input
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="jdoe@example.edu"
            list={users?.length ? "group-member-user-ids" : undefined}
          />
        </Field>
        <Field label="Role">
          <select value={role} onChange={(e) => setRole(e.target.value as GroupRole)}>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {ROLE_LABEL[r]}
              </option>
            ))}
          </select>
        </Field>
        <button type="submit" className="primary" disabled={add.busy || !userId.trim()}>
          {add.busy ? "Adding…" : "Add member"}
        </button>
      </div>
      {users && users.length > 0 && (
        <datalist id="group-member-user-ids">
          {users.map((u) => (
            <option key={u.userId} value={u.userId}>
              {[u.firstName, u.lastName].filter(Boolean).join(" ") || u.email || ""}
            </option>
          ))}
        </datalist>
      )}
      <span className="small muted">
        Moderators and admins can add, change and remove members. Only the owner can rename or delete the group.
      </span>
      <Alert>{add.error}</Alert>
    </form>
  );
}
