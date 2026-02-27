"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Server, RefreshCw, Loader2, AlertTriangle, Cpu, HardDrive, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { clusterInfoApi } from "@/lib/api/clusterInfo";
import { useGateway } from "@/contexts/GatewayContext";
import type { ClusterInfo, PartitionInfo } from "@/lib/api/clusterInfo";
import { toast } from "@/hooks/useToast";

export interface ClusterInfoPanelProps {
  credentialToken: string;
  computeResourceId: string;
  hostname: string;
  port?: number;
  /** Callback when cluster info is successfully fetched */
  onClusterInfoFetched?: (info: ClusterInfo) => void;
  /** Optional: show compact view */
  compact?: boolean;
}

export function ClusterInfoPanel({
  credentialToken,
  computeResourceId,
  hostname,
  port = 22,
  onClusterInfoFetched,
  compact = false,
}: ClusterInfoPanelProps) {
  const { effectiveGatewayId } = useGateway();
  const gatewayId = effectiveGatewayId || "";
  const queryClient = useQueryClient();
  const [fetchError, setFetchError] = useState<string>("");

  const queryKey = ["cluster-info", credentialToken, computeResourceId];
  const { data: clusterInfo, isLoading: loadingCached } = useQuery({
    queryKey,
    queryFn: () => clusterInfoApi.get(credentialToken, computeResourceId),
    enabled: !!credentialToken && !!computeResourceId && !!gatewayId,
  });

  const fetchMutation = useMutation({
    mutationFn: () =>
      clusterInfoApi.fetch({
        credentialToken,
        computeResourceId,
        hostname,
        port,
        gatewayId: gatewayId || undefined,
      }),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKey, data);
      setFetchError("");
      onClusterInfoFetched?.(data);
      toast({ title: "Cluster info fetched" });
    },
    onError: (err: Error) => {
      setFetchError(err.message || "Failed to fetch cluster info");
      toast({ title: "Failed to fetch cluster info", description: err.message, variant: "destructive" });
    },
  });

  const handleFetch = () => {
    setFetchError("");
    fetchMutation.mutate();
  };

  const isFetching = fetchMutation.isPending;
  const hasInfo = clusterInfo && (clusterInfo.partitions?.length > 0 || (clusterInfo.accounts?.length ?? 0) > 0);

  if (compact) {
    return (
      <Card>
        <CardContent className="pt-4">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              {loadingCached && !clusterInfo && <Loader2 className="h-4 w-4 animate-spin" />}
              {hasInfo ? (
                <span>
                  {clusterInfo.partitions?.length ?? 0} partition(s), {clusterInfo.accounts?.length ?? 0} account(s)
                  {clusterInfo.fetchedAt && (
                    <span className="ml-1 text-xs">
                      (fetched {new Date(clusterInfo.fetchedAt).toLocaleString()})
                    </span>
                  )}
                </span>
              ) : (
                <span>No cluster info cached</span>
              )}
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={handleFetch}
              disabled={isFetching || !credentialToken || !hostname}
            >
              {isFetching ? <Loader2 className="h-4 w-4 animate-spin" /> : <RefreshCw className="h-4 w-4" />}
              <span className="ml-1">{hasInfo ? "Refresh" : "Fetch Info"}</span>
            </Button>
          </div>
          {fetchError && (
            <p className="mt-2 text-sm text-destructive flex items-center gap-1">
              <AlertTriangle className="h-4 w-4" /> {fetchError}
            </p>
          )}
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="flex items-center gap-2 text-base">
          <Server className="h-5 w-5" />
          Cluster Info (SLURM)
        </CardTitle>
        <CardDescription>
          Partitions and accounts available for this credential on the compute resource
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center justify-between gap-2">
          {clusterInfo?.fetchedAt && (
            <p className="text-xs text-muted-foreground">
              Last fetched: {new Date(clusterInfo.fetchedAt).toLocaleString()}
            </p>
          )}
          <Button
            variant="outline"
            size="sm"
            onClick={handleFetch}
            disabled={isFetching || !credentialToken || !hostname}
          >
            {isFetching ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <RefreshCw className="h-4 w-4" />
            )}
            <span className="ml-2">{hasInfo ? "Refresh Info" : "Fetch Cluster Info"}</span>
          </Button>
        </div>

        {fetchError && (
          <div className="flex items-center gap-2 p-2 rounded-md bg-destructive/10 text-destructive text-sm">
            <AlertTriangle className="h-4 w-4 shrink-0" /> {fetchError}
          </div>
        )}

        {loadingCached && !clusterInfo && !isFetching && (
          <p className="text-sm text-muted-foreground">Loading cached info...</p>
        )}

        {hasInfo && (
          <div className="space-y-3">
            {clusterInfo.partitions && clusterInfo.partitions.length > 0 && (
              <div>
                <h4 className="text-sm font-medium mb-2 flex items-center gap-1">
                  <Cpu className="h-4 w-4" /> Partitions
                </h4>
                <div className="space-y-2">
                  {clusterInfo.partitions.map((p: PartitionInfo) => (
                    <PartitionRow key={p.partitionName} partition={p} />
                  ))}
                </div>
              </div>
            )}
            {clusterInfo.accounts && clusterInfo.accounts.length > 0 && (
              <div>
                <h4 className="text-sm font-medium mb-2 flex items-center gap-1">
                  <Users className="h-4 w-4" /> Accounts
                </h4>
                <div className="flex flex-wrap gap-1">
                  {clusterInfo.accounts.map((a: string) => (
                    <Badge key={a} variant="secondary" className="font-mono text-xs">
                      {a}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {!hasInfo && !loadingCached && !isFetching && (
          <p className="text-sm text-muted-foreground">
            Click &quot;Fetch Cluster Info&quot; to run SLURM discovery on the cluster (requires SSH access).
          </p>
        )}
      </CardContent>
    </Card>
  );
}

function PartitionRow({ partition }: { partition: PartitionInfo }) {
  return (
    <div className="flex flex-wrap items-center gap-2 rounded border p-2 text-sm">
      <span className="font-medium">{partition.partitionName}</span>
      <Badge variant="outline" className="text-xs">
        {partition.nodeCount} nodes
      </Badge>
      <Badge variant="outline" className="text-xs">
        <Cpu className="h-3 w-3 mr-0.5 inline" /> {partition.maxCpusPerNode} CPUs/node
      </Badge>
      {partition.maxGpusPerNode > 0 && (
        <Badge variant="outline" className="text-xs">
          <HardDrive className="h-3 w-3 mr-0.5 inline" /> {partition.maxGpusPerNode} GPUs/node
        </Badge>
      )}
      {partition.accounts && partition.accounts.length > 0 && (
        <span className="text-muted-foreground text-xs">
          Accounts: {partition.accounts.join(", ")}
        </span>
      )}
    </div>
  );
}
