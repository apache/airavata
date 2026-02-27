"use client";

import { SessionProvider, useSession, signOut } from "next-auth/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, useEffect, useRef, Suspense } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Toaster } from "@/components/ui/toaster";
import { PortalConfigProvider } from "@/contexts/PortalConfigContext";
import { GatewayProvider, useGateway } from "@/contexts/GatewayContext";
import { CreateExperimentModalProvider } from "@/contexts/CreateExperimentModalContext";
import { UserRoleProvider } from "@/contexts/AdvancedFeaturesContext";

interface ProvidersProps {
  children: React.ReactNode;
}

// When backend was reset (e.g. cold-start) but portal kept running, session cookie can be stale.
// Validate session against Keycloak on load; if 401, redirect to login?session_expired=1 so
// middleware allows us through (avoids redirect loop). On /login with session_expired=1 we clear session.
function SessionRecovery({ children }: { children: React.ReactNode }) {
  const { data: session, status } = useSession();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();
  const didCheck = useRef(false);
  const didClearOnLogin = useRef(false);

  // On /login?session_expired=1: clear stale session so user sees login form (breaks redirect loop).
  useEffect(() => {
    if (pathname !== "/login") return;
    if (searchParams.get("session_expired") !== "1") return;
    if (didClearOnLogin.current) return;
    didClearOnLogin.current = true;
    signOut({ redirect: false }).then(() => {
      router.replace("/login", { scroll: false });
    });
  }, [pathname, searchParams, router]);

  // When authenticated and not on login: validate session; if 401, go to login with session_expired=1.
  useEffect(() => {
    if (status !== "authenticated" || !session) return;
    if (pathname === "/login") return;
    if (didCheck.current) return;
    didCheck.current = true;

    fetch("/api/auth/session-check", { credentials: "include" })
      .then((res) => {
        if (res.status === 401) {
          window.location.href = "/login?session_expired=1";
        }
      })
      .catch(() => {});
  }, [status, session, pathname]);

  return <>{children}</>;
}

// Redirect to onboarding when authenticated and no gateways exist (except when already on onboarding page)
function OnboardingRedirect({ children }: { children: React.ReactNode }) {
  const { data: session, status } = useSession();
  const { needsFirstGateway } = useGateway();
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    if (status === "loading") return;
    if (!session || !needsFirstGateway) return;
    if (pathname === "/onboarding/create-gateway") return;
    router.replace("/onboarding/create-gateway");
  }, [session, status, needsFirstGateway, pathname, router]);

  return <>{children}</>;
}

// Wrapper component to provide user role context with gateway context values
function UserRoleProviderWrapper({ children }: { children: React.ReactNode }) {
  const { isRootUser, accessibleGateways } = useGateway();
  const hasGatewayAccess = accessibleGateways.length > 0;

  return (
    <UserRoleProvider isRootUser={isRootUser} hasGatewayAccess={hasGatewayAccess}>
      {children}
    </UserRoleProvider>
  );
}

export function Providers({ children }: ProvidersProps) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            // Data is considered fresh for 5 minutes
            staleTime: 5 * 60 * 1000,
            // Keep unused data in cache for 30 minutes before garbage collection
            gcTime: 30 * 60 * 1000,
            // Don't refetch on window focus - reduces unnecessary API calls
            refetchOnWindowFocus: false,
            // Don't refetch when reconnecting - let staleTime handle it
            refetchOnReconnect: false,
            // Retry failed requests up to 2 times with exponential backoff
            retry: 2,
            retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
            // Don't retry on 4xx errors (client errors)
            retryOnMount: true,
          },
          mutations: {
            // Don't retry mutations by default
            retry: false,
          },
        },
      })
  );

  return (
    <SessionProvider>
      <Suspense fallback={null}>
        <SessionRecovery>
        <QueryClientProvider client={queryClient}>
          <PortalConfigProvider>
          <GatewayProvider>
            <OnboardingRedirect>
              <UserRoleProviderWrapper>
                <CreateExperimentModalProvider>
                  {children}
                  <Toaster />
                </CreateExperimentModalProvider>
              </UserRoleProviderWrapper>
            </OnboardingRedirect>
          </GatewayProvider>
          </PortalConfigProvider>
        </QueryClientProvider>
        </SessionRecovery>
      </Suspense>
    </SessionProvider>
  );
}
