"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Home,
  Settings,
  Server,
  Building2,
  X,
  BookOpen,
  Shield,
  GitBranch,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { useGateway } from "@/contexts/GatewayContext";
import { useUserRole } from "@/contexts/AdvancedFeaturesContext";
import { RoleSelector } from "@/components/gateway/RoleSelector";
import { useMemo, useState, useEffect } from "react";

interface SidebarProps {
  open?: boolean;
  onClose?: () => void;
}

// Reserved paths that are not gateway names
const RESERVED_PATHS = ["admin", "datasets", "repositories", "experiments", "catalog", "storage", "resources", "compute", "groups", "sharing", "access", "account", "api", "auth", "login", "not-found", "no-permissions"];

// Helper to get gateway name from pathname
function getGatewayNameFromPath(pathname: string): string | null {
  const match = pathname.match(/^\/([^/]+)/);
  if (!match) return null;
  const firstSegment = match[1];
  // Check if it's a reserved path (not a gateway name)
  if (RESERVED_PATHS.includes(firstSegment)) return null;
  return firstSegment;
}

// Helper to check if path is gateway-scoped
function isGatewayScopedPath(pathname: string): boolean {
  const gatewayName = getGatewayNameFromPath(pathname);
  return gatewayName !== null;
}

// Helper to check if path is root admin
function isRootAdminPath(pathname: string): boolean {
  return pathname.startsWith("/admin/") && !pathname.match(/^\/[^/]+\/admin\//);
}

export function Sidebar({ open, onClose }: SidebarProps) {
  const pathname = usePathname();
  const {
    isRootUser,
    selectedGatewayId,
    getGatewayName,
    accessibleGateways,
    hasNoGatewayAndIsRoot,
  } = useGateway();
  const { selectedRole } = useUserRole();

  // Track mounted state to prevent hydration mismatch
  const [mounted, setMounted] = useState(false);
  useEffect(() => {
    setMounted(true);
  }, []);

  const pathGatewayName = useMemo(() => getGatewayNameFromPath(pathname), [pathname]);
  const contextGatewayName = useMemo(() => {
    if (selectedGatewayId && accessibleGateways.length > 0) {
      return getGatewayName(selectedGatewayId);
    }
    return null;
  }, [selectedGatewayId, accessibleGateways, getGatewayName]);

  const isGatewayScoped = isGatewayScopedPath(pathname);
  const isRootAdmin = isRootAdminPath(pathname);
  const currentGatewayName = pathGatewayName || contextGatewayName || "default";

  const gatewayPrefix = `/${currentGatewayName}`;
  const gatewayAdminPrefix = `/${currentGatewayName}/admin`;

  // Non–gateway-specific items: shown only in system-admin mode under "System" group
  const systemAdminItems = useMemo(
    () => [
      { href: "/admin/gateways", label: "Gateways", icon: Building2 },
    ],
    []
  );

  // Combine base items with gateway admin items based on selected role (system items are in separate "System" group)
  const mainNavItems = useMemo(() => {
    // User-level items (available to all users)
    const baseNavItems = [
      { href: gatewayPrefix, label: "Home", icon: Home, exact: true },
      { href: `${gatewayPrefix}/catalog`, label: "Catalog", icon: BookOpen },
      { href: `${gatewayPrefix}/workflows`, label: "Workflows", icon: GitBranch },
      { href: `${gatewayPrefix}/resources`, label: "Resources", icon: Server },
      { href: `${gatewayPrefix}/access`, label: "Access", icon: Shield },
    ];

    // Admin-only items (only visible when in admin mode)
    const adminOnlyItems = [
      { href: `${gatewayAdminPrefix}/gateway`, label: "My Gateway", icon: Building2 },
    ];

    // Regular user mode - only base items
    return { baseNavItems, adminOnlyItems };
  }, [selectedRole, gatewayPrefix, gatewayAdminPrefix]);

  const disabled = hasNoGatewayAndIsRoot;

  const NavLink = ({
    href,
    label,
    icon: Icon,
    disabled: isDisabled,
    exact,
  }: {
    href: string;
    label: string;
    icon: any;
    disabled?: boolean;
    exact?: boolean;
  }) => {
    const isActive = !isDisabled && (exact ? pathname === href : (pathname === href || pathname.startsWith(href + "/")));

    if (isDisabled) {
      return (
        <span
          className={cn(
            "flex cursor-not-allowed items-center gap-3 rounded-lg px-3 py-2 text-sm text-muted-foreground/50"
          )}
        >
          <Icon className="h-4 w-4" />
          {label}
        </span>
      );
    }

    return (
      <Link
        href={href}
        onClick={onClose}
        className={cn(
          "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors",
          isActive
            ? "bg-primary text-primary-foreground"
            : "text-muted-foreground hover:bg-muted hover:text-foreground"
        )}
      >
        <Icon className="h-4 w-4" />
        {label}
      </Link>
    );
  };

  // Skeleton content for initial server render to prevent hydration mismatch
  const skeletonContent = (
    <div className="flex h-full flex-col">
      <div className="flex h-16 items-center justify-between border-b px-4 md:hidden">
        <span className="font-semibold">Navigation</span>
        <Button variant="ghost" size="icon" onClick={onClose}>
          <X className="h-5 w-5" />
        </Button>
      </div>
      <div className="flex-1 overflow-y-auto py-4">
        <nav className="grid gap-1 px-4">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-9 w-full rounded-lg" />
          ))}
        </nav>
      </div>
      <div className="border-t px-4 py-2">
        <Skeleton className="h-9 w-full rounded-lg" />
      </div>
    </div>
  );

  const sidebarContent = (
    <div className="flex h-full flex-col">
      <div className="flex h-16 items-center justify-between border-b px-4 md:hidden">
        <span className="font-semibold">Navigation</span>
        <div className="flex items-center gap-2">
          {(isRootUser || accessibleGateways.length > 0) && (
            <RoleSelector className="w-[160px]" />
          )}
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto py-4">
        {hasNoGatewayAndIsRoot && (
          <div className="mb-4 px-4">
            <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-800 dark:border-amber-800 dark:bg-amber-950/50 dark:text-amber-200">
              No gateway available. System administration only.
            </p>
          </div>
        )}

        <nav className="grid gap-1 px-4">
          {mainNavItems.baseNavItems.map((item) => (
            <NavLink key={item.href} {...item} disabled={disabled} />
          ))}
          {(selectedRole === "gateway-admin" || selectedRole === "system-admin") && (
            <>
              <Separator className="my-2" />
              <p className="mb-1 px-3 py-1 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                Administration
              </p>
              {mainNavItems.adminOnlyItems.map((item) => (
                <NavLink key={item.href} {...item} disabled={disabled} />
              ))}
            </>
          )}
          {selectedRole === "system-admin" && (
            <>
              <Separator className="my-2" />
              <p className="mb-1 px-3 py-1 text-xs font-medium uppercase tracking-wider text-muted-foreground">
                System
              </p>
              {systemAdminItems.map((item) => (
                <NavLink key={item.href} {...item} disabled={disabled} />
              ))}
            </>
          )}
        </nav>
      </div>

      <div className="border-t px-4 py-2">
        <Link
          href="/account"
          onClick={onClose}
          className={cn(
            "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors",
            pathname === "/account" || pathname.startsWith("/account/")
              ? "bg-primary text-primary-foreground"
              : "text-muted-foreground hover:bg-muted hover:text-foreground"
          )}
        >
          <Settings className="h-4 w-4" />
          Account
        </Link>
      </div>
    </div>
  );

  // Use skeleton on server render, actual content only after mount
  const content = mounted ? sidebarContent : skeletonContent;

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          onClick={onClose}
        />
      )}

      {/* Mobile sidebar */}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 h-full w-64 transform border-r bg-background transition-transform duration-200 ease-in-out md:hidden",
          open ? "translate-x-0" : "-translate-x-full"
        )}
      >
        {content}
      </aside>

      {/* Desktop sidebar */}
      <aside className="hidden h-full w-64 shrink-0 border-r bg-background md:block">
        {content}
      </aside>
    </>
  );
}
