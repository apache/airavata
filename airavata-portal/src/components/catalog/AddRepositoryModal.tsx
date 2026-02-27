"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { X } from "lucide-react";
import { useCatalogTags, useCreateCatalogArtifact } from "@/hooks";
import { toast } from "@/hooks/useToast";
import type { CatalogArtifact } from "@/types/catalog";
import { ArtifactType, Privacy } from "@/types/catalog";
import { useRouter } from "next/navigation";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function AddRepositoryModal({ open, onOpenChange }: Props) {
  const router = useRouter();
  const createResource = useCreateCatalogArtifact();
  const { data: availableTags } = useCatalogTags();
  
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    type: ArtifactType.REPOSITORY,
    privacy: Privacy.PUBLIC,
    authors: [] as string[],
    tags: [] as Array<{ id: string; name: string; color?: string }>,
    repositoryUrl: "",
    branch: "",
    commit: "",
    notebookPath: "",
    jupyterServerUrl: "",
    modelUrl: "",
    applicationInterfaceId: "",
    framework: "",
  });

  const [newAuthor, setNewAuthor] = useState("");
  const [newTag, setNewTag] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.description) {
      toast({
        title: "Error",
        description: "Name and description are required",
        variant: "destructive",
      });
      return;
    }

    try {
      const resourceData: Partial<CatalogArtifact> = {
        name: formData.name,
        description: formData.description,
        type: ArtifactType.REPOSITORY,
        privacy: formData.privacy,
        authors: formData.authors,
        tags: formData.tags,
      };

      // Add repository-specific fields if provided
      if (formData.repositoryUrl) (resourceData as any).repositoryUrl = formData.repositoryUrl;
      if (formData.branch) (resourceData as any).branch = formData.branch;
      if (formData.commit) (resourceData as any).commit = formData.commit;
      if (formData.notebookPath) (resourceData as any).notebookPath = formData.notebookPath;
      if (formData.jupyterServerUrl) (resourceData as any).jupyterServerUrl = formData.jupyterServerUrl;
      if (formData.modelUrl) (resourceData as any).modelUrl = formData.modelUrl;
      if (formData.applicationInterfaceId) (resourceData as any).applicationInterfaceId = formData.applicationInterfaceId;
      if (formData.framework) (resourceData as any).framework = formData.framework;

      const result = await createResource.mutateAsync(resourceData);
      toast({
        title: "Repository created",
        description: "Your repository has been created successfully.",
      });
      
      onOpenChange(false);
      router.push(`/repositories/${result.id}`);
      
      // Reset form
      setFormData({
        name: "",
        description: "",
        type: ArtifactType.REPOSITORY,
        privacy: Privacy.PUBLIC,
        authors: [],
        tags: [],
        repositoryUrl: "",
        branch: "",
        commit: "",
        notebookPath: "",
        jupyterServerUrl: "",
        modelUrl: "",
        applicationInterfaceId: "",
        framework: "",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to create repository",
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
          <DialogTitle>Add Repository</DialogTitle>
          <DialogDescription>
            Create a new repository resource in the catalog
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
                placeholder="Enter repository name"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Describe your repository"
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
                    <SelectItem value={ArtifactType.REPOSITORY}>Repository</SelectItem>
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

          {/* Repository-specific fields */}
          <div className="space-y-4 border-t pt-4">
            <div className="space-y-2">
              <Label htmlFor="repositoryUrl">Repository URL (optional)</Label>
              <Input
                id="repositoryUrl"
                value={formData.repositoryUrl}
                onChange={(e) => setFormData({ ...formData, repositoryUrl: e.target.value })}
                placeholder="https://github.com/user/repo"
              />
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="branch">Branch (optional)</Label>
                <Input
                  id="branch"
                  value={formData.branch}
                  onChange={(e) => setFormData({ ...formData, branch: e.target.value })}
                  placeholder="main"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="commit">Commit (optional)</Label>
                <Input
                  id="commit"
                  value={formData.commit}
                  onChange={(e) => setFormData({ ...formData, commit: e.target.value })}
                  placeholder="abc123..."
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="notebookPath">Notebook Path (optional)</Label>
              <Input
                id="notebookPath"
                value={formData.notebookPath}
                onChange={(e) => setFormData({ ...formData, notebookPath: e.target.value })}
                placeholder="/path/to/notebook.ipynb"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="jupyterServerUrl">Jupyter Server URL (optional)</Label>
              <Input
                id="jupyterServerUrl"
                value={formData.jupyterServerUrl}
                onChange={(e) => setFormData({ ...formData, jupyterServerUrl: e.target.value })}
                placeholder="https://jupyter.example.com"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="modelUrl">Model URL (optional)</Label>
              <Input
                id="modelUrl"
                value={formData.modelUrl}
                onChange={(e) => setFormData({ ...formData, modelUrl: e.target.value })}
                placeholder="https://example.com/model"
              />
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="applicationInterfaceId">Application Interface ID (optional)</Label>
                <Input
                  id="applicationInterfaceId"
                  value={formData.applicationInterfaceId}
                  onChange={(e) => setFormData({ ...formData, applicationInterfaceId: e.target.value })}
                  placeholder="Application interface ID"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="framework">Framework (optional)</Label>
                <Input
                  id="framework"
                  value={formData.framework}
                  onChange={(e) => setFormData({ ...formData, framework: e.target.value })}
                  placeholder="TensorFlow, PyTorch, etc."
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={createResource.isPending}>
              Cancel
            </Button>
            <Button type="submit" disabled={createResource.isPending}>
              {createResource.isPending ? "Creating..." : "Create Repository"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
