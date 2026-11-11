"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import { useSession } from "@/lib/session";
import { InteractiveAuthDialog } from "./InteractiveAuth";

// Admin-only destinations, shown below the rest once the session is known to be an admin.
const ADMIN_NAV = [
  { href: "/users/", label: "Users" },
  { href: "/service-accounts/", label: "Service accounts" },
];

const NAV = [
  { href: "/", label: "Overview" },
  { href: "/credentials/", label: "SSH credentials" },
  { href: "/compute/", label: "Compute clusters" },
  { href: "/storages/", label: "Storages" },
  { href: "/applications/", label: "Applications" },
  { href: "/processes/", label: "Processes" },
  { href: "/data/", label: "Data products" },
  { href: "/groups/", label: "Groups" },
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
  const { token, me, isAdmin, active, signOut } = useSession();
  const { current, connection } = useInteractiveAuth();
  const pathname = usePathname();
  const router = useRouter();
  const onLogin = pathname.startsWith("/login");

  useEffect(() => {
    if (token === null && !onLogin) router.replace("/login/");
  }, [token, onLogin, router]);

  if (onLogin) return <>{children}</>;
  if (!token) return null;
  if (!active) return <AwaitingActivation />;

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
          <span className="truncate" title={me?.userId}>
            {displayName(me) ?? "Signed in"}
            {isAdmin && " · admin"}
          </span>
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

function displayName(me: ReturnType<typeof useSession>["me"]): string | null {
  if (!me) return null;
  const name = [me.user?.firstName, me.user?.lastName].filter(Boolean).join(" ");
  return name || me.userId;
}

// AwaitingActivation stands in for the dashboard while an account is not active: the
// server refuses it everything else until an administrator activates it.
function AwaitingActivation() {
  const { me, refreshMe, signOut } = useSession();
  const [checking, setChecking] = useState(false);
  const suspended = me?.status === "SUSPENDED";
  const isNew = suspended && me?.user && Date.now() - me.user.createdAt < 7 * 24 * 60 * 60 * 1000;

  return (
    <div className="center">
      <div className="card" style={{ width: "min(480px, 100%)" }}>
        <div className="card-head">
          <h2>{isNew ? "Waiting for approval" : suspended ? "Account suspended" : "Account inactive"}</h2>
        </div>
        <div className="card-body form">
          <p style={{ margin: 0 }}>
            {isNew
              ? "You're signed in, and your Airavata account has been created. An administrator needs to activate it before you can use the dashboard."
              : "An administrator needs to activate your Airavata account before you can use the dashboard."}
          </p>
          <dl className="kv">
            <dt>Name</dt>
            <dd>{displayName(me)}</dd>
            {me?.user?.email && (
              <>
                <dt>Email</dt>
                <dd>{me.user.email}</dd>
              </>
            )}
            <dt>User id</dt>
            <dd>
              <code>{me?.userId}</code>
            </dd>
          </dl>
          <p className="small muted" style={{ margin: 0 }}>
            Send your user id to your Airavata administrator if they need it to find you.
          </p>
          <div className="row">
            <button
              className="primary"
              disabled={checking}
              onClick={async () => {
                setChecking(true);
                try {
                  await refreshMe();
                } finally {
                  setChecking(false);
                }
              }}
            >
              {checking ? "Checking…" : "Check again"}
            </button>
            <button onClick={signOut}>Sign out</button>
          </div>
        </div>
      </div>
    </div>
  );
}
