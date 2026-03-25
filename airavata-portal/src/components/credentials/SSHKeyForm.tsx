"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Upload, Key, Download, CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { apiClient } from "@/lib/api/client";
import { toast } from "@/hooks/useToast";
import type { SSHCredential } from "@/lib/api/credentials";

interface Props {
  onSubmit: (credential: SSHCredential) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
  gatewayId: string;
}

export function SSHKeyForm({ onSubmit, onCancel, isLoading, gatewayId }: Props) {
  const [formData, setFormData] = useState({
    name: "",
    publicKey: "",
    privateKey: "",
    passphrase: "",
    description: "",
  });
  const [mode, setMode] = useState<"upload" | "generate">("upload");
  const [isGenerating, setIsGenerating] = useState(false);
  const [testHost, setTestHost] = useState("");
  const [testPort, setTestPort] = useState("22");
  const [isTesting, setIsTesting] = useState(false);
  const [testResult, setTestResult] = useState<{ success: boolean; message: string } | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name?.trim() || !formData.publicKey || !formData.privateKey) {
      toast({
        title: "Validation Error",
        description: "Name, public key, and private key are required",
        variant: "destructive",
      });
      return;
    }

    await onSubmit({
      gatewayId,
      name: formData.name.trim(),
      publicKey: formData.publicKey,
      privateKey: formData.privateKey,
      passphrase: formData.passphrase || undefined,
      description: formData.description || undefined,
    });
  };

  const handleFileUpload = async (
    e: React.ChangeEvent<HTMLInputElement>,
    field: "publicKey" | "privateKey"
  ) => {
    const file = e.target.files?.[0];
    if (file) {
      const text = await file.text();
      setFormData((prev) => ({ ...prev, [field]: text }));
    }
  };

  const handleGenerate = async () => {
    setIsGenerating(true);
    try {
      // Try backend first, fallback to Next.js API route
      let response;
      try {
        response = await apiClient.post<{ privateKey: string; publicKey: string; keySize?: string }>("/api/v1/ssh-keygen?keySize=2048");
      } catch (backendError) {
        // Fallback to Next.js API route
        const nextResponse = await fetch("/api/v1/ssh-keygen?keySize=2048", {
          method: "POST",
        });
        if (!nextResponse.ok) throw new Error("Key generation failed");
        response = await nextResponse.json();
      }
      
      setFormData((prev) => ({
        ...prev,
        privateKey: response.privateKey,
        publicKey: response.publicKey,
      }));
      toast({
        title: "Keys Generated",
        description: "SSH key pair has been generated successfully.",
      });
    } catch (error) {
      toast({
        title: "Generation Failed",
        description: error instanceof Error ? error.message : "Failed to generate SSH keys",
        variant: "destructive",
      });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDownloadKeys = () => {
    if (!formData.privateKey || !formData.publicKey) {
      toast({
        title: "No Keys",
        description: "Please generate or upload keys first",
        variant: "destructive",
      });
      return;
    }

    // Download private key
    const privateBlob = new Blob([formData.privateKey], { type: "text/plain" });
    const privateUrl = URL.createObjectURL(privateBlob);
    const privateLink = document.createElement("a");
    privateLink.href = privateUrl;
    privateLink.download = "id_rsa";
    privateLink.click();
    URL.revokeObjectURL(privateUrl);

    // Download public key
    const publicBlob = new Blob([formData.publicKey], { type: "text/plain" });
    const publicUrl = URL.createObjectURL(publicBlob);
    const publicLink = document.createElement("a");
    publicLink.href = publicUrl;
    publicLink.download = "id_rsa.pub";
    publicLink.click();
    URL.revokeObjectURL(publicUrl);

    toast({
      title: "Keys Downloaded",
      description: "Private and public keys have been downloaded.",
    });
  };

  const handleTestConnection = async () => {
    if (!testHost || !formData.privateKey) {
      toast({
        title: "Missing Information",
        description: "Please provide host and generate/upload keys first",
        variant: "destructive",
      });
      return;
    }

    setIsTesting(true);
    setTestResult(null);

    try {
      const result = await apiClient.post<{ success: boolean; message: string; details?: string }>("/api/v1/connectivity-test/ssh", {
        host: testHost,
        port: parseInt(testPort) || 22,
        username: undefined,
        privateKey: formData.privateKey,
      });

      setTestResult(result);
      if (result.success) {
        toast({
          title: "Connection Successful",
          description: result.message,
        });
      } else {
        toast({
          title: "Connection Failed",
          description: result.message,
          variant: "destructive",
        });
      }
    } catch (error) {
      setTestResult({
        success: false,
        message: error instanceof Error ? error.message : "Connection test failed",
      });
      toast({
        title: "Test Failed",
        description: error instanceof Error ? error.message : "Failed to test connection",
        variant: "destructive",
      });
    } finally {
      setIsTesting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <Tabs value={mode} onValueChange={(v) => setMode(v as "upload" | "generate")}>
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="upload">Upload Keys</TabsTrigger>
          <TabsTrigger value="generate">Generate Keys</TabsTrigger>
        </TabsList>

        <TabsContent value="upload" className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
              placeholder="e.g. Laptop SSH, HPC login"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Input
              id="description"
              value={formData.description}
              onChange={(e) => setFormData((prev) => ({ ...prev, description: e.target.value }))}
              placeholder="Optional notes about this credential"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="public-key">Public Key *</Label>
            <div className="flex gap-2">
              <Textarea
                id="public-key"
                value={formData.publicKey}
                onChange={(e) => setFormData((prev) => ({ ...prev, publicKey: e.target.value }))}
                placeholder="Paste public key content..."
                className="font-mono text-sm"
                rows={4}
              />
              <div>
                <Label htmlFor="public-key-file" className="cursor-pointer">
                  <div className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2">
                    <Upload className="h-4 w-4" />
                  </div>
                </Label>
                <input
                  id="public-key-file"
                  type="file"
                  accept=".pub"
                  className="hidden"
                  onChange={(e) => handleFileUpload(e, "publicKey")}
                />
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="private-key">Private Key *</Label>
            <div className="flex gap-2">
              <Textarea
                id="private-key"
                value={formData.privateKey}
                onChange={(e) => setFormData((prev) => ({ ...prev, privateKey: e.target.value }))}
                placeholder="Paste private key content..."
                className="font-mono text-sm"
                rows={4}
              />
              <div>
                <Label htmlFor="private-key-file" className="cursor-pointer">
                  <div className="inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-input bg-background hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2">
                    <Upload className="h-4 w-4" />
                  </div>
                </Label>
                <input
                  id="private-key-file"
                  type="file"
                  className="hidden"
                  onChange={(e) => handleFileUpload(e, "privateKey")}
                />
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="passphrase">Passphrase</Label>
            <Input
              id="passphrase"
              type="password"
              value={formData.passphrase}
              onChange={(e) => setFormData((prev) => ({ ...prev, passphrase: e.target.value }))}
              placeholder="Enter passphrase if key is encrypted"
            />
          </div>
        </TabsContent>

        <TabsContent value="generate" className="space-y-4">
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="gen-name">Name *</Label>
              <Input
                id="gen-name"
                value={formData.name}
                onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
                placeholder="e.g. Laptop SSH, HPC login"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="gen-description">Description</Label>
              <Input
                id="gen-description"
                value={formData.description}
                onChange={(e) => setFormData((prev) => ({ ...prev, description: e.target.value }))}
                placeholder="Optional notes about this credential"
              />
            </div>

            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={handleGenerate}
                disabled={isGenerating}
                className="flex-1"
              >
                {isGenerating ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Key className="mr-2 h-4 w-4" />
                    Generate Keys
                  </>
                )}
              </Button>
              {formData.privateKey && formData.publicKey && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleDownloadKeys}
                >
                  <Download className="mr-2 h-4 w-4" />
                  Download Keys
                </Button>
              )}
            </div>

            {formData.privateKey && formData.publicKey && (
              <div className="space-y-4 p-4 border rounded-lg bg-muted/50">
                <div className="space-y-2">
                  <Label>Generated Public Key</Label>
                  <Textarea
                    value={formData.publicKey}
                    readOnly
                    className="font-mono text-sm"
                    rows={3}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Generated Private Key</Label>
                  <Textarea
                    value={formData.privateKey.substring(0, 100) + "..."}
                    readOnly
                    className="font-mono text-sm"
                    rows={2}
                  />
                  <p className="text-xs text-muted-foreground">
                    Private key is stored securely. Full key will be saved when you create the credential.
                  </p>
                </div>

                {/* Connection Testing */}
                <div className="space-y-3 pt-4 border-t">
                  <Label>Test Connection (Optional)</Label>
                  <div className="flex gap-2">
                    <Input
                      placeholder="Host (e.g., localhost)"
                      value={testHost}
                      onChange={(e) => setTestHost(e.target.value)}
                      className="flex-1"
                    />
                    <Input
                      placeholder="Port"
                      value={testPort}
                      onChange={(e) => setTestPort(e.target.value)}
                      type="number"
                      className="w-24"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handleTestConnection}
                      disabled={isTesting || !testHost}
                    >
                      {isTesting ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        "Test"
                      )}
                    </Button>
                  </div>
                  {testResult && (
                    <div className={`flex items-center gap-2 p-2 rounded ${
                      testResult.success ? "bg-green-50 text-green-700" : "bg-red-50 text-red-700"
                    }`}>
                      {testResult.success ? (
                        <CheckCircle2 className="h-4 w-4" />
                      ) : (
                        <XCircle className="h-4 w-4" />
                      )}
                      <span className="text-sm">{testResult.message}</span>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </TabsContent>
      </Tabs>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading}>
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading || (mode === "generate" && (!formData.privateKey || !formData.publicKey))}>
          {isLoading ? "Creating..." : "Create Credential"}
        </Button>
      </div>
    </form>
  );
}
