"use client";

import { JupyterReactTheme, Notebook, NotebookToolbar, useJupyter } from "@datalayer/jupyter-react";
import type { Kernel } from "@datalayer/jupyter-react";
import { PageConfig } from "@jupyterlab/coreutils";
import type { INotebookContent } from "@jupyterlab/nbformat";
import type { NotebookModel } from "@jupyterlab/notebook";
import type { Contents, ServiceManager } from "@jupyterlab/services";
import { useEffect, useMemo, useRef, useState } from "react";
import { API_BASE, getToken } from "@/lib/api";
import { isNotebook, NotebookFiles } from "./NotebookFiles";

// jupyter-react's lite server registers a service worker at the origin root, which would
// sit in front of every request to the API server. It only syncs files between the
// kernel's filesystem and the browser's, which this page does not use, and the kernel
// treats it as optional. Disable it before the lite server starts.
PageConfig.Extension.disabled.push("@jupyterlite/server-extension:service-worker");

// The last notebook opened, reopened on the next visit (a per-browser convenience).
const LAST_OPEN_KEY = "airavata.notebook.open";
const WELCOME_PATH = "Welcome.ipynb";
const SAVE_DELAY_MS = 1000;

// Run in the kernel when it starts, so cells can call the Airavata API as the signed-in
// user: `await airavata("/api/v1/...")`. The kernel is a Pyodide web worker on this
// origin, so pyfetch reaches the API without CORS.
function initCode(apiUrl: string, token: string | null) {
  return `
import json as _json
from pyodide.http import pyfetch as _pyfetch

AIRAVATA_API_URL = ${JSON.stringify(apiUrl)}
_AIRAVATA_TOKEN = ${JSON.stringify(token ?? "")}

async def airavata(path, method="GET", body=None):
    """Call the Airavata API as the dashboard's signed-in user and return the decoded JSON."""
    headers = {"Authorization": "Bearer " + _AIRAVATA_TOKEN}
    if body is not None:
        headers["Content-Type"] = "application/json"
        body = _json.dumps(body)
    resp = await _pyfetch(AIRAVATA_API_URL + path, method=method, headers=headers, body=body)
    text = await resp.string()
    if not resp.ok:
        raise RuntimeError(f"{method} {path}: HTTP {resp.status}: {text}")
    return _json.loads(text) if text else None
`;
}

const WELCOME: INotebookContent = {
  nbformat: 4,
  nbformat_minor: 5,
  metadata: {
    kernelspec: { name: "python", display_name: "Python (Pyodide)", language: "python" },
    language_info: { name: "python" },
  },
  cells: [
    {
      cell_type: "markdown",
      id: "intro",
      metadata: {},
      source:
        "# Airavata notebook\n\n" +
        "Python runs in your browser on [Pyodide](https://pyodide.org). " +
        "`await airavata(path)` calls the Airavata API as you; install pure-Python " +
        "packages with `%pip install <name>`.\n\n" +
        "Notebooks and files in the panel on the left are kept in this browser, not on the server.",
    },
    {
      cell_type: "code",
      id: "processes",
      metadata: {},
      execution_count: null,
      outputs: [],
      source: 'processes = await airavata("/api/v1/processes/me")\nlen(processes)',
    },
  ],
};

function readLastOpen(): string | null {
  try {
    return window.localStorage.getItem(LAST_OPEN_KEY);
  } catch {
    return null;
  }
}

function writeLastOpen(path: string | null) {
  try {
    if (path) window.localStorage.setItem(LAST_OPEN_KEY, path);
    else window.localStorage.removeItem(LAST_OPEN_KEY);
  } catch {}
}

function useColorMode(): "light" | "dark" {
  const [dark, setDark] = useState(() => window.matchMedia("(prefers-color-scheme: dark)").matches);
  useEffect(() => {
    const mq = window.matchMedia("(prefers-color-scheme: dark)");
    const on = () => setDark(mq.matches);
    mq.addEventListener("change", on);
    return () => mq.removeEventListener("change", on);
  }, []);
  return dark ? "dark" : "light";
}

// NotebookView is the Notebooks page: a file browser over the in-browser contents store
// beside the open notebook, all on jupyter-react's "lite" mode (a Pyodide kernel and
// IndexedDB-backed contents, no Jupyter server).
export default function NotebookView() {
  const code = useMemo(() => initCode(API_BASE || window.location.origin, getToken()), []);
  const { serviceManager, defaultKernel } = useJupyter({ lite: true, startDefaultKernel: true, initCode: code });
  const [openPath, setOpenPathState] = useState<string | null>(null);
  const [ready, setReady] = useState(false);
  // Changes only when a different file is opened, not when the open one is renamed, so a
  // rename keeps the editor (and any unsaved edits) in place.
  const [docId, setDocId] = useState(0);
  const colorMode = useColorMode();

  const setOpenPath = (path: string | null, newDoc: boolean) => {
    setOpenPathState(path);
    writeLastOpen(path);
    if (newDoc) setDocId((d) => d + 1);
  };

  // Pick the notebook to show first: the last one opened if it is still there, else the
  // welcome notebook, created on the very first visit.
  useEffect(() => {
    if (!serviceManager) return;
    const contents = serviceManager.contents;
    let cancelled = false;
    (async () => {
      const last = readLastOpen();
      let path: string | null = null;
      if (last) {
        try {
          await contents.get(last, { content: false });
          path = last;
        } catch {}
      }
      if (!path) {
        const root = await contents.get("", { content: true });
        if ((root.content as Contents.IModel[]).length === 0) {
          await contents.save(WELCOME_PATH, { type: "notebook", format: "json", content: WELCOME });
          path = WELCOME_PATH;
        }
      }
      if (cancelled) return;
      setOpenPath(path, true);
      setReady(true);
    })().catch(() => !cancelled && setReady(true));
    return () => {
      cancelled = true;
    };
  }, [serviceManager]);

  // Follow the open notebook when it, or a folder above it, is renamed or deleted.
  useEffect(() => {
    if (!serviceManager) return;
    const contents = serviceManager.contents;
    const onChange = (_: unknown, change: Contents.IChangedArgs) => {
      if (change.type !== "rename" && change.type !== "delete") return;
      const oldPath = change.oldValue?.path;
      if (!oldPath) return;
      setOpenPathState((current) => {
        if (!current || (current !== oldPath && !current.startsWith(oldPath + "/"))) return current;
        const newPath = change.type === "rename" ? change.newValue?.path : undefined;
        const next = newPath ? newPath + current.slice(oldPath.length) : null;
        writeLastOpen(next);
        return next;
      });
    };
    contents.fileChanged.connect(onChange);
    return () => {
      contents.fileChanged.disconnect(onChange);
    };
  }, [serviceManager]);

  if (!serviceManager || !defaultKernel || !ready) return <p className="muted">Starting the Python kernel…</p>;

  return (
    <div className="nb-workspace">
      <NotebookFiles
        contents={serviceManager.contents}
        openPath={openPath}
        onOpen={(path) => path !== openPath && setOpenPath(path, true)}
      />
      <div className="nb-editor">
        {openPath && isNotebook(openPath) ? (
          <NotebookEditor
            key={docId}
            docId={docId}
            path={openPath}
            serviceManager={serviceManager}
            kernel={defaultKernel}
            colorMode={colorMode}
          />
        ) : (
          <div className="empty card">Open a notebook from the panel on the left, or create one.</div>
        )}
      </div>
    </div>
  );
}

type SaveState = "saved" | "pending" | "saving" | "error";

const SAVE_LABEL: Record<SaveState, string> = {
  saved: "Saved",
  pending: "Unsaved changes",
  saving: "Saving…",
  error: "Save failed",
};

// NotebookEditor loads one notebook from the contents store and writes it back a moment
// after each change. jupyter-react's Notebook is given the content, not the path: with a
// path it would hand the file to JupyterLab's document context, which this page does not
// drive. All notebooks share the page's one kernel.
function NotebookEditor({
  docId,
  path,
  serviceManager,
  kernel,
  colorMode,
}: {
  docId: number;
  path: string;
  serviceManager: ServiceManager.IManager;
  kernel: Kernel;
  colorMode: "light" | "dark";
}) {
  const contents = serviceManager.contents;
  const [nbformat, setNbformat] = useState<INotebookContent | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saveState, setSaveState] = useState<SaveState>("saved");
  // The current path, so a save after a rename goes to the new name.
  const pathRef = useRef(path);
  pathRef.current = path;
  const modelRef = useRef<NotebookModel | null>(null);
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);
  const dirty = useRef(false);

  useEffect(() => {
    let cancelled = false;
    contents
      .get(path, { content: true })
      .then((m) => !cancelled && setNbformat(m.content as INotebookContent))
      .catch((e) => !cancelled && setLoadError(e instanceof Error ? e.message : String(e)));
    return () => {
      cancelled = true;
    };
    // Loaded once per document; a rename changes the path but not the content.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [docId]);

  const save = async () => {
    clearTimeout(timer.current);
    const model = modelRef.current;
    if (!model || !dirty.current) return;
    dirty.current = false;
    const target = pathRef.current;
    // Deleted while edits were pending: do not bring it back.
    try {
      await contents.get(target, { content: false });
    } catch {
      return;
    }
    setSaveState("saving");
    try {
      await contents.save(target, { type: "notebook", format: "json", content: model.toJSON() });
      setSaveState(dirty.current ? "pending" : "saved");
    } catch {
      dirty.current = true;
      setSaveState("error");
    }
  };
  const saveRef = useRef(save);
  saveRef.current = save;

  // Write any pending edits when switching away from this notebook or leaving the page.
  useEffect(
    () => () => {
      void saveRef.current();
    },
    [],
  );

  const disconnect = useRef<() => void>(undefined);
  useEffect(() => () => disconnect.current?.(), []);
  const onModel = useMemo(
    () => (model: NotebookModel | null) => {
      disconnect.current?.();
      disconnect.current = undefined;
      modelRef.current = model;
      if (!model) return;
      const changed = () => {
        dirty.current = true;
        setSaveState("pending");
        clearTimeout(timer.current);
        timer.current = setTimeout(() => void saveRef.current(), SAVE_DELAY_MS);
      };
      model.contentChanged.connect(changed);
      disconnect.current = () => model.contentChanged.disconnect(changed);
    },
    [],
  );

  if (loadError) return <div className="alert error">Could not open {path}: {loadError}</div>;
  if (!nbformat) return <p className="muted">Opening {path}…</p>;

  return (
    <>
      <div className="nb-editor-head">
        <strong className="truncate" title={path}>
          {path}
        </strong>
        <span className={`muted nb-save ${saveState}`}>{SAVE_LABEL[saveState]}</span>
      </div>
      <JupyterReactTheme colormode={colorMode} useBaseStyles={false}>
        <Notebook
          id={`airavata-notebook-${docId}`}
          kernel={kernel}
          serviceManager={serviceManager}
          nbformat={nbformat}
          height="calc(100vh - 230px)"
          Toolbar={NotebookToolbar}
          onNotebookModelChanged={onModel}
        />
      </JupyterReactTheme>
    </>
  );
}
