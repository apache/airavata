"use client";

import { createContext, useContext, ReactNode, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchPortalConfig, type PortalConfig } from "@/lib/api/config";

const DEFAULTS: PortalConfig = {
  defaultGatewayId: "default",
  assumeRootWhenNoGateways: false,
  appVersion: "",
};

const PortalConfigContext = createContext<PortalConfig>(DEFAULTS);

export function PortalConfigProvider({ children }: { children: ReactNode }) {
  const { data } = useQuery({
    queryKey: ["portalConfig"],
    queryFn: fetchPortalConfig,
    staleTime: 10 * 60 * 1000,
    gcTime: 60 * 60 * 1000,
    retry: 2,
    refetchOnWindowFocus: false,
  });

  const config = useMemo(
    () => (data ?? DEFAULTS),
    [data]
  );

  return (
    <PortalConfigContext.Provider value={config}>
      {children}
    </PortalConfigContext.Provider>
  );
}

export function usePortalConfig(): PortalConfig {
  const context = useContext(PortalConfigContext);
  return context ?? DEFAULTS;
}
