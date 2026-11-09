"use client";

import type { Contents } from "@jupyterlab/services";
import { useCallback, useEffect, useRef, useState } from "react";
import { Alert, Field, Modal } from "./ui";

// The file side of the Notebooks page: a browser over jupyter-react's contents service.
// In lite mode that service keeps files in this browser's IndexedDB, so everything here
// is local to the browser, the same store the in-browser kernel's notebooks live in.

export const EMPTY_NOTEBOOK = {
  nbformat: 4,
  nbformat_minor: 5,
  metadata: {
    kernelspec: { name: "python", display_name: "Python (Pyodide)", language: "python" },
    language_info: { name: "python" },
  },
  cells: [],
};

export function joinPath(dir: string, name: string) {
  return dir ? `${dir}/${name}` : name;
}

export function isNotebook(path: string) {
  return path.toLowerCase().endsWith(".ipynb");
}

function parentOf(path: string) {
  const i = path.lastIndexOf("/");
  return i < 0 ? "" : path.slice(0, i);
}

async function exists(contents: Contents.IManager, path: string) {
  try {
    await contents.get(path, { content: false });
    return true;
  } catch {
    return false;
  }
}

// Folders first, then files, each alphabetically.
function sortItems(items: Contents.IModel[]) {
  return [...items].sort((a, b) => {
    const da = a.type === "directory" ? 0 : 1;
    const db = b.type === "directory" ? 0 : 1;
    return da - db || a.name.localeCompare(b.name, undefined, { numeric: true });
  });
}

async function download(contents: Contents.IManager, item: Contents.IModel) {
  const model = await contents.get(item.path, { content: true });
  let blob: Blob;
  if (model.type === "notebook") {
    blob = new Blob([JSON.stringify(model.content, null, 1)], { type: "application/x-ipynb+json" });
  } else if (model.format === "base64") {
    const bin = atob(model.content as string);
    const bytes = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
    blob = new Blob([bytes], { type: model.mimetype || "application/octet-stream" });
  } else {
    blob = new Blob([String(model.content ?? "")], { type: model.mimetype || "text/plain" });
  }
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = item.name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 0);
}

function readAsBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result).replace(/^data:[^,]*,/, ""));
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}

async function upload(contents: Contents.IManager, dir: string, file: File) {
  const path = joinPath(dir, file.name);
  if (isNotebook(file.name)) {
    const content = JSON.parse(await file.text());
    await contents.save(path, { type: "notebook", format: "json", content });
  } else {
    await contents.save(path, { type: "file", format: "base64", content: await readAsBase64(file) });
  }
}

type Dialog =
  | { kind: "notebook" }
  | { kind: "folder" }
  | { kind: "rename"; item: Contents.IModel };

const DIALOG_TITLE = { notebook: "New notebook", folder: "New folder", rename: "Rename" } as const;

export function NotebookFiles({
  contents,
  openPath,
  onOpen,
}: {
  contents: Contents.IManager;
  openPath: string | null;
  onOpen: (path: string) => void;
}) {
  const [dir, setDir] = useState(() => (openPath ? parentOf(openPath) : ""));
  const [items, setItems] = useState<Contents.IModel[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [dialog, setDialog] = useState<Dialog | null>(null);
  const [dragging, setDragging] = useState(false);
  const uploadInput = useRef<HTMLInputElement>(null);

  const refresh = useCallback(async () => {
    try {
      const model = await contents.get(dir, { content: true });
      setItems(sortItems(model.content as Contents.IModel[]));
    } catch (e) {
      // The folder went away (deleted or renamed elsewhere): fall back to the root.
      if (dir) setDir("");
      else setError(String(e));
    }
  }, [contents, dir]);

  useEffect(() => {
    void refresh();
    const onChange = () => void refresh();
    contents.fileChanged.connect(onChange);
    return () => {
      contents.fileChanged.disconnect(onChange);
    };
  }, [contents, refresh]);

  const run = async (action: () => Promise<unknown>) => {
    setError(null);
    try {
      await action();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  const uploadFiles = (files: FileList | null) =>
    files &&
    run(async () => {
      for (const file of Array.from(files)) await upload(contents, dir, file);
    });

  const crumbs = dir ? dir.split("/") : [];

  return (
    <section
      className={`nb-files card${dragging ? " dragging" : ""}`}
      onDragOver={(e) => {
        if (!e.dataTransfer.types.includes("Files")) return;
        e.preventDefault();
        setDragging(true);
      }}
      onDragLeave={(e) => e.currentTarget === e.target && setDragging(false)}
      onDrop={(e) => {
        if (!e.dataTransfer.files.length) return;
        e.preventDefault();
        setDragging(false);
        void uploadFiles(e.dataTransfer.files);
      }}
    >
      <div className="nb-files-toolbar">
        <button className="small primary" onClick={() => setDialog({ kind: "notebook" })}>
          New notebook
        </button>
        <button className="small" onClick={() => setDialog({ kind: "folder" })} title="New folder">
          New folder
        </button>
        <button className="small" onClick={() => uploadInput.current?.click()} title="Upload files">
          Upload
        </button>
        <input
          ref={uploadInput}
          type="file"
          multiple
          hidden
          onChange={(e) => {
            void uploadFiles(e.target.files);
            e.target.value = "";
          }}
        />
      </div>

      <nav className="nb-crumbs" aria-label="Folder">
        <button className="link" onClick={() => setDir("")} disabled={!dir}>
          Files
        </button>
        {crumbs.map((name, i) => (
          <span key={i}>
            <span className="muted">/</span>
            <button
              className="link"
              onClick={() => setDir(crumbs.slice(0, i + 1).join("/"))}
              disabled={i === crumbs.length - 1}
            >
              {name}
            </button>
          </span>
        ))}
      </nav>

      {error && (
        <div className="nb-files-alert">
          <Alert>{error}</Alert>
        </div>
      )}

      <ul className="nb-file-list">
        {items === null && <li className="empty">Loading…</li>}
        {items?.length === 0 && <li className="empty">Empty folder. Create a notebook or drop files here.</li>}
        {items?.map((item) => {
          const folder = item.type === "directory";
          const opens = folder || isNotebook(item.path);
          return (
            <li key={item.path} className={item.path === openPath ? "active" : ""}>
              <button
                className="nb-file-name"
                title={opens ? item.name : `${item.name} — only notebooks open here; use Download`}
                onClick={() => (folder ? setDir(item.path) : opens ? onOpen(item.path) : undefined)}
                disabled={!opens}
              >
                <FileIcon kind={folder ? "folder" : isNotebook(item.path) ? "notebook" : "file"} />
                <span className="truncate">{item.name}</span>
              </button>
              <RowMenu
                label={item.name}
                onRename={() => setDialog({ kind: "rename", item })}
                onDownload={folder ? undefined : () => void run(() => download(contents, item))}
                onDelete={() => run(() => contents.delete(item.path))}
              />
            </li>
          );
        })}
      </ul>

      {dialog && (
        <NameDialog
          title={DIALOG_TITLE[dialog.kind]}
          initial={dialog.kind === "rename" ? dialog.item.name : dialog.kind === "notebook" ? "Untitled.ipynb" : "New folder"}
          onClose={() => setDialog(null)}
          onSubmit={async (raw) => {
            let name = raw.trim();
            if (!name || name.includes("/")) throw new Error("Enter a name without slashes.");
            if (dialog.kind === "notebook" && !isNotebook(name)) name += ".ipynb";
            const base = dialog.kind === "rename" ? parentOf(dialog.item.path) : dir;
            const path = joinPath(base, name);
            if (dialog.kind === "rename" && path === dialog.item.path) return;
            if (await exists(contents, path)) throw new Error(`“${name}” already exists here.`);
            if (dialog.kind === "rename") {
              await contents.rename(dialog.item.path, path);
            } else if (dialog.kind === "notebook") {
              await contents.save(path, { type: "notebook", format: "json", content: EMPTY_NOTEBOOK });
              onOpen(path);
            } else {
              const created = await contents.newUntitled({ path: dir, type: "directory" });
              await contents.rename(created.path, path);
            }
          }}
        />
      )}
    </section>
  );
}

// RowMenu is a row's actions behind one "⋯" button, so long names keep the row's width.
// Delete asks again in place before it acts.
function RowMenu({
  label,
  onRename,
  onDownload,
  onDelete,
}: {
  label: string;
  onRename: () => void;
  onDownload?: () => void;
  onDelete: () => Promise<unknown>;
}) {
  const [open, setOpen] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    if (!open) return;
    const onDown = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setOpen(false);
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && setOpen(false);
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const close = () => {
    setOpen(false);
    setConfirming(false);
  };

  return (
    <span className={`nb-row-menu${open ? " open" : ""}`} ref={ref}>
      <button
        className="small nb-row-menu-button"
        aria-label={`Actions for ${label}`}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => (open ? close() : setOpen(true))}
      >
        ⋯
      </button>
      {open && (
        <span className="nb-row-menu-items" role="menu">
          <button role="menuitem" onClick={() => (close(), onRename())}>
            Rename
          </button>
          {onDownload && (
            <button role="menuitem" onClick={() => (close(), onDownload())}>
              Download
            </button>
          )}
          <button
            role="menuitem"
            className="danger"
            onClick={() => (confirming ? (close(), void onDelete()) : setConfirming(true))}
          >
            {confirming ? "Click again to delete" : "Delete"}
          </button>
        </span>
      )}
    </span>
  );
}

function NameDialog({
  title,
  initial,
  onClose,
  onSubmit,
}: {
  title: string;
  initial: string;
  onClose: () => void;
  onSubmit: (name: string) => Promise<void>;
}) {
  const [name, setName] = useState(initial);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const input = useRef<HTMLInputElement>(null);

  // Select the name without its extension, as file managers do.
  useEffect(() => {
    const el = input.current;
    if (!el) return;
    el.focus();
    const dot = initial.lastIndexOf(".");
    el.setSelectionRange(0, dot > 0 ? dot : initial.length);
  }, [initial]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await onSubmit(name);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className="primary" type="submit" form="nb-name-form" disabled={busy}>
            {busy ? "Working…" : "Save"}
          </button>
        </>
      }
    >
      <form id="nb-name-form" className="stack" onSubmit={submit}>
        <Alert>{error}</Alert>
        <Field label="Name">
          <input ref={input} value={name} onChange={(e) => setName(e.target.value)} />
        </Field>
      </form>
    </Modal>
  );
}

function FileIcon({ kind }: { kind: "folder" | "notebook" | "file" }) {
  const common = { width: 16, height: 16, viewBox: "0 0 16 16", "aria-hidden": true, className: `nb-icon ${kind}` };
  if (kind === "folder")
    return (
      <svg {...common}>
        <path d="M1.5 3.5A1 1 0 0 1 2.5 2.5h3.6l1.5 1.5h5.9a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1h-11a1 1 0 0 1-1-1z" />
      </svg>
    );
  if (kind === "notebook")
    return (
      <svg {...common}>
        <rect x="2.5" y="1.5" width="11" height="13" rx="1.5" />
        <path d="M5 1.5v13M7.5 5h4M7.5 7.5h4" />
      </svg>
    );
  return (
    <svg {...common}>
      <path d="M3.5 1.5h6l3 3v10h-9z M9.5 1.5v3h3" />
    </svg>
  );
}
