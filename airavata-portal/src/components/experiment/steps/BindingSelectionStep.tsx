"use client";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Server, Key } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { bindingsApi, resourcesApi, credentialsApi } from "@/lib/api";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import type { ResourceBinding, Resource } from "@/types";

interface Props {
  data: any;
  onUpdate: (data: any) => void;
  onNext: () => void;
  onBack: () => void;
}

export function BindingSelectionStep({ data, onUpdate, onNext, onBack }: Props) {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;

  const { data: bindings = [], isLoading: isLoadingBindings } = useQuery({
    queryKey: ["bindings", gatewayId],
    queryFn: () => bindingsApi.list({ gatewayId }),
    enabled: !!gatewayId,
  });

  const { data: resources = [], isLoading: isLoadingResources } = useQuery({
    queryKey: ["resources", gatewayId],
    queryFn: () => resourcesApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const { data: credentials = [] } = useQuery({
    queryKey: ["credentials", gatewayId],
    queryFn: () => credentialsApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  // Build resource map for quick lookup
  const resourceMap = new Map<string, Resource>(
    resources.map((r) => [r.resourceId, r])
  );

  const credentialMap = new Map(
    credentials.map((c) => [c.token, c])
  );

  // Only show bindings for resources with compute capability and that are enabled
  const computeBindings = bindings.filter((b) => {
    if (!b.enabled) return false;
    const resource = resourceMap.get(b.resourceId);
    return resource?.capabilities?.compute != null;
  });

  const handleSelect = (binding: ResourceBinding) => {
    const resource = resourceMap.get(binding.resourceId);
    onUpdate({
      bindingId: binding.bindingId,
      selectedBinding: binding,
      selectedResource: resource,
    });
  };

  const handleNext = () => {
    if (!data.bindingId) {
      alert("Please select a resource binding");
      return;
    }
    onNext();
  };

  const isLoading = isLoadingBindings || isLoadingResources;

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <Label>Resource Binding *</Label>
        <p className="text-sm text-muted-foreground">
          Select the credential and compute resource to use for this experiment
        </p>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-20 w-full" />
          ))}
        </div>
      ) : computeBindings.length === 0 ? (
        <div className="p-4 border border-yellow-200 bg-yellow-50 rounded-lg">
          <p className="text-sm text-yellow-800">
            No compute resource bindings available. Please configure credentials and resource
            bindings in the Admin panel before creating experiments.
          </p>
        </div>
      ) : (
        <div className="grid gap-3">
          {computeBindings.map((binding) => {
            const resource = resourceMap.get(binding.resourceId);
            const credential = credentialMap.get(binding.credentialId);
            const isSelected = data.bindingId === binding.bindingId;

            return (
              <Card
                key={binding.bindingId}
                className={`p-4 cursor-pointer transition-colors hover:bg-accent ${
                  isSelected ? "border-primary bg-accent" : ""
                }`}
                onClick={() => handleSelect(binding)}
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-start gap-3 flex-1">
                    <Server className="h-5 w-5 text-muted-foreground mt-0.5 shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-semibold">
                          {resource?.name || binding.resourceId}
                        </span>
                        {resource?.capabilities?.compute && (
                          <Badge variant="outline" className="text-xs">
                            {resource.capabilities.compute.type}
                          </Badge>
                        )}
                      </div>
                      {resource?.hostName && (
                        <p className="text-sm text-muted-foreground">{resource.hostName}</p>
                      )}
                      <div className="flex items-center gap-3 mt-1.5 flex-wrap">
                        <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                          <Key className="h-3.5 w-3.5" />
                          <span>{credential?.name || binding.credentialId.substring(0, 12)}</span>
                        </div>
                        <div className="text-xs text-muted-foreground">
                          Login: <code className="font-mono">{binding.loginUsername}</code>
                        </div>
                      </div>
                    </div>
                  </div>
                  {isSelected && (
                    <Badge className="shrink-0">Selected</Badge>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      <div className="flex justify-between">
        <Button variant="outline" onClick={onBack}>
          Back
        </Button>
        <Button onClick={handleNext} disabled={!data.bindingId}>
          Next
        </Button>
      </div>
    </div>
  );
}
