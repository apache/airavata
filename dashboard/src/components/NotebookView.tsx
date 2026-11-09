"use client";

import { JupyterReactTheme, Notebook, NotebookToolbar, useJupyter } from "@datalayer/jupyter-react";
import { PageConfig } from "@jupyterlab/coreutils";
import type { INotebookContent } from "@jupyterlab/nbformat";
import type { NotebookModel } from "@jupyterlab/notebook";
import { useEffect, useMemo, useRef, useState } from "react";
import { API_BASE, getToken } from "@/lib/api";

// jupyter-react's lite server registers a service worker at the origin root, which would
// sit in front of every request to the API server. It only syncs files between the
// kernel's filesystem and the browser's, which this page does not use, and the kernel
// treats it as optional. Disable it before the lite server starts.
PageConfig.Extension.disabled.push("@jupyterlite/server-extension:service-worker");

const STORAGE_KEY = "airavata.notebook";
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

const STARTER: INotebookContent = {
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
        "packages with `%pip install <name>`.",
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

function loadSaved(): INotebookContent {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    if (saved) return JSON.parse(saved) as INotebookContent;
  } catch {
    // Unreadable or unavailable storage: start from the starter notebook.
  }
  return STARTER;
}

// NotebookView is one notebook backed by an in-browser Pyodide kernel (jupyter-react's
// "lite" mode), kept in local storage so it survives reloads.
export default function NotebookView() {
  const code = useMemo(() => initCode(API_BASE || window.location.origin, getToken()), []);
  const { serviceManager, defaultKernel } = useJupyter({ lite: true, startDefaultKernel: true, initCode: code });
  const [nbformat, setNbformat] = useState(loadSaved);
  // Bumped by "Reset" to remount the notebook on the starter content.
  const [generation, setGeneration] = useState(0);
  const saveTimer = useRef<ReturnType<typeof setTimeout>>(undefined);
  const disconnect = useRef<() => void>(undefined);

  useEffect(() => () => {
    clearTimeout(saveTimer.current);
    disconnect.current?.();
  }, []);

  const onModel = (model: NotebookModel | null) => {
    disconnect.current?.();
    disconnect.current = undefined;
    if (!model) return;
    const save = () => {
      clearTimeout(saveTimer.current);
      saveTimer.current = setTimeout(() => {
        try {
          window.localStorage.setItem(STORAGE_KEY, JSON.stringify(model.toJSON()));
        } catch {
          // Storage full or unavailable; the notebook still works for this page.
        }
      }, SAVE_DELAY_MS);
    };
    model.contentChanged.connect(save);
    disconnect.current = () => model.contentChanged.disconnect(save);
  };

  const reset = () => {
    if (!window.confirm("Replace this notebook with the starter notebook? Your cells will be lost.")) return;
    clearTimeout(saveTimer.current);
    try {
      window.localStorage.removeItem(STORAGE_KEY);
    } catch {}
    setNbformat(STARTER);
    setGeneration((g) => g + 1);
  };

  if (!serviceManager || !defaultKernel) return <p className="muted">Starting the Python kernel…</p>;

  return (
    <div className="notebook-view">
      <div className="row">
        <span className="muted">Saved in this browser automatically.</span>
        <button className="small" onClick={reset}>
          Reset notebook
        </button>
      </div>
      <JupyterReactTheme colormode="light" useBaseStyles={false}>
        <Notebook
          key={generation}
          id={`airavata-notebook-${generation}`}
          kernel={defaultKernel}
          serviceManager={serviceManager}
          nbformat={nbformat}
          height="calc(100vh - 200px)"
          Toolbar={NotebookToolbar}
          onNotebookModelChanged={onModel}
        />
      </JupyterReactTheme>
    </div>
  );
}
