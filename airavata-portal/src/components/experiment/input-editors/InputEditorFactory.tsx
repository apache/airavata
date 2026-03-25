"use client";

import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Link2 } from "lucide-react";
import type { InputDataObjectType } from "@/types";
import type { ArtifactModel } from "@/types";
import { DataType } from "@/types";
import { useSession } from "next-auth/react";
import { useGateway } from "@/contexts/GatewayContext";
import { useAccessibleArtifacts } from "@/hooks";

interface Props {
  input: InputDataObjectType;
  value?: string;
  onChange: (value: string | undefined) => void;
}

function artifactToUri(artifact: ArtifactModel): string {
  return `airavata-dp://${artifact.gatewayId}/${artifact.artifactUri}`;
}

function isDpUri(v: string): boolean {
  return typeof v === "string" && v.startsWith("airavata-dp://");
}

export function InputEditorFactory({ input, value, onChange }: Props) {
  const { data: session } = useSession();
  const { effectiveGatewayId } = useGateway();
  const userId = (session?.user as { id?: string; email?: string })?.id ?? (session?.user as { email?: string })?.email ?? "";
  const gatewayId = effectiveGatewayId ?? "";
  const { data: accessibleProducts = [] } = useAccessibleArtifacts(userId, gatewayId, { pageSize: 50 });

  switch (input.type) {
    case DataType.STRING:
      return (
        <Input
          value={value || ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={`Enter ${input.name}`}
          required={input.isRequired}
        />
      );

    case DataType.INTEGER:
      return (
        <Input
          type="number"
          value={value || ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={`Enter ${input.name}`}
          required={input.isRequired}
        />
      );

    case DataType.FLOAT:
      return (
        <Input
          type="number"
          step="any"
          value={value || ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={`Enter ${input.name}`}
          required={input.isRequired}
        />
      );


    case DataType.STDIN:
      return (
        <Input
          value={value || ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={`Enter ${input.name} (standard input)`}
          required={input.isRequired}
        />
      );

    case DataType.URI:
    case DataType.URI_COLLECTION: {
      const canLink = !!userId && !!gatewayId && accessibleProducts.length > 0;
      const currentDpUri = value && isDpUri(value) ? value : "";
      const selectedProduct = currentDpUri
        ? accessibleProducts.find((p) => artifactToUri(p) === currentDpUri)
        : null;
      const selectedUri = selectedProduct ? selectedProduct.artifactUri : "";

      return (
        <div className="space-y-2">
          {canLink && (
            <div className="flex flex-col gap-2">
              <span className="text-sm font-medium text-muted-foreground flex items-center gap-2">
                <Link2 className="h-4 w-4" />
                Link artifact
              </span>
              <Select
                value={selectedUri || "__none__"}
                onValueChange={(artifactUri) => {
                  if (artifactUri === "__none__") {
                    onChange(undefined);
                    return;
                  }
                  const p = accessibleProducts.find((x) => x.artifactUri === artifactUri);
                  if (p) onChange(artifactToUri(p));
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Choose an artifact…" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="__none__">None (enter path or URI below)</SelectItem>
                  {accessibleProducts.map((p) => (
                    <SelectItem key={p.artifactUri} value={p.artifactUri}>
                      {p.name}
                      {p.primaryStorageResourceId && p.primaryFilePath
                        ? ` — ${p.primaryStorageResourceId}:${p.primaryFilePath}`
                        : ""}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {selectedProduct?.primaryStorageResourceId != null && selectedProduct?.primaryFilePath != null && (
                <p className="text-xs text-muted-foreground">
                  Primary storage: {selectedProduct.primaryStorageResourceId}
                  {selectedProduct.primaryFilePath ? ` — ${selectedProduct.primaryFilePath}` : ""}
                </p>
              )}
            </div>
          )}
          <Input
            value={value || ""}
            onChange={(e) => onChange(e.target.value)}
            placeholder="File path or URI (e.g. airavata-dp://gateway/product-uri)"
            required={input.isRequired}
          />
          <p className="text-xs text-muted-foreground">
            Enter a file path from storage, link a data product above, or use an airavata-dp:// URI
          </p>
        </div>
      );
    }

    default:
      return (
        <Textarea
          value={value || ""}
          onChange={(e) => onChange(e.target.value)}
          placeholder={`Enter ${input.name}`}
          required={input.isRequired}
          rows={3}
        />
      );
  }
}
