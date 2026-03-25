"use client";

import { useGateway } from "@/contexts/GatewayContext";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Building2, ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";

export function GatewaySelector() {
  const {
    selectedGatewayId,
    setSelectedGatewayId,
    accessibleGateways,
    isLoading,
    isRootUser,
    getGatewayName,
    hasNoGatewayAndIsRoot,
  } = useGateway();

  if (isLoading) {
    return <Skeleton className="h-9 w-48" />;
  }

  if (accessibleGateways.length === 0) {
    if (hasNoGatewayAndIsRoot) {
      return (
        <div
          className={cn(
            "flex h-9 w-[220px] items-center gap-2 rounded-md border border-dashed border-muted-foreground/30 bg-muted/30 px-3 text-sm text-muted-foreground"
          )}
          title="No gateway available – system administration only"
        >
          <ShieldCheck className="h-4 w-4 flex-shrink-0" />
          <span className="truncate">No gateway</span>
        </div>
      );
    }
    return null;
  }

  const GatewayIcon = Building2;
  const iconColorClass = "text-primary";

  return (
    <Select
      value={selectedGatewayId || ""}
      onValueChange={(value) => setSelectedGatewayId(value)}
    >
      <SelectTrigger className="w-[220px]">
        <div className="inline-flex items-center gap-2 overflow-hidden">
          <GatewayIcon className={cn("h-4 w-4 flex-shrink-0", iconColorClass)} />
          <span className="truncate">
            {selectedGatewayId ? getGatewayName(selectedGatewayId) : "Select gateway"}
          </span>
        </div>
      </SelectTrigger>
      <SelectContent>
        {accessibleGateways.map((gateway) => (
          <SelectItem key={gateway.gatewayId} value={gateway.gatewayId}>
            <div className="inline-flex items-center gap-2">
              <Building2 className="h-3.5 w-3.5 flex-shrink-0 text-primary" />
              <span>{gateway.gatewayName || gateway.gatewayId}</span>
            </div>
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
