"use client";

import Link from "next/link";
import { useSession, signOut } from "next-auth/react";
import { Bell, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { GatewaySelector } from "@/components/gateway/GatewaySelector";
import { RoleSelector } from "@/components/gateway/RoleSelector";
import { useGateway } from "@/contexts/GatewayContext";

interface HeaderProps {
  onMenuClick?: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const { data: session } = useSession();
  const { dashboardHref, isRootUser, accessibleGateways } = useGateway();
  
  // Show role selector if user has multiple roles available
  const showRoleSelector = isRootUser || accessibleGateways.length > 0;

  const getInitials = (name?: string | null) => {
    if (!name) return "U";
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2);
  };

  return (
    <header className="z-40 shrink-0 border-b bg-background">
      <div className="flex h-16 items-center gap-4 px-4 md:px-6">
        <Button variant="ghost" size="icon" className="md:hidden" onClick={onMenuClick}>
          <Menu className="h-5 w-5" />
        </Button>

        <Link href={dashboardHref} className="flex items-center gap-2 font-semibold">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            A
          </div>
          <span className="hidden md:inline-block">Airavata Portal</span>
        </Link>

        <div className="flex-1" />

        <div className="flex items-center gap-4">
          <div className="hidden md:flex items-center gap-4">
            <GatewaySelector />
            {showRoleSelector && <RoleSelector />}
          </div>
          <Button variant="ghost" size="icon" className="relative">
            <Bell className="h-5 w-5" />
            <span className="sr-only">Notifications</span>
          </Button>
        </div>

        {/* Expanded user icon with avatar, name, and email */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="relative h-auto px-2 py-1.5 rounded-lg">
              <div className="flex items-center gap-3">
                <Avatar className="h-8 w-8">
                  <AvatarImage src={session?.user?.image || ""} alt={session?.user?.name || ""} />
                  <AvatarFallback>{getInitials(session?.user?.name)}</AvatarFallback>
                </Avatar>
                <div className="hidden md:flex flex-col items-start">
                  <span className="text-sm font-medium leading-tight">{session?.user?.name || "User"}</span>
                  <span className="text-xs text-muted-foreground leading-tight">{session?.user?.email}</span>
                </div>
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="end" forceMount>
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">{session?.user?.name}</p>
                <p className="text-xs leading-none text-muted-foreground">
                  {session?.user?.email}
                </p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem asChild>
              <Link href="/account">Account</Link>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="cursor-pointer text-red-600"
              onSelect={async () => {
                // Federated logout flow:
                // 1. Call NextAuth signOut to clear local session (HTTP-only cookies)
                // 2. Get IdP logout URL from server
                // 3. Redirect to IdP for complete logout
                try {
                  // First, clear local NextAuth session
                  await signOut({ redirect: false });
                  
                  // Then get the IdP logout URL and redirect
                  const response = await fetch('/api/auth/logout', { method: 'POST' });
                  const data = await response.json();
                  if (data.logoutUrl) {
                    window.location.href = data.logoutUrl;
                  } else {
                    window.location.href = '/login';
                  }
                } catch {
                  // Fallback: just redirect to login
                  window.location.href = '/login';
                }
              }}
            >
              Log out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
