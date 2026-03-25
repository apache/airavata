"use client";

import { useState, useMemo, Fragment } from "react";
import { Plus, Search, BookOpen, Database, GitBranch, AppWindow, ChevronDown, ChevronRight, Play, ArrowRight, Info } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { SearchBar } from "@/components/ui/search-bar";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useCatalogArtifacts, useCatalogTags } from "@/hooks";
import { useQuery } from "@tanstack/react-query";
import { artifactsApi, apiClient } from "@/lib/api";
import { ArtifactType, type ArtifactFilters as Filters } from "@/types/catalog";
import type { CatalogArtifact } from "@/types/catalog";
import { cn } from "@/lib/utils";
import type { Application } from "@/types";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { CreateApplicationWizard } from "@/components/applications/CreateApplicationWizard";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { AddDatasetModal } from "@/components/catalog/AddDatasetModal";
import { AddRepositoryModal } from "@/components/catalog/AddRepositoryModal";
import { toast } from "@/hooks/useToast";
import { useCreateExperimentModal } from "@/contexts/CreateExperimentModalContext";
import { getCatalogResourcePermalink } from "@/lib/permalink";

// Row component for application entries in the catalog table
function CatalogApplicationRow({ app }: { app: Application }) {
  const { openModal } = useCreateExperimentModal();
  const appPermalink = `/catalog/APPLICATION/${app.applicationId}`;

  const handleRun = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    openModal({ application: app });
  };

  return (
    <TableRow className="cursor-pointer" onClick={() => window.location.href = appPermalink}>
      <TableCell className="py-1.5 pr-3 pl-9">
        <div className="flex items-center gap-2">
          <Link href={appPermalink} className="font-medium text-sm hover:underline">
            {app.name}
            {app.version && (
              <span className="ml-1.5 text-xs text-muted-foreground font-normal">v{app.version}</span>
            )}
          </Link>
          {app.description ? (
            <Popover>
              <PopoverTrigger asChild>
                <button type="button" className="shrink-0" onClick={(e) => e.stopPropagation()}>
                  <Info className="h-3.5 w-3.5 text-muted-foreground" />
                </button>
              </PopoverTrigger>
              <PopoverContent side="top" className="text-sm">
                {app.description}
              </PopoverContent>
            </Popover>
          ) : null}
        </div>
      </TableCell>
      <TableCell className="py-1.5 px-3">
        <div className="flex flex-wrap items-center gap-1.5">
          <Badge variant="outline" className="text-xs font-normal">
            <span className="font-medium mr-1">Inputs:</span>{app.inputs.length}
          </Badge>
          <Badge variant="outline" className="text-xs font-normal">
            <span className="font-medium mr-1">Outputs:</span>{app.outputs.length}
          </Badge>
        </div>
      </TableCell>
      <TableCell className="py-1.5 px-3" onClick={(e) => e.stopPropagation()}>
        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={handleRun}>
          <Play className="h-4 w-4" />
        </Button>
      </TableCell>
    </TableRow>
  );
}

// Row component for repository/dataset entries in the catalog table
function CatalogResourceRow({ resource }: { resource: CatalogArtifact }) {
  const permalink = getCatalogResourcePermalink(resource.id, resource.type);

  return (
    <TableRow className="cursor-pointer" onClick={() => window.location.href = permalink}>
      <TableCell className="py-1.5 pr-3 pl-9">
        <div className="flex items-center gap-2">
          <Link href={permalink} className="font-medium text-sm hover:underline">
            {resource.name}
          </Link>
          {resource.description ? (
            <Popover>
              <PopoverTrigger asChild>
                <button type="button" className="shrink-0" onClick={(e) => e.stopPropagation()}>
                  <Info className="h-3.5 w-3.5 text-muted-foreground" />
                </button>
              </PopoverTrigger>
              <PopoverContent side="top" className="text-sm">
                {resource.description}
              </PopoverContent>
            </Popover>
          ) : null}
        </div>
      </TableCell>
      <TableCell className="py-1.5 px-3">
        <div className="flex flex-wrap items-center gap-1.5">
          {resource.authors && resource.authors.length > 0 && (
            <Badge variant="outline" className="text-xs font-normal">
              <span className="font-medium mr-1">Authors:</span>{resource.authors.join(", ")}
            </Badge>
          )}
          {resource.tags && resource.tags.length > 0 && resource.tags.map((tag) => (
            <Badge key={tag.id} variant="outline" className="text-xs font-normal">
              <span className="font-medium mr-1">Tag:</span>{tag.name}
            </Badge>
          ))}
        </div>
      </TableCell>
      <TableCell className="py-1.5 px-3">
        <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
          <Link href={permalink}>
            <ArrowRight className="h-4 w-4" />
          </Link>
        </Button>
      </TableCell>
    </TableRow>
  );
}

// Resource type configuration with icons and labels
const RESOURCE_TYPES = [
  { type: null, label: "All", icon: BookOpen },
  { type: "APPLICATION", label: "Applications", icon: AppWindow },
  { type: ArtifactType.REPOSITORY, label: "Repositories", icon: GitBranch },
  { type: ArtifactType.DATASET, label: "Datasets", icon: Database },
] as const;

export default function CatalogPage() {
  const { selectedGatewayId } = useGateway();
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId = selectedGatewayId || defaultGatewayId;

  const [filters, setFilters] = useState<Filters>({
    pageSize: 20,
    pageNumber: 0,
  });
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedType, setSelectedType] = useState<string | ArtifactType | null>(null);
  const [isCreateAppOpen, setIsCreateAppOpen] = useState(false);
  const [isCreateDatasetOpen, setIsCreateDatasetOpen] = useState(false);
  const [isCreateRepositoryOpen, setIsCreateRepositoryOpen] = useState(false);

  // Fetch applications using new API
  const { data: applications = [], isLoading: applicationsLoading, refetch: refetchApplications } = useQuery({
    queryKey: ["applications", gatewayId],
    queryFn: () => apiClient.get<Application[]>(`/api/v1/applications?gatewayId=${gatewayId}`),
    enabled: !!gatewayId,
    staleTime: 30000,
  });

  // Build catalog filters — only fetch repositories
  const catalogFilters: Filters | undefined = useMemo(() => {
    if (selectedType === null || selectedType === ArtifactType.REPOSITORY) {
      return { ...filters, type: ArtifactType.REPOSITORY };
    }
    return undefined;
  }, [filters, selectedType]);

  const shouldFetchRepositories = selectedType === null || selectedType === ArtifactType.REPOSITORY;
  const { data: resources = [], isLoading: resourcesLoading, error: resourcesError } = useCatalogArtifacts(
    shouldFetchRepositories ? catalogFilters : undefined
  );

  const shouldFetchDatasets = selectedType === null || selectedType === ArtifactType.DATASET;
  const { data: artifacts = [], isLoading: artifactsLoading, error: artifactsError } = useQuery({
    queryKey: ["artifacts", "public", shouldFetchDatasets ? (filters.nameSearch ?? "") : null, filters.pageNumber ?? 0, filters.pageSize ?? 20],
    queryFn: async () => {
      if (!shouldFetchDatasets) return [];
      try {
        return await artifactsApi.getPublic(filters.nameSearch ?? "", filters.pageNumber ?? 0, filters.pageSize ?? 20);
      } catch (error: any) {
        const status = error?.response?.status ?? error?.status;
        if (status === 404 || status === 500) {
          console.warn(`Artifacts public endpoint returned ${status}, treating as empty`);
          return [];
        }
        console.error("Error fetching public artifacts:", error);
        throw error;
      }
    },
    enabled: shouldFetchDatasets,
    staleTime: 30 * 1000,
    retry: 1,
    refetchOnWindowFocus: false,
  });

  const { data: allTags } = useCatalogTags();

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    const timer = setTimeout(() => {
      setFilters({ ...filters, nameSearch: value || undefined });
    }, 400);
    return () => clearTimeout(timer);
  };

  const selectResourceType = (type: string | ArtifactType | null) => {
    setSelectedType(type);
    if (type === "APPLICATION") return;
    setFilters({ ...filters, type: type === null ? undefined : type as ArtifactType });
  };

  const toggleTag = (tagId: string) => {
    const currentTags = filters.tags || [];
    const newTags = currentTags.includes(tagId)
      ? currentTags.filter((t) => t !== tagId)
      : [...currentTags, tagId];
    setFilters({ ...filters, tags: newTags.length > 0 ? newTags : undefined });
  };

  const filteredApplications = useMemo(() => {
    if (selectedType !== null && selectedType !== "APPLICATION") return [];
    if (!searchTerm) return applications;
    const searchLower = searchTerm.toLowerCase();
    return applications.filter((app) =>
      app.name.toLowerCase().includes(searchLower) ||
      app.description?.toLowerCase().includes(searchLower)
    );
  }, [applications, searchTerm, selectedType]);

  const filteredResources = useMemo(() => {
    if (selectedType === "APPLICATION") return [];
    return resources;
  }, [resources, selectedType]);

  const datasetItems = useMemo((): CatalogArtifact[] => {
    if (!shouldFetchDatasets || !artifacts || artifacts.length === 0) return [];
    return artifacts.map((dp) => ({
      id: dp.artifactUri,
      type: ArtifactType.DATASET,
      name: dp.name ?? "",
      description: dp.description ?? "",
      authors: dp.authors ?? [],
      tags: (dp.tags ?? []).map((t) => ({ id: t.id ?? t.name ?? "", name: t.name ?? t.id ?? "" })),
      headerImage: dp.headerImage,
    }) as CatalogArtifact);
  }, [shouldFetchDatasets, artifacts]);

  const catalogGroups = useMemo(() => {
    const groups: Array<{
      key: string;
      label: string;
      icon: typeof AppWindow;
      items: Array<{ type: "APPLICATION" | ArtifactType; data: Application | CatalogArtifact }>;
    }> = [];

    const showApps = selectedType === null || selectedType === "APPLICATION";
    const showRepos = selectedType === null || selectedType === ArtifactType.REPOSITORY;
    const showDatasets = selectedType === null || selectedType === ArtifactType.DATASET;

    if (showApps && filteredApplications.length > 0) {
      groups.push({
        key: "APPLICATION",
        label: "Applications",
        icon: AppWindow,
        items: filteredApplications.map((app) => ({ type: "APPLICATION" as const, data: app })),
      });
    }
    if (showRepos) {
      const repos = filteredResources.filter((r) => r.type === ArtifactType.REPOSITORY);
      if (repos.length > 0) {
        groups.push({
          key: "REPOSITORY",
          label: "Repositories",
          icon: GitBranch,
          items: repos.map((r) => ({ type: ArtifactType.REPOSITORY, data: r })),
        });
      }
    }
    if (showDatasets && datasetItems.length > 0) {
      groups.push({
        key: "DATASET",
        label: "Datasets",
        icon: Database,
        items: datasetItems.map((r) => ({ type: ArtifactType.DATASET, data: r })),
      });
    }

    return groups;
  }, [filteredApplications, filteredResources, datasetItems, selectedType]);

  const hasResults = catalogGroups.some((g) => g.items.length > 0);
  const isLoading = resourcesLoading || applicationsLoading || (shouldFetchDatasets && artifactsLoading);
  const [collapsedGroups, setCollapsedGroups] = useState<Set<string>>(new Set());

  const handleApplicationCreated = (application: Application) => {
    toast({
      title: "Application created",
      description: `${application.name} has been created successfully.`,
    });
    setIsCreateAppOpen(false);
    refetchApplications();
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Catalog</h1>
          <p className="text-muted-foreground">
            Discover applications and research resources
          </p>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Add
              <ChevronDown className="ml-2 h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => setIsCreateAppOpen(true)}>
              <AppWindow className="mr-2 h-4 w-4" />
              Application
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setIsCreateRepositoryOpen(true)}>
              <GitBranch className="mr-2 h-4 w-4" />
              Repository
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setIsCreateDatasetOpen(true)}>
              <Database className="mr-2 h-4 w-4" />
              Dataset
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Search Bar with Inline Type Selectors */}
      <SearchBar
        placeholder="Search catalog..."
        value={searchTerm}
        onChange={(value) => handleSearchChange(value)}
      >
        <div className="h-6 w-px bg-border" />
        <div className="flex items-center gap-0.5 px-1">
          {RESOURCE_TYPES.map(({ type, label, icon: Icon }) => (
            <button
              key={label}
              type="button"
              onClick={() => selectResourceType(type)}
              className={cn(
                "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors",
                (type === null ? selectedType === null : selectedType === type)
                  ? "bg-background text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground hover:bg-background/50"
              )}
            >
              <Icon className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">{label}</span>
            </button>
          ))}
        </div>
      </SearchBar>

      {/* Tags */}
      {selectedType !== "APPLICATION" && allTags && allTags.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          {allTags.map((tag) => (
            <Badge
              key={tag.id}
              variant={filters.tags?.includes(tag.id) ? "default" : "outline"}
              className="cursor-pointer"
              onClick={() => toggleTag(tag.id)}
            >
              {tag.name}
            </Badge>
          ))}
        </div>
      )}

      {/* Error Display */}
      {((resourcesError && (resourcesError as any)?.response?.status !== 404) ||
        (artifactsError && (artifactsError as any)?.response?.status !== 404)) && (
        <Card>
          <CardContent className="py-16">
            <div className="text-center">
              <Search className="mx-auto h-12 w-12 text-muted-foreground/50" />
              <h3 className="mt-4 text-lg font-semibold">Error loading resources</h3>
              <p className="text-muted-foreground mt-2">
                {resourcesError instanceof Error
                  ? resourcesError.message
                  : artifactsError instanceof Error
                  ? artifactsError.message
                  : "Failed to load catalog resources"}
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Results Table */}
      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : !hasResults ? (
        <Card>
          <CardContent className="py-16">
            <div className="text-center">
              <Search className="mx-auto h-12 w-12 text-muted-foreground/50" />
              <h3 className="mt-4 text-lg font-semibold">No results found</h3>
              <p className="text-muted-foreground mt-2">
                Try adjusting your filters or search terms
              </p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-3">Name</TableHead>
                <TableHead className="h-9 px-3">Details</TableHead>
                <TableHead className="h-9 px-3 w-10">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {catalogGroups.map((group) => {
                const GroupIcon = group.icon;
                const isCollapsed = collapsedGroups.has(group.key);
                return (
                  <Fragment key={group.key}>
                    <TableRow
                      className="bg-muted/50 hover:bg-muted cursor-pointer"
                      onClick={() => {
                        setCollapsedGroups((prev) => {
                          const next = new Set(prev);
                          if (next.has(group.key)) next.delete(group.key);
                          else next.add(group.key);
                          return next;
                        });
                      }}
                    >
                      <TableCell colSpan={2} className="py-1.5 px-3">
                        <div className="flex items-center gap-2">
                          <ChevronRight className={cn("h-4 w-4 transition-transform", !isCollapsed && "rotate-90")} />
                          <GroupIcon className="h-4 w-4 text-muted-foreground" />
                          <span className="text-xs font-semibold uppercase tracking-wide">
                            {group.label}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell className="py-1.5 px-3">
                        <Badge variant="secondary" className="text-xs">{group.items.length}</Badge>
                      </TableCell>
                    </TableRow>
                    {!isCollapsed && group.items.map((item) => {
                      if (item.type === "APPLICATION") {
                        const app = item.data as Application;
                        return <CatalogApplicationRow key={`app-${app.applicationId}`} app={app} />;
                      }
                      const resource = item.data as CatalogArtifact;
                      return <CatalogResourceRow key={`resource-${resource.id}`} resource={resource} />;
                    })}
                  </Fragment>
                );
              })}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Create Application Dialog */}
      <Dialog open={isCreateAppOpen} onOpenChange={setIsCreateAppOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create New Application</DialogTitle>
            <DialogDescription>
              Define a new application with its inputs, outputs, and scripts
            </DialogDescription>
          </DialogHeader>
          <CreateApplicationWizard
            gatewayId={gatewayId}
            onSuccess={handleApplicationCreated}
            onCancel={() => setIsCreateAppOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Add Dataset Modal */}
      <AddDatasetModal open={isCreateDatasetOpen} onOpenChange={setIsCreateDatasetOpen} />

      {/* Add Repository Modal */}
      <AddRepositoryModal open={isCreateRepositoryOpen} onOpenChange={setIsCreateRepositoryOpen} />
    </div>
  );
}
