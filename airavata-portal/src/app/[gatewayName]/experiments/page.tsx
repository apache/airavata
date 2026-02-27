"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect } from "react";

export default function GatewayExperimentsPage() {
  const params = useParams();
  const router = useRouter();
  const gatewayName = (params?.gatewayName as string) || "default";

  useEffect(() => {
    router.replace(`/${gatewayName}`);
  }, [gatewayName, router]);

  return null;
}
