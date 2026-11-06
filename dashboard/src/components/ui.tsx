"use client";

import { useEffect, useRef, useState } from "react";

export function PageHead({ title, sub, children }: { title: string; sub?: React.ReactNode; children?: React.ReactNode }) {
  return (
    <div className="page-head">
      <div>
        <h1>{title}</h1>
        {sub && <p>{sub}</p>}
      </div>
      {children && <div className="row">{children}</div>}
    </div>
  );
}

export function Card({
  title,
  actions,
  children,
  flush,
}: {
  title?: React.ReactNode;
  actions?: React.ReactNode;
  children: React.ReactNode;
  flush?: boolean;
}) {
  return (
    <section className="card">
      {(title || actions) && (
        <div className="card-head">
          {typeof title === "string" ? <h2>{title}</h2> : title}
          <span className="spacer" />
          {actions}
        </div>
      )}
      {flush ? children : <div className="card-body">{children}</div>}
    </section>
  );
}

export function Alert({ kind = "error", children }: { kind?: "error" | "info" | "warn"; children: React.ReactNode }) {
  if (!children) return null;
  return <div className={`alert ${kind}`}>{children}</div>;
}

// openModals is the stack of open dialogs, newest last.
const openModals: (() => void)[] = [];

export function Modal({
  title,
  onClose,
  children,
  footer,
  wide,
}: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
  footer?: React.ReactNode;
  wide?: boolean;
}) {
  const closeRef = useRef(onClose);
  closeRef.current = onClose;
  useEffect(() => {
    // Only the topmost dialog answers Escape, so dismissing a prompt raised over a
    // form does not throw the form away with it.
    const entry = () => closeRef.current();
    openModals.push(entry);
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape" && openModals[openModals.length - 1] === entry) entry();
    };
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("keydown", onKey);
      openModals.splice(openModals.indexOf(entry), 1);
    };
  }, []);

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal${wide ? " wide" : ""}`} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head">
          <h2>{title}</h2>
          <span className="spacer" />
          <button className="small" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-foot">{footer}</div>}
      </div>
    </div>
  );
}

export function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <label className="field">
      <span>
        {label} {hint && <span className="hint">— {hint}</span>}
      </span>
      {children}
    </label>
  );
}

export function Table<T>({
  rows,
  columns,
  empty,
  rowKey,
}: {
  rows: T[] | undefined;
  columns: { header: string; cell: (row: T) => React.ReactNode; className?: string }[];
  empty: string;
  rowKey: (row: T) => string;
}) {
  if (!rows) return <div className="empty">Loading…</div>;
  if (rows.length === 0) return <div className="empty">{empty}</div>;
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.header} className={c.className}>
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={rowKey(r)}>
              {columns.map((c) => (
                <td key={c.header} className={c.className}>
                  {c.cell(r)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ConfirmButton asks before a destructive action, inline rather than in a dialog.
export function ConfirmButton({
  onConfirm,
  children,
  confirmText = "Confirm?",
}: {
  onConfirm: () => Promise<unknown> | void;
  children: React.ReactNode;
  confirmText?: string;
}) {
  const [armed, setArmed] = useState(false);
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    if (!armed) return;
    const id = setTimeout(() => setArmed(false), 4000);
    return () => clearTimeout(id);
  }, [armed]);

  if (!armed)
    return (
      <button className="small danger" onClick={() => setArmed(true)}>
        {children}
      </button>
    );
  return (
    <button
      className="small danger"
      disabled={busy}
      onClick={async () => {
        setBusy(true);
        try {
          await onConfirm();
        } finally {
          setBusy(false);
          setArmed(false);
        }
      }}
    >
      {busy ? "Working…" : confirmText}
    </button>
  );
}

export function Tabs<K extends string>({
  tabs,
  value,
  onChange,
}: {
  tabs: { key: K; label: React.ReactNode }[];
  value: K;
  onChange: (k: K) => void;
}) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((t) => (
        <button key={t.key} role="tab" className={t.key === value ? "active" : ""} onClick={() => onChange(t.key)}>
          {t.label}
        </button>
      ))}
    </div>
  );
}

const STATUS_TONE: Record<string, string> = {
  COMPLETED: "ok",
  PROVISIONED: "ok",
  END: "ok",
  RUNNING: "info",
  EXECUTING: "info",
  SUBMITTED: "info",
  BEGIN: "info",
  PROVISIONING: "info",
  STAGE_OUT: "info",
  FAILED: "danger",
  FAIL: "danger",
  SUBMISSION_FAILED: "danger",
  CANCELLED: "warn",
  CANCELED: "warn",
  PAUSED: "warn",
  CANCELLING: "warn",
  TIME_LIMIT: "danger",
  READ: "",
  WRITE: "info",
};

export function StatusBadge({ status }: { status: string | null | undefined }) {
  if (!status) return <span className="badge">—</span>;
  return <span className={`badge ${STATUS_TONE[status] ?? ""}`}>{status}</span>;
}

export function Id({ value }: { value: string | null | undefined }) {
  if (!value) return <span className="muted">—</span>;
  return (
    <code className="truncate" title={value}>
      {value.length > 13 ? `${value.slice(0, 8)}…` : value}
    </code>
  );
}

export function formatTime(ms: number | null | undefined): string {
  if (!ms) return "—";
  return new Date(ms).toLocaleString();
}

// Number inputs keep empty as null so optional numeric fields are omitted rather than
// sent as zero.
export function numOrNull(v: string): number | null {
  if (v.trim() === "") return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

export function strOrNull(v: string): string | null {
  return v.trim() === "" ? null : v;
}
