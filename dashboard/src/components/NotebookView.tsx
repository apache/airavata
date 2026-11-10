"use client";

import { createLiteServiceManager, JupyterReactTheme, Kernel, Notebook, NotebookToolbar } from "@datalayer/jupyter-react";
import { PageConfig } from "@jupyterlab/coreutils";
import type { INotebookContent } from "@jupyterlab/nbformat";
import type { NotebookModel } from "@jupyterlab/notebook";
import { ServerConnection, ServiceManager } from "@jupyterlab/services";
import type { Contents } from "@jupyterlab/services";
import { useEffect, useMemo, useRef, useState } from "react";
import { API_BASE, api, ApiError, errorText, getToken } from "@/lib/api";
import { latestStatus, useDeploymentNames, withStatuses } from "@/lib/catalog";
import { useResource } from "@/lib/hooks";
import type { JupyterServer, Process } from "@/lib/types";
import { isNotebook, NotebookFiles } from "./NotebookFiles";
import { Alert } from "./ui";

// jupyter-react's lite server registers a service worker at the origin root, which would
// sit in front of every request to the API server. It only syncs files between the
// kernel's filesystem and the browser's, which this page does not use, and the kernel
// treats it as optional. Disable it before the lite server starts.
PageConfig.Extension.disabled.push("@jupyterlite/server-extension:service-worker");

// Where notebooks run: in this browser on Pyodide, or on the Jupyter Server a
// process's job started, reached through Airavata over the job's Linkspan link.
type Source = { kind: "browser" } | { kind: "process"; processId: string };

// The source chosen last, and the notebook open last in each (per-browser conveniences).
const SOURCE_KEY = "airavata.notebook.source";
const LAST_OPEN_KEY = "airavata.notebook.open";
const WELCOME_PATH = "Welcome.ipynb";
const SAVE_DELAY_MS = 1000;
const LINK_POLL_MS = 5000;

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

function readStored(key: string): string | null {
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeStored(key: string, value: string | null) {
  try {
    if (value) window.localStorage.setItem(key, value);
    else window.localStorage.removeItem(key);
  } catch {}
}

function readSource(): Source {
  const id = readStored(SOURCE_KEY);
  return id ? { kind: "process", processId: id } : { kind: "browser" };
}

function lastOpenKey(source: Source) {
  return source.kind === "browser" ? LAST_OPEN_KEY : `${LAST_OPEN_KEY}.${source.processId}`;
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

// The in-browser service manager is made once per page load: switching sources and
// back reuses it, and with it the files kept in this browser.
let liteManager: Promise<ServiceManager.IManager> | null = null;
function getLiteManager() {
  liteManager ??= createLiteServiceManager(true);
  return liteManager;
}

// A manager for the Jupyter Server behind Airavata's proxy. Jupyter clients send the
// token as "Authorization: token …" on requests and, with appendToken, as ?token= on
// WebSockets, which cannot carry headers; Airavata's proxy accepts either.
function remoteManager(access: JupyterServer): ServiceManager.IManager {
  const baseUrl = (API_BASE || window.location.origin) + access.baseUrl;
  const serverSettings = ServerConnection.makeSettings({
    baseUrl,
    wsUrl: baseUrl.replace(/^http/, "ws"),
    token: access.token,
    appendToken: true,
  });
  return new ServiceManager({ serverSettings });
}

// useKernel starts the default kernel of a service manager, runs initCode in it, and
// shuts it down when the manager changes or the page is left, so a remote job is not
// left holding kernels nobody can reach.
function useKernel(serviceManager: ServiceManager.IManager | null, initCode: string) {
  const [kernel, setKernel] = useState<Kernel | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    if (!serviceManager) return;
    let cancelled = false;
    let started: Kernel | null = null;
    (async () => {
      await serviceManager.kernelspecs.ready;
      const name = serviceManager.kernelspecs.specs?.default || "python3";
      await serviceManager.kernels.ready;
      if (cancelled) return;
      started = new Kernel({
        kernelName: name,
        kernelSpecName: name,
        kernelManager: serviceManager.kernels,
        kernelspecsManager: serviceManager.kernelspecs,
        sessionManager: serviceManager.sessions,
      });
      await started.ready;
      if (initCode) {
        try {
          await started.execute(initCode)?.done;
        } catch (e) {
          console.error("Failed to run the kernel's initial code", e);
        }
      }
      if (!cancelled) setKernel(started);
    })().catch((e) => !cancelled && setError(errorText(e)));
    return () => {
      cancelled = true;
      setKernel(null);
      setError(null);
      void started?.shutdown().catch(() => {});
    };
  }, [serviceManager, initCode]);
  return { kernel, error };
}

// NotebookView is the Notebooks page: a choice of where notebooks run, then a file
// browser over that place's files beside the open notebook.
export default function NotebookView() {
  const [source, setSourceState] = useState<Source>(readSource);
  const setSource = (next: Source) => {
    writeStored(SOURCE_KEY, next.kind === "process" ? next.processId : null);
    setSourceState(next);
  };
  return (
    <div className="stack">
      <SourcePicker source={source} onChange={setSource} />
      {source.kind === "browser" ? (
        <BrowserWorkspace key="browser" />
      ) : (
        <ProcessWorkspace key={source.processId} processId={source.processId} />
      )}
    </div>
  );
}

// Processes that may be running a job, which is when a Linkspan in it can link.
const LIVE = new Set(["SUBMITTED", "RUNNING"]);

function SourcePicker({ source, onChange }: { source: Source; onChange: (s: Source) => void }) {
  const names = useDeploymentNames();
  const rows = useResource(async () => withStatuses(await api.get<Process[]>("/api/v1/processes/me")), [], 30_000);
  const live = (rows.data ?? []).filter(
    (r) => r.process.processType === "BATCH_JOB" && LIVE.has(latestStatus(r)?.status ?? ""),
  );
  const selected = source.kind === "process" ? source.processId : "";
  const listed = live.some((r) => r.process.processId === selected);
  const label = (p: Process) =>
    `${names.appName(p.batchProcess?.deploymentId) ?? "Process"} · ${p.processId.slice(0, 8)}`;

  return (
    <div className="nb-source">
      <label htmlFor="nb-source">Run notebooks</label>
      <select
        id="nb-source"
        value={selected}
        onChange={(e) => onChange(e.target.value ? { kind: "process", processId: e.target.value } : { kind: "browser" })}
      >
        <option value="">In this browser (Pyodide)</option>
        {live.map((r) => (
          <option key={r.process.processId} value={r.process.processId}>
            On {label(r.process)} ({latestStatus(r)?.status?.toLowerCase()})
          </option>
        ))}
        {selected && !listed && <option value={selected}>On process {selected.slice(0, 8)}</option>}
      </select>
      <span className="muted">
        {source.kind === "browser"
          ? "Files are kept in this browser."
          : "Files and kernels are on the cluster, in the Jupyter Server the process's job started."}
      </span>
    </div>
  );
}

function BrowserWorkspace() {
  const [serviceManager, setServiceManager] = useState<ServiceManager.IManager | null>(null);
  useEffect(() => {
    let cancelled = false;
    void getLiteManager().then((m) => !cancelled && setServiceManager(m));
    return () => {
      cancelled = true;
    };
  }, []);
  const code = useMemo(() => initCode(API_BASE || window.location.origin, getToken()), []);
  const { kernel, error } = useKernel(serviceManager, code);
  if (error) return <Alert>The Python kernel did not start: {error}</Alert>;
  if (!serviceManager || !kernel) return <p className="muted">Starting the Python kernel…</p>;
  return <Workspace serviceManager={serviceManager} kernel={kernel} storeKey={LAST_OPEN_KEY} welcome />;
}

// ProcessWorkspace opens the Jupyter Server of a process's job once its Linkspan has
// linked to Airavata, polling until then; the job may still be queued or building its
// Python environment.
function ProcessWorkspace({ processId }: { processId: string }) {
  const [access, setAccess] = useState<JupyterServer | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let cancelled = false;
    let timer: ReturnType<typeof setTimeout>;
    const poll = async () => {
      try {
        const next = await api.get<JupyterServer>(`/api/v1/processes/${processId}/jupyter-server`);
        if (cancelled) return;
        setError(null);
        // Keep the object while nothing changed, so the service manager is not rebuilt.
        setAccess((prev) => (prev && prev.token === next.token && prev.linked === next.linked ? prev : next));
      } catch (e) {
        if (cancelled) return;
        setError(e instanceof ApiError && e.status === 404 ? "This process has not been submitted yet." : errorText(e));
      }
      timer = setTimeout(poll, LINK_POLL_MS);
    };
    void poll();
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [processId]);

  // One manager per issued token: a resubmitted job's server is a new server. It
  // outlives a dropped link, so the page recovers when Linkspan redials.
  const token = access?.linked ? access.token : null;
  const [serviceManager, setServiceManager] = useState<ServiceManager.IManager | null>(null);
  useEffect(() => {
    if (!token || !access) return;
    const manager = remoteManager(access);
    setServiceManager(manager);
    return () => {
      setServiceManager(null);
      manager.dispose();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);
  const { kernel, error: kernelError } = useKernel(serviceManager, "");

  if (error) return <Alert>{error}</Alert>;
  if (!access) return <p className="muted">Looking up the process…</p>;
  if (!serviceManager) {
    return (
      <Alert kind="info">
        Waiting for the job&apos;s Linkspan to link to Airavata. This happens once the job is running; the first time,
        it also builds the Jupyter environment, which takes a few minutes.
      </Alert>
    );
  }
  if (kernelError) return <Alert>The job&apos;s Jupyter Server did not start a kernel: {kernelError}</Alert>;
  if (!kernel) return <p className="muted">Starting a kernel on the cluster…</p>;
  return (
    <>
      {!access.linked && <Alert kind="warn">The job&apos;s link to Airavata dropped; waiting for Linkspan to redial.</Alert>}
      <Workspace serviceManager={serviceManager} kernel={kernel} storeKey={lastOpenKey({ kind: "process", processId })} />
    </>
  );
}

// Workspace is the file browser beside the open notebook, over one service manager's
// contents and one kernel all its notebooks share. welcome creates the welcome notebook
// in an empty store, which only the browser's own store gets.
function Workspace({
  serviceManager,
  kernel,
  storeKey,
  welcome = false,
}: {
  serviceManager: ServiceManager.IManager;
  kernel: Kernel;
  storeKey: string;
  welcome?: boolean;
}) {
  const [openPath, setOpenPathState] = useState<string | null>(null);
  const [ready, setReady] = useState(false);
  // Changes only when a different file is opened, not when the open one is renamed, so a
  // rename keeps the editor (and any unsaved edits) in place.
  const [docId, setDocId] = useState(0);
  const colorMode = useColorMode();

  const setOpenPath = (path: string | null, newDoc: boolean) => {
    setOpenPathState(path);
    writeStored(storeKey, path);
    if (newDoc) setDocId((d) => d + 1);
  };

  // Pick the notebook to show first: the last one opened if it is still there, else the
  // welcome notebook, created on the very first visit.
  useEffect(() => {
    const contents = serviceManager.contents;
    let cancelled = false;
    (async () => {
      const last = readStored(storeKey);
      let path: string | null = null;
      if (last) {
        try {
          await contents.get(last, { content: false });
          path = last;
        } catch {}
      }
      if (!path && welcome) {
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceManager]);

  // Follow the open notebook when it, or a folder above it, is renamed or deleted.
  useEffect(() => {
    const contents = serviceManager.contents;
    const onChange = (_: unknown, change: Contents.IChangedArgs) => {
      if (change.type !== "rename" && change.type !== "delete") return;
      const oldPath = change.oldValue?.path;
      if (!oldPath) return;
      setOpenPathState((current) => {
        if (!current || (current !== oldPath && !current.startsWith(oldPath + "/"))) return current;
        const newPath = change.type === "rename" ? change.newValue?.path : undefined;
        const next = newPath ? newPath + current.slice(oldPath.length) : null;
        writeStored(storeKey, next);
        return next;
      });
    };
    contents.fileChanged.connect(onChange);
    return () => {
      contents.fileChanged.disconnect(onChange);
    };
  }, [serviceManager, storeKey]);

  if (!ready) return <p className="muted">Opening files…</p>;

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
            kernel={kernel}
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
