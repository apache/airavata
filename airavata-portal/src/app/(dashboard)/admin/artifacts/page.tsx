"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import { Plus, Search, Trash2, Eye, Edit } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { SearchBar } from "@/components/ui/search-bar";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
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
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { artifactsApi } from "@/lib/api";
import { toast } from "@/hooks/useToast";
import type { ArtifactModel, ArtifactType } from "@/types";
import { useRouter } from "next/navigation";

export default function ArtifactsPage() {
  const router = useRouter();
  const { data: session } = useSession();
  const queryClient = useQueryClient();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = session?.user?.gatewayId || defaultGatewayId;
  const userId = session?.user?.userName || session?.user?.email || "default-admin";

  const [searchQuery, setSearchQuery] = useState("");
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [viewingArtifact, setViewingArtifact] = useState<ArtifactModel | null>(null);
  const [deletingArtifact, setDeletingArtifact] = useState<ArtifactModel | null>(null);
  const [newArtifact, setNewArtifact] = useState<Partial<ArtifactModel>>({
    gatewayId,
    ownerName: userId,
    ownerId: userId,
    name: "",
    description: "",
    artifactType: "DATASET" as ArtifactType,
    privacy: "PRIVATE",
    scope: "USER",
  });

  const { data: artifacts, isLoading, refetch } = useQuery({
    queryKey: ["artifacts", gatewayId, userId, searchQuery],
    queryFn: () => {
      return artifactsApi.search({
        gatewayId,
        userId,
        name: searchQuery.trim() || "",
        limit: 100,
        offset: 0,
      });
    },
    enabled: !!gatewayId && !!userId,
  });

  const createMutation = useMutation({
    mutationFn: (artifact: Partial<ArtifactModel>) => artifactsApi.create(artifact),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
      toast({
        title: "Artifact created",
        description: "Artifact has been created successfully.",
      });
      setIsCreateOpen(false);
      setNewArtifact({
        gatewayId,
        ownerName: userId,
        ownerId: userId,
        name: "",
        description: "",
        artifactType: "DATASET" as ArtifactType,
        privacy: "PRIVATE",
        scope: "USER",
      });
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to create artifact",
        variant: "destructive",
      });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ artifactUri, artifact }: { artifactUri: string; artifact: Partial<ArtifactModel> }) =>
      artifactsApi.update(artifactUri, artifact),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
      toast({
        title: "Artifact updated",
        description: "Artifact has been updated successfully.",
      });
      setViewingArtifact(null);
      refetch();
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to update artifact",
        variant: "destructive",
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (artifactUri: string) => artifactsApi.delete(artifactUri),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
      toast({
        title: "Artifact deleted",
        description: "Artifact has been deleted successfully.",
      });
      setDeletingArtifact(null);
      refetch();
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message || "Failed to delete artifact",
        variant: "destructive",
      });
    },
  });

  const handleSearch = () => {
    refetch();
  };

  const handleCreate = () => {
    if (!newArtifact.name?.trim()) {
      toast({
        title: "Validation error",
        description: "Artifact name is required",
        variant: "destructive",
      });
      return;
    }
    createMutation.mutate(newArtifact);
  };

  const handleDelete = () => {
    if (!deletingArtifact) return;
    deleteMutation.mutate(deletingArtifact.artifactUri);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Artifacts</h1>
          <p className="text-muted-foreground">
            Manage artifacts and their replica locations
          </p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          New Artifact
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Search Artifacts</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <SearchBar
              placeholder="Search by artifact name (leave empty to list all)..."
              value={searchQuery}
              onChange={setSearchQuery}
              onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              wrapperClassName="flex-1"
            />
            <Button onClick={handleSearch}>
              <Search className="mr-2 h-4 w-4" />
              Search
            </Button>
          </div>
        </CardContent>
      </Card>

      {isLoading ? (
        <Skeleton className="h-96 w-full" />
      ) : artifacts && artifacts.length > 0 ? (
        <Card>
          <div className="border rounded-lg overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="h-9 px-3">Name</TableHead>
                  <TableHead className="h-9 px-3">Type</TableHead>
                  <TableHead className="h-9 px-3">Owner</TableHead>
                  <TableHead className="h-9 px-3">Primary Storage</TableHead>
                  <TableHead className="h-9 px-3">Privacy</TableHead>
                  <TableHead className="h-9 px-3">Size</TableHead>
                  <TableHead className="h-9 px-3">Replicas</TableHead>
                  <TableHead className="h-9 px-3 text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {artifacts.map((artifact) => (
                  <TableRow key={artifact.artifactUri}>
                    <TableCell className="py-1.5 px-3 font-medium">{artifact.name}</TableCell>
                    <TableCell className="py-1.5 px-3">
                      <Badge variant="secondary">{artifact.artifactType}</Badge>
                    </TableCell>
                    <TableCell className="py-1.5 px-3">{artifact.ownerName}</TableCell>
                    <TableCell className="py-1.5 px-3">
                      {artifact.primaryStorageResourceId && artifact.primaryFilePath
                        ? `${artifact.primaryStorageResourceId}: ...${String(artifact.primaryFilePath).slice(-20)}`
                        : "—"}
                    </TableCell>
                    <TableCell className="py-1.5 px-3">
                      <Badge variant={artifact.privacy === "PUBLIC" ? "default" : "secondary"}>
                        {artifact.privacy ?? "PRIVATE"}
                      </Badge>
                    </TableCell>
                    <TableCell className="py-1.5 px-3">
                      {artifact.size
                        ? `${(artifact.size / 1024 / 1024).toFixed(2)} MB`
                        : "N/A"}
                    </TableCell>
                    <TableCell className="py-1.5 px-3">{artifact.replicaLocations?.length || 0}</TableCell>
                    <TableCell className="py-1.5 px-3 text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setViewingArtifact(artifact)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => router.push(`/admin/artifacts/${encodeURIComponent(artifact.artifactUri)}`)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-destructive hover:text-destructive"
                          onClick={() => setDeletingArtifact(artifact)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </Card>
      ) : (
        <Card>
          <CardContent className="py-16 text-center text-muted-foreground">
            {searchQuery
              ? "No artifacts found. Try a different search term."
              : "No artifacts found. Create a new artifact or search for existing ones."}
          </CardContent>
        </Card>
      )}

      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Create Artifact</DialogTitle>
            <DialogDescription>
              Create a new artifact entry
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium">Name *</label>
              <Input
                value={newArtifact.name || ""}
                onChange={(e) => setNewArtifact({ ...newArtifact, name: e.target.value })}
                placeholder="Enter artifact name"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Description</label>
              <textarea
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={newArtifact.description || ""}
                onChange={(e) => setNewArtifact({ ...newArtifact, description: e.target.value })}
                placeholder="Enter description"
                rows={3}
              />
            </div>
            <div>
              <label className="text-sm font-medium">Type *</label>
              <select
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={newArtifact.artifactType || "DATASET"}
                onChange={(e) => setNewArtifact({ ...newArtifact, artifactType: e.target.value as ArtifactType })}
              >
                <option value="DATASET">Dataset</option>
                <option value="REPOSITORY">Repository</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium">Primary Storage Resource ID</label>
              <Input
                value={newArtifact.primaryStorageResourceId || ""}
                onChange={(e) => setNewArtifact({ ...newArtifact, primaryStorageResourceId: e.target.value || undefined })}
                placeholder="Storage resource where main copy lives"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Primary File Path</label>
              <Input
                value={newArtifact.primaryFilePath || ""}
                onChange={(e) => setNewArtifact({ ...newArtifact, primaryFilePath: e.target.value || undefined })}
                placeholder="Path on primary storage"
              />
            </div>
            <div>
              <label className="text-sm font-medium">Privacy</label>
              <select
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={newArtifact.privacy || "PRIVATE"}
                onChange={(e) => setNewArtifact({ ...newArtifact, privacy: e.target.value })}
              >
                <option value="PRIVATE">Private</option>
                <option value="PUBLIC">Public</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium">Scope</label>
              <select
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={newArtifact.scope || "USER"}
                onChange={(e) => setNewArtifact({ ...newArtifact, scope: e.target.value })}
              >
                <option value="USER">User</option>
                <option value="GATEWAY">Gateway</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium">Format</label>
              <Input
                value={newArtifact.format || ""}
                onChange={(e) => setNewArtifact({ ...newArtifact, format: e.target.value || undefined })}
                placeholder="e.g. CSV, HDF5"
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreate} disabled={createMutation.isPending}>
                {createMutation.isPending ? "Creating..." : "Create"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={!!viewingArtifact} onOpenChange={(open) => !open && setViewingArtifact(null)}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{viewingArtifact?.name}</DialogTitle>
            <DialogDescription>Artifact details</DialogDescription>
          </DialogHeader>
          {viewingArtifact && (
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Artifact URI</p>
                <p className="font-mono text-sm break-all">{viewingArtifact.artifactUri}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Type</p>
                <Badge>{viewingArtifact.artifactType}</Badge>
              </div>
              {(viewingArtifact.primaryStorageResourceId || viewingArtifact.primaryFilePath) && (
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Primary Storage</p>
                  <Card>
                    <CardContent className="pt-4">
                      {viewingArtifact.primaryStorageResourceId && (
                        <p className="font-medium">{viewingArtifact.primaryStorageResourceId}</p>
                      )}
                      {viewingArtifact.primaryFilePath && (
                        <p className="text-sm text-muted-foreground font-mono">{viewingArtifact.primaryFilePath}</p>
                      )}
                    </CardContent>
                  </Card>
                </div>
              )}
              {(viewingArtifact.privacy || viewingArtifact.scope) && (
                <div className="flex gap-4">
                  {viewingArtifact.privacy && (
                    <div>
                      <p className="text-sm text-muted-foreground">Privacy</p>
                      <Badge variant="secondary">{viewingArtifact.privacy}</Badge>
                    </div>
                  )}
                  {viewingArtifact.scope && (
                    <div>
                      <p className="text-sm text-muted-foreground">Scope</p>
                      <Badge variant="secondary">{viewingArtifact.scope}</Badge>
                    </div>
                  )}
                </div>
              )}
              {viewingArtifact.format && (
                <div>
                  <p className="text-sm text-muted-foreground">Format</p>
                  <p>{viewingArtifact.format}</p>
                </div>
              )}
              {viewingArtifact.authors && viewingArtifact.authors.length > 0 && (
                <div>
                  <p className="text-sm text-muted-foreground">Authors</p>
                  <p>{viewingArtifact.authors.join(", ")}</p>
                </div>
              )}
              {viewingArtifact.tags && viewingArtifact.tags.length > 0 && (
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Tags</p>
                  <div className="flex flex-wrap gap-1">
                    {viewingArtifact.tags.map((t, i) => (
                      <Badge key={i} variant="outline">{t.name ?? t.id ?? ""}</Badge>
                    ))}
                  </div>
                </div>
              )}
              {viewingArtifact.description && (
                <div>
                  <p className="text-sm text-muted-foreground">Description</p>
                  <p>{viewingArtifact.description}</p>
                </div>
              )}
              {viewingArtifact.replicaLocations && viewingArtifact.replicaLocations.length > 0 && (
                <div>
                  <p className="text-sm text-muted-foreground mb-2">Replica Locations</p>
                  <div className="space-y-2">
                    {viewingArtifact.replicaLocations.map((replica, index) => (
                      <Card key={index}>
                        <CardContent className="pt-4">
                          <p className="font-medium">{replica.storageResourceId}</p>
                          {replica.filePath && (
                            <p className="text-sm text-muted-foreground font-mono">{replica.filePath}</p>
                          )}
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      <AlertDialog open={!!deletingArtifact} onOpenChange={(open) => !open && setDeletingArtifact(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Artifact</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{deletingArtifact?.name}"? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground">
              {deleteMutation.isPending ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
