"use client";

import { useState } from "react";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { X } from "lucide-react";
import { useCatalogTags, useCreateArtifact } from "@/hooks";
import { toast } from "@/hooks/useToast";
import { ArtifactType, Privacy } from "@/types/catalog";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function AddDatasetModal({ open, onOpenChange }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: session } = useSession();
  const { selectedGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = selectedGatewayId || defaultGatewayId;
  const userId = session?.user?.userName || session?.user?.email || "";
  const createArtifact = useCreateArtifact();
  const { data: availableTags } = useCatalogTags();

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    type: ArtifactType.DATASET,
    privacy: Privacy.PUBLIC,
    scope: "USER" as string,
    authors: [] as string[],
    tags: [] as Array<{ id: string; name: string; color?: string }>,
    primaryStorageResourceId: "",
    primaryFilePath: "",
    size: undefined as number | undefined,
    format: "",
  });

  const [newAuthor, setNewAuthor] = useState("");
  const [newTag, setNewTag] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.description || !formData.primaryStorageResourceId || !formData.primaryFilePath) {
      toast({
        title: "Error",
        description: "Name, description, primary storage resource, and primary file path are required",
        variant: "destructive",
      });
      return;
    }

    try {
      const product = {
        name: formData.name,
        description: formData.description,
        gatewayId,
        ownerName: userId,
        ownerId: userId,
        privacy: formData.privacy,
        scope: formData.scope,
        authors: formData.authors,
        tags: formData.tags.map((t) => ({ id: t.id, name: t.name, color: t.color })),
        primaryStorageResourceId: formData.primaryStorageResourceId,
        primaryFilePath: formData.primaryFilePath,
        size: formData.size ?? 0,
        format: formData.format || undefined,
        artifactType: ArtifactType.DATASET,
      };

      const result = await createArtifact.mutateAsync(product);
      queryClient.invalidateQueries({ queryKey: ["artifacts"] });
      queryClient.invalidateQueries({ queryKey: ["artifacts", "public"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-artifacts"] });
      toast({
        title: "Dataset created",
        description: "Your dataset has been created successfully.",
      });
      onOpenChange(false);
      router.push(`/datasets/${result.artifactUri}`);
      setFormData({
        name: "",
        description: "",
        type: ArtifactType.DATASET,
        privacy: Privacy.PUBLIC,
        scope: "USER",
        authors: [],
        tags: [],
        primaryStorageResourceId: "",
        primaryFilePath: "",
        size: undefined,
        format: "",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create dataset",
        variant: "destructive",
      });
    }
  };

  const addAuthor = () => {
    if (newAuthor.trim() && !formData.authors.includes(newAuthor.trim())) {
      setFormData({
        ...formData,
        authors: [...formData.authors, newAuthor.trim()],
      });
      setNewAuthor("");
    }
  };

  const removeAuthor = (author: string) => {
    setFormData({
      ...formData,
      authors: formData.authors.filter((a) => a !== author),
    });
  };

  const addTag = () => {
    if (newTag.trim()) {
      const tagName = newTag.trim();
      const existingTag = formData.tags.find((t) => t.name === tagName);
      if (!existingTag) {
        setFormData({
          ...formData,
          tags: [...formData.tags, { id: tagName, name: tagName }],
        });
      }
      setNewTag("");
    }
  };

  const removeTag = (tagId: string) => {
    setFormData({
      ...formData,
      tags: formData.tags.filter((t) => t.id !== tagId),
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add Dataset</DialogTitle>
          <DialogDescription>
            Create a new dataset resource in the catalog
          </DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information Section - includes name, description, resource type, privacy, authors, tags */}
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter dataset name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Describe your dataset"
                rows={4}
                required
              />
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="type">Resource Type *</Label>
                <Select
                  value={formData.type}
                  onValueChange={(value) => setFormData({ ...formData, type: value as ArtifactType })}
                  disabled
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={ArtifactType.DATASET}>Dataset</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="privacy">Privacy *</Label>
                <Select
                  value={formData.privacy}
                  onValueChange={(value) => setFormData({ ...formData, privacy: value as Privacy })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={Privacy.PUBLIC}>Public</SelectItem>
                    <SelectItem value={Privacy.PRIVATE}>Private</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Authors */}
            <div className="space-y-2">
              <Label>Authors</Label>
              <div className="flex gap-2">
                <Input
                  value={newAuthor}
                  onChange={(e) => setNewAuthor(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      addAuthor();
                    }
                  }}
                  placeholder="Enter author name or email"
                />
                <Button type="button" onClick={addAuthor} variant="outline">
                  Add
                </Button>
              </div>
              {formData.authors.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {formData.authors.map((author) => (
                    <Badge key={author} variant="secondary" className="gap-1">
                      {author}
                      <button
                        type="button"
                        onClick={() => removeAuthor(author)}
                        className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                </div>
              )}
            </div>

            {/* Tags */}
            <div className="space-y-2">
              <Label>Tags</Label>
              <div className="flex gap-2">
                <Input
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      addTag();
                    }
                  }}
                  placeholder="Enter tag name"
                  list="available-tags"
                />
                <datalist id="available-tags">
                  {availableTags?.map((tag) => (
                    <option key={tag.id} value={tag.name} />
                  ))}
                </datalist>
                <Button type="button" onClick={addTag} variant="outline">
                  Add
                </Button>
              </div>
              {formData.tags.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {formData.tags.map((tag) => (
                    <Badge key={tag.id} variant="secondary" className="gap-1" style={{ backgroundColor: tag.color }}>
                      {tag.name}
                      <button
                        type="button"
                        onClick={() => removeTag(tag.id)}
                        className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Primary storage (where the main copy lives) */}
          <div className="space-y-4 border-t pt-4">
            <div className="space-y-2">
              <Label htmlFor="primaryStorageResourceId">Primary Storage Resource ID *</Label>
              <Input
                id="primaryStorageResourceId"
                value={formData.primaryStorageResourceId}
                onChange={(e) => setFormData({ ...formData, primaryStorageResourceId: e.target.value })}
                placeholder="Storage resource where the main copy lives"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="primaryFilePath">Primary File Path *</Label>
              <Input
                id="primaryFilePath"
                value={formData.primaryFilePath}
                onChange={(e) => setFormData({ ...formData, primaryFilePath: e.target.value })}
                placeholder="/path/on/storage/to/dataset"
                required
              />
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="size">Size (bytes, optional)</Label>
                <Input
                  id="size"
                  type="number"
                  value={formData.size || ""}
                  onChange={(e) => setFormData({ ...formData, size: e.target.value ? parseInt(e.target.value) : undefined })}
                  placeholder="1024000"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="format">Format (optional)</Label>
                <Input
                  id="format"
                  value={formData.format}
                  onChange={(e) => setFormData({ ...formData, format: e.target.value })}
                  placeholder="CSV, JSON, etc."
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={createArtifact.isPending}>
              Cancel
            </Button>
            <Button type="submit" disabled={createArtifact.isPending}>
              {createArtifact.isPending ? "Creating..." : "Create Dataset"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
