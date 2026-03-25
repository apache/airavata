"use client";

import { useState } from "react";
import { Plus, Key, Lock, Trash2, MoreVertical, Copy, CheckCircle, Eye } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
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
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { SSHKeyForm } from "@/components/credentials/SSHKeyForm";
import { PasswordCredentialForm } from "@/components/credentials/PasswordCredentialForm";
import { useCredentials, useCreateSSHCredential, useCreatePasswordCredential, useDeleteCredential, useCredentialResourceCounts } from "@/hooks/useCredentials";
import { toast } from "@/hooks/useToast";
import { useSession } from "next-auth/react";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { formatDate } from "@/lib/utils";
import type { CredentialSummary, SSHCredential, PasswordCredential } from "@/lib/api/credentials";
import { useQuery } from "@tanstack/react-query";
import { bindingsApi } from "@/lib/api";
import { Server } from "lucide-react";
import type { ResourceBinding } from "@/types";

// Component to display credential details in the view dialog
function CredentialDetails({
  credential,
  copiedToken,
  onCopyToken,
}: {
  credential: CredentialSummary;
  copiedToken: boolean;
  onCopyToken: () => void;
}) {
  const { data: bindings = [], isLoading: loadingBindings } = useQuery({
    queryKey: ["bindings-by-credential", credential.token],
    queryFn: () => bindingsApi.list({ credentialId: credential.token }),
    enabled: !!credential.token,
  });

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <p className="text-sm font-medium text-muted-foreground">Name</p>
          <p className="text-sm">{credential.name || credential.description || "—"}</p>
        </div>
        <div>
          <p className="text-sm font-medium text-muted-foreground">Type</p>
          <Badge variant="secondary">{credential.type}</Badge>
        </div>
      </div>
      <p className="text-xs text-muted-foreground">Login username is configured per resource binding.</p>

      {credential.description && (
        <div>
          <p className="text-sm font-medium text-muted-foreground">Description</p>
          <p className="text-sm">{credential.description}</p>
        </div>
      )}

      <div>
        <p className="text-sm font-medium text-muted-foreground mb-1">Token</p>
        <div className="flex items-center gap-2">
          <code className="flex-1 text-xs bg-muted p-2 rounded font-mono break-all">
            {credential.token}
          </code>
          <Button variant="outline" size="icon" onClick={onCopyToken}>
            {copiedToken ? <CheckCircle className="h-4 w-4 text-green-600" /> : <Copy className="h-4 w-4" />}
          </Button>
        </div>
      </div>

      {credential.persistedTime && (
        <div>
          <p className="text-sm font-medium text-muted-foreground">Created</p>
          <p className="text-sm">{formatDate(credential.persistedTime)}</p>
        </div>
      )}

      {/* Show resource bindings */}
      <div className="pt-2 border-t">
        <p className="text-sm font-medium text-muted-foreground mb-2">Resource Bindings</p>
        {loadingBindings ? (
          <Skeleton className="h-16 w-full" />
        ) : bindings.length > 0 ? (
          <div className="space-y-2">
            {bindings.map((binding: ResourceBinding) => (
              <div key={binding.bindingId} className="flex items-center gap-2 p-2 bg-muted rounded">
                <Server className="h-4 w-4 text-muted-foreground" />
                <div className="flex-1">
                  <p className="text-sm font-medium font-mono text-xs">{binding.resourceId}</p>
                  <p className="text-xs text-muted-foreground">Login: {binding.loginUsername}</p>
                </div>
                <Badge variant={binding.enabled ? "default" : "secondary"} className="text-xs">
                  {binding.enabled ? "Active" : "Disabled"}
                </Badge>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">No resource bindings</p>
        )}
      </div>
    </div>
  );
}

// Table row for a single credential (resource counts shown in table)
function CredentialTableRow({
  credential,
  resourceCounts,
  onView,
  onDelete,
}: {
  credential: CredentialSummary;
  resourceCounts: { compute: number; storage: number };
  onView: () => void;
  onDelete: () => void;
}) {
  return (
    <TableRow className="cursor-pointer hover:bg-muted/50" onClick={onView}>
      <TableCell className="py-1.5 px-3 font-medium">{credential.name || credential.description || "—"}</TableCell>
      <TableCell className="max-w-[180px] truncate py-1.5 px-3 text-muted-foreground">
        {credential.description || "—"}
      </TableCell>
      <TableCell className="py-1.5 px-3">
        <Badge variant="secondary" className="text-xs">{credential.type}</Badge>
      </TableCell>
      <TableCell className="py-1.5 px-3 font-mono text-xs text-muted-foreground">
        {credential.token.substring(0, 12)}…
      </TableCell>
      <TableCell className="py-1.5 px-3 text-muted-foreground whitespace-nowrap">
        {credential.persistedTime ? formatDate(credential.persistedTime) : "—"}
      </TableCell>
      <TableCell className="py-1.5 px-3 text-muted-foreground tabular-nums">
        {resourceCounts.compute}
      </TableCell>
      <TableCell className="py-1.5 px-3 text-muted-foreground tabular-nums">
        {resourceCounts.storage}
      </TableCell>
      <TableCell className="w-0 py-1.5 px-3" onClick={(e) => e.stopPropagation()}>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="h-7 w-7">
              <MoreVertical className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={onView}>
              <Eye className="h-4 w-4 mr-2" />
              View details
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem className="text-destructive focus:text-destructive" onClick={onDelete}>
              <Trash2 className="h-4 w-4 mr-2" />
              Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </TableCell>
    </TableRow>
  );
}

export default function CredentialsPage() {
  const { data: session } = useSession();
  const { effectiveGatewayId } = useGateway();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [credentialType, setCredentialType] = useState<"ssh" | "password">("ssh");
  
  // View/Edit state
  const [viewingCredential, setViewingCredential] = useState<CredentialSummary | null>(null);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [copiedToken, setCopiedToken] = useState(false);
  
  // Delete state
  const [deletingCredential, setDeletingCredential] = useState<CredentialSummary | null>(null);
  
  const { defaultGatewayId } = usePortalConfig();
  const gatewayId =
    effectiveGatewayId ||
    session?.user?.gatewayId ||
    defaultGatewayId;
  
  const { data: credentials, isLoading } = useCredentials();
  const createSSH = useCreateSSHCredential();
  const createPassword = useCreatePasswordCredential();
  const deleteCredential = useDeleteCredential();
  const { counts: resourceCounts } = useCredentialResourceCounts();

  const handleCreateSSH = async (credential: SSHCredential) => {
    try {
      await createSSH.mutateAsync(credential);
      toast({ title: "SSH credential created", description: "Your SSH credential has been stored successfully." });
      setIsCreateOpen(false);
    } catch (error: unknown) {
      console.error("SSH credential creation error:", error);
      let errorMessage = "Failed to create credential";
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      // Check for network/connection errors
      if (error && typeof error === 'object' && 'code' in error && (error.code === 'ERR_NETWORK' || 
          ('message' in error && typeof error.message === 'string' && error.message.includes('Network Error')))) {
        errorMessage = "Cannot connect to the server. Please ensure the backend is running.";
      } else if (error && typeof error === 'object' && 'status' in error && error.status === 500) {
        errorMessage = `Server error: ${error instanceof Error ? error.message : 'Internal server error. Check backend logs for details.'}`;
      }
      toast({ title: "Error creating credential", description: errorMessage, variant: "destructive" });
    }
  };

  const handleCreatePassword = async (credential: PasswordCredential) => {
    try {
      await createPassword.mutateAsync(credential);
      toast({ title: "Password credential created", description: "Your password credential has been stored successfully." });
      setIsCreateOpen(false);
    } catch (error: unknown) {
      console.error("Password credential creation error:", error);
      let errorMessage = "Failed to create credential";
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      // Check for network/connection errors
      if (error && typeof error === 'object' && 'code' in error && (error.code === 'ERR_NETWORK' || 
          ('message' in error && typeof error.message === 'string' && error.message.includes('Network Error')))) {
        errorMessage = "Cannot connect to the server. Please ensure the backend is running.";
      } else if (error && typeof error === 'object' && 'status' in error && error.status === 500) {
        errorMessage = `Server error: ${error instanceof Error ? error.message : 'Internal server error. Check backend logs for details.'}`;
      }
      toast({ title: "Error creating credential", description: errorMessage, variant: "destructive" });
    }
  };

  const handleOpenView = (credential: CredentialSummary) => {
    setViewingCredential(credential);
    setIsViewOpen(true);
    setCopiedToken(false);
  };

  const handleCopyToken = async () => {
    if (viewingCredential) {
      await navigator.clipboard.writeText(viewingCredential.token);
      setCopiedToken(true);
      toast({ title: "Token copied", description: "Credential token copied to clipboard." });
      setTimeout(() => setCopiedToken(false), 2000);
    }
  };

  const handleDelete = async () => {
    if (!deletingCredential) return;
    try {
      await deleteCredential.mutateAsync(deletingCredential.token);
      toast({ title: "Credential deleted", description: "The credential has been removed." });
      setDeletingCredential(null);
      if (viewingCredential?.token === deletingCredential.token) {
        setIsViewOpen(false);
        setViewingCredential(null);
      }
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Failed to delete credential";
      toast({ title: "Error", description: errorMessage, variant: "destructive" });
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Credentials</h1>
          <p className="text-muted-foreground">
            Manage SSH keys and password credentials for compute resources
          </p>
        </div>
        <Button onClick={() => setIsCreateOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          New Credential
        </Button>
      </div>

      {isLoading ? (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-3">Name</TableHead>
                <TableHead className="h-9 px-3">Description</TableHead>
                <TableHead className="h-9 px-3">Type</TableHead>
                <TableHead className="h-9 px-3">Token</TableHead>
                <TableHead className="h-9 px-3">Created</TableHead>
                <TableHead className="h-9 px-3 text-right">Compute</TableHead>
                <TableHead className="h-9 px-3 text-right">Storage</TableHead>
                <TableHead className="w-0 h-9 px-3" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {[...Array(5)].map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={8} className="h-12">
                    <Skeleton className="h-4 w-full" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      ) : !credentials || credentials.length === 0 ? (
        <Card>
          <CardContent className="py-16">
            <div className="text-center">
              <Key className="mx-auto h-12 w-12 text-muted-foreground/50" />
              <h3 className="mt-4 text-lg font-semibold">No credentials</h3>
              <p className="text-muted-foreground mt-2">
                Create your first credential to enable secure connections
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
                <TableHead className="h-9 px-3">Description</TableHead>
                <TableHead className="h-9 px-3">Type</TableHead>
                <TableHead className="h-9 px-3">Token</TableHead>
                <TableHead className="h-9 px-3">Created</TableHead>
                <TableHead className="h-9 px-3 text-right">Compute</TableHead>
                <TableHead className="h-9 px-3 text-right">Storage</TableHead>
                <TableHead className="w-0 h-9 px-3" />
              </TableRow>
            </TableHeader>
            <TableBody>
              {credentials.map((credential) => (
                <CredentialTableRow
                  key={credential.token}
                  credential={credential}
                  resourceCounts={resourceCounts[credential.token] || { compute: 0, storage: 0 }}
                  onView={() => handleOpenView(credential)}
                  onDelete={() => setDeletingCredential(credential)}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Create Credential Dialog */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create New Credential</DialogTitle>
            <DialogDescription>
              Add a new SSH key or password credential for connecting to compute resources
            </DialogDescription>
          </DialogHeader>

          <Tabs value={credentialType} onValueChange={(v) => setCredentialType(v as "ssh" | "password")}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="ssh">
                <Key className="mr-2 h-4 w-4" />
                SSH Key
              </TabsTrigger>
              <TabsTrigger value="password">
                <Lock className="mr-2 h-4 w-4" />
                Password
              </TabsTrigger>
            </TabsList>

            <TabsContent value="ssh" className="mt-6">
              <SSHKeyForm
                onSubmit={handleCreateSSH}
                onCancel={() => setIsCreateOpen(false)}
                isLoading={createSSH.isPending}
                gatewayId={gatewayId}
              />
            </TabsContent>

            <TabsContent value="password" className="mt-6">
              <PasswordCredentialForm
                onSubmit={handleCreatePassword}
                onCancel={() => setIsCreateOpen(false)}
                isLoading={createPassword.isPending}
                gatewayId={gatewayId}
              />
            </TabsContent>
          </Tabs>
        </DialogContent>
      </Dialog>

      {/* View Credential Dialog */}
      <Dialog open={isViewOpen} onOpenChange={(open) => { setIsViewOpen(open); if (!open) setViewingCredential(null); }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              {viewingCredential?.type === "SSH" ? (
                <Key className="h-5 w-5 text-green-600" />
              ) : (
                <Lock className="h-5 w-5 text-orange-600" />
              )}
              Credential Details
            </DialogTitle>
            <DialogDescription>
              View credential information. Credentials cannot be edited for security reasons.
            </DialogDescription>
          </DialogHeader>
          
          {viewingCredential && (
            <CredentialDetails
              credential={viewingCredential}
              copiedToken={copiedToken}
              onCopyToken={handleCopyToken}
            />
          )}
          
          <DialogFooter className="gap-2 sm:gap-0">
            <Button variant="outline" onClick={() => { setIsViewOpen(false); setViewingCredential(null); }}>
              Close
            </Button>
            <Button 
              variant="destructive" 
              onClick={() => { if (viewingCredential) setDeletingCredential(viewingCredential); }}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={!!deletingCredential} onOpenChange={(open) => !open && setDeletingCredential(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Credential</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete the credential &quot;{deletingCredential?.name || deletingCredential?.description || deletingCredential?.token.substring(0, 12)}&quot;? 
              This may affect compute resources that use this credential. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleteCredential.isPending}>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDelete} 
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteCredential.isPending}
            >
              {deleteCredential.isPending ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
