"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useGateway } from "@/contexts/GatewayContext";

export default function ProjectsPage() {
  const router = useRouter();
  const { selectedGatewayId, getGatewayName, accessibleGateways } = useGateway();

  useEffect(() => {
    const dashboardHref =
      selectedGatewayId && accessibleGateways?.length
        ? `/${getGatewayName(selectedGatewayId)}`
        : "/default";
    router.replace(dashboardHref);
  }, [selectedGatewayId, accessibleGateways, getGatewayName, router]);

  return null;
}
