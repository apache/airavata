"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { ShieldX, Home, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useGateway } from "@/contexts/GatewayContext";

interface NoPermissionsProps {
  resourceType: "dataset" | "repository" | "application" | "experiment";
  resourceId: string;
}

export function NoPermissions({ resourceType, resourceId }: NoPermissionsProps) {
  const router = useRouter();
  const { dashboardHref } = useGateway();
  const resourceTypeLabel = resourceType.charAt(0).toUpperCase() + resourceType.slice(1);

  return (
    <div className="flex items-center justify-center min-h-screen p-4">
      <Card className="max-w-md w-full">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-red-100 rounded-full">
              <ShieldX className="h-8 w-8 text-red-600" />
            </div>
          </div>
          <CardTitle>Access Denied</CardTitle>
          <CardDescription>
            You don't have permission to access this {resourceType}.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2 mb-6">
            <p className="text-sm text-muted-foreground text-center">
              <span className="font-medium">{resourceTypeLabel}</span> with ID{" "}
              <code className="text-xs bg-muted px-1 py-0.5 rounded">{resourceId}</code> exists,
              but you don't have the required permissions to view it.
            </p>
          </div>
          <div className="flex justify-center gap-3">
            <Button variant="outline" onClick={() => router.back()}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Go Back
            </Button>
            <Button asChild>
              <Link href={dashboardHref}>
                <Home className="h-4 w-4 mr-2" />
                Dashboard
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
