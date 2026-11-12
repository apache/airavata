"use client";

import { useMemo } from "react";
import { Alert } from "@/components/ui";
import { api } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { PermissionInfo } from "@/lib/types";

// sameSet reports whether two permission lists hold the same permissions, ignoring order.
export function sameSet(a: string[], b: string[]): boolean {
  return a.length === b.length && a.every((p) => b.includes(p));
}

// Headings for the part of a permission before the colon. An unknown one falls back to
// its capitalised name, so a permission added on the server still appears.
const GROUP_LABELS: Record<string, string> = {
  audit: "Audit log",
  user: "Users",
  admin: "Administrators",
  group: "Groups",
  serviceaccount: "Service accounts",
  cluster: "Clusters",
  clusterconfig: "Cluster configs",
  template: "Application templates",
  deployment: "Deployments",
  storage: "Storages",
  dataproduct: "Data products",
  virtualdirectory: "Virtual data directories",
  resource: "Shareable records",
  process: "Processes",
};
const groupLabel = (g: string) => GROUP_LABELS[g] ?? g.charAt(0).toUpperCase() + g.slice(1);

// PermissionPicker lets an administrator choose what a service account may do.
//
// It offers only the permissions a service account may hold, grouped by what they act
// on. A permission the signed-in administrator doesn't hold themselves is shown but
// can't be added, because the server refuses to let anyone grant what they lack. One the
// account already has stays removable either way.
//
// `initial` is what the account holds now (empty when creating), which is what decides
// whether a permission the administrator lacks can still be unticked.
export function PermissionPicker({
  value,
  onChange,
  initial = [],
}: {
  value: string[];
  onChange: (next: string[]) => void;
  initial?: string[];
}) {
  const { can } = useSession();
  const catalogue = useResource(() => api.get<PermissionInfo[]>("/api/v1/permissions"), []);

  const groups = useMemo(() => {
    const out = new Map<string, PermissionInfo[]>();
    for (const p of catalogue.data ?? []) {
      if (!p.grantableToServiceAccount) continue;
      const group = p.permission.split(":")[0];
      out.set(group, [...(out.get(group) ?? []), p]);
    }
    return [...out.entries()];
  }, [catalogue.data]);

  const toggle = (permission: string, on: boolean) =>
    onChange(on ? [...value, permission].sort() : value.filter((p) => p !== permission));

  return (
    <div className="stack" style={{ gap: 6 }}>
      <div className="row" style={{ justifyContent: "space-between" }}>
        <strong>Permissions</strong>
        <span className="muted small">
          {value.length === 0 ? "none selected" : `${value.length} selected`}
        </span>
      </div>
      <p className="muted small" style={{ margin: 0 }}>
        The account can do only what is ticked here, on top of working with its own records. It gets nothing from any role.
      </p>
      <Alert>{catalogue.error}</Alert>
      <div className="perm-list" role="group" aria-label="Permissions">
        {catalogue.loading && !catalogue.data && <span className="muted small">Loading…</span>}
        {groups.map(([group, perms]) => (
          <fieldset key={group} className="perm-group">
            <legend>{groupLabel(group)}</legend>
            {perms.map((p) => {
              const checked = value.includes(p.permission);
              const lacks = !can(p.permission) && !initial.includes(p.permission);
              return (
                <label
                  key={p.permission}
                  className="check perm-row"
                  title={lacks ? "You don't hold this permission, so you can't grant it." : undefined}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    disabled={lacks}
                    onChange={(e) => toggle(p.permission, e.target.checked)}
                  />
                  <span>
                    <code className="small">{p.permission}</code>
                    <span className="muted small"> — {p.description}</span>
                    {lacks && <span className="badge warn"> you lack this</span>}
                  </span>
                </label>
              );
            })}
          </fieldset>
        ))}
      </div>
    </div>
  );
}
