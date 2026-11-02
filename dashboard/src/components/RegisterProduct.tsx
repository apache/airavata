"use client";

import { useRef, useState } from "react";
import { api } from "@/lib/api";
import { useAction } from "@/lib/hooks";
import type { DataProduct, SCPDataStorage } from "@/lib/types";
import { Alert, Field, Modal, strOrNull } from "./ui";

// RegisterProduct records a dataset's location on a storage, optionally uploading its
// content in the same step.
export function RegisterProduct({
  storages,
  onClose,
  onCreated,
  isFile,
}: {
  storages: SCPDataStorage[];
  onClose: () => void;
  onCreated: (p: DataProduct) => void;
  isFile?: boolean;
}) {
  const [f, setF] = useState({ dataName: "", dataDescription: "", path: "", dataStorageId: "", isFile: isFile ?? true });
  const [file, setFile] = useState<File | null>(null);
  const set = (patch: Partial<typeof f>) => setF((x) => ({ ...x, ...patch }));
  // Kept across attempts, so retrying after a failed upload does not register the
  // product a second time.
  const created = useRef<DataProduct | null>(null);
  const save = useAction(async () => {
    created.current ??= await api.post<DataProduct>("/api/v1/data-products", {
      ...f,
      dataDescription: strOrNull(f.dataDescription),
    });
    if (file && f.isFile) await api.upload(`/api/v1/data-products/${created.current.dataId}/content`, file);
    onCreated(created.current);
  });

  return (
    <Modal
      title="Register data product"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" disabled={save.busy} onClick={() => void save.run()}>
            {save.busy ? (file ? "Registering and uploading…" : "Registering…") : "Register"}
          </button>
        </>
      }
    >
      <div className="form">
        <Field label="Name">
          <input value={f.dataName} onChange={(e) => set({ dataName: e.target.value })} />
        </Field>
        <Field label="Storage" hint={storages.length === 0 ? "add a storage first" : undefined}>
          <select value={f.dataStorageId} onChange={(e) => set({ dataStorageId: e.target.value })}>
            <option value="">Select a storage…</option>
            {storages.map((s) => (
              <option key={s.dataId} value={s.dataId}>
                {s.dataName} ({s.loginUser}@{s.hostName})
              </option>
            ))}
          </select>
        </Field>
        <Field label="Path on the storage">
          <input value={f.path} onChange={(e) => set({ path: e.target.value })} placeholder="/data/input.fasta" />
        </Field>
        <div className="row">
          <label className="check">
            <input type="radio" checked={f.isFile} onChange={() => set({ isFile: true })} /> File
          </label>
          <label className="check">
            <input type="radio" checked={!f.isFile} onChange={() => set({ isFile: false })} /> Directory
          </label>
        </div>
        <Field label="Description" hint="optional">
          <input value={f.dataDescription} onChange={(e) => set({ dataDescription: e.target.value })} />
        </Field>
        {f.isFile && (
          <Field label="Upload content now" hint="optional; otherwise the path is only recorded">
            <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
          </Field>
        )}
        <Alert>{save.error}</Alert>
      </div>
    </Modal>
  );
}
