"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import { useSession } from "@/lib/session";
import { InteractiveAuthDialog } from "./InteractiveAuth";

// Admin-only destinations, shown below the rest once the session is known to be an admin.
const ADMIN_NAV = [{ href: "/service-accounts/", label: "Service accounts" }];

const NAV = [
  { href: "/", label: "Overview" },
  { href: "/credentials/", label: "SSH credentials" },
  { href: "/compute/", label: "Compute clusters" },
  { href: "/storages/", label: "Storages" },
  { href: "/applications/", label: "Applications" },
  { href: "/processes/", label: "Processes" },
  { href: "/data/", label: "Data products" },
  { href: "/notebooks/", label: "Notebooks" },
  { href: "/interactive-auth/", label: "Interactive auth" },
];

const CONNECTION_LABEL = {
  live: "Live",
  polling: "Polling",
  connecting: "Connecting",
  off: "Off",
} as const;

// Shell is the signed-in frame: navigation, the interactive-auth subscription status,
// and the dialog that surfaces a waiting SSH login on any page. Signed-out visitors are
// sent to /login.
export function Shell({ children }: { children: React.ReactNode }) {
  const { token, isAdmin, signOut } = useSession();
  const { current, connection } = useInteractiveAuth();
  const pathname = usePathname();
  const router = useRouter();
  const onLogin = pathname.startsWith("/login");

  useEffect(() => {
    if (token === null && !onLogin) router.replace("/login/");
  }, [token, onLogin, router]);

  if (onLogin) return <>{children}</>;
  if (!token) return null;

  const isActive = (href: string) => (href === "/" ? pathname === "/" : pathname.startsWith(href.replace(/\/$/, "")));

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          Apache Airavata
          <small>Dashboard</small>
        </div>
        <nav className="nav">
          {[...NAV, ...(isAdmin ? ADMIN_NAV : [])].map((n) => (
            <Link key={n.href} href={n.href} className={isActive(n.href) ? "active" : ""}>
              {n.label}
              {n.href === "/interactive-auth/" && current && <span className="badge warn">1+</span>}
            </Link>
          ))}
        </nav>
        <div className="sidebar-foot">
          <span className="row" title="Interactive auth subscription">
            <span className={`dot ${connection === "live" ? "ok" : connection === "polling" ? "warn" : ""}`} />
            Auth requests: {CONNECTION_LABEL[connection]}
          </span>
          <span>{isAdmin ? "Signed in · admin" : "Signed in"}</span>
          <button className="small" onClick={signOut}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="main">{children}</main>
      <InteractiveAuthDialog />
    </div>
  );
}
