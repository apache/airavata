"use client";

import { useMemo, useState } from "react";
import {
  BatchJobConfigDraft,
  BatchJobConfigFields,
  draftFrom,
  toBatchJobConfig,
} from "@/components/BatchJobConfigFields";
import { Alert, Card, ConfirmButton, Field, Id, Modal, PageHead, strOrNull, Table, Tabs } from "@/components/ui";
import { api, errorText } from "@/lib/api";
import { useAction, useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import {
  INPUT_TYPES,
  OUTPUT_TYPES,
  type BatchDeployment,
  type InputType,
  type OutputType,
  type SlurmCluster,
  type Template,
  type TemplateInput,
  type TemplateOutput,
} from "@/lib/types";

type Tab = "templates" | "deployments";

export default function ApplicationsPage() {
  const [tab, setTab] = useState<Tab>("templates");
  return (
    <div className="stack">
      <PageHead
        title="Applications"
        sub="A template declares an application's inputs and outputs; a deployment says how to run it on a cluster."
      />
      <Tabs
        value={tab}
        onChange={setTab}
        tabs={[
          { key: "templates", label: "Templates" },
          { key: "deployments", label: "Deployments" },
        ]}
      />
      {tab === "templates" ? <Templates /> : <Deployments />}
    </div>
  );
}

function Templates() {
  const { isAdmin } = useSession();
  const templates = useResource(() => api.get<Template[]>("/api/v1/application-templates"));
  const [editing, setEditing] = useState<Template | "new" | null>(null);
  const [viewing, setViewing] = useState<Template | null>(null);
  const [error, setError] = useState<string | null>(null);

  return (
    <>
      <Alert>{templates.error || error}</Alert>
      <Card
        title="Application templates"
        flush
        actions={
          isAdmin ? (
            <button className="primary" onClick={() => setEditing("new")}>
              New template
            </button>
          ) : (
            <span className="small muted">Editing applications requires an admin</span>
          )
        }
      >
        <Table
          rows={templates.data}
          rowKey={(t) => t.templateId}
          empty="No application templates."
          columns={[
            {
              header: "Name",
              cell: (t) => (
                <button className="link" onClick={() => setViewing(t)}>
                  {t.templateName || "Unnamed"}
                </button>
              ),
            },
            { header: "ID", cell: (t) => <Id value={t.templateId} /> },
            { header: "Description", cell: (t) => <span className="muted">{t.templateDescription || "—"}</span> },
            { header: "Inputs", cell: (t) => t.inputs.map((i) => i.inputName).join(", ") || "—" },
            { header: "Outputs", cell: (t) => t.outputs.map((o) => o.outputName).join(", ") || "—" },
            {
              header: "",
              className: "actions",
              cell: (t) =>
                isAdmin && (
                  <div className="row" style={{ justifyContent: "flex-end" }}>
                    <button className="small" onClick={() => setEditing(t)}>
                      Edit
                    </button>
                    <ConfirmButton
                      onConfirm={async () => {
                        setError(null);
                        try {
                          await api.del(`/api/v1/application-templates/${t.templateId}`);
                          await templates.reload();
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
      {editing && (
        <TemplateEditor
          template={editing === "new" ? null : editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            void templates.reload();
          }}
        />
      )}
      {viewing && <TemplateDetail template={viewing} onClose={() => setViewing(null)} />}
    </>
  );
}

function TemplateDetail({ template, onClose }: { template: Template; onClose: () => void }) {
  return (
    <Modal title={template.templateName || "Template"} onClose={onClose} wide>
      <div className="stack">
        <p className="muted" style={{ margin: 0 }}>
          {template.templateDescription || "No description."}
        </p>
        <Card title="Inputs" flush>
          <Table
            rows={template.inputs}
            rowKey={(i) => i.inputId ?? i.inputName}
            empty="No inputs."
            columns={[
              { header: "Name", cell: (i) => <code>{i.inputName}</code> },
              { header: "Display", cell: (i) => i.displayName || "—" },
              { header: "Type", cell: (i) => <span className="badge">{i.inputType}</span> },
              { header: "Required", cell: (i) => (i.required ? "yes" : "no") },
              { header: "Default", cell: (i) => i.defaultValue ?? "—" },
            ]}
          />
        </Card>
        <Card title="Outputs" flush>
          <Table
            rows={template.outputs}
            rowKey={(o) => o.outputId ?? o.outputName}
            empty="No outputs."
            columns={[
              { header: "Name", cell: (o) => <code>{o.outputName}</code> },
              { header: "Display", cell: (o) => o.displayName || "—" },
              { header: "Type", cell: (o) => <span className="badge">{o.outputType}</span> },
            ]}
          />
        </Card>
      </div>
    </Modal>
  );
}

const blankInput = (): TemplateInput => ({
  inputName: "",
  displayName: null,
  inputDescription: null,
  inputType: "STRING",
  required: false,
  defaultValue: null,
});

const blankOutput = (): TemplateOutput => ({
  outputName: "",
  displayName: null,
  outputDescription: null,
  outputType: "FILE",
});

function TemplateEditor({
  template,
  onClose,
  onSaved,
}: {
  template: Template | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [name, setName] = useState(template?.templateName ?? "");
  const [description, setDescription] = useState(template?.templateDescription ?? "");
  const [inputs, setInputs] = useState<TemplateInput[]>(template?.inputs ?? []);
  const [outputs, setOutputs] = useState<TemplateOutput[]>(template?.outputs ?? []);
  const setIn = (i: number, patch: Partial<TemplateInput>) =>
    setInputs((xs) => xs.map((x, j) => (j === i ? { ...x, ...patch } : x)));
  const setOut = (i: number, patch: Partial<TemplateOutput>) =>
    setOutputs((xs) => xs.map((x, j) => (j === i ? { ...x, ...patch } : x)));

  const save = useAction(async () => {
    const body = {
      templateName: name,
      templateDescription: strOrNull(description),
      inputs: inputs.map(({ inputId: _, ...rest }) => rest),
      outputs: outputs.map(({ outputId: _, ...rest }) => rest),
    };
    if (template) await api.put(`/api/v1/application-templates/${template.templateId}`, body);
    else await api.post("/api/v1/application-templates", body);
    onSaved();
  });

  return (
    <Modal
      title={template ? "Edit template" : "New application template"}
      wide
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Save template"}
          </button>
        </>
      }
    >
      <div className="form">
        {template && (
          <Alert kind="warn">
            Saving replaces every input and output declaration, which gives them new ids. Existing processes keep
            pointing at the old ones.
          </Alert>
        )}
        <div className="form-grid">
          <Field label="Template name">
            <input value={name} onChange={(e) => setName(e.target.value)} />
          </Field>
          <Field label="Description" hint="optional">
            <input value={description} onChange={(e) => setDescription(e.target.value)} />
          </Field>
        </div>

        <fieldset>
          <legend>Inputs</legend>
          <div className="stack">
            {inputs.map((x, i) => (
              <div key={i} className="form-grid" style={{ alignItems: "end" }}>
                <Field label="Name">
                  <input value={x.inputName} onChange={(e) => setIn(i, { inputName: e.target.value })} />
                </Field>
                <Field label="Display name">
                  <input
                    value={x.displayName ?? ""}
                    onChange={(e) => setIn(i, { displayName: strOrNull(e.target.value) })}
                  />
                </Field>
                <Field label="Type">
                  <select value={x.inputType ?? ""} onChange={(e) => setIn(i, { inputType: e.target.value as InputType })}>
                    {INPUT_TYPES.map((t) => (
                      <option key={t}>{t}</option>
                    ))}
                  </select>
                </Field>
                <Field label="Default value">
                  <input
                    value={x.defaultValue ?? ""}
                    onChange={(e) => setIn(i, { defaultValue: strOrNull(e.target.value) })}
                  />
                </Field>
                <label className="check">
                  <input type="checkbox" checked={x.required} onChange={(e) => setIn(i, { required: e.target.checked })} />
                  Required
                </label>
                <button className="small danger" onClick={() => setInputs((xs) => xs.filter((_, j) => j !== i))}>
                  Remove
                </button>
              </div>
            ))}
            <div>
              <button className="small" onClick={() => setInputs((xs) => [...xs, blankInput()])}>
                + Add input
              </button>
            </div>
          </div>
        </fieldset>

        <fieldset>
          <legend>Outputs</legend>
          <div className="stack">
            {outputs.map((x, i) => (
              <div key={i} className="form-grid" style={{ alignItems: "end" }}>
                <Field label="Name">
                  <input value={x.outputName} onChange={(e) => setOut(i, { outputName: e.target.value })} />
                </Field>
                <Field label="Display name">
                  <input
                    value={x.displayName ?? ""}
                    onChange={(e) => setOut(i, { displayName: strOrNull(e.target.value) })}
                  />
                </Field>
                <Field label="Type">
                  <select
                    value={x.outputType ?? ""}
                    onChange={(e) => setOut(i, { outputType: e.target.value as OutputType })}
                  >
                    {OUTPUT_TYPES.map((t) => (
                      <option key={t}>{t}</option>
                    ))}
                  </select>
                </Field>
                <button className="small danger" onClick={() => setOutputs((xs) => xs.filter((_, j) => j !== i))}>
                  Remove
                </button>
              </div>
            ))}
            <div>
              <button className="small" onClick={() => setOutputs((xs) => [...xs, blankOutput()])}>
                + Add output
              </button>
            </div>
          </div>
        </fieldset>
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}

function Deployments() {
  const { isAdmin } = useSession();
  const deployments = useResource(() => api.get<BatchDeployment[]>("/api/v1/slurm-deployments"));
  const templates = useResource(() => api.get<Template[]>("/api/v1/application-templates"));
  const clusters = useResource(() => api.get<SlurmCluster[]>("/api/v1/slurm-clusters"));
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<BatchDeployment | null>(null);
  const [error, setError] = useState<string | null>(null);

  const templateName = useMemo(() => {
    const m = new Map(templates.data?.map((t) => [t.templateId, t.templateName ?? t.templateId]));
    return (id: string | null) => (id ? (m.get(id) ?? id) : "—");
  }, [templates.data]);
  const clusterName = useMemo(() => {
    const m = new Map(clusters.data?.map((c) => [c.slurmClusterId, c.clusterName]));
    return (id: string | null) => (id ? (m.get(id) ?? id) : "—");
  }, [clusters.data]);

  return (
    <>
      <Alert>{deployments.error || error}</Alert>
      <Card
        title="Batch deployments"
        flush
        actions={
          isAdmin && (
            <button className="primary" onClick={() => setCreating(true)}>
              New deployment
            </button>
          )
        }
      >
        <Table
          rows={deployments.data}
          rowKey={(d) => d.deploymentId}
          empty="No deployments."
          columns={[
            {
              header: "Application",
              cell: (d) => (
                <button className="link" onClick={() => setViewing(d)}>
                  {templateName(d.templateId)}
                </button>
              ),
            },
            { header: "ID", cell: (d) => <Id value={d.deploymentId} /> },
            { header: "Cluster", cell: (d) => clusterName(d.slurmClusterId) },
            { header: "Partition", cell: (d) => d.defaultPartition ?? "—" },
            {
              header: "Defaults",
              cell: (d) =>
                d.defaultBatchJobConfig
                  ? `${d.defaultBatchJobConfig.allocation} · ${d.defaultBatchJobConfig.wallTimeMinutes} min`
                  : "—",
            },
            {
              header: "",
              className: "actions",
              cell: (d) =>
                isAdmin && (
                  <ConfirmButton
                    onConfirm={async () => {
                      setError(null);
                      try {
                        await api.del(`/api/v1/slurm-deployments/${d.deploymentId}`);
                        await deployments.reload();
                      } catch (e) {
                        setError(errorText(e));
                      }
                    }}
                  >
                    Delete
                  </ConfirmButton>
                ),
            },
          ]}
        />
      </Card>
      {creating && (
        <CreateDeployment
          templates={templates.data ?? []}
          clusters={clusters.data ?? []}
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false);
            void deployments.reload();
          }}
        />
      )}
      {viewing && (
        <Modal title={`Deployment of ${templateName(viewing.templateId)}`} onClose={() => setViewing(null)} wide>
          <div className="stack">
            <dl className="kv">
              <dt>ID</dt>
              <dd>
                <code>{viewing.deploymentId}</code>
              </dd>
              <dt>Cluster</dt>
              <dd>{clusterName(viewing.slurmClusterId)}</dd>
              <dt>Default partition</dt>
              <dd>{viewing.defaultPartition ?? "—"}</dd>
            </dl>
            <h3>Slurm run section</h3>
            <pre className="block">{viewing.slurmRunSection}</pre>
            <h3>Default batch job config</h3>
            <pre className="block">{JSON.stringify(viewing.defaultBatchJobConfig, null, 2)}</pre>
          </div>
        </Modal>
      )}
    </>
  );
}

function CreateDeployment({
  templates,
  clusters,
  onClose,
  onCreated,
}: {
  templates: Template[];
  clusters: SlurmCluster[];
  onClose: () => void;
  onCreated: () => void;
}) {
  const [templateId, setTemplateId] = useState("");
  const [clusterId, setClusterId] = useState("");
  const [partition, setPartition] = useState("");
  const [runSection, setRunSection] = useState("");
  const [config, setConfig] = useState<BatchJobConfigDraft>(() => draftFrom({ wallTimeMinutes: 60, allocation: "" }));
  const partitions = clusters.find((c) => c.slurmClusterId === clusterId)?.partitions ?? [];
  const template = templates.find((t) => t.templateId === templateId);

  const save = useAction(async () => {
    await api.post("/api/v1/slurm-deployments", {
      templateId,
      slurmClusterId: strOrNull(clusterId),
      slurmRunSection: runSection,
      defaultPartition: strOrNull(partition),
      defaultBatchJobConfig: toBatchJobConfig(config),
    });
    onCreated();
  });

  return (
    <Modal
      title="New batch deployment"
      wide
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? "Saving…" : "Create deployment"}
          </button>
        </>
      }
    >
      <div className="form">
        <div className="form-grid">
          <Field label="Application template">
            <select value={templateId} onChange={(e) => setTemplateId(e.target.value)}>
              <option value="">Select…</option>
              {templates.map((t) => (
                <option key={t.templateId} value={t.templateId}>
                  {t.templateName}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Cluster">
            <select
              value={clusterId}
              onChange={(e) => {
                setClusterId(e.target.value);
                setPartition("");
              }}
            >
              <option value="">Select…</option>
              {clusters.map((c) => (
                <option key={c.slurmClusterId} value={c.slurmClusterId}>
                  {c.clusterName}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Default partition" hint="optional">
            <select value={partition} onChange={(e) => setPartition(e.target.value)} disabled={!partitions.length}>
              <option value="">None</option>
              {partitions.map((p) => (
                <option key={p.partitionId ?? p.name}>{p.name}</option>
              ))}
            </select>
          </Field>
        </div>
        <Field
          label="Slurm run section"
          hint={
            template?.inputs.length
              ? `the commands the job script runs; inputs: ${template.inputs.map((i) => i.inputName).join(", ")}`
              : "the commands the job script runs"
          }
        >
          <textarea
            style={{ minHeight: 140 }}
            value={runSection}
            onChange={(e) => setRunSection(e.target.value)}
            placeholder={"module load app\napp --input {{ inputs.myInput }}"}
          />
        </Field>
        <fieldset>
          <legend>Default resources</legend>
          <BatchJobConfigFields value={config} onChange={setConfig} />
        </fieldset>
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}
