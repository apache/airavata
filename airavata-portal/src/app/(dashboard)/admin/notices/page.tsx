"use client";

import { useState } from "react";
import { Plus, Pencil, Trash2, Bell } from "lucide-react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { noticesApi, type Notice } from "@/lib/api/notices";
import { formatDate } from "@/lib/utils";
import { toast } from "@/hooks/useToast";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";

export default function NoticesPage() {
  const { effectiveGatewayId } = useGateway();
  const queryClient = useQueryClient();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = effectiveGatewayId || defaultGatewayId;
  
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    notificationMessage: "",
    priority: "NORMAL" as "LOW" | "NORMAL" | "HIGH",
  });

  const { data: notices, isLoading, error, isError } = useQuery({
    queryKey: ["notices", gatewayId],
    queryFn: () => noticesApi.list(gatewayId),
    enabled: !!gatewayId,
    retry: 1,
  });

  // Debug logging
  console.log("[Notices] gatewayId:", gatewayId, "isLoading:", isLoading, "isError:", isError, "notices:", notices);

  const createNotice = useMutation({
    mutationFn: (notice: Partial<Notice>) => noticesApi.create(notice),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notices"] });
      toast({ title: "Notice created", description: "The notice has been published." });
      setIsCreateOpen(false);
      setFormData({ title: "", notificationMessage: "", priority: "NORMAL" });
    },
  });

  const deleteNotice = useMutation({
    mutationFn: (noticeId: string) => noticesApi.delete(noticeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notices"] });
      toast({ title: "Notice deleted", description: "The notice has been removed." });
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
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <Bell className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Notices</h1>
            <p className="text-muted-foreground">Manage announcements for users</p>
          </div>
        </div>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          New Notice
        </Button>
      </div>

      {isError && (
        <Card className="border-destructive">
          <CardContent className="py-6 text-center text-destructive">
            Error loading notices: {error instanceof Error ? error.message : "Unknown error"}
          </CardContent>
        </Card>
      )}

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
                      <Badge variant={notice.priority === "HIGH" ? "destructive" : "secondary"}>
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
                <p className="whitespace-pre-wrap">{notice.notificationMessage}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : !isError ? (
        <Card>
          <CardContent className="py-16 text-center">
            <Bell className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
            <p className="text-muted-foreground">No notices. Create one to announce updates to users.</p>
          </CardContent>
        </Card>
      ) : null}

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
                onChange={(e) => setFormData((prev) => ({ ...prev, title: e.target.value }))}
                placeholder="Enter notice title"
              />
            </div>
            <div className="space-y-2">
              <Label>Message *</Label>
              <Textarea
                value={formData.notificationMessage}
                onChange={(e) => setFormData((prev) => ({ ...prev, notificationMessage: e.target.value }))}
                placeholder="Enter notice message"
                rows={4}
              />
            </div>
            <div className="space-y-2">
              <Label>Priority</Label>
              <Select
                value={formData.priority}
                onValueChange={(value) => setFormData((prev) => ({ ...prev, priority: value as any }))}
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
              <Button type="button" variant="outline" onClick={() => setIsCreateOpen(false)}>
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
