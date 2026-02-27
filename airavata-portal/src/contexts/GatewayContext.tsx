"use client";

import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from "react";
import { useSession } from "next-auth/react";
import { useQuery } from "@tanstack/react-query";
import { gatewaysApi } from "@/lib/api/gateways";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import type { Gateway } from "@/types";

interface GatewayContextType {
  selectedGatewayId: string | null;
  setSelectedGatewayId: (gatewayId: string | null) => void;
  accessibleGateways: Gateway[];
  isLoading: boolean;
  isRootUser: boolean;
  getGatewayName: (gatewayId: string) => string;
  effectiveGatewayId: string | undefined;
  /** True when root user has no gateways – system administration only, rest disabled */
  hasNoGatewayAndIsRoot: boolean;
  /** True when authenticated and no gateways exist – user must create first gateway before using portal */
  needsFirstGateway: boolean;
  /** URL for Home links: /<gateway>, or /admin/gateways when hasNoGatewayAndIsRoot */
  dashboardHref: string;
}

const GatewayContext = createContext<GatewayContextType | undefined>(undefined);

export function GatewayProvider({ children }: { children: ReactNode }) {
  const { data: session } = useSession();
  const { defaultGatewayId, assumeRootWhenNoGateways } = usePortalConfig();
  const [selectedGatewayId, setSelectedGatewayId] = useState<string | null>(null);

  const { data: gateways = [], isLoading } = useQuery({
    queryKey: ["gateways"],
    queryFn: () => gatewaysApi.list(),
    enabled: !!session?.accessToken,
  });

  const isRootFromGateways = gateways.length > 0 && gateways.some((g) => g.gatewayId === "default");
  /** When there are no gateways, we cannot infer root from the list. assumeRootWhenNoGateways from API config treats as root (system-admin-only mode). */
  const isRootUser =
    isRootFromGateways || (gateways.length === 0 && assumeRootWhenNoGateways);
  const effectiveGatewayId = selectedGatewayId || undefined;
  const hasNoGatewayAndIsRoot = gateways.length === 0 && isRootUser;
  const needsFirstGateway = !!session && !isLoading && gateways.length === 0;

  const getGatewayName = useCallback(
    (gatewayId: string): string => {
      const g = gateways.find((x) => x.gatewayId === gatewayId);
      return g?.gatewayName ?? gatewayId;
    },
    [gateways]
  );

  const dashboardHref = hasNoGatewayAndIsRoot
    ? "/admin/gateways"
    : selectedGatewayId && gateways.length > 0
      ? `/${selectedGatewayId}`
      : "/default";

  useEffect(() => {
    if (gateways.length === 0 || selectedGatewayId) return;
    const stored = typeof window !== "undefined" ? sessionStorage.getItem("selectedGatewayId") : null;
    const sessionGw = session?.user?.gatewayId;
    let next: string;
    if (stored && gateways.some((g) => g.gatewayId === stored)) {
      next = stored;
    } else if (sessionGw && gateways.some((g) => g.gatewayId === sessionGw)) {
      next = sessionGw;
    } else {
      next = gateways[0].gatewayId;
    }
    setSelectedGatewayId(next);
    if (typeof window !== "undefined") sessionStorage.setItem("selectedGatewayId", next);
  }, [gateways, session?.user?.gatewayId, selectedGatewayId]);

  useEffect(() => {
    if (selectedGatewayId && typeof window !== "undefined") {
      sessionStorage.setItem("selectedGatewayId", selectedGatewayId);
    }
  }, [selectedGatewayId]);

  return (
    <GatewayContext.Provider
      value={{
        selectedGatewayId,
        setSelectedGatewayId,
        accessibleGateways: gateways,
        isLoading,
        isRootUser,
        getGatewayName,
        effectiveGatewayId,
        hasNoGatewayAndIsRoot,
        needsFirstGateway,
        dashboardHref,
      }}
    >
      {children}
    </GatewayContext.Provider>
  );
}

export function useGateway() {
  const context = useContext(GatewayContext);
  if (context === undefined) {
    throw new Error("useGateway must be used within a GatewayProvider");
  }
  return context;
}
