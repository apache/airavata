"use client";

import { useState, Suspense, useMemo } from "react";
import { useSearchParams, useRouter, useParams } from "next/navigation";
import { useSession } from "next-auth/react";
import { Plus, FlaskConical, FolderKanban, X, CheckCircle, XCircle, Loader2, HardDrive, Folder, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { SearchBar } from "@/components/ui/search-bar";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { RecentExperiments, QuickActions } from "@/components/dashboard";
import { resourcesApi } from "@/lib/api/resources";
import { ProjectForm } from "@/components/project";
import { useQuery } from "@tanstack/react-query";
import { useProjects, useCreateProject, useDeleteProject, useExperiments, useDeleteExperiment } from "@/hooks";
import type { Project, ExperimentModel, Resource } from "@/types";
import { ExperimentState } from "@/types";
import { Skeleton } from "@/components/ui/skeleton";
import { FileBrowser } from "@/components/storage";

function DashboardContent() {
  const params = useParams();
  const gatewayName = (params?.gatewayName as string) || "default";
  const searchParams = useSearchParams();
  const router = useRouter();
  const { data: session } = useSession();
  const initialOpen = searchParams.get("action") === "new";

  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(initialOpen);
  const [isCreateExperimentOpen, setIsCreateExperimentOpen] = useState(false);
  const [selectedProjectIds, setSelectedProjectIds] = useState<Set<string>>(new Set());
  const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);
  const [experimentToDelete, setExperimentToDelete] = useState<ExperimentModel | null>(null);
  const [search, setSearch] = useState("");
  const [fileSearch, setFileSearch] = useState("");
  const [currentPath, setCurrentPath] = useState<string[]>([]);
  const [currentStorage, setCurrentStorage] = useState<{ id: string; name: string } | null>(null);

  const { data: allResources = [], isLoading: resourcesLoading } = useQuery({
    queryKey: ["resources", gatewayName],
    queryFn: () => resourcesApi.list(gatewayName),
    enabled: !!gatewayName,
  });

  // Storage resources are resources with a storage capability
  const storageResources = useMemo(
    () => allResources.filter((r: Resource) => !!r.capabilities.storage),
    [allResources]
  );

  const storageEntries = useMemo(
    () => storageResources.map((r) => [r.resourceId, r.name] as const),
    [storageResources]
  );

  const { data: projects, isLoading: projectsLoading } = useProjects();
  const { data: allExperiments, isLoading: experimentsLoading } = useExperiments({ limit: 1000 });
  const createProject = useCreateProject();
  const deleteProject = useDeleteProject();
  const deleteExperiment = useDeleteExperiment();

  const experimentsByProject = useMemo(() => {
    if (!allExperiments) return {};
    const grouped: Record<string, ExperimentModel[]> = {};
    allExperiments.forEach((exp) => {
      const projectId = exp.projectId || "unassigned";
      if (!grouped[projectId]) grouped[projectId] = [];
      grouped[projectId].push(exp);
    });
    Object.keys(grouped).forEach((projectId) => {
      grouped[projectId].sort((a, b) => (b.creationTime || 0) - (a.creationTime || 0));
    });
    return grouped;
  }, [allExperiments]);

  const sortedProjects = useMemo(() => {
    if (!projects) return [];
    return [...projects].sort((a, b) => {
      const aExperiments = experimentsByProject[a.projectID] || [];
      const bExperiments = experimentsByProject[b.projectID] || [];
      const aLatest = aExperiments.length > 0 ? aExperiments[0].creationTime || 0 : 0;
      const bLatest = bExperiments.length > 0 ? bExperiments[0].creationTime || 0 : 0;
      if (aLatest > 0 && bLatest > 0) return bLatest - aLatest;
      if (aLatest > 0) return -1;
      if (bLatest > 0) return 1;
      return (b.creationTime || 0) - (a.creationTime || 0);
    });
  }, [projects, experimentsByProject]);

  const toggleProjectFilter = (projectId: string) => {
    setSelectedProjectIds((prev) => {
      const next = new Set(prev);
      if (next.has(projectId)) next.delete(projectId);
      else next.add(projectId);
      return next;
    });
  };

  const filteredProjects = useMemo(() => {
    if (!sortedProjects || !search) return sortedProjects;
    const searchLower = search.toLowerCase();
    return sortedProjects.filter(
      (p) =>
        p.name.toLowerCase().includes(searchLower) ||
        p.description?.toLowerCase().includes(searchLower) ||
        experimentsByProject[p.projectID]?.some((e) =>
          e.experimentName.toLowerCase().includes(searchLower)
        )
    );
  }, [sortedProjects, search, experimentsByProject]);

  const filteredExperiments = useMemo(() => {
    if (!allExperiments) return [];
    let filtered = allExperiments;
    if (selectedProjectIds.size > 0) {
      filtered = filtered.filter((exp) => exp.projectId && selectedProjectIds.has(exp.projectId));
    }
    if (search) {
      const searchLower = search.toLowerCase();
      filtered = filtered.filter(
        (exp) =>
          exp.experimentName.toLowerCase().includes(searchLower) ||
          exp.projectId?.toLowerCase().includes(searchLower) ||
          projects?.find((p) => p.projectID === exp.projectId)?.name.toLowerCase().includes(searchLower)
      );
    }
    return filtered;
  }, [allExperiments, search, selectedProjectIds, projects]);

  const recentExperimentsToShow = useMemo(() => filteredExperiments, [filteredExperiments]);
  const recentProjectsToShow = useMemo(() => {
    if (selectedProjectIds.size > 0) {
      return sortedProjects?.filter((p) => selectedProjectIds.has(p.projectID)) || [];
    }
    return sortedProjects || [];
  }, [sortedProjects, selectedProjectIds]);

  const handleCreateProject = async (data: { name: string; description?: string }) => {
    const owner = session?.user?.email || session?.user?.name || "unknown";
    const result = await createProject.mutateAsync({
      name: data.name,
      description: data.description,
      owner,
    });
    setIsCreateProjectOpen(false);
    if (result?.projectId) setSelectedProjectIds(new Set([result.projectId]));
  };

  const handleDeleteProject = async () => {
    if (projectToDelete) {
      await deleteProject.mutateAsync(projectToDelete.projectID);
      setProjectToDelete(null);
    }
  };

  const handleDeleteExperiment = async () => {
    if (experimentToDelete) {
      await deleteExperiment.mutateAsync(experimentToDelete.experimentId);
      setExperimentToDelete(null);
    }
  };

  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold tracking-tight text-center">
        Good {new Date().getHours() < 12 ? "morning" : new Date().getHours() < 17 ? "afternoon" : "evening"}, {session?.user?.name?.split(" ")[0] || "User"}!
      </h1>

      <QuickActions gatewayName={gatewayName} onCreateProject={() => setIsCreateProjectOpen(true)} />

      <hr className="border-border" />

      <Tabs defaultValue="experiments" className="space-y-4">
        <TabsList>
          <TabsTrigger value="experiments" className="flex items-center gap-2">
            <FlaskConical className="h-4 w-4" />
            My Experiments
          </TabsTrigger>
          <TabsTrigger value="files" className="flex items-center gap-2">
            <HardDrive className="h-4 w-4" />
            My Files
          </TabsTrigger>
        </TabsList>

        <TabsContent value="experiments" className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold">My Experiments</h2>
            <div className="flex items-center gap-4 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-1.5">
                <FlaskConical className="h-3.5 w-3.5" />
                {allExperiments?.length || 0} experiments
              </span>
              <span className="inline-flex items-center gap-1.5">
                <FolderKanban className="h-3.5 w-3.5" />
                {projects?.length || 0} projects
              </span>
              <span className="text-border">|</span>
              <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 bg-amber-50 text-amber-700 border border-amber-200">
                <Loader2 className="h-3 w-3" />
                {allExperiments?.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.EXECUTING).length || 0} running
              </span>
              <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 bg-emerald-50 text-emerald-700 border border-emerald-200">
                <CheckCircle className="h-3 w-3" />
                {allExperiments?.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.COMPLETED).length || 0} completed
              </span>
              <span className="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 bg-red-50 text-red-700 border border-red-200">
                <XCircle className="h-3 w-3" />
                {allExperiments?.filter((e) => e.experimentStatus?.[0]?.state === ExperimentState.FAILED).length || 0} failed
              </span>
            </div>
          </div>

          <SearchBar
            placeholder="Search projects and experiments..."
            value={search}
            onChange={setSearch}
          >
            {search && (
              <>
                <div className="h-6 w-px bg-border" />
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={() => setSearch("")}
                  className="h-8 w-8 p-0"
                  title="Clear search"
                >
                  <X className="h-4 w-4" />
                </Button>
              </>
            )}
          </SearchBar>

          <RecentExperiments
            experiments={filteredExperiments}
            projects={sortedProjects || []}
            isLoading={experimentsLoading || projectsLoading}
            onDelete={setExperimentToDelete}
          />
        </TabsContent>

        <TabsContent value="files" className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold">My Files</h2>
            <div className="flex items-center gap-4 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-1.5">
                <HardDrive className="h-3.5 w-3.5" />
                {storageEntries.length} storage{storageEntries.length !== 1 ? "s" : ""}
              </span>
            </div>
          </div>

          {!currentStorage && (
            <SearchBar
              placeholder="Search storage resources..."
              value={fileSearch}
              onChange={setFileSearch}
            >
              {fileSearch && (
                <>
                  <div className="h-6 w-px bg-border" />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setFileSearch("")}
                    className="h-8 w-8 p-0"
                    title="Clear search"
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </>
              )}
            </SearchBar>
          )}

          <div className="flex items-center gap-1 text-sm font-mono">
            <Folder className="h-3.5 w-3.5 text-muted-foreground" />
            {!currentStorage ? (
              <span className="text-muted-foreground">/</span>
            ) : (
              <div className="flex items-center gap-1">
                <Button variant="ghost" size="sm" className="h-6 px-1 text-muted-foreground hover:text-foreground" onClick={() => { setCurrentStorage(null); setCurrentPath([]); }}>
                  /
                </Button>
                <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                <Button variant="ghost" size="sm" className="h-6 px-1" onClick={() => setCurrentPath([])}>
                  {currentStorage.name}
                </Button>
                {currentPath.map((segment, idx) => (
                  <span key={idx} className="flex items-center gap-1">
                    <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                    <Button variant="ghost" size="sm" className="h-6 px-1" onClick={() => setCurrentPath(currentPath.slice(0, idx + 1))}>
                      {segment}
                    </Button>
                  </span>
                ))}
              </div>
            )}
          </div>

          {!currentStorage ? (
            resourcesLoading ? (
              <Skeleton className="h-48 w-full" />
            ) : storageEntries.length === 0 ? (
              <div className="border rounded-lg py-16 text-center">
                <HardDrive className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
                <p className="text-muted-foreground">No storage resources available</p>
              </div>
            ) : (() => {
              const filtered = fileSearch
                ? storageEntries.filter(([id, name]) =>
                    name.toLowerCase().includes(fileSearch.toLowerCase()) ||
                    id.toLowerCase().includes(fileSearch.toLowerCase())
                  )
                : storageEntries;
              return filtered.length === 0 ? (
                <div className="border rounded-lg py-16 text-center">
                  <p className="text-muted-foreground">No storage resources match your search</p>
                </div>
              ) : (
                <div className="border rounded-lg overflow-hidden">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead className="h-9 px-3">Storage</TableHead>
                        <TableHead className="h-9 px-3 w-10"></TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filtered.map(([id, name]) => (
                        <TableRow key={id} className="cursor-pointer" onClick={() => { setCurrentStorage({ id, name }); setCurrentPath([]); }}>
                          <TableCell className="py-1.5 px-3">
                            <div className="flex items-center gap-2">
                              <Folder className="h-4 w-4 text-muted-foreground shrink-0" />
                              <span className="font-medium text-sm">{name || id}</span>
                            </div>
                          </TableCell>
                          <TableCell className="py-1.5 px-3">
                            <ChevronRight className="h-4 w-4 text-muted-foreground" />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              );
            })()
          ) : (
            <FileBrowser files={[]} isLoading={false} currentPath={currentPath} onNavigate={setCurrentPath} hideToolbar={true} />
          )}
        </TabsContent>
      </Tabs>

      <Dialog open={isCreateProjectOpen} onOpenChange={setIsCreateProjectOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New Project</DialogTitle>
            <DialogDescription>Create a new project to organize your experiments</DialogDescription>
          </DialogHeader>
          <ProjectForm
            onSubmit={handleCreateProject}
            onCancel={() => setIsCreateProjectOpen(false)}
            isLoading={createProject.isPending}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={isCreateExperimentOpen} onOpenChange={setIsCreateExperimentOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Experiment</DialogTitle>
            <DialogDescription>
              {selectedProjectIds.size === 1
                ? `Create an experiment in "${projects?.find((p) => p.projectID === Array.from(selectedProjectIds)[0])?.name || "selected project"}"`
                : "Select a project to create an experiment in"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {projectsLoading ? (
              <div className="text-center py-4">Loading projects...</div>
            ) : !projects || projects.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground mb-4">
                  You need to create a project first before creating experiments.
                </p>
                <Button
                  onClick={() => {
                    setIsCreateExperimentOpen(false);
                    setIsCreateProjectOpen(true);
                  }}
                >
                  Create Project
                </Button>
              </div>
            ) : selectedProjectIds.size === 1 ? (
              <div className="space-y-4">
                <div className="p-4 border rounded-lg bg-muted/30">
                  <p className="text-sm font-medium mb-2">Selected Project:</p>
                  <p className="text-sm text-muted-foreground">
                    {projects.find((p) => p.projectID === Array.from(selectedProjectIds)[0])?.name}
                  </p>
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setIsCreateExperimentOpen(false)}>
                    Cancel
                  </Button>
                  <Button
                    onClick={() => {
                      router.push(`/experiments/create?projectId=${Array.from(selectedProjectIds)[0]}`);
                    }}
                  >
                    Create Experiment
                  </Button>
                </div>
              </div>
            ) : (
              <div className="space-y-2 max-h-[400px] overflow-y-auto">
                {projects.map((project) => (
                  <Button
                    key={project.projectID}
                    variant="outline"
                    className="w-full justify-start h-auto py-3"
                    onClick={() => {
                      setSelectedProjectIds(new Set([project.projectID]));
                      router.push(`/experiments/create?projectId=${project.projectID}`);
                    }}
                  >
                    <div className="flex items-center gap-3 w-full">
                      <FolderKanban className="h-5 w-5 text-purple-600" />
                      <div className="flex-1 text-left">
                        <p className="font-semibold">{project.name}</p>
                        {project.description && (
                          <p className="text-sm text-muted-foreground line-clamp-1">{project.description}</p>
                        )}
                      </div>
                    </div>
                  </Button>
                ))}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={!!projectToDelete} onOpenChange={() => setProjectToDelete(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Project</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete &quot;{projectToDelete?.name}&quot;? This action cannot be undone
              and all associated experiments will be removed.
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-3">
            <Button variant="outline" onClick={() => setProjectToDelete(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteProject} disabled={deleteProject.isPending}>
              {deleteProject.isPending ? "Deleting..." : "Delete"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={!!experimentToDelete} onOpenChange={() => setExperimentToDelete(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Experiment</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete &quot;{experimentToDelete?.experimentName}&quot;? This action
              cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-3">
            <Button variant="outline" onClick={() => setExperimentToDelete(null)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDeleteExperiment} disabled={deleteExperiment.isPending}>
              {deleteExperiment.isPending ? "Deleting..." : "Delete"}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default function DashboardPage() {
  return (
    <Suspense
      fallback={
        <div className="space-y-4">
          <Skeleton className="h-10 w-64" />
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {[...Array(6)].map((_, i) => (
              <Skeleton key={i} className="h-40" />
            ))}
          </div>
        </div>
      }
    >
      <DashboardContent />
    </Suspense>
  );
}
