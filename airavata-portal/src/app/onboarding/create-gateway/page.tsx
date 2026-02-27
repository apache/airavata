"use client";

import { useRouter } from "next/navigation";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { Building2 } from "lucide-react";
import { GatewayForm } from "@/components/gateways/GatewayForm";
import { gatewaysApi } from "@/lib/api/gateways";
import { toast } from "@/hooks/useToast";
import type { Gateway } from "@/types";

export default function OnboardingCreateGatewayPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: (data: Partial<Gateway>) => gatewaysApi.create(data),
    onSuccess: (result) => {
      const gatewayName = result?.gatewayId;
      queryClient.invalidateQueries({ queryKey: ["gateways"] });
      toast({
        title: "Gateway created",
        description: "You can now use the portal.",
      });
      if (gatewayName) {
        router.replace(`/${gatewayName}`);
      } else {
        router.replace("/default");
      }
    },
    onError: (error: Error) => {
      toast({
        title: "Error",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-muted/30 px-4 py-12">
      <div className="w-full max-w-2xl space-y-6">
        <div className="flex flex-col items-center gap-3 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary/10">
            <Building2 className="h-8 w-8 text-primary" />
          </div>
          <h1 className="text-2xl font-semibold tracking-tight">
            Create your first gateway
          </h1>
          <p className="text-muted-foreground">
            You need at least one gateway to use the portal. Create one below to
            get started.
          </p>
        </div>
        <GatewayForm
          onSubmit={async (formData) => {
            await createMutation.mutateAsync(formData);
          }}
          onCancel={() => {}}
          isLoading={createMutation.isPending}
        />
      </div>
    </div>
  );
}
