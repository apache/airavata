"use client";

import { useState } from "react";
import { Wifi, WifiOff, CheckCircle, XCircle, Loader2, Key, AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { useCredentials } from "@/hooks/useCredentials";
import { connectivityApi } from "@/lib/api/connectivity";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import type { ConnectivityTestResult } from "@/lib/api/connectivity";

interface TestConnectionModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  resourceType: "compute" | "storage";
  hostname: string;
  port?: number;
  /** When provided (compute only), enables "Fetch Cluster Info" and shows partitions/accounts */
  computeResourceId?: string;
}

type TestStatus = "idle" | "testing" | "success" | "error";

export function TestConnectionModal({
  open,
  onOpenChange,
  resourceType,
  hostname,
  port = 22,
  computeResourceId,
}: TestConnectionModalProps) {
  const { data: credentials, isLoading: credentialsLoading } = useCredentials();
  const { selectedGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = selectedGatewayId || defaultGatewayId;

  const [selectedCredentialToken, setSelectedCredentialToken] = useState<string>("");
  const [loginUsername, setLoginUsername] = useState<string>("");
  const [testStatus, setTestStatus] = useState<TestStatus>("idle");
  const [testResult, setTestResult] = useState<ConnectivityTestResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [clusterInfoResult, setClusterInfoResult] = useState<{
    partitions: { partitionName: string; nodeCount: number; maxCpusPerNode: number; maxGpusPerNode: number; accounts?: string[] }[];
    accounts: string[];
  } | null>(null);
  const [clusterInfoStatus, setClusterInfoStatus] = useState<"idle" | "fetching" | "success" | "error">("idle");
  const [clusterInfoError, setClusterInfoError] = useState<string>("");

  const sshCredentials = credentials?.filter((c) => c.type === "SSH") || [];
  const canFetchClusterInfo = resourceType === "compute" && !!computeResourceId && !!selectedCredentialToken && !!hostname && !!loginUsername.trim();

  const handleTest = async () => {
    if (!selectedCredentialToken) return;

    setTestStatus("testing");
    setTestResult(null);
    setErrorMessage("");

    try {
      const result = await connectivityApi.validateSSH({
        credentialToken: selectedCredentialToken,
        hostname,
        port,
        gatewayId,
      });

      setTestResult(result);
      setTestStatus(result.success ? "success" : "error");
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Connection test failed");
      setTestStatus("error");
    }
  };

  const handleFetchClusterInfo = async () => {
    if (!selectedCredentialToken || !computeResourceId || !hostname || !gatewayId) return;

    setClusterInfoStatus("fetching");
    setClusterInfoResult(null);

    try {
      const { clusterInfoApi: clusterApi } = await import("@/lib/api/clusterInfo");
      const info = await clusterApi.fetch({
        credentialToken: selectedCredentialToken,
        computeResourceId,
        hostname,
        port,
        gatewayId,
      });
      setClusterInfoResult({
        partitions: info.partitions ?? [],
        accounts: info.accounts ?? [],
      });
      setClusterInfoStatus("success");
    } catch (error) {
      setClusterInfoStatus("error");
      setClusterInfoError(error instanceof Error ? error.message : "Failed to fetch cluster info");
    }
  };

  const handleClose = () => {
    setSelectedCredentialToken("");
    setLoginUsername("");
    setTestStatus("idle");
    setTestResult(null);
    setErrorMessage("");
    setClusterInfoResult(null);
    setClusterInfoStatus("idle");
    setClusterInfoError("");
    onOpenChange(false);
  };

  const getStatusIcon = () => {
    switch (testStatus) {
      case "testing":
        return <Loader2 className="h-8 w-8 text-blue-500 animate-spin" />;
      case "success":
        return <CheckCircle className="h-8 w-8 text-green-500" />;
      case "error":
        return <XCircle className="h-8 w-8 text-red-500" />;
      default:
        return <Wifi className="h-8 w-8 text-muted-foreground" />;
    }
  };

  const getStatusMessage = () => {
    switch (testStatus) {
      case "testing":
        return "Testing connection...";
      case "success":
        return "Connection successful!";
      case "error":
        return "Connection failed";
      default:
        return "Select a credential and test the connection";
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Wifi className="h-5 w-5" />
            Test Connection
          </DialogTitle>
          <DialogDescription>
            Test SSH connectivity to {resourceType === "compute" ? "compute" : "storage"} resource
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Resource Info */}
          <Card>
            <CardContent className="pt-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Hostname:</span>
                  <p className="font-medium">{hostname}</p>
                </div>
                <div>
                  <span className="text-muted-foreground">SSH Port:</span>
                  <p className="font-medium">{port}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Credential Selection */}
          <div className="space-y-2">
            <Label htmlFor="credential">SSH Credential</Label>
            {credentialsLoading ? (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading credentials...
              </div>
            ) : sshCredentials.length === 0 ? (
              <div className="flex items-center gap-2 p-3 rounded-md bg-yellow-50 border border-yellow-200">
                <AlertTriangle className="h-4 w-4 text-yellow-600" />
                <span className="text-sm text-yellow-700">
                  No SSH credentials available. Please create one first.
                </span>
              </div>
            ) : (
              <Select
                value={selectedCredentialToken}
                onValueChange={setSelectedCredentialToken}
                disabled={testStatus === "testing"}
              >
                <SelectTrigger id="credential">
                  <SelectValue placeholder="Select an SSH credential" />
                </SelectTrigger>
                <SelectContent>
                  {sshCredentials.map((cred) => (
                    <SelectItem key={cred.token} value={cred.token}>
                      <div className="flex items-center gap-2">
                        <Key className="h-4 w-4" />
                        <span>{cred.name || cred.description || cred.token.substring(0, 12)}</span>
                        {(cred.name || cred.description) && (
                          <span className="text-muted-foreground">
                            - {cred.name || cred.description}
                          </span>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          </div>

          {/* Test Result */}
          <Card className={`${
            testStatus === "success" 
              ? "border-green-200 bg-green-50" 
              : testStatus === "error" 
              ? "border-red-200 bg-red-50" 
              : ""
          }`}>
            <CardContent className="pt-4">
              <div className="flex flex-col items-center text-center py-4">
                {getStatusIcon()}
                <p className={`mt-2 font-medium ${
                  testStatus === "success" 
                    ? "text-green-700" 
                    : testStatus === "error" 
                    ? "text-red-700" 
                    : ""
                }`}>
                  {getStatusMessage()}
                </p>
                
                {testResult && (
                  <div className="mt-3 text-sm text-muted-foreground w-full text-left">
                    {testResult.message && (
                      <p className="mb-2">{testResult.message}</p>
                    )}
                    {testResult.details && (
                      <p className="text-xs font-mono bg-muted p-2 rounded">
                        {testResult.details}
                      </p>
                    )}
                    {(testResult.auth_validated ?? testResult.authentication) && (
                      <div className="mt-2">
                        <Badge variant="outline">
                          Auth: {testResult.auth_validated ? "Validated" : testResult.authentication ?? "—"}
                        </Badge>
                      </div>
                    )}
                    {testResult.username && (
                      <p className="mt-1 text-xs">User: {testResult.username}</p>
                    )}
                  </div>
                )}

                {errorMessage && testStatus === "error" && !testResult && (
                  <p className="mt-2 text-sm text-red-600">{errorMessage}</p>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Fetch Cluster Info (compute only when computeResourceId provided) */}
          {canFetchClusterInfo && (
            <div className="space-y-2">
              <Label>Cluster Info (SLURM)</Label>
              <Button
                variant="secondary"
                size="sm"
                onClick={handleFetchClusterInfo}
                disabled={clusterInfoStatus === "fetching" || testStatus === "testing"}
              >
                {clusterInfoStatus === "fetching" ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : null}
                Fetch Cluster Info
              </Button>
              {clusterInfoResult && clusterInfoStatus === "success" && (
                <div className="rounded border p-3 text-sm space-y-2">
                  {clusterInfoResult.partitions.length > 0 && (
                    <div>
                      <p className="font-medium text-muted-foreground mb-1">Partitions</p>
                      <ul className="list-disc list-inside space-y-0.5">
                        {clusterInfoResult.partitions.slice(0, 10).map((p) => (
                          <li key={p.partitionName}>
                            {p.partitionName}: {p.nodeCount} nodes, {p.maxCpusPerNode} CPUs/node
                            {p.maxGpusPerNode > 0 ? `, ${p.maxGpusPerNode} GPUs/node` : ""}
                          </li>
                        ))}
                        {clusterInfoResult.partitions.length > 10 && (
                          <li className="text-muted-foreground">… and {clusterInfoResult.partitions.length - 10} more</li>
                        )}
                      </ul>
                    </div>
                  )}
                  {clusterInfoResult.accounts.length > 0 && (
                    <div>
                      <p className="font-medium text-muted-foreground mb-1">Accounts</p>
                      <div className="flex flex-wrap gap-1">
                        {clusterInfoResult.accounts.map((a) => (
                          <Badge key={a} variant="secondary" className="font-mono text-xs">
                            {a}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
              {clusterInfoStatus === "error" && clusterInfoError && (
                <p className="text-sm text-red-600">{clusterInfoError}</p>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            Close
          </Button>
          <Button
            onClick={handleTest}
            disabled={!selectedCredentialToken || !loginUsername.trim() || testStatus === "testing" || sshCredentials.length === 0}
          >
            {testStatus === "testing" ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Testing...
              </>
            ) : (
              <>
                <Wifi className="h-4 w-4 mr-2" />
                Test Connection
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
