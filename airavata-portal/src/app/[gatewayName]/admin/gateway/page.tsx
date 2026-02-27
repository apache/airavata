"use client";

import { useState } from "react";
import { useParams, useSearchParams } from "next/navigation";
import { Plus, Trash2, Bell } from "lucide-react";
import { useSession } from "next-auth/react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

// UI primitives
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { SearchBar } from "@/components/ui/search-bar";

// Feature components
import { GatewayStatistics } from "@/components/statistics/GatewayStatistics";
import { UserList } from "@/components/users/UserList";

// Contexts & hooks
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { useGateway } from "@/contexts/GatewayContext";
import { useUsers } from "@/hooks/useUsers";
import { toast } from "@/hooks/useToast";

// API
import { gatewaysApi } from "@/lib/api";
import { noticesApi, type Notice } from "@/lib/api/notices";
import { formatDate } from "@/lib/utils";

// ---------------------------------------------------------------------------
// Settings tab
// ---------------------------------------------------------------------------

function SettingsTab() {
  const { data: session } = useSession();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;

  const { data: gateways, isLoading } = useQuery({
    queryKey: ["gateways"],
    queryFn: () => gatewaysApi.list(),
  });

  const currentGateway = gateways?.find((g) => g.gatewayId === gatewayId);

  return (
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
                  <Badge variant="secondary">
                    {currentGateway.gatewayApprovalStatus}
                  </Badge>
                </div>
              )}
            </>
          ) : (
            <p className="text-muted-foreground">
              No gateway information available
            </p>
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
          <CardDescription>Backend API endpoint settings</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-2">
            <Label>API Base URL</Label>
            <Input value="Proxied via this application" disabled />
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
  );
}

// ---------------------------------------------------------------------------
// Users tab
// ---------------------------------------------------------------------------

function UsersTab() {
  const [searchTerm, setSearchTerm] = useState("");
  const { data: users, isLoading } = useUsers();

  const filteredUsers = users?.filter(
    (user) =>
      user.userId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      `${user.firstName} ${user.lastName}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-4">
      <SearchBar
        placeholder="Search users by name, username, or email..."
        value={searchTerm}
        onChange={setSearchTerm}
      />
      <UserList users={filteredUsers} isLoading={isLoading} />
    </div>
  );
}

// ---------------------------------------------------------------------------
// Notices tab
// ---------------------------------------------------------------------------

function NoticesTab() {
  const { effectiveGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;
  const queryClient = useQueryClient();

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    notificationMessage: "",
    priority: "NORMAL" as "LOW" | "NORMAL" | "HIGH",
  });

  const {
    data: notices,
    isLoading,
    error,
    isError,
  } = useQuery({
    queryKey: ["notices", gatewayId],
    queryFn: () => noticesApi.list(gatewayId),
    enabled: !!gatewayId,
    retry: 1,
  });

  const createNotice = useMutation({
    mutationFn: (notice: Partial<Notice>) => noticesApi.create(notice),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notices"] });
      toast({
        title: "Notice created",
        description: "The notice has been published.",
      });
      setIsCreateOpen(false);
      setFormData({ title: "", notificationMessage: "", priority: "NORMAL" });
    },
  });

  const deleteNotice = useMutation({
    mutationFn: (noticeId: string) => noticesApi.delete(noticeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notices"] });
      toast({
        title: "Notice deleted",
        description: "The notice has been removed.",
      });
    },
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    createNotice.mutate({
      ...formData,
      gatewayId,
      publishedTime: Date.now(),
    });
  };

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex justify-end">
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          New Notice
        </Button>
      </div>

      {/* Error state */}
      {isError && (
        <Card className="border-destructive">
          <CardContent className="py-6 text-center text-destructive">
            Error loading notices:{" "}
            {error instanceof Error ? error.message : "Unknown error"}
          </CardContent>
        </Card>
      )}

      {/* Notice list */}
      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      ) : !isError && notices && notices.length > 0 ? (
        <div className="space-y-3">
          {notices.map((notice) => (
            <Card key={notice.notificationId}>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <CardTitle>{notice.title}</CardTitle>
                      <Badge
                        variant={
                          notice.priority === "HIGH" ? "destructive" : "secondary"
                        }
                      >
                        {notice.priority}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground mt-1">
                      {formatDate(notice.publishedTime)}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-destructive"
                    onClick={() => deleteNotice.mutate(notice.notificationId)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <p className="whitespace-pre-wrap">
                  {notice.notificationMessage}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : !isError ? (
        <Card>
          <CardContent className="py-16 text-center">
            <Bell className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
            <p className="text-muted-foreground">
              No notices. Create one to announce updates to users.
            </p>
          </CardContent>
        </Card>
      ) : null}

      {/* Create dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Notice</DialogTitle>
            <DialogDescription>Publish a new announcement</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="space-y-2">
              <Label>Title *</Label>
              <Input
                value={formData.title}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, title: e.target.value }))
                }
                placeholder="Enter notice title"
              />
            </div>
            <div className="space-y-2">
              <Label>Message *</Label>
              <Textarea
                value={formData.notificationMessage}
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    notificationMessage: e.target.value,
                  }))
                }
                placeholder="Enter notice message"
                rows={4}
              />
            </div>
            <div className="space-y-2">
              <Label>Priority</Label>
              <Select
                value={formData.priority}
                onValueChange={(value) =>
                  setFormData((prev) => ({
                    ...prev,
                    priority: value as "LOW" | "NORMAL" | "HIGH",
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LOW">Low</SelectItem>
                  <SelectItem value="NORMAL">Normal</SelectItem>
                  <SelectItem value="HIGH">High</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex justify-end gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsCreateOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={createNotice.isPending}>
                {createNotice.isPending ? "Creating..." : "Create"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

export default function MyGatewayPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const gatewayName = params.gatewayName as string;
  const defaultTab = searchParams.get("tab") || "overview";

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">My Gateway</h1>
        <p className="text-muted-foreground">
          Manage your gateway configuration, users, and announcements
        </p>
      </div>

      <Tabs defaultValue={defaultTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="settings">Settings</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="notices">Notices</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <GatewayStatistics
            gatewayId={gatewayName}
            showGatewayHeader={false}
          />
        </TabsContent>

        <TabsContent value="settings">
          <SettingsTab />
        </TabsContent>

        <TabsContent value="users">
          <UsersTab />
        </TabsContent>

        <TabsContent value="notices">
          <NoticesTab />
        </TabsContent>
      </Tabs>
    </div>
  );
}
