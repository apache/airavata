"use client";

import { useState } from "react";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue, SelectSeparator } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Key, Plus } from "lucide-react";
import { useGateway } from "@/contexts/GatewayContext";
import { useCredentials, useCreateSSHCredential, useCreatePasswordCredential } from "@/hooks/useCredentials";
import { SSHKeyForm } from "@/components/credentials/SSHKeyForm";
import { PasswordCredentialForm } from "@/components/credentials/PasswordCredentialForm";
import { toast } from "@/hooks/useToast";
import type { CredentialSummary } from "@/lib/api/credentials";
import type { SSHCredential, PasswordCredential } from "@/lib/api/credentials";
import { useSession } from "next-auth/react";

const CREATE_NEW_VALUE = "__create_new__";

export interface CredentialPickerProps {
  value: string;
  onChange: (token: string) => void;
  /** "SSH" to show only SSH credentials (e.g. for compute/storage); "all" for both */
  filter?: "SSH" | "all";
  placeholder?: string;
  disabled?: boolean;
  /** Label above the picker */
  label?: string;
  /** Optional inline action (e.g., test button) rendered next to the select */
  inlineAction?: React.ReactNode;
  /** Optional helper text shown below the select in muted style */
  helperText?: React.ReactNode;
}

export function CredentialPicker({
  value,
  onChange,
  filter = "all",
  placeholder = "Select credential",
  disabled = false,
  label = "Credential",
  inlineAction,
  helperText,
}: CredentialPickerProps) {
  const { effectiveGatewayId } = useGateway();
  const gatewayId = effectiveGatewayId || "";
  const { data: session } = useSession();
  const userId = (session?.user as { email?: string })?.email ?? "";

  const { data: credentials = [], isLoading: credentialsLoading } = useCredentials();
  const [addOpen, setAddOpen] = useState(false);
  const [addTab, setAddTab] = useState<"SSH" | "PASSWORD">("SSH");

  const createSSH = useCreateSSHCredential();
  const createPassword = useCreatePasswordCredential();

  const filtered: CredentialSummary[] =
    filter === "SSH" ? credentials.filter((c) => c.type === "SSH") : credentials;

  const handleCreateSSH = async (credential: SSHCredential) => {
    const result = await createSSH.mutateAsync({ ...credential, userId });
    toast({ title: "SSH credential created" });
    setAddOpen(false);
    if (result?.token) onChange(result.token);
  };

  const handleCreatePassword = async (credential: PasswordCredential) => {
    const result = await createPassword.mutateAsync({ ...credential, userId });
    toast({ title: "Password credential created" });
    setAddOpen(false);
    if (result?.token) onChange(result.token);
  };

  // Key to force Select re-render to clear "Create New" selection
  const [selectKey, setSelectKey] = useState(0);

  const handleValueChange = (newValue: string) => {
    if (newValue === CREATE_NEW_VALUE) {
      // Immediately reset the Select to clear "Create New" selection, then open modal
      setSelectKey((k) => k + 1);
      setAddOpen(true);
    } else {
      onChange(newValue);
    }
  };

  return (
    <div className="space-y-2">
      <Label>{label}</Label>
      <div className="flex items-center gap-2">
        <Select key={selectKey} value={value || ""} onValueChange={handleValueChange} disabled={disabled || credentialsLoading}>
          <SelectTrigger className="flex-1">
            <SelectValue placeholder={credentialsLoading ? "Loading..." : placeholder} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={CREATE_NEW_VALUE}>
              <div className="flex items-center gap-2 text-primary">
                <Plus className="h-4 w-4" />
                <span>Create New</span>
              </div>
            </SelectItem>
            {filtered.length > 0 && <SelectSeparator />}
            {filtered.map((c) => (
              <SelectItem key={c.token} value={c.token}>
                <div className="flex items-center gap-2">
                  <Key className="h-4 w-4 text-muted-foreground" />
                  <span>{c.name || c.description || c.token.substring(0, 12)}</span>
                  <span className="text-muted-foreground text-xs">({c.type})</span>
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {inlineAction}
      </div>
      {helperText && (
        <p className="text-sm text-muted-foreground">{helperText}</p>
      )}

      <Dialog open={addOpen} onOpenChange={setAddOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Create Credential</DialogTitle>
            <DialogDescription>Create a new SSH key or password credential</DialogDescription>
          </DialogHeader>
          <Tabs value={addTab} onValueChange={(v) => setAddTab(v as "SSH" | "PASSWORD")}>
            <TabsList>
              <TabsTrigger value="SSH">SSH Key</TabsTrigger>
              <TabsTrigger value="PASSWORD">Password</TabsTrigger>
            </TabsList>
            <TabsContent value="SSH">
              <SSHKeyForm
                onSubmit={handleCreateSSH}
                onCancel={() => setAddOpen(false)}
                isLoading={createSSH.isPending}
                gatewayId={gatewayId}
              />
            </TabsContent>
            <TabsContent value="PASSWORD">
              <PasswordCredentialForm
                onSubmit={handleCreatePassword}
                onCancel={() => setAddOpen(false)}
                isLoading={createPassword.isPending}
                gatewayId={gatewayId}
              />
            </TabsContent>
          </Tabs>
        </DialogContent>
      </Dialog>
    </div>
  );
}
