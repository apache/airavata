"use client";

import { useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import {
  BatchJobConfigDraft,
  BatchJobConfigFields,
  draftFrom,
  toBatchJobConfig,
} from "@/components/BatchJobConfigFields";
import { RegisterProduct } from "@/components/RegisterProduct";
import { Alert, Card, Field, PageHead, strOrNull } from "@/components/ui";
import { api, ApiError, errorText } from "@/lib/api";
import { useDeploymentNames } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import {
  isFileType,
  type BatchDeployment,
  type DataProduct,
  type InputType,
  type Process,
  type SCPDataStorage,
  type SlurmClusterConfig,
  type TemplateInput,
  type TemplateOutput,
} from "@/lib/types";

// scalarValue encodes a non-file input the way the run section expects it: a JSON
// document with a single "value". An empty box sends null, so the declared default
// applies.
function scalarValue(type: InputType | null, raw: string): string | null {
  if (raw.trim() === "") return null;
  let value: unknown = raw;
  if (type === "INTEGER" || type === "FLOAT") value = Number(raw);
  if (type === "BOOLEAN") value = raw === "true";
  return JSON.stringify({ value });
}

export default function NewProcessPage() {
  const router = useRouter();
  const names = useDeploymentNames();
  const deployments = useResource(() => api.get<BatchDeployment[]>("/api/v1/slurm-deployments"));
  const configs = useResource(async () => {
    const [mine, shared] = await Promise.all([
      api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/me"),
      api.get<SlurmClusterConfig[]>("/api/v1/slurm-cluster-configs/shared-with-me"),
    ]);
    return [...mine, ...shared];
  });
  const products = useResource(async () => {
    const [mine, shared] = await Promise.all([
      api.get<DataProduct[]>("/api/v1/data-products/me"),
      api.get<DataProduct[]>("/api/v1/data-products/shared-with-me"),
    ]);
    return [...mine, ...shared];
  });
  const storages = useResource(async () => {
    const [mine, shared] = await Promise.all([
      api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/me"),
      api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/shared-with-me"),
    ]);
    return [...mine, ...shared];
  });

  const [deploymentId, setDeploymentId] = useState("");
  const [configId, setConfigId] = useState("");
  const [baseWorkDir, setBaseWorkDir] = useState("");
  const [jobConfig, setJobConfig] = useState<BatchJobConfigDraft>(() => draftFrom(null));
  const [inputs, setInputs] = useState<Record<string, string>>({});
  const [outputs, setOutputs] = useState<Record<string, string>>({});
  const [registerFor, setRegisterFor] = useState<{ kind: "input" | "output"; id: string; isFile: boolean } | null>(
    null,
  );
  const [busy, setBusy] = useState<"create" | "launch" | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [created, setCreated] = useState<Process | null>(null);

  const deployment = names.deployment(deploymentId);
  const template = names.template(deployment?.templateId);
  const config = configs.data?.find((c) => c.slurmClusterConfigId === configId);
  const eligibleConfigs = useMemo(
    () =>
      (configs.data ?? []).filter((c) => !deployment?.slurmClusterId || c.slurmClusterId === deployment.slurmClusterId),
    [configs.data, deployment?.slurmClusterId],
  );

  // A new deployment brings its own resource defaults and declarations.
  const chooseDeployment = (id: string) => {
    setDeploymentId(id);
    setJobConfig(draftFrom(deployments.data?.find((d) => d.deploymentId === id)?.defaultBatchJobConfig));
    setInputs({});
    setOutputs({});
    setConfigId("");
  };

  const submit = async (launch: boolean) => {
    if (!deployment || !template) return;
    setBusy(launch ? "launch" : "create");
    setError(null);
    try {
      // A process created on an earlier attempt whose launch failed is launched again
      // rather than created twice.
      const proc =
        created ??
        (await api.post<Process>("/api/v1/processes", {
          processType: "BATCH_JOB",
          batchProcess: {
            deploymentId,
            slurmClusterConfigId: configId,
            baseWorkDir: strOrNull(baseWorkDir),
            batchJobConfig: toBatchJobConfig(jobConfig),
            inputMappings: template.inputs.map((i) => ({
              templateInputId: i.inputId,
              value: isFileType(i.inputType) ? strOrNull(inputs[i.inputId!] ?? "") : scalarValue(i.inputType, inputs[i.inputId!] ?? ""),
            })),
            outputMappings: template.outputs
              .filter((o) => outputs[o.outputId!])
              .map((o) => ({ templateOutputId: o.outputId, value: outputs[o.outputId!] })),
          },
        }));
      setCreated(proc);
      if (launch) await api.post(`/api/v1/processes/${proc.processId}/launch`);
      router.push(`/processes/view/?id=${proc.processId}`);
    } catch (e) {
      setError(e instanceof ApiError ? e.describe() : errorText(e));
    } finally {
      setBusy(null);
    }
  };

  const productOptions = (type: string | null) =>
    (products.data ?? []).filter((p) => (type === "FILE" ? p.isFile : type ? !p.isFile || type === "FILE_LIST" : true));

  const productPicker = (kind: "input" | "output", id: string, type: string | null, value: string, set: (v: string) => void) => (
    <div className="row" style={{ flexWrap: "nowrap" }}>
      <select value={value} onChange={(e) => set(e.target.value)}>
        <option value="">{kind === "input" ? "Select a data product…" : "Not collected"}</option>
        {productOptions(type).map((p) => (
          <option key={p.dataId} value={p.dataId}>
            {p.dataName} — {p.path}
          </option>
        ))}
      </select>
      <button type="button" className="small" onClick={() => setRegisterFor({ kind, id, isFile: type === "FILE" })}>
        New…
      </button>
    </div>
  );

  const inputField = (i: TemplateInput) => {
    const id = i.inputId!;
    const value = inputs[id] ?? "";
    const set = (v: string) => setInputs((x) => ({ ...x, [id]: v }));
    const label = `${i.displayName || i.inputName}${i.required ? " *" : ""}`;
    const hint = [i.inputType, i.inputDescription].filter(Boolean).join(" · ");
    if (isFileType(i.inputType))
      return (
        <Field key={id} label={label} hint={hint}>
          {productPicker("input", id, i.inputType, value, set)}
        </Field>
      );
    if (i.inputType === "BOOLEAN")
      return (
        <Field key={id} label={label} hint={hint}>
          <select value={value} onChange={(e) => set(e.target.value)}>
            <option value="">{i.defaultValue ? `Default (${i.defaultValue})` : "Unset"}</option>
            <option value="true">true</option>
            <option value="false">false</option>
          </select>
        </Field>
      );
    return (
      <Field key={id} label={label} hint={hint}>
        <input
          type={i.inputType === "INTEGER" || i.inputType === "FLOAT" ? "number" : "text"}
          step={i.inputType === "FLOAT" ? "any" : undefined}
          value={value}
          placeholder={i.defaultValue ? `Default: ${i.defaultValue}` : undefined}
          onChange={(e) => set(e.target.value)}
        />
      </Field>
    );
  };

  const outputField = (o: TemplateOutput) => {
    const id = o.outputId!;
    return (
      <Field
        key={id}
        label={o.displayName || o.outputName}
        hint={[o.outputType, o.outputDescription].filter(Boolean).join(" · ")}
      >
        {productPicker("output", id, o.outputType, outputs[id] ?? "", (v) => setOutputs((x) => ({ ...x, [id]: v })))}
      </Field>
    );
  };

  const ready = deployment && template && configId && jobConfig.allocation.trim() && jobConfig.wallTimeMinutes;

  return (
    <div className="stack">
      <PageHead title="New process" sub="Pick a deployed application, fill in its inputs, and submit it to a cluster." />
      <Alert>{deployments.error || configs.error || products.error}</Alert>

      <Card title="1. Application">
        <div className="form-grid">
          <Field label="Deployment">
            <select value={deploymentId} onChange={(e) => chooseDeployment(e.target.value)} disabled={!!created}>
              <option value="">Select an application…</option>
              {deployments.data?.map((d) => (
                <option key={d.deploymentId} value={d.deploymentId}>
                  {names.appName(d.deploymentId) ?? d.deploymentId}
                  {d.defaultPartition ? ` (${d.defaultPartition})` : ""}
                </option>
              ))}
            </select>
          </Field>
          <Field
            label="Cluster config"
            hint={deployment && eligibleConfigs.length === 0 ? "you have no login on this deployment's cluster" : "the login this run submits as"}
          >
            <select value={configId} onChange={(e) => setConfigId(e.target.value)} disabled={!deployment || !!created}>
              <option value="">Select…</option>
              {eligibleConfigs.map((c) => (
                <option key={c.slurmClusterConfigId} value={c.slurmClusterConfigId}>
                  {c.name || c.slurmCluster?.clusterName} — {c.loginUser}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Base work directory" hint={config ? `defaults to ${config.workRoot}` : "optional"}>
            <input
              value={baseWorkDir}
              onChange={(e) => setBaseWorkDir(e.target.value)}
              placeholder={config?.workRoot}
              disabled={!!created}
            />
          </Field>
        </div>
        {template?.templateDescription && <p className="muted small">{template.templateDescription}</p>}
      </Card>

      {template && (
        <>
          <Card title="2. Inputs">
            {template.inputs.length === 0 ? (
              <span className="muted">This application takes no inputs.</span>
            ) : (
              <div className="form-grid">{template.inputs.map(inputField)}</div>
            )}
          </Card>
          <Card title="3. Outputs">
            {template.outputs.length === 0 ? (
              <span className="muted">This application declares no outputs.</span>
            ) : (
              <div className="form">
                <span className="small muted">
                  Choose the data product each output is copied to after the job finishes.
                </span>
                <div className="form-grid">{template.outputs.map(outputField)}</div>
              </div>
            )}
          </Card>
          <Card title="4. Resources">
            <BatchJobConfigFields
              value={jobConfig}
              onChange={setJobConfig}
              discover={{
                configId: configId || null,
                partition: deployment?.defaultPartition,
                hint: "Choose a cluster config above to discover the allocations it may charge.",
              }}
            />
          </Card>
        </>
      )}

      {created && (
        <Alert kind="warn">
          Process <code>{created.processId}</code> was created but not launched. Launch it again below, or open it from
          the processes list.
        </Alert>
      )}
      <Alert>{error}</Alert>
      <div className="row">
        <button className="primary" disabled={!ready || !!busy} onClick={() => void submit(true)}>
          {busy === "launch" ? "Launching…" : created ? "Launch" : "Create and launch"}
        </button>
        {!created && (
          <button disabled={!ready || !!busy} onClick={() => void submit(false)}>
            {busy === "create" ? "Creating…" : "Create only"}
          </button>
        )}
      </div>

      {registerFor && (
        <RegisterProduct
          storages={storages.data ?? []}
          isFile={registerFor.isFile}
          onClose={() => setRegisterFor(null)}
          onCreated={(p) => {
            const target = registerFor;
            setRegisterFor(null);
            void products.reload().then(() => {
              if (target.kind === "input") setInputs((x) => ({ ...x, [target.id]: p.dataId }));
              else setOutputs((x) => ({ ...x, [target.id]: p.dataId }));
            });
          }}
        />
      )}
    </div>
  );
}
