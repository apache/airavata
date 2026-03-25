"use client";

import { useSession } from "next-auth/react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { useQuery } from "@tanstack/react-query";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { gatewaysApi } from "@/lib/api";
import { Skeleton } from "@/components/ui/skeleton";

export default function AdminSettingsPage() {
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  const { data: gateways, isLoading } = useQuery({
    queryKey: ["gateways"],
    queryFn: () => gatewaysApi.list(),
  });

  const currentGateway = gateways?.find((g) => g.gatewayId === gatewayId);

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Admin Settings</h1>
        <p className="text-muted-foreground">
          Configure system-wide settings and gateway preferences
        </p>
      </div>

      <div className="grid gap-6">
        {/* Current Gateway Info */}
        <Card>
          <CardHeader>
            <CardTitle>Current Gateway</CardTitle>
            <CardDescription>
              Information about the currently selected gateway
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {isLoading ? (
              <Skeleton className="h-20 w-full" />
            ) : currentGateway ? (
              <>
                <div className="grid gap-2">
                  <Label>Gateway ID</Label>
                  <Input value={currentGateway.gatewayId} disabled />
                </div>
                <div className="grid gap-2">
                  <Label>Gateway Name</Label>
                  <Input value={currentGateway.gatewayName} disabled />
                </div>
                {currentGateway.domain && (
                  <div className="grid gap-2">
                    <Label>Domain</Label>
                    <Input value={currentGateway.domain} disabled />
                  </div>
                )}
                {currentGateway.gatewayAdminEmail && (
                  <div className="grid gap-2">
                    <Label>Admin Email</Label>
                    <Input value={currentGateway.gatewayAdminEmail} disabled />
                  </div>
                )}
                {currentGateway.gatewayApprovalStatus && (
                  <div className="flex items-center gap-2">
                    <Label>Approval Status</Label>
                    <Badge variant="secondary">{currentGateway.gatewayApprovalStatus}</Badge>
                  </div>
                )}
              </>
            ) : (
              <p className="text-muted-foreground">No gateway information available</p>
            )}
          </CardContent>
        </Card>

        {/* System Configuration */}
        <Card>
          <CardHeader>
            <CardTitle>System Configuration</CardTitle>
            <CardDescription>
              Global system settings and preferences
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label>Email Notifications</Label>
                <p className="text-sm text-muted-foreground">
                  Enable email notifications for system events
                </p>
              </div>
              <Switch defaultChecked />
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label>Auto-approve Experiments</Label>
                <p className="text-sm text-muted-foreground">
                  Automatically approve experiment submissions
                </p>
              </div>
              <Switch />
            </div>
            <Separator />
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label>Resource Monitoring</Label>
                <p className="text-sm text-muted-foreground">
                  Enable real-time resource monitoring
                </p>
              </div>
              <Switch defaultChecked />
            </div>
          </CardContent>
        </Card>

        {/* API Configuration */}
        <Card>
          <CardHeader>
            <CardTitle>API Configuration</CardTitle>
            <CardDescription>
              Backend API endpoint settings
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-2">
              <Label>API Base URL</Label>
              <Input
                value="Proxied via this application"
                disabled
              />
              <p className="text-xs text-muted-foreground">
                Configured via environment variable
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Actions */}
        <div className="flex justify-end">
          <Button>Save Settings</Button>
        </div>
      </div>
    </div>
  );
}
