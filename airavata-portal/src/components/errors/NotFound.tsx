"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FileQuestion, Home, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useGateway } from "@/contexts/GatewayContext";

interface NotFoundProps {
  resourceType: "dataset" | "repository" | "application" | "experiment";
  resourceId: string;
}

export function NotFound({ resourceType, resourceId }: NotFoundProps) {
  const router = useRouter();
  const { dashboardHref } = useGateway();
  const resourceTypeLabel = resourceType.charAt(0).toUpperCase() + resourceType.slice(1);

  return (
    <div className="flex items-center justify-center min-h-screen p-4">
      <Card className="max-w-md w-full">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-blue-100 rounded-full">
              <FileQuestion className="h-8 w-8 text-blue-600" />
            </div>
          </div>
          <CardTitle>{resourceTypeLabel} Not Found</CardTitle>
          <CardDescription>
            The {resourceType} you are looking for does not exist or has been removed.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2 mb-6">
            <p className="text-sm text-muted-foreground text-center">
              No {resourceType} found with ID{" "}
              <code className="text-xs bg-muted px-1 py-0.5 rounded">{resourceId}</code>.
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
