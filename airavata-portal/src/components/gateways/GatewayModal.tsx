"use client";

import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { GatewayForm } from "./GatewayForm";
import { gatewaysApi } from "@/lib/api";
import { toast } from "@/hooks/useToast";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import type { Gateway } from "@/types";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  gateway?: Gateway;
}

export function GatewayModal({ open, onOpenChange, gateway }: Props) {
  const queryClient = useQueryClient();
  const isEditing = !!gateway;

  const createMutation = useMutation({
    mutationFn: (data: Partial<Gateway>) => gatewaysApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
      toast({ title: "Gateway created", description: "The gateway has been added successfully." });
      onOpenChange(false);
    },
    onError: (error: Error) => {
      toast({ title: "Error", description: error.message, variant: "destructive" });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ gatewayId, data }: { gatewayId: string; data: Partial<Gateway> }) =>
      gatewaysApi.update(gatewayId, data),
    onSuccess: (_, { gatewayId }) => {
      queryClient.invalidateQueries({ queryKey: ["gateway", gatewayId] });
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
      toast({ title: "Gateway updated", description: "The gateway has been updated successfully." });
      onOpenChange(false);
    },
    onError: (error: Error) => {
      toast({ title: "Error", description: error.message, variant: "destructive" });
    },
  });

  const isLoading = createMutation.isPending || updateMutation.isPending;

  const handleSubmit = async (formData: Partial<Gateway>) => {
    if (isEditing) {
      await updateMutation.mutateAsync({ gatewayId: gateway.gatewayId, data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
  };

  const handleCancel = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit Gateway" : "Add Gateway"}</DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Update the gateway settings."
              : "Add a new gateway. Gateway ID is a unique identifier (e.g. my-gateway)."}
          </DialogDescription>
        </DialogHeader>
        <GatewayForm
          gateway={gateway}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isLoading={isLoading}
        />
      </DialogContent>
    </Dialog>
  );
}
