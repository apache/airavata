"use client";

import Link from "next/link";
import { ShieldX, Home } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useGateway } from "@/contexts/GatewayContext";

export default function NoPermissionsPage() {
  const { dashboardHref } = useGateway();

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
            You don't have permission to access this resource.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-2 mb-6">
            <p className="text-sm text-muted-foreground text-center">
              This page requires administrator privileges or gateway access that you don't currently have.
            </p>
          </div>
          <div className="flex justify-center gap-3">
            <Button asChild>
              <Link href={dashboardHref}>
                <Home className="h-4 w-4 mr-2" />
                Go to Dashboard
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
