"use client";

import dynamic from "next/dynamic";
import { PageHead } from "@/components/ui";

// JupyterLab touches window and document as soon as it is imported, so the notebook is
// only ever loaded in the browser. It also keeps the large JupyterLab bundle off every
// other page.
const NotebookView = dynamic(() => import("@/components/NotebookView"), {
  ssr: false,
  loading: () => <p className="muted">Loading notebook…</p>,
});

export default function NotebooksPage() {
  return (
    <div className="stack">
      <PageHead
        title="Notebooks"
        sub="Python notebooks that run in your browser on Pyodide, or on a cluster in the Jupyter Server of a running process whose job runs Linkspan."
      />
      <NotebookView />
    </div>
  );
}
