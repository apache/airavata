"use client";

import { useGateway } from "@/contexts/GatewayContext";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

interface GatewayBadgeProps {
  gatewayId: string;
  className?: string;
}

export function GatewayBadge({ gatewayId, className }: GatewayBadgeProps) {
  const { getGatewayName } = useGateway();
  const gatewayName = getGatewayName(gatewayId);

  return (
    <Badge
      variant="secondary"
      className={cn(
        "text-xs font-normal bg-muted text-muted-foreground border-border hover:bg-muted/80",
        className
      )}
    >
      {gatewayName}
    </Badge>
  );
}
