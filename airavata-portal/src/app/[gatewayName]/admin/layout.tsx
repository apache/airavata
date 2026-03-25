"use client";

import { useParams } from "next/navigation";
import { useGatewayAdminRouteGuard, getGatewayIdFromName } from "@/lib/route-guards";
import { useGateway } from "@/contexts/GatewayContext";
import { useEffect } from "react";

/**
 * Gateway admin layout: runs admin guard and sets gateway context only.
 * Does NOT wrap with DashboardLayout – parent [gatewayName] layout already provides it.
 * Wrapping again would duplicate header/sidebar inside the page content.
 */
export default function GatewayAdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const params = useParams();
  const gatewayName = params.gatewayName as string;
  const { accessibleGateways, getGatewayName, setSelectedGatewayId } = useGateway();

  useGatewayAdminRouteGuard(gatewayName);

  useEffect(() => {
    const gatewayId = getGatewayIdFromName(gatewayName, accessibleGateways);
    if (gatewayId) {
      setSelectedGatewayId(gatewayId);
    }
  }, [gatewayName, accessibleGateways, getGatewayName, setSelectedGatewayId]);

  return <>{children}</>;
}
