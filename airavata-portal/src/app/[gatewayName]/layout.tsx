"use client";

import { useParams } from "next/navigation";
import { useEffect } from "react";
import { DashboardLayout } from "@/components/layout";
import { useGatewayRouteGuard, getGatewayIdFromName } from "@/lib/route-guards";
import { useGateway } from "@/contexts/GatewayContext";

export default function GatewayLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const params = useParams();
  const gatewayName = params.gatewayName as string;
  const { accessibleGateways, getGatewayName, setSelectedGatewayId } = useGateway();

  // Guard ensures user has access to this gateway
  useGatewayRouteGuard(gatewayName);

  // Set the gateway context based on the route
  useEffect(() => {
    const gatewayId = getGatewayIdFromName(gatewayName, accessibleGateways);
    if (gatewayId) {
      setSelectedGatewayId(gatewayId);
    }
  }, [gatewayName, accessibleGateways, getGatewayName, setSelectedGatewayId]);

  return <DashboardLayout>{children}</DashboardLayout>;
}
