"use client";

import { useState } from "react";
import * as React from "react";
import { Button } from "@/components/ui/button";
import { CheckCircle2, XCircle, Loader2, Play } from "lucide-react";
import { apiClient } from "@/lib/api/client";

export interface TestResult {
  success: boolean;
  message: string;
}

interface Props {
  host: string;
  port?: number;
  type: "ssh" | "sftp" | "slurm";
  credentialToken?: string;
  onTestComplete?: (success: boolean) => void;
  /** Callback with full test result for external rendering */
  onResultChange?: (result: TestResult | null) => void;
  /** When true, render only the Test button (no border, no label, no host input). Use inline with parent-provided label and host. */
  inline?: boolean;
}

export function ConnectivityTest({ host, port, type, credentialToken, onTestComplete, onResultChange, inline }: Props) {
  const [isTesting, setIsTesting] = useState(false);
  const [testResult, setTestResultState] = useState<TestResult | null>(null);
  
  const setTestResult = (result: TestResult | null) => {
    setTestResultState(result);
    onResultChange?.(result);
  };
  
  // Use test infrastructure ports for localhost
  const getTestPort = () => {
    if (host === "localhost" || host === "127.0.0.1") {
      if (type === "slurm") return 6817;
      if (type === "sftp") return 10023;
      return 10022; // SSH for compute
    }
    return port || (type === "slurm" ? 6817 : 22);
  };
  
  const testPort = getTestPort();

  const handleTest = async () => {
    if (!host) return;

    setIsTesting(true);
    setTestResult(null);

    try {
      let result;
      const actualPort = host === "localhost" || host === "127.0.0.1" 
        ? (type === "sftp" ? 10023 : type === "slurm" ? 6817 : 10022)
        : testPort;
      
      // Try backend first, fallback to Next.js API route
      try {
        if (type === "slurm") {
          const sshPort = host === "localhost" || host === "127.0.0.1" ? 10022 : 22;
          result = await apiClient.post<{ success: boolean; message: string }>("/api/v1/connectivity-test/slurm", {
            host,
            sshPort,
            slurmPort: actualPort,
          });
        } else {
          result = await apiClient.post<{ success: boolean; message: string }>(`/api/v1/connectivity-test/${type}`, {
            host,
            port: actualPort,
            credentialToken,
          });
        }
      } catch {
        // Fallback to Next.js API route
        const nextResponse = await fetch(`/api/v1/connectivity-test/${type}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(
            type === "slurm"
              ? { host, sshPort: host === "localhost" ? 10022 : 22, slurmPort: actualPort }
              : { host, port: actualPort, credentialToken }
          ),
        });
        if (!nextResponse.ok) throw new Error("Connection failed");
        result = await nextResponse.json();
      }

      setTestResult({ success: result.success, message: result.message });
      onTestComplete?.(result.success);
    } catch (error) {
      setTestResult({
        success: false,
        message: error instanceof Error ? error.message : "Connection failed",
      });
      onTestComplete?.(false);
    } finally {
      setIsTesting(false);
    }
  };

  if (inline) {
    return (
      <Button
        type="button"
        size="sm"
        onClick={handleTest}
        disabled={isTesting || !host}
        className="bg-green-600 hover:bg-green-700 text-white"
      >
        {isTesting ? (
          <>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            Testing...
          </>
        ) : (
          <>
            <Play className="mr-1 h-4 w-4" />
            Test
          </>
        )}
      </Button>
    );
  }

  const displayUrl = `${host}:${testPort}`;

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-3">
        <span className="text-sm font-medium">Test Connection</span>
        <code className="text-sm text-muted-foreground bg-muted px-2 py-0.5 rounded">{displayUrl}</code>
        <Button
          type="button"
          size="sm"
          onClick={handleTest}
          disabled={isTesting || !host}
          className="bg-green-600 hover:bg-green-700 text-white"
        >
          {isTesting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Testing...
            </>
          ) : (
            <>
              <Play className="mr-1 h-4 w-4" />
              Test
            </>
          )}
        </Button>
      </div>

      {testResult && (
        <div className={`flex items-center gap-2 px-3 py-2 rounded text-sm ${
          testResult.success
            ? "bg-green-50 text-green-700 dark:bg-green-950 dark:text-green-300"
            : "bg-red-50 text-red-700 dark:bg-red-950 dark:text-red-300"
        }`}>
          {testResult.success ? (
            <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
          ) : (
            <XCircle className="h-4 w-4 flex-shrink-0" />
          )}
          <span>{testResult.success ? "Connection successful" : testResult.message}</span>
        </div>
      )}
    </div>
  );
}
