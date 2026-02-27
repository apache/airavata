"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect } from "react";
import { Skeleton } from "@/components/ui/skeleton";

/**
 * Redirect to the gateway's statistics page for a consistent URL structure.
 * The canonical URL for viewing a gateway is /{gatewayName}/admin/gateway
 */
export default function GatewayDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const gatewayName = params.gatewayName as string;

  useEffect(() => {
    // Redirect to the canonical statistics page URL
    router.replace(`/${gatewayName}/admin/gateway`);
  }, [gatewayName, router]);

  // Show loading state while redirecting
  return (
    <div className="space-y-6">
      <Skeleton className="h-10 w-64" />
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Skeleton key={i} className="h-32" />
        ))}
      </div>
    </div>
  );
}
