"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, CheckCircle, XCircle, Trash2, Mail, User as UserIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { useUser, useEnableUser, useDisableUser, useDeleteUser } from "@/hooks/useUsers";
import { formatDate } from "@/lib/utils";
import { toast } from "@/hooks/useToast";
import { useRouter } from "next/navigation";
import { useGateway } from "@/contexts/GatewayContext";

export default function UserDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { selectedGatewayId, getGatewayName } = useGateway();
  const gatewayName = selectedGatewayId ? getGatewayName(selectedGatewayId) : "default";
  const userId = params.userId as string;

  const { data: user, isLoading } = useUser(userId);
  const enableUser = useEnableUser();
  const disableUser = useDisableUser();
  const deleteUser = useDeleteUser();

  const handleEnable = async () => {
    try {
      await enableUser.mutateAsync(userId);
      toast({
        title: "User enabled",
        description: "The user account has been activated.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to enable user",
        variant: "destructive",
      });
    }
  };

  const handleDisable = async () => {
    if (!confirm("Are you sure you want to disable this user?")) {
      return;
    }
    try {
      await disableUser.mutateAsync(userId);
      toast({
        title: "User disabled",
        description: "The user account has been deactivated.",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to disable user",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async () => {
    if (!confirm("Are you sure you want to delete this user? This action cannot be undone.")) {
      return;
    }
    try {
      await deleteUser.mutateAsync(userId);
      toast({
        title: "User deleted",
        description: "The user account has been deleted.",
      });
      router.push(`/${gatewayName}/admin/gateway?tab=users`);
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to delete user",
        variant: "destructive",
      });
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10" />
          <Skeleton className="h-8 w-64" />
        </div>
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (!user) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <h2 className="text-xl font-semibold">User not found</h2>
        <p className="text-muted-foreground mt-2">The requested user could not be found.</p>
        <Button asChild className="mt-4">
          <Link href={`/${gatewayName}/admin/gateway?tab=users`}>Back to Users</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={`/${gatewayName}/admin/gateway?tab=users`}>
              <ArrowLeft className="h-5 w-5" />
            </Link>
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold tracking-tight">
                {user.firstName} {user.lastName}
              </h1>
              {user.enabled ? (
                <Badge variant="default" className="gap-1">
                  <CheckCircle className="h-3 w-3" />
                  Active
                </Badge>
              ) : (
                <Badge variant="secondary" className="gap-1">
                  <XCircle className="h-3 w-3" />
                  Disabled
                </Badge>
              )}
            </div>
            <p className="text-muted-foreground">{user.email}</p>
          </div>
        </div>
        <div className="flex gap-2">
          {user.enabled ? (
            <Button
              variant="outline"
              onClick={handleDisable}
              disabled={disableUser.isPending}
            >
              <XCircle className="mr-2 h-4 w-4" />
              Disable
            </Button>
          ) : (
            <Button
              variant="outline"
              onClick={handleEnable}
              disabled={enableUser.isPending}
            >
              <CheckCircle className="mr-2 h-4 w-4" />
              Enable
            </Button>
          )}
          <Button
            variant="destructive"
            onClick={handleDelete}
            disabled={deleteUser.isPending}
          >
            <Trash2 className="mr-2 h-4 w-4" />
            Delete
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>User Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div>
              <p className="text-sm text-muted-foreground">Username</p>
              <p className="font-medium">{user.userId}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Email</p>
              <div className="flex items-center gap-2">
                <p className="font-medium">{user.email}</p>
                {user.emailVerified && (
                  <Badge variant="outline" className="gap-1">
                    <Mail className="h-3 w-3" />
                    Verified
                  </Badge>
                )}
              </div>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Gateway</p>
              <p className="font-medium">{user.gatewayId}</p>
            </div>
            {user.createdTime && (
              <div>
                <p className="text-sm text-muted-foreground">Created</p>
                <p className="font-medium">{formatDate(user.createdTime)}</p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Groups</CardTitle>
            <CardDescription>Groups this user belongs to</CardDescription>
          </CardHeader>
          <CardContent>
            {user.groups && user.groups.length > 0 ? (
              <div className="space-y-2">
                {user.groups.map((group) => (
                  <Badge key={group} variant="secondary">
                    {group}
                  </Badge>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Not a member of any groups</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
