"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { Gateway } from "@/types";

interface Props {
  gateway?: Gateway;
  onSubmit: (gateway: Partial<Gateway>) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
}

export function GatewayForm({ gateway, onSubmit, onCancel, isLoading }: Props) {
  const [formData, setFormData] = useState<Partial<Gateway>>({
    gatewayId: gateway?.gatewayId || "",
    gatewayName: gateway?.gatewayName || "",
    gatewayURL: gateway?.gatewayURL || "",
    gatewayAdminEmail: gateway?.gatewayAdminEmail || "",
    gatewayApprovalStatus: gateway?.gatewayApprovalStatus || "APPROVED",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.gatewayId || !formData.gatewayName) {
      alert("Gateway ID and name are required");
      return;
    }
    await onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Gateway Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="gatewayId">Gateway ID *</Label>
            <Input
              id="gatewayId"
              value={formData.gatewayId}
              onChange={(e) => setFormData({ ...formData, gatewayId: e.target.value })}
              placeholder="e.g., my-gateway"
              required
              disabled={!!gateway}
            />
            {gateway && (
              <p className="text-xs text-muted-foreground">Gateway ID cannot be changed</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="gatewayName">Gateway Name *</Label>
            <Input
              id="gatewayName"
              value={formData.gatewayName}
              onChange={(e) => setFormData({ ...formData, gatewayName: e.target.value })}
              placeholder="My Science Gateway"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="gatewayURL">Gateway URL</Label>
            <Input
              id="gatewayURL"
              type="url"
              value={formData.gatewayURL}
              onChange={(e) => setFormData({ ...formData, gatewayURL: e.target.value })}
              placeholder="https://gateway.example.com"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="gatewayAdminEmail">Admin Email</Label>
            <Input
              id="gatewayAdminEmail"
              type="email"
              value={formData.gatewayAdminEmail}
              onChange={(e) => setFormData({ ...formData, gatewayAdminEmail: e.target.value })}
              placeholder="admin@example.com"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="gatewayDescription">Description</Label>
            <Textarea
              id="gatewayDescription"
              value={formData.gatewayPublicAbstract || ""}
              onChange={(e) => setFormData({ ...formData, gatewayPublicAbstract: e.target.value })}
              placeholder="Describe this gateway"
              rows={3}
            />
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading}>
          Cancel
        </Button>
        <Button type="submit" disabled={isLoading}>
          {isLoading ? "Saving..." : gateway ? "Update Gateway" : "Create Gateway"}
        </Button>
      </div>
    </form>
  );
}
