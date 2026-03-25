"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Key,
  Link2,
  Plus,
  Trash2,
  MoreVertical,
  Copy,
  CheckCircle,
  Server,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
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
import { SSHKeyForm } from "@/components/credentials/SSHKeyForm";
import { PasswordCredentialForm } from "@/components/credentials/PasswordCredentialForm";
import { credentialsApi } from "@/lib/api/credentials";
import { bindingsApi } from "@/lib/api/bindings";
import { resourcesApi } from "@/lib/api/resources";
import { toast } from "@/hooks/useToast";
import { formatDate } from "@/lib/utils";
import type { ResourceBinding } from "@/types";
import type { SSHCredential, PasswordCredential } from "@/lib/api/credentials";

const emptyBindingForm = {
  credentialId: "",
  resourceId: "",
  loginUsername: "",
};

export default function AccessPage() {
  const params = useParams();
  const gatewayId = params.gatewayName as string;

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Access</h1>
        <p className="text-muted-foreground">
          Manage credentials and resource bindings
        </p>
      </div>

      <Tabs defaultValue="credentials" className="space-y-4">
        <TabsList>
          <TabsTrigger value="credentials" className="flex items-center gap-2">
            <Key className="h-4 w-4" />
            Credentials
          </TabsTrigger>
          <TabsTrigger value="bindings" className="flex items-center gap-2">
            <Link2 className="h-4 w-4" />
            Resource Bindings
          </TabsTrigger>
        </TabsList>

        <TabsContent value="credentials">
          <CredentialsSection gatewayId={gatewayId} />
        </TabsContent>

        <TabsContent value="bindings">
          <BindingsSection gatewayId={gatewayId} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

// ─── Credentials Section ─────────────────────────────────────────────────────

function CredentialsSection({ gatewayId }: { gatewayId: string }) {
  const queryClient = useQueryClient();
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [credentialType, setCredentialType] = useState<"SSH" | "PASSWORD">("SSH");
  const [deletingToken, setDeletingToken] = useState<string | null>(null);
  const [copiedToken, setCopiedToken] = useState<string | null>(null);

  const { data: credentials = [], isLoading } = useQuery({
    queryKey: ["credentials", gatewayId],
    queryFn: () => credentialsApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const createSSHMutation = useMutation({
    mutationFn: (cred: SSHCredential) => credentialsApi.createSSH(cred),
    onSuccess: () => {
      toast({ title: "SSH credential created" });
      queryClient.invalidateQueries({ queryKey: ["credentials", gatewayId] });
      setIsAddOpen(false);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to create credential",
        description: err?.message,
        variant: "destructive",
      });
    },
  });

  const createPasswordMutation = useMutation({
    mutationFn: (cred: PasswordCredential) => credentialsApi.createPassword(cred),
    onSuccess: () => {
      toast({ title: "Password credential created" });
      queryClient.invalidateQueries({ queryKey: ["credentials", gatewayId] });
      setIsAddOpen(false);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to create credential",
        description: err?.message,
        variant: "destructive",
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (token: string) => credentialsApi.delete(token, gatewayId),
    onSuccess: () => {
      toast({ title: "Credential deleted" });
      queryClient.invalidateQueries({ queryKey: ["credentials", gatewayId] });
      setDeletingToken(null);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to delete credential",
        description: err?.message,
        variant: "destructive",
      });
    },
  });

  const handleCreateSSH = async (credential: SSHCredential): Promise<void> => {
    createSSHMutation.mutate({ ...credential, gatewayId });
  };

  const handleCreatePassword = async (credential: PasswordCredential): Promise<void> => {
    createPasswordMutation.mutate({ ...credential, gatewayId });
  };

  const copyToken = (token: string) => {
    navigator.clipboard.writeText(token);
    setCopiedToken(token);
    setTimeout(() => setCopiedToken(null), 2000);
    toast({ title: "Token copied to clipboard" });
  };

  const deletingCred = credentials.find((c) => c.token === deletingToken);
  const isCreating = createSSHMutation.isPending || createPasswordMutation.isPending;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Credentials</h2>
        <Button onClick={() => setIsAddOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          Add Credential
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-3">Name</TableHead>
              <TableHead className="h-9 px-3">Type</TableHead>
              <TableHead className="h-9 px-3">Description</TableHead>
              <TableHead className="h-9 px-3">Created</TableHead>
              <TableHead className="h-9 px-3 w-0" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <>
                {[...Array(3)].map((_, i) => (
                  <TableRow key={i}>
                    {[...Array(5)].map((__, j) => (
                      <TableCell key={j} className="py-1.5 px-3">
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </>
            ) : credentials.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-16 text-center">
                  <Key className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
                  <p className="text-muted-foreground">No credentials found</p>
                  <Button
                    variant="outline"
                    className="mt-4"
                    onClick={() => setIsAddOpen(true)}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Add your first credential
                  </Button>
                </TableCell>
              </TableRow>
            ) : (
              credentials.map((cred) => (
                <TableRow key={cred.token} className="hover:bg-muted/50">
                  <TableCell className="py-1.5 px-3 font-medium">
                    {cred.name || cred.description || (
                      <span className="text-muted-foreground italic">Unnamed</span>
                    )}
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    <Badge variant="secondary">{cred.type}</Badge>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground max-w-[180px] truncate">
                    {cred.description || "—"}
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground text-sm">
                    {cred.persistedTime ? formatDate(cred.persistedTime) : "—"}
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
                        <DropdownMenuItem onClick={() => copyToken(cred.token)}>
                          {copiedToken === cred.token ? (
                            <>
                              <CheckCircle className="mr-2 h-4 w-4 text-green-600" />
                              Copied!
                            </>
                          ) : (
                            <>
                              <Copy className="mr-2 h-4 w-4" />
                              Copy Token
                            </>
                          )}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="text-destructive focus:text-destructive"
                          onClick={() => setDeletingToken(cred.token)}
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

      {/* Add Credential Dialog */}
      <Dialog open={isAddOpen} onOpenChange={setIsAddOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Credential</DialogTitle>
            <DialogDescription>
              Create a new credential for accessing resources.
            </DialogDescription>
          </DialogHeader>
          <Tabs
            value={credentialType}
            onValueChange={(v) => setCredentialType(v as "SSH" | "PASSWORD")}
          >
            <TabsList>
              <TabsTrigger value="SSH">SSH Key</TabsTrigger>
              <TabsTrigger value="PASSWORD">Password</TabsTrigger>
            </TabsList>
            <TabsContent value="SSH">
              <SSHKeyForm
                onSubmit={handleCreateSSH}
                onCancel={() => setIsAddOpen(false)}
                isLoading={isCreating}
                gatewayId={gatewayId}
              />
            </TabsContent>
            <TabsContent value="PASSWORD">
              <PasswordCredentialForm
                onSubmit={handleCreatePassword}
                onCancel={() => setIsAddOpen(false)}
                isLoading={isCreating}
                gatewayId={gatewayId}
              />
            </TabsContent>
          </Tabs>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deletingToken}
        onOpenChange={(open) => !open && setDeletingToken(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Credential</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete &quot;
              {deletingCred?.name || deletingCred?.description || deletingToken?.substring(0, 16)}
              &quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteMutation.isPending}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deletingToken && deleteMutation.mutate(deletingToken)}
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

// ─── Bindings Section ─────────────────────────────────────────────────────────

function BindingsSection({ gatewayId }: { gatewayId: string }) {
  const queryClient = useQueryClient();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [deletingBindingId, setDeletingBindingId] = useState<string | null>(null);
  const [formData, setFormData] = useState(emptyBindingForm);

  const { data: bindings = [], isLoading: bindingsLoading } = useQuery({
    queryKey: ["bindings", gatewayId],
    queryFn: () => bindingsApi.list({ gatewayId }),
    enabled: !!gatewayId,
  });

  const { data: credentials = [] } = useQuery({
    queryKey: ["credentials", gatewayId],
    queryFn: () => credentialsApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const { data: resources = [] } = useQuery({
    queryKey: ["resources", gatewayId],
    queryFn: () => resourcesApi.list(gatewayId),
    enabled: !!gatewayId,
  });

  const createMutation = useMutation({
    mutationFn: (binding: Partial<ResourceBinding>) => bindingsApi.create(binding),
    onSuccess: () => {
      toast({ title: "Binding created" });
      queryClient.invalidateQueries({ queryKey: ["bindings", gatewayId] });
      setIsCreateOpen(false);
      setFormData(emptyBindingForm);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to create binding",
        description: err?.message,
        variant: "destructive",
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (bindingId: string) => bindingsApi.delete(bindingId),
    onSuccess: () => {
      toast({ title: "Binding deleted" });
      queryClient.invalidateQueries({ queryKey: ["bindings", gatewayId] });
      setDeletingBindingId(null);
    },
    onError: (err: Error) => {
      toast({
        title: "Failed to delete binding",
        description: err?.message,
        variant: "destructive",
      });
    },
  });

  const getCredentialName = (credentialId: string) => {
    const cred = credentials.find((c) => c.token === credentialId);
    return cred?.name || cred?.description || credentialId.substring(0, 16) + "...";
  };

  const getResourceName = (resourceId: string) => {
    const res = resources.find((r) => r.resourceId === resourceId);
    return res?.name ?? resourceId;
  };

  const handleCreate = () => {
    createMutation.mutate({
      gatewayId,
      credentialId: formData.credentialId,
      resourceId: formData.resourceId,
      loginUsername: formData.loginUsername.trim(),
      enabled: true,
    });
  };

  const deletingBinding = bindings.find((b) => b.bindingId === deletingBindingId);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Resource Bindings</h2>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          Create Binding
        </Button>
      </div>

      <div className="border rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="h-9 px-3">Credential</TableHead>
              <TableHead className="h-9 px-3">Resource</TableHead>
              <TableHead className="h-9 px-3">Login Username</TableHead>
              <TableHead className="h-9 px-3">Status</TableHead>
              <TableHead className="h-9 px-3 w-0" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {bindingsLoading ? (
              <>
                {[...Array(3)].map((_, i) => (
                  <TableRow key={i}>
                    {[...Array(5)].map((__, j) => (
                      <TableCell key={j} className="py-1.5 px-3">
                        <Skeleton className="h-4 w-24" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </>
            ) : bindings.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-16 text-center">
                  <Server className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
                  <p className="text-muted-foreground">No resource bindings configured</p>
                  <p className="text-sm text-muted-foreground mt-1">
                    Bindings connect credentials to resources for authentication.
                  </p>
                  <Button
                    variant="outline"
                    className="mt-4"
                    onClick={() => setIsCreateOpen(true)}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Create your first binding
                  </Button>
                </TableCell>
              </TableRow>
            ) : (
              bindings.map((binding) => (
                <TableRow key={binding.bindingId} className="hover:bg-muted/50">
                  <TableCell className="py-1.5 px-3 font-medium">
                    <div className="flex items-center gap-2">
                      <Key className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                      {getCredentialName(binding.credentialId)}
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-2">
                      <Server className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                      {getResourceName(binding.resourceId)}
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 font-mono text-sm">
                    {binding.loginUsername || (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    <Badge variant={binding.enabled ? "default" : "secondary"}>
                      {binding.enabled ? "Enabled" : "Disabled"}
                    </Badge>
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
                          className="text-destructive focus:text-destructive"
                          onClick={() => setDeletingBindingId(binding.bindingId)}
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

      {/* Create Binding Dialog */}
      <Dialog
        open={isCreateOpen}
        onOpenChange={(open) => {
          setIsCreateOpen(open);
          if (!open) setFormData(emptyBindingForm);
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Resource Binding</DialogTitle>
            <DialogDescription>
              Link a credential to a resource with a login username.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Credential *</Label>
              <select
                className="w-full p-2 border rounded text-sm"
                value={formData.credentialId}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, credentialId: e.target.value }))
                }
              >
                <option value="">Select a credential</option>
                {credentials.map((cred) => (
                  <option key={cred.token} value={cred.token}>
                    {cred.name || cred.description || cred.token.substring(0, 16)} ({cred.type})
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <Label>Resource *</Label>
              <select
                className="w-full p-2 border rounded text-sm"
                value={formData.resourceId}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, resourceId: e.target.value }))
                }
              >
                <option value="">Select a resource</option>
                {resources.map((res) => (
                  <option key={res.resourceId} value={res.resourceId}>
                    {res.name} ({res.hostName})
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="login-username">Login Username *</Label>
              <Input
                id="login-username"
                placeholder="e.g. myuser"
                value={formData.loginUsername}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, loginUsername: e.target.value }))
                }
              />
              <p className="text-xs text-muted-foreground">
                SSH/login username on the selected resource.
              </p>
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
                !formData.credentialId ||
                !formData.resourceId ||
                !formData.loginUsername.trim()
              }
            >
              {createMutation.isPending ? "Creating..." : "Create Binding"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <AlertDialog
        open={!!deletingBindingId}
        onOpenChange={(open) => !open && setDeletingBindingId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Binding</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete the binding between &quot;
              {deletingBinding ? getCredentialName(deletingBinding.credentialId) : ""}
              &quot; and &quot;
              {deletingBinding ? getResourceName(deletingBinding.resourceId) : ""}
              &quot;? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteMutation.isPending}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() =>
                deletingBindingId && deleteMutation.mutate(deletingBindingId)
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
