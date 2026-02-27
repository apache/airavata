"use client";

import { useParams } from "next/navigation";
import { GatewayStatistics } from "@/components/statistics/GatewayStatistics";

export default function GatewayStatisticsPage() {
  const params = useParams();
  const gatewayName = params.gatewayName as string;

  // Use the gateway name from the URL as the gateway ID
  // The statistics component will fetch gateway details and statistics for this gateway
  return (
    <GatewayStatistics 
      gatewayId={gatewayName}
      showGatewayHeader={false}
    />
  );
}
