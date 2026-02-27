"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Server, Copy, Cpu, Database, Trash2, MoreVertical } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { resourcesApi } from "@/lib/api/resources";
import { toast } from "@/hooks/useToast";
import type { Resource } from "@/types";

const emptyCreateForm = {
  name: "",
  hostName: "",
  port: "22",
  description: "",
  hasCompute: false,
  computeType: "FORK" as "SLURM" | "FORK",
  hasStorage: false,
  storageProtocol: "SFTP" as "SFTP" | "SCP",
};

export default function ResourcesPage() {
  const params = useParams();
  const gatewayId = params.gatewayName as string;
  const queryClient = useQueryClient();

  const [searchTerm, setSearchTerm] = useState("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [deletingResourceId, setDeletingResourceId] = useState<string | null>(null);
  const [formData, setFormData] = useState(emptyCreateForm);

  const { data: resources = [], isLoading } = useQuery({
    queryKey: ["resources", gatewayId],
    queryFn: () => resourcesApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const createMutation = useMutation({
    mutationFn: (resource: Partial<Resource>) => resourcesApi.create(resource),
    onSuccess: () => {
      toast({ title: "Resource created" });
      queryClient.invalidateQueries({ queryKey: ["resources", gatewayId] });
      setIsCreateOpen(false);
      setFormData(emptyCreateForm);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to create resource",
        description: err?.message ?? "An error occurred.",
        variant: "destructive",
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (resourceId: string) => resourcesApi.delete(resourceId),
    onSuccess: () => {
      toast({ title: "Resource deleted" });
      queryClient.invalidateQueries({ queryKey: ["resources", gatewayId] });
      setDeletingResourceId(null);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to delete resource",
        description: err?.message ?? "An error occurred.",
        variant: "destructive",
      });
    },
  });

  const filteredResources = resources.filter((r) => {
    const term = searchTerm.toLowerCase();
    return (
      r.name.toLowerCase().includes(term) ||
      r.hostName.toLowerCase().includes(term) ||
      r.resourceId.toLowerCase().includes(term)
    );
  });

  const handleCreate = () => {
    const port = parseInt(formData.port, 10);
    const resource: Partial<Resource> = {
      gatewayId,
      name: formData.name.trim(),
      hostName: formData.hostName.trim(),
      port: isNaN(port) ? 22 : port,
      description: formData.description.trim() || undefined,
      capabilities: {
        ...(formData.hasCompute ? { compute: { type: formData.computeType } } : {}),
        ...(formData.hasStorage ? { storage: { protocol: formData.storageProtocol } } : {}),
      },
    };
    createMutation.mutate(resource);
  };

  const copyResourceId = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(id);
    toast({ title: "Copied", description: "Resource ID copied to clipboard." });
  };

  const getCapabilityBadges = (resource: Resource) => {
    const badges: React.ReactNode[] = [];
    if (resource.capabilities.compute) {
      badges.push(
        <Badge key="compute" variant="secondary" className="gap-1">
          <Cpu className="h-3 w-3" />
          {resource.capabilities.compute.type}
        </Badge>
      );
    }
    if (resource.capabilities.storage) {
      badges.push(
        <Badge key="storage" variant="outline" className="gap-1">
          <Database className="h-3 w-3" />
          {resource.capabilities.storage.protocol}
        </Badge>
      );
    }
    return badges;
  };

  const deletingResource = resources.find((r) => r.resourceId === deletingResourceId);

  return (
    <div className="space-y-4">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Resources</h1>
          <p className="text-muted-foreground">Manage compute and storage resources</p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          Create Resource
        </Button>
      </div>

      {/* Search */}
      <Input
        placeholder="Search by name, hostname, or ID..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="max-w-sm"
      />

      {/* Resources table */}
      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-3">Name</TableHead>
              <TableHead className="h-9 px-3">Host</TableHead>
              <TableHead className="h-9 px-3">Port</TableHead>
              <TableHead className="h-9 px-3">Capabilities</TableHead>
              <TableHead className="h-9 px-3 w-0" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <>
                {[...Array(4)].map((_, i) => (
                  <TableRow key={i}>
                    <TableCell className="py-1.5 px-3">
                      <Skeleton className="h-4 w-36" />
                    </TableCell>
                    <TableCell className="py-1.5 px-3">
                      <Skeleton className="h-4 w-48" />
                    </TableCell>
                    <TableCell className="py-1.5 px-3">
                      <Skeleton className="h-4 w-12" />
                    </TableCell>
                    <TableCell className="py-1.5 px-3">
                      <Skeleton className="h-4 w-24" />
                    </TableCell>
                    <TableCell className="py-1.5 px-3" />
                  </TableRow>
                ))}
              </>
            ) : filteredResources.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-16 text-center">
                  <Server className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
                  <p className="text-muted-foreground">
                    {searchTerm ? "No resources match your search" : "No resources configured"}
                  </p>
                  {!searchTerm && (
                    <Button
                      variant="outline"
                      className="mt-4"
                      onClick={() => setIsCreateOpen(true)}
                    >
                      <Plus className="mr-2 h-4 w-4" />
                      Create your first resource
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ) : (
              filteredResources.map((resource) => (
                <TableRow key={resource.resourceId} className="hover:bg-muted/50">
                  <TableCell className="py-1.5 px-3 font-medium">
                    <div className="flex items-center gap-2">
                      <span>{resource.name}</span>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 shrink-0 opacity-50 hover:opacity-100"
                        onClick={(e) => copyResourceId(resource.resourceId, e)}
                        title={`Copy ID: ${resource.resourceId}`}
                      >
                        <Copy className="h-3 w-3" />
                      </Button>
                    </div>
                    {resource.description && (
                      <p className="text-xs text-muted-foreground truncate max-w-[240px]">
                        {resource.description}
                      </p>
                    )}
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground font-mono text-sm">
                    {resource.hostName}
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground tabular-nums">
                    {resource.port}
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      {getCapabilityBadges(resource)}
                      {!resource.capabilities.compute && !resource.capabilities.storage && (
                        <span className="text-xs text-muted-foreground">None</span>
                      )}
                    </div>
                  </TableCell>
                  <TableCell
                    className="py-1.5 px-3 w-0"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={(e) => copyResourceId(resource.resourceId, e)}
                        >
                          <Copy className="mr-2 h-4 w-4" />
                          Copy ID
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="text-destructive focus:text-destructive"
                          onClick={() => setDeletingResourceId(resource.resourceId)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Create Resource Dialog */}
      <Dialog
        open={isCreateOpen}
        onOpenChange={(open) => {
          setIsCreateOpen(open);
          if (!open) setFormData(emptyCreateForm);
        }}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Create Resource</DialogTitle>
            <DialogDescription>
              Add a new compute or storage resource to this gateway.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 col-span-2">
                <Label htmlFor="res-name">Name *</Label>
                <Input
                  id="res-name"
                  placeholder="e.g. My HPC Cluster"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, name: e.target.value }))
                  }
                />
              </div>
              <div className="space-y-2 col-span-2 sm:col-span-1">
                <Label htmlFor="res-host">Hostname *</Label>
                <Input
                  id="res-host"
                  placeholder="e.g. cluster.example.edu"
                  value={formData.hostName}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, hostName: e.target.value }))
                  }
                />
              </div>
              <div className="space-y-2 col-span-2 sm:col-span-1">
                <Label htmlFor="res-port">Port</Label>
                <Input
                  id="res-port"
                  type="number"
                  placeholder="22"
                  value={formData.port}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, port: e.target.value }))
                  }
                />
              </div>
              <div className="space-y-2 col-span-2">
                <Label htmlFor="res-description">Description</Label>
                <Input
                  id="res-description"
                  placeholder="Optional description"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, description: e.target.value }))
                  }
                />
              </div>
            </div>

            <div className="space-y-3 border rounded-md p-3">
              <p className="text-sm font-medium">Capabilities</p>
              <div className="space-y-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    className="h-4 w-4"
                    checked={formData.hasCompute}
                    onChange={(e) =>
                      setFormData((prev) => ({ ...prev, hasCompute: e.target.checked }))
                    }
                  />
                  <Cpu className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">Compute</span>
                </label>
                {formData.hasCompute && (
                  <div className="ml-6 space-y-1">
                    <Label className="text-xs text-muted-foreground">Type</Label>
                    <select
                      className="w-full p-1.5 text-sm border rounded"
                      value={formData.computeType}
                      onChange={(e) =>
                        setFormData((prev) => ({
                          ...prev,
                          computeType: e.target.value as "SLURM" | "FORK",
                        }))
                      }
                    >
                      <option value="FORK">FORK</option>
                      <option value="SLURM">SLURM</option>
                    </select>
                  </div>
                )}
              </div>
              <div className="space-y-2">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    className="h-4 w-4"
                    checked={formData.hasStorage}
                    onChange={(e) =>
                      setFormData((prev) => ({ ...prev, hasStorage: e.target.checked }))
                    }
                  />
                  <Database className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">Storage</span>
                </label>
                {formData.hasStorage && (
                  <div className="ml-6 space-y-1">
                    <Label className="text-xs text-muted-foreground">Protocol</Label>
                    <select
                      className="w-full p-1.5 text-sm border rounded"
                      value={formData.storageProtocol}
                      onChange={(e) =>
                        setFormData((prev) => ({
                          ...prev,
                          storageProtocol: e.target.value as "SFTP" | "SCP",
                        }))
                      }
                    >
                      <option value="SFTP">SFTP</option>
                      <option value="SCP">SCP</option>
                    </select>
                  </div>
                )}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleCreate}
              disabled={
                createMutation.isPending ||
                !formData.name.trim() ||
                !formData.hostName.trim()
              }
            >
              {createMutation.isPending ? "Creating..." : "Create Resource"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deletingResourceId}
        onOpenChange={(open) => !open && setDeletingResourceId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Resource</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;{deletingResource?.name ?? deletingResourceId}
              &quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteMutation.isPending}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() =>
                deletingResourceId && deleteMutation.mutate(deletingResourceId)
              }
              disabled={deleteMutation.isPending}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleteMutation.isPending ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
