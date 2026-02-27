"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { User, Mail, Building, Shield, Bell, Settings } from "lucide-react";
import { toast } from "@/hooks/useToast";

export default function AccountPage() {
  const { data: session } = useSession();
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [experimentAlerts, setExperimentAlerts] = useState(true);

  const getInitials = (name?: string | null) => {
    if (!name) return "U";
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2);
  };

  const handleSaveSettings = () => {
    toast({
      title: "Settings saved",
      description: "Your preferences have been updated successfully.",
    });
  };

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Account</h1>
        <p className="text-muted-foreground">
          Manage your profile and account settings
        </p>
      </div>

      {/* Profile Header */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Avatar className="h-20 w-20">
              <AvatarImage src={session?.user?.image || ""} alt={session?.user?.name || ""} />
              <AvatarFallback className="text-2xl">
                {getInitials(session?.user?.name)}
              </AvatarFallback>
            </Avatar>
            <div className="space-y-1">
              <CardTitle className="text-2xl">{session?.user?.name || "User"}</CardTitle>
              <CardDescription className="text-base flex items-center gap-2">
                <Mail className="h-4 w-4" />
                {session?.user?.email}
              </CardDescription>
              <div className="flex items-center gap-2">
                <Badge variant="secondary" className="flex items-center gap-1">
                  <Building className="h-3 w-3" />
                  {session?.user?.gatewayId || "default"}
                </Badge>
                <Badge variant="default" className="bg-green-500">Active</Badge>
              </div>
            </div>
          </div>
        </CardHeader>
      </Card>

      <Tabs defaultValue="profile" className="space-y-4">
        <TabsList>
          <TabsTrigger value="profile" className="flex items-center gap-2">
            <User className="h-4 w-4" />
            Profile
          </TabsTrigger>
          <TabsTrigger value="notifications" className="flex items-center gap-2">
            <Bell className="h-4 w-4" />
            Notifications
          </TabsTrigger>
          <TabsTrigger value="session" className="flex items-center gap-2">
            <Shield className="h-4 w-4" />
            Session
          </TabsTrigger>
        </TabsList>

        {/* Profile Tab */}
        <TabsContent value="profile" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="h-5 w-5" />
                Account Details
              </CardTitle>
              <CardDescription>
                Your account information from your identity provider
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-2">
                <Label>Username</Label>
                <Input value={session?.user?.userName || ""} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Full Name</Label>
                <Input value={session?.user?.name || ""} disabled />
              </div>
              <div className="grid gap-2">
                <Label>Email</Label>
                <Input value={session?.user?.email || ""} disabled />
              </div>
              <p className="text-sm text-muted-foreground">
                Profile information is managed by your identity provider and cannot be changed here.
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building className="h-5 w-5" />
                Gateway Access
              </CardTitle>
              <CardDescription>
                Information about your gateway membership
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-2">
                <Label>Current Gateway</Label>
                <Input value={session?.user?.gatewayId || "default"} disabled />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Access Status</span>
                <Badge variant="default" className="bg-green-500">Active</Badge>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Notifications Tab */}
        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Bell className="h-5 w-5" />
                Notification Preferences
              </CardTitle>
              <CardDescription>
                Configure how you receive notifications
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Email Notifications</Label>
                  <p className="text-sm text-muted-foreground">
                    Receive email updates about your experiments
                  </p>
                </div>
                <Switch
                  checked={emailNotifications}
                  onCheckedChange={setEmailNotifications}
                />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>Experiment Alerts</Label>
                  <p className="text-sm text-muted-foreground">
                    Get notified when experiments complete or fail
                  </p>
                </div>
                <Switch
                  checked={experimentAlerts}
                  onCheckedChange={setExperimentAlerts}
                />
              </div>
            </CardContent>
          </Card>

          <div className="flex justify-end">
            <Button onClick={handleSaveSettings}>Save Preferences</Button>
          </div>
        </TabsContent>

        {/* Session Tab */}
        <TabsContent value="session" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                Session Information
              </CardTitle>
              <CardDescription>
                Details about your current authentication session
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>Session Status</Label>
                  <div className="flex items-center gap-2">
                    <Badge variant="default" className="bg-green-500">
                      {session ? "Active" : "Not authenticated"}
                    </Badge>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label>Authentication Provider</Label>
                  <p className="text-sm font-medium">Keycloak</p>
                </div>
              </div>
              <Separator />
              <div className="space-y-2">
                <Label>Username</Label>
                <Input value={session?.user?.userName || ""} disabled />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
