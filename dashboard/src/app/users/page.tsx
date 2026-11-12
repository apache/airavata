"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { Alert, Card, ConfirmButton, formatTime, PageHead, StatusBadge, Table, Tabs } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { UserAccount, UserStatus } from "@/lib/types";

type Tab = UserStatus | "ALL";

const AUTH_LABEL: Record<string, string> = { CILOGON: "CILogon", SYSTEM: "System" };

const fullName = (u: UserAccount) => [u.firstName, u.lastName].filter(Boolean).join(" ");
const isAdminAccount = (u: UserAccount) => u.roles.includes("ADMIN") || u.roles.includes("SUPER_ADMIN");

export default function UsersPage() {
  const { me, can } = useSession();
  const canListUsers = can("user:list");
  const users = useResource(
    () => (canListUsers ? api.get<UserAccount[]>("/api/v1/users") : Promise.resolve([])),
    [canListUsers],
  );
  // Opens on the accounts waiting for a decision, which is why an admin usually comes here.
  const [tab, setTab] = useState<Tab>("SUSPENDED");
  const [filter, setFilter] = useState("");
  const [error, setError] = useState<string | null>(null);

  const isSuperAdmin = !!me?.authorities.includes("SUPER_ADMIN");

  // Service accounts are managed on their own page.
  const people = useMemo(() => users.data?.filter((u) => u.authMethod !== "SERVICE_ACCOUNT"), [users.data]);
  const count = (s: UserStatus) => people?.filter((u) => (u.status ?? "ACTIVE") === s).length ?? 0;

  const rows = useMemo(() => {
    const q = filter.trim().toLowerCase();
    return people
      ?.filter((u) => tab === "ALL" || (u.status ?? "ACTIVE") === tab)
      .filter((u) => !q || [u.userId, u.email, fullName(u)].some((v) => v?.toLowerCase().includes(q)));
  }, [people, tab, filter]);

  const setStatus = async (u: UserAccount, status: UserStatus) => {
    setError(null);
    try {
      await api.post(`/api/v1/users/${encodeURIComponent(u.userId)}/status`, { status });
      await users.reload();
    } catch (e) {
      setError(errorText(e));
    }
  };

  const setAdmin = async (u: UserAccount, admin: boolean) => {
    setError(null);
    try {
      await api.post(`/api/v1/users/${encodeURIComponent(u.userId)}/admin`, { admin });
      await users.reload();
    } catch (e) {
      setError(errorText(e));
    }
  };

  if (!canListUsers) {
    return (
      <div className="stack">
        <PageHead title="Users" />
        <Alert kind="info">Only administrators can manage users.</Alert>
      </div>
    );
  }

  const label = (text: string, n: number) => (
    <>
      {text}
      {n > 0 && <span className="badge">{n}</span>}
    </>
  );

  return (
    <div className="stack">
      <PageHead
        title="Users"
        sub={
          <>
            Everyone who has signed in. A new account starts out suspended and can&apos;t use Airavata until you activate it.
            Service accounts are on the <Link href="/service-accounts/">Service accounts</Link> page.
          </>
        }
      />
      <Tabs
        value={tab}
        onChange={setTab}
        tabs={[
          { key: "SUSPENDED", label: label("Suspended", count("SUSPENDED")) },
          { key: "ACTIVE", label: "Active" },
          { key: "INACTIVE", label: "Inactive" },
          { key: "ALL", label: "All" },
        ]}
      />
      <Alert>{users.error || error}</Alert>
      <Card
        flush
        title={
          <input
            style={{ maxWidth: 320 }}
            placeholder="Filter by name, email or id"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
        }
      >
        <Table
          rows={rows}
          rowKey={(u) => u.userId}
          empty={filter ? "No users match." : tab === "SUSPENDED" ? "Nobody is waiting to be activated." : "No users."}
          columns={[
            {
              header: "User",
              cell: (u) => (
                <div>
                  <strong>{fullName(u) || <span className="muted">No name</span>}</strong>
                  <div>
                    <code className="small">{u.userId}</code>
                  </div>
                </div>
              ),
            },
            { header: "Email", cell: (u) => u.email || <span className="muted">—</span> },
            { header: "Sign-in", cell: (u) => (u.authMethod ? (AUTH_LABEL[u.authMethod] ?? u.authMethod) : "—") },
            {
              header: "Role",
              cell: (u) =>
                u.roles.length === 0 ? (
                  <span className="small">User</span>
                ) : (
                  <div className="row" style={{ gap: 4 }}>
                    {u.roles.map((r) => (
                      <span key={r} className={`badge ${r === "USER" ? "" : "info"}`}>
                        {r === "SUPER_ADMIN" ? "Super admin" : r === "ADMIN" ? "Admin" : "User"}
                      </span>
                    ))}
                  </div>
                ),
            },
            { header: "Status", cell: (u) => <StatusBadge status={u.status ?? "ACTIVE"} /> },
            {
              header: "First seen",
              cell: (u) => (
                <span className="small" title={formatTime(u.createdAt)}>
                  {new Date(u.createdAt).toLocaleDateString()}
                </span>
              ),
            },
            {
              header: "",
              className: "actions",
              cell: (u) => (
                <StatusActions
                  user={u}
                  self={u.userId === me?.userId}
                  canTouchAdmins={isSuperAdmin}
                  onSet={setStatus}
                  onSetAdmin={setAdmin}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}

function StatusActions({
  user,
  self,
  canTouchAdmins,
  onSet,
  onSetAdmin,
}: {
  user: UserAccount;
  self: boolean;
  canTouchAdmins: boolean;
  onSet: (u: UserAccount, status: UserStatus) => Promise<void>;
  onSetAdmin: (u: UserAccount, admin: boolean) => Promise<void>;
}) {
  const [busy, setBusy] = useState(false);
  const status = user.status ?? "ACTIVE";

  if (self) return <span className="small muted">You</span>;
  if (user.authMethod === "SYSTEM") return null;
  if (isAdminAccount(user) && !canTouchAdmins)
    return (
      <span className="small muted" title="Only a Super Admin can change an administrator's status">
        Admin
      </span>
    );

  const activate = async () => {
    setBusy(true);
    try {
      await onSet(user, "ACTIVE");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="row" style={{ justifyContent: "flex-end" }}>
      {canTouchAdmins && !user.roles.includes("SUPER_ADMIN") && (
        <ConfirmButton
          confirmText={user.roles.includes("ADMIN") ? "Remove admin?" : "Make admin?"}
          onConfirm={() => onSetAdmin(user, !user.roles.includes("ADMIN"))}
        >
          {user.roles.includes("ADMIN") ? "Remove admin" : "Make admin"}
        </ConfirmButton>
      )}
      {status !== "ACTIVE" && (
        <button className="small primary" disabled={busy} onClick={activate}>
          {busy ? "Working…" : "Activate"}
        </button>
      )}
      {status !== "SUSPENDED" && (
        <ConfirmButton confirmText="Suspend?" onConfirm={() => onSet(user, "SUSPENDED")}>
          Suspend
        </ConfirmButton>
      )}
      {status !== "INACTIVE" && (
        <ConfirmButton confirmText="Deactivate?" onConfirm={() => onSet(user, "INACTIVE")}>
          Deactivate
        </ConfirmButton>
      )}
    </div>
  );
}
