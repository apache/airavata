"use client";

import { Star, Calendar, User, ExternalLink } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils";
import type { CatalogArtifact, DatasetArtifact, RepositoryArtifact } from "@/types/catalog";
import { ArtifactType } from "@/types/catalog";

interface Props {
  resource: CatalogArtifact;
  /** When false, do not render the page title/description block (used when the page shows its own header with back button). Default true. */
  showHeader?: boolean;
}

export function ResourceDetail({ resource, showHeader = true }: Props) {
  const renderTypeSpecificDetails = () => {
    switch (resource.type) {
      case ArtifactType.DATASET:
        const dataset = resource as DatasetArtifact;
        return (
          <Card>
            <CardHeader>
              <CardTitle>Dataset Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <div>
                <p className="text-sm text-muted-foreground">URL</p>
                <a
                  href={dataset.datasetUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline flex items-center gap-1 break-all"
                >
                  {dataset.datasetUrl}
                  <ExternalLink className="h-3 w-3" />
                </a>
              </div>
              {dataset.size && (
                <div>
                  <p className="text-sm text-muted-foreground">Size</p>
                  <p>{(dataset.size / 1024 / 1024).toFixed(2)} MB</p>
                </div>
              )}
              {dataset.format && (
                <div>
                  <p className="text-sm text-muted-foreground">Format</p>
                  <Badge variant="outline">{dataset.format}</Badge>
                </div>
              )}
            </CardContent>
          </Card>
        );

      case ArtifactType.REPOSITORY:
        const repo = resource as RepositoryArtifact;
        return (
          <Card>
            <CardHeader>
              <CardTitle>Repository Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {repo.repositoryUrl && (
                <div>
                  <p className="text-sm text-muted-foreground">Repository URL</p>
                  <a
                    href={repo.repositoryUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline flex items-center gap-1 break-all"
                  >
                    {repo.repositoryUrl}
                    <ExternalLink className="h-3 w-3" />
                  </a>
                </div>
              )}
              {repo.branch && (
                <div>
                  <p className="text-sm text-muted-foreground">Branch</p>
                  <Badge variant="outline">{repo.branch}</Badge>
                </div>
              )}
              {repo.commit && (
                <div>
                  <p className="text-sm text-muted-foreground">Commit</p>
                  <p className="font-mono text-sm">{repo.commit.substring(0, 8)}</p>
                </div>
              )}
              {repo.notebookPath && (
                <div>
                  <p className="text-sm text-muted-foreground">Notebook Path</p>
                  <p className="font-mono text-sm">{repo.notebookPath}</p>
                </div>
              )}
              {repo.jupyterServerUrl && (
                <div>
                  <p className="text-sm text-muted-foreground">Jupyter Server URL</p>
                  <a
                    href={repo.jupyterServerUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline flex items-center gap-1 break-all"
                  >
                    {repo.jupyterServerUrl}
                    <ExternalLink className="h-3 w-3" />
                  </a>
                </div>
              )}
              {repo.modelUrl && (
                <div>
                  <p className="text-sm text-muted-foreground">Model URL</p>
                  <a
                    href={repo.modelUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline flex items-center gap-1 break-all"
                  >
                    {repo.modelUrl}
                    <ExternalLink className="h-3 w-3" />
                  </a>
                </div>
              )}
              {repo.applicationInterfaceId && (
                <div>
                  <p className="text-sm text-muted-foreground">Application Interface</p>
                  <p className="font-mono text-sm">{repo.applicationInterfaceId}</p>
                </div>
              )}
              {repo.framework && (
                <div>
                  <p className="text-sm text-muted-foreground">Framework</p>
                  <Badge variant="outline">{repo.framework}</Badge>
                </div>
              )}
            </CardContent>
          </Card>
        );

      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {showHeader && (
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-3xl font-bold tracking-tight">{resource.name}</h1>
              <Badge>{resource.type}</Badge>
              <Badge variant="outline">{resource.status}</Badge>
            </div>
            <p className="text-muted-foreground">{resource.description}</p>
          </div>
          <Button variant="outline" size="sm">
            <Star className="mr-2 h-4 w-4" />
            Star
          </Button>
        </div>
      )}

      {resource.headerImage && (
        <Card>
          <CardContent className="p-0">
            <img
              src={resource.headerImage}
              alt={resource.name}
              className="w-full h-96 object-cover rounded-lg"
            />
          </CardContent>
        </Card>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <User className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Authors</CardTitle>
          </CardHeader>
          <CardContent>
            {resource.authors.map((author, idx) => (
              <p key={idx} className="text-sm">
                {author}
              </p>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <CardTitle className="text-sm font-medium">Created</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm">{formatDate(resource.createdAt)}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Privacy</CardTitle>
          </CardHeader>
          <CardContent>
            <Badge variant={resource.privacy === "PUBLIC" ? "default" : "secondary"}>
              {resource.privacy}
            </Badge>
          </CardContent>
        </Card>
      </div>

      {resource.tags && resource.tags.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Tags</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {resource.tags.map((tag) => (
                <Badge key={tag.id} variant="secondary">
                  {tag.name}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {renderTypeSpecificDetails()}
    </div>
  );
}
