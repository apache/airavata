"use client";

import { Field, numOrNull, strOrNull } from "./ui";
import type { BatchJobConfig } from "@/lib/types";

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

export function BatchJobConfigFields({
  value,
  onChange,
}: {
  value: BatchJobConfigDraft;
  onChange: (v: BatchJobConfigDraft) => void;
}) {
  return (
    <div className="form-grid">
      {FIELDS.map((f) => (
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
