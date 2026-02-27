"use client";

import { useState } from "react";
import { CheckCircle, XCircle, Loader2, Key, Play, Plug } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCredentials } from "@/hooks/useCredentials";
import { credentialsApi } from "@/lib/api/credentials";
import { connectivityApi } from "@/lib/api/connectivity";
import { useGateway } from "@/contexts/GatewayContext";
import type { ConnectivityTestResult } from "@/lib/api/connectivity";

type TestStatus = "idle" | "testing" | "success" | "error";

interface StorageTestConnectionCardProps {
  hostName: string;
  port?: number;
}

export function StorageTestConnectionCard({ hostName, port = 22 }: StorageTestConnectionCardProps) {
  const { data: credentials, isLoading: credentialsLoading } = useCredentials();
  const { effectiveGatewayId } = useGateway();
  const gatewayId = effectiveGatewayId || "";

  const [selectedCredentialToken, setSelectedCredentialToken] = useState<string>("");
  const [loginUsername, setLoginUsername] = useState<string>("");
  const [testStatus, setTestStatus] = useState<TestStatus>("idle");
  const [testResult, setTestResult] = useState<ConnectivityTestResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>("");

  const sshCredentials = credentials?.filter((c) => c.type === "SSH") || [];

  const handleTest = async () => {
    if (!selectedCredentialToken || !gatewayId) return;

    setTestStatus("testing");
    setTestResult(null);
    setErrorMessage("");

    try {
      const credential = await credentialsApi.getSSH(selectedCredentialToken, gatewayId);
      if (!credential) throw new Error("Failed to retrieve credential details");

      const result = await connectivityApi.testSSH({
        host: hostName,
        port,
        username: loginUsername || undefined,
        privateKey: credential.privateKey,
      });

      setTestResult(result);
      setTestStatus(result.success ? "success" : "error");
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Connection test failed");
      setTestStatus("error");
    }
  };

  return (
    <Card className="flex flex-col">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Plug className="h-5 w-5" />
            Test Connection
          </CardTitle>
          <Button
            type="button"
            onClick={handleTest}
            disabled={
              !selectedCredentialToken ||
              testStatus === "testing" ||
              sshCredentials.length === 0
            }
          >
            {testStatus === "testing" ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : testStatus === "success" ? (
              <CheckCircle className="h-4 w-4 mr-2 text-green-500" />
            ) : testStatus === "error" ? (
              <XCircle className="h-4 w-4 mr-2 text-red-500" />
            ) : (
              <Play className="h-4 w-4 mr-2" />
            )}
            {testStatus === "testing" ? "Testing..." : testStatus === "success" ? "Success" : testStatus === "error" ? "Failed" : "Test"}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4 flex-1">
        {/* Login username (required for SSH test) */}
        <div>
          <p className="text-sm font-medium text-muted-foreground mb-1">Login username <span className="text-destructive">*</span></p>
          <input
            type="text"
            className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm"
            placeholder="e.g. myuser"
            value={loginUsername}
            onChange={(e) => setLoginUsername(e.target.value)}
          />
          <p className="text-xs text-muted-foreground mt-0.5">SSH login username on this resource. Set per resource in access grant.</p>
        </div>
        {/* Credential picker */}
        <div>
          <p className="text-sm font-medium text-muted-foreground mb-1">Credential</p>
          {credentialsLoading ? (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Loading...</span>
            </div>
          ) : (
            <Select
              value={selectedCredentialToken}
              onValueChange={(v) => {
                setSelectedCredentialToken(v);
                setTestStatus("idle");
                setTestResult(null);
                setErrorMessage("");
              }}
              disabled={testStatus === "testing" || sshCredentials.length === 0}
            >
              <SelectTrigger>
                <SelectValue placeholder={sshCredentials.length === 0 ? "No credentials" : "Select credential"} />
              </SelectTrigger>
              <SelectContent>
                {sshCredentials.map((c) => (
                  <SelectItem key={c.token} value={c.token}>
                    <div className="flex items-center gap-2">
                      <Key className="h-4 w-4" />
                      <span>{c.name || c.description || c.token.substring(0, 12)}</span>
                      {(c.name || c.description) && (
                        <span className="text-muted-foreground">— {c.name || c.description}</span>
                      )}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>

        {/* Warning when no credentials */}
        {!credentialsLoading && sshCredentials.length === 0 && (
          <p className="text-sm text-amber-600 dark:text-amber-400">
            No SSH credentials available. Add a credential to test access.
          </p>
        )}

        {/* Error message */}
        {errorMessage && testStatus === "error" && (
          <p className="text-sm text-red-600 dark:text-red-400">{errorMessage}</p>
        )}
      </CardContent>
    </Card>
  );
}
