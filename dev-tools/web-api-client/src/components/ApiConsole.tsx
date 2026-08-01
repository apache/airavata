"use client";

import { useState } from "react";

type Method = "GET" | "POST" | "PUT" | "DELETE";

interface ProxyResponse {
  status: number;
  statusText: string;
  headers: Record<string, string>;
  body: string;
  durationMs: number;
}

interface ProxyError {
  error: string;
  message?: string;
}

const METHODS: Method[] = ["GET", "POST", "PUT", "DELETE"];
const DEFAULT_HEADERS = '{\n  "Content-Type": "application/json"\n}';

function statusClass(status: number) {
  if (status >= 200 && status < 300) return "status-2xx";
  if (status >= 300 && status < 400) return "status-3xx";
  if (status >= 400 && status < 500) return "status-4xx";
  return "status-5xx";
}

function prettyPrint(text: string) {
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}

export default function ApiConsole({ accessTokenExpires }: { accessTokenExpires?: number }) {
  const [method, setMethod] = useState<Method>("GET");
  const [url, setUrl] = useState("");
  const [headersText, setHeadersText] = useState(DEFAULT_HEADERS);
  const [body, setBody] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<ProxyResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const showBody = method === "POST" || method === "PUT";
  const tokenExpired = accessTokenExpires ? Date.now() > accessTokenExpires : false;

  async function handleSend() {
    setLoading(true);
    setError(null);
    setResult(null);

    let parsedHeaders: Record<string, string> = {};
    if (headersText.trim()) {
      try {
        parsedHeaders = JSON.parse(headersText);
      } catch {
        setError("Headers must be valid JSON");
        setLoading(false);
        return;
      }
    }

    try {
      const res = await fetch("/api/proxy", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          url,
          method,
          headers: parsedHeaders,
          body: showBody ? body : undefined,
        }),
      });

      const data = (await res.json()) as ProxyResponse | ProxyError;
      if (!res.ok || "error" in data) {
        const errData = data as ProxyError;
        setError(errData.message ?? errData.error ?? "Request failed");
      } else {
        setResult(data as ProxyResponse);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      {tokenExpired && (
        <p className="error-text">
          Your CILogon access token has expired. Sign out and sign in again to refresh it.
        </p>
      )}

      <div className="panel">
        <div className="request-row">
          <select
            className={`method-${method.toLowerCase()}`}
            value={method}
            onChange={(e) => setMethod(e.target.value as Method)}
          >
            {METHODS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
          <input
            className="url-input"
            type="text"
            placeholder="http://localhost:9090/api/v1/experiments"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <button className="btn btn-primary" onClick={handleSend} disabled={loading || !url}>
            {loading ? "Sending…" : "Send"}
          </button>
        </div>

        <details>
          <summary>Headers (JSON) — Authorization is added automatically from your CILogon session</summary>
          <textarea
            rows={4}
            value={headersText}
            onChange={(e) => setHeadersText(e.target.value)}
          />
        </details>

        {showBody && (
          <>
            <div className="field-label">Request body</div>
            <textarea
              rows={10}
              placeholder='{\n  "key": "value"\n}'
              value={body}
              onChange={(e) => setBody(e.target.value)}
            />
          </>
        )}

        {error && <p className="error-text">{error}</p>}

        {result && (
          <div className="response-panel">
            <div className="status-line">
              <span className={`status-badge ${statusClass(result.status)}`}>
                {result.status} {result.statusText}
              </span>
              <span className="hint">{result.durationMs} ms</span>
            </div>

            <details>
              <summary>Response headers</summary>
              <pre className="body-view">{JSON.stringify(result.headers, null, 2)}</pre>
            </details>

            <div className="field-label">Response body</div>
            <pre className="body-view">{prettyPrint(result.body) || "(empty)"}</pre>
          </div>
        )}
      </div>
    </div>
  );
}
