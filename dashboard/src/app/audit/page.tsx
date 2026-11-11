"use client";

import { useEffect, useState } from "react";
import { Alert, Card, Field, formatTime, PageHead, StatusBadge, Table } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { AuditEvent, AuditEventPage } from "@/lib/types";

const PAGE_SIZE = 100;

interface Filters {
  userId: string;
  entityType: string;
  // Calendar days as the date inputs give them ("YYYY-MM-DD"), in the browser's time zone.
  from: string;
  to: string;
}

const NO_FILTERS: Filters = { userId: "", entityType: "", from: "", to: "" };

// startOfDay is local midnight of a "YYYY-MM-DD" day, in milliseconds since the epoch.
function startOfDay(day: string, plusDays = 0): number {
  const [y, m, d] = day.split("-").map(Number);
  return new Date(y, m - 1, d + plusDays).getTime();
}

// query builds the request string. The "to" day is inclusive for the person picking it,
// so the server's exclusive bound is the start of the day after.
function query(f: Filters, before?: number): string {
  const p = new URLSearchParams({ limit: String(PAGE_SIZE) });
  if (f.userId.trim()) p.set("userId", f.userId.trim());
  if (f.entityType) p.set("entityType", f.entityType);
  if (f.from) p.set("from", String(startOfDay(f.from)));
  if (f.to) p.set("to", String(startOfDay(f.to, 1)));
  if (before) p.set("before", String(before));
  return p.toString();
}

export default function AuditPage() {
  const { isAdmin } = useSession();
  // What the form holds, versus what the table was last loaded with.
  const [draft, setDraft] = useState<Filters>(NO_FILTERS);
  const [applied, setApplied] = useState<Filters>(NO_FILTERS);

  const types = useResource(
    () => (isAdmin ? api.get<string[]>("/api/v1/audit-events/entity-types") : Promise.resolve([])),
    [isAdmin],
  );
  const first = useResource(
    () =>
      isAdmin
        ? api.get<AuditEventPage>(`/api/v1/audit-events?${query(applied)}`)
        : Promise.resolve({ items: [], nextCursor: null }),
    [isAdmin, applied],
  );

  // Older pages loaded with "Load more", appended below the first. A new first page
  // (new filters, or a refresh) starts over.
  const [older, setOlder] = useState<AuditEvent[]>([]);
  const [cursor, setCursor] = useState<number | null>(null);
  const [loadingMore, setLoadingMore] = useState(false);
  const [moreError, setMoreError] = useState<string | null>(null);
  useEffect(() => {
    setOlder([]);
    setCursor(first.data?.nextCursor ?? null);
    setMoreError(null);
  }, [first.data]);

  const loadMore = async () => {
    if (cursor === null) return;
    setLoadingMore(true);
    setMoreError(null);
    try {
      const page = await api.get<AuditEventPage>(`/api/v1/audit-events?${query(applied, cursor)}`);
      setOlder((prev) => [...prev, ...page.items]);
      setCursor(page.nextCursor);
    } catch (e) {
      setMoreError(errorText(e));
    } finally {
      setLoadingMore(false);
    }
  };

  const set = <K extends keyof Filters>(k: K, v: Filters[K]) => setDraft((d) => ({ ...d, [k]: v }));
  const badRange = !!draft.from && !!draft.to && draft.from > draft.to;
  const filtered = JSON.stringify(applied) !== JSON.stringify(NO_FILTERS);

  if (!isAdmin) {
    return (
      <div className="stack">
        <PageHead title="Audit log" />
        <Alert kind="info">Only administrators can read the audit log.</Alert>
      </div>
    );
  }

  const rows = first.data ? [...first.data.items, ...older] : undefined;

  return (
    <div className="stack">
      <PageHead
        title="Audit log"
        sub="Every change to a record: who made it, what they did and to which record. Newest first. Values are never recorded, and system is the actor for changes Airavata makes itself."
      >
        <button onClick={() => void first.reload()} disabled={first.loading}>
          Refresh
        </button>
      </PageHead>

      <Card>
        <form
          className="row"
          style={{ alignItems: "flex-end" }}
          onSubmit={(e) => {
            e.preventDefault();
            if (!badRange) setApplied(draft);
          }}
        >
          <Field label="User id">
            <input
              style={{ minWidth: 240 }}
              placeholder="cilogon:12345, root, sa:ci, system"
              value={draft.userId}
              onChange={(e) => set("userId", e.target.value)}
            />
          </Field>
          <Field label="Record type">
            <select value={draft.entityType} onChange={(e) => set("entityType", e.target.value)}>
              <option value="">All</option>
              {types.data?.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </Field>
          <Field label="From">
            <input type="date" value={draft.from} max={draft.to || undefined} onChange={(e) => set("from", e.target.value)} />
          </Field>
          <Field label="To">
            <input type="date" value={draft.to} min={draft.from || undefined} onChange={(e) => set("to", e.target.value)} />
          </Field>
          <button className="primary" type="submit" disabled={badRange}>
            Apply
          </button>
          <button
            type="button"
            disabled={!filtered && JSON.stringify(draft) === JSON.stringify(NO_FILTERS)}
            onClick={() => {
              setDraft(NO_FILTERS);
              setApplied(NO_FILTERS);
            }}
          >
            Clear
          </button>
        </form>
        {badRange && <p className="small muted">&ldquo;From&rdquo; must not be after &ldquo;To&rdquo;.</p>}
      </Card>

      <Alert>{first.error || types.error || moreError}</Alert>
      <Card flush>
        <Table
          rows={rows}
          rowKey={(e) => String(e.id)}
          empty={filtered ? "No events match these filters." : "Nothing has been recorded yet."}
          columns={[
            {
              header: "When",
              cell: (e) => <span className="small">{formatTime(e.occurredAt)}</span>,
            },
            {
              header: "User",
              cell: (e) =>
                e.actor === "system" ? <span className="muted">system</span> : <code className="small">{e.actor}</code>,
            },
            { header: "Action", cell: (e) => <StatusBadge status={e.action} /> },
            { header: "Record type", cell: (e) => e.entityType },
            {
              header: "Record id",
              cell: (e) => (e.entityId ? <code className="small">{e.entityId}</code> : <span className="muted">—</span>),
            },
            {
              header: "Changed fields",
              cell: (e) => (e.changedFields ? <code className="small">{e.changedFields}</code> : <span className="muted">—</span>),
            },
          ]}
        />
        {cursor !== null && (
          <div className="row" style={{ justifyContent: "center", padding: 12 }}>
            <button onClick={() => void loadMore()} disabled={loadingMore}>
              {loadingMore ? "Loading…" : "Load more"}
            </button>
          </div>
        )}
      </Card>
    </div>
  );
}
