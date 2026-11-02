"use client";

import { useEffect, useState } from "react";
import { api, errorText } from "@/lib/api";
import type { Allocation, BatchJobConfig } from "@/lib/types";
import { Field, numOrNull, strOrNull } from "./ui";

// The form keeps every field as text so an empty box means "not set"; toBatchJobConfig
// turns that into the request, leaving unset fields out.
export type BatchJobConfigDraft = Record<keyof Omit<BatchJobConfig, "batchJobConfigId">, string>;

const NUMERIC = new Set(["wallTimeMinutes", "cpus", "ntasksPerNode", "cpusPerTask", "nodes", "ntasks", "gpus", "gpusPerNode"]);

const FIELDS: { key: keyof BatchJobConfigDraft; label: string; placeholder?: string }[] = [
  { key: "allocation", label: "Allocation (account)" },
  { key: "wallTimeMinutes", label: "Wall time (minutes)" },
  { key: "nodes", label: "Nodes" },
  { key: "ntasks", label: "Tasks" },
  { key: "ntasksPerNode", label: "Tasks per node" },
  { key: "cpus", label: "CPUs" },
  { key: "cpusPerTask", label: "CPUs per task" },
  { key: "mem", label: "Memory", placeholder: "16G" },
  { key: "memPerCpu", label: "Memory per CPU", placeholder: "2G" },
  { key: "gpus", label: "GPUs" },
  { key: "gpusPerNode", label: "GPUs per node" },
  { key: "memPerGpu", label: "Memory per GPU" },
  { key: "cpusPerGpu", label: "CPUs per GPU" },
  { key: "gres", label: "GRES", placeholder: "gpu:a100:1" },
  { key: "constraints", label: "Constraints" },
];

export function draftFrom(c: BatchJobConfig | null | undefined): BatchJobConfigDraft {
  const out = {} as BatchJobConfigDraft;
  for (const { key } of FIELDS) {
    const v = c?.[key];
    out[key] = v === null || v === undefined ? "" : String(v);
  }
  return out;
}

export function toBatchJobConfig(d: BatchJobConfigDraft): BatchJobConfig {
  const out: Record<string, unknown> = {};
  for (const { key } of FIELDS) {
    const v = NUMERIC.has(key) ? numOrNull(d[key]) : strOrNull(d[key]);
    if (v !== null) out[key] = v;
  }
  out.allocation = d.allocation;
  out.wallTimeMinutes = numOrNull(d.wallTimeMinutes);
  return out as unknown as BatchJobConfig;
}

// AllocationDiscovery says which login to discover allocations under: a cluster config
// id, or null while none is chosen (the button is then disabled, with hint saying why).
export interface AllocationDiscovery {
  configId: string | null;
  hint?: string;
  // The partition the job will run in, to flag accounts limited to other partitions.
  partition?: string | null;
}

export function BatchJobConfigFields({
  value,
  onChange,
  discover,
}: {
  value: BatchJobConfigDraft;
  onChange: (v: BatchJobConfigDraft) => void;
  discover?: AllocationDiscovery;
}) {
  return (
    <div className="form-grid">
      {discover && (
        <AllocationField
          value={value.allocation}
          onChange={(allocation) => onChange({ ...value, allocation })}
          discover={discover}
        />
      )}
      {FIELDS.filter((f) => !(discover && f.key === "allocation")).map((f) => (
        <Field key={f.key} label={f.label} hint={f.key === "allocation" || f.key === "wallTimeMinutes" ? "required" : undefined}>
          <input
            type={NUMERIC.has(f.key) ? "number" : "text"}
            value={value[f.key]}
            placeholder={f.placeholder}
            onChange={(e) => onChange({ ...value, [f.key]: e.target.value })}
          />
        </Field>
      ))}
    </div>
  );
}

// AllocationField is the allocation box with a "Discover" button that asks the cluster
// which accounts the chosen login may charge, offering each as a one-click choice.
function AllocationField({
  value,
  onChange,
  discover,
}: {
  value: string;
  onChange: (v: string) => void;
  discover: AllocationDiscovery;
}) {
  const [found, setFound] = useState<Allocation[] | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // What was found belongs to one login; another login may charge other accounts.
  useEffect(() => {
    setFound(null);
    setError(null);
  }, [discover.configId]);

  const run = async () => {
    if (!discover.configId) return;
    setBusy(true);
    setError(null);
    try {
      const res = await api.post<{ allocations: Allocation[] }>(
        `/api/v1/slurm-cluster-configs/${discover.configId}/discover-allocations`,
      );
      setFound(res.allocations);
      if (res.allocations.length === 1 && !value.trim()) onChange(res.allocations[0].account);
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  };

  const outside = (a: Allocation) =>
    !!discover.partition && a.partitions.length > 0 && !a.partitions.includes(discover.partition);
  const chosen = found?.find((a) => a.account === value);

  return (
    <div style={{ gridColumn: "1 / -1" }} className="form">
      <Field label="Allocation (account)" hint="required">
        <div className="row" style={{ flexWrap: "nowrap" }}>
          <input value={value} onChange={(e) => onChange(e.target.value)} placeholder="e.g. TG-ABC123456" />
          <button
            type="button"
            className="small"
            disabled={!discover.configId || busy}
            title={discover.configId ? "Ask the cluster which accounts this login may charge" : discover.hint}
            onClick={(e) => {
              e.preventDefault();
              void run();
            }}
          >
            {busy ? "Discovering…" : "Discover"}
          </button>
        </div>
      </Field>
      {!discover.configId && discover.hint && <span className="small muted">{discover.hint}</span>}
      {busy && <span className="small muted">Logging in to the cluster… answer the login prompt if one appears.</span>}
      {error && <span className="small" style={{ color: "var(--danger)" }}>{error}</span>}
      {found && found.length === 0 && <span className="small muted">The cluster reported no accounts for this login.</span>}
      {found && found.length > 0 && (
        <div className="row">
          {found.map((a) => (
            <button
              key={a.account}
              type="button"
              className={`small${a.account === value ? " primary" : ""}`}
              title={[
                a.description,
                a.partitions.length ? `Partitions: ${a.partitions.join(", ")}` : "Any partition",
                a.qos.length ? `QOS: ${a.qos.join(", ")}` : "",
              ]
                .filter(Boolean)
                .join("\n")}
              onClick={() => onChange(a.account)}
            >
              {a.account}
              {a.description && <span className="muted"> · {a.description}</span>}
              {outside(a) && <span className="badge warn">other partitions</span>}
            </button>
          ))}
        </div>
      )}
      {chosen && outside(chosen) && (
        <span className="small" style={{ color: "var(--warn)" }}>
          {chosen.account} is limited to {chosen.partitions.join(", ")}, not {discover.partition}.
        </span>
      )}
    </div>
  );
}
