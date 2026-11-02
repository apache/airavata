"use client";

import { useMemo, useRef, useState } from "react";
import { RegisterProduct } from "@/components/RegisterProduct";
import {
  Alert,
  Card,
  ConfirmButton,
  formatTime,
  Id,
  PageHead,
  StatusBadge,
  Table,
  Tabs,
} from "@/components/ui";
import { api, errorText, tryGet } from "@/lib/api";
import { useResource } from "@/lib/hooks";
import { useSession } from "@/lib/session";
import type { DataProduct, SCPDataStorage } from "@/lib/types";

type Tab = "mine" | "shared" | "all";

export default function DataPage() {
  const { isAdmin } = useSession();
  const [tab, setTab] = useState<Tab>("mine");
  const [filter, setFilter] = useState("");
  const [registering, setRegistering] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const products = useResource(async () => {
    if (tab === "mine") return api.get<DataProduct[]>("/api/v1/data-products/me");
    if (tab === "shared") return api.get<DataProduct[]>("/api/v1/data-products/shared-with-me");
    return (await tryGet<DataProduct[]>("/api/v1/data-products")) ?? [];
  }, [tab]);
  const storages = useResource(async () => {
    const [mine, shared] = await Promise.all([
      api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/me"),
      api.get<SCPDataStorage[]>("/api/v1/scp-data-storages/shared-with-me"),
    ]);
    return [...mine, ...shared];
  });
  const storageName = useMemo(() => {
    const m = new Map(storages.data?.map((s) => [s.dataId, `${s.dataName ?? "storage"} (${s.hostName})`]));
    return (id: string | null) => (id ? (m.get(id) ?? null) : null);
  }, [storages.data]);

  const rows = useMemo(() => {
    const q = filter.trim().toLowerCase();
    const sorted = [...(products.data ?? [])].sort((a, b) => b.createdAt - a.createdAt);
    if (!q) return products.data ? sorted : undefined;
    return sorted.filter((p) =>
      [p.dataName, p.path, p.dataDescription, p.dataId].some((v) => v?.toLowerCase().includes(q)),
    );
  }, [products.data, filter]);

  const download = async (p: DataProduct) => {
    setError(null);
    setNotice(`Downloading ${p.dataName ?? p.path}…`);
    try {
      await api.download(`/api/v1/data-products/${p.dataId}/content`, p.path?.split("/").pop() || p.dataId);
      setNotice(null);
    } catch (e) {
      setNotice(null);
      setError(errorText(e));
    }
  };

  // tab "mine" lists only what the caller owns; elsewhere ownership is unknown, so the
  // owner-only delete is offered on "mine" and left to the server to refuse otherwise.
  const canWrite = (p: DataProduct) => tab === "mine" || tab === "all" || p.permission === "WRITE";

  return (
    <div className="stack">
      <PageHead title="Data products" sub="Registered datasets on your storages. File products can be downloaded and uploaded.">
        <button className="primary" onClick={() => setRegistering(true)}>
          Register data product
        </button>
      </PageHead>
      <Tabs
        value={tab}
        onChange={setTab}
        tabs={[
          { key: "mine", label: "Mine" },
          { key: "shared", label: "Shared with me" },
          ...(isAdmin ? [{ key: "all" as const, label: "All (admin)" }] : []),
        ]}
      />
      <Alert kind="info">{notice}</Alert>
      <Alert>{products.error || error}</Alert>
      <Card
        flush
        title={
          <input
            style={{ maxWidth: 320 }}
            placeholder="Filter by name, path or id"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
        }
        actions={
          <button className="small" onClick={() => void products.reload()} disabled={products.loading}>
            Refresh
          </button>
        }
      >
        <Table
          rows={rows}
          rowKey={(p) => p.dataId}
          empty="No data products here."
          columns={[
            {
              header: "Name",
              cell: (p) => (
                <div>
                  <strong>{p.dataName ?? "Unnamed"}</strong>
                  {p.dataDescription && <div className="small muted">{p.dataDescription}</div>}
                </div>
              ),
            },
            { header: "ID", cell: (p) => <Id value={p.dataId} /> },
            {
              header: "Path",
              cell: (p) => (
                <div>
                  <code className="truncate" style={{ maxWidth: 320 }} title={p.path ?? ""}>
                    {p.path}
                  </code>
                  <div className="small muted">{storageName(p.dataStorageId) ?? <Id value={p.dataStorageId} />}</div>
                </div>
              ),
            },
            { header: "Kind", cell: (p) => <span className="badge">{p.isFile ? "file" : "directory"}</span> },
            { header: "Status", cell: (p) => <StatusBadge status={p.provisionStatus} /> },
            { header: "Created", cell: (p) => <span className="small">{formatTime(p.createdAt)}</span> },
            {
              header: "",
              className: "actions",
              cell: (p) => (
                <div className="row" style={{ justifyContent: "flex-end" }}>
                  {p.isFile && (
                    <button className="small" onClick={() => void download(p)}>
                      Download
                    </button>
                  )}
                  {p.isFile && canWrite(p) && <UploadButton product={p} onDone={() => void products.reload()} onError={setError} />}
                  {tab !== "shared" && (
                    <ConfirmButton
                      onConfirm={async () => {
                        setError(null);
                        try {
                          await api.del(`/api/v1/data-products/${p.dataId}`);
                          await products.reload();
                        } catch (e) {
                          setError(errorText(e));
                        }
                      }}
                    >
                      Delete
                    </ConfirmButton>
                  )}
                </div>
              ),
            },
          ]}
        />
      </Card>
      {registering && (
        <RegisterProduct
          storages={storages.data ?? []}
          onClose={() => setRegistering(false)}
          onCreated={() => {
            setRegistering(false);
            setTab("mine");
            void products.reload();
          }}
        />
      )}
    </div>
  );
}

function UploadButton({
  product,
  onDone,
  onError,
}: {
  product: DataProduct;
  onDone: () => void;
  onError: (e: string | null) => void;
}) {
  const input = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  return (
    <>
      <input
        ref={input}
        type="file"
        hidden
        onChange={async (e) => {
          const file = e.target.files?.[0];
          e.target.value = "";
          if (!file) return;
          setBusy(true);
          onError(null);
          try {
            await api.upload(`/api/v1/data-products/${product.dataId}/content`, file);
            onDone();
          } catch (err) {
            onError(errorText(err));
          } finally {
            setBusy(false);
          }
        }}
      />
      <button
        className="small"
        disabled={busy}
        title={`Upload to ${product.path}`}
        onClick={() => input.current?.click()}
      >
        {busy ? "Uploading…" : "Upload"}
      </button>
    </>
  );
}
