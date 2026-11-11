"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useInteractiveAuth } from "@/lib/interactive-auth";
import { useSession } from "@/lib/session";
import { AiravataMark, Icon, type IconName } from "./Icons";
import { InteractiveAuthDialog } from "./InteractiveAuth";

// Admin-only destinations, shown below the rest once the session is known to be an admin.
const ADMIN_NAV: NavItem[] = [
  { href: "/users/", label: "Users", icon: "user" },
  { href: "/service-accounts/", label: "Service accounts", icon: "bot" },
  { href: "/audit/", label: "Audit log", icon: "history" },
];

const NAV: NavItem[] = [
  { href: "/", label: "Overview", icon: "overview" },
  { href: "/credentials/", label: "SSH credentials", icon: "key" },
  { href: "/compute/", label: "Compute clusters", icon: "server" },
  { href: "/storages/", label: "Storages", icon: "database" },
  { href: "/applications/", label: "Applications", icon: "app" },
  { href: "/processes/", label: "Processes", icon: "activity" },
  { href: "/data/", label: "Data products", icon: "file" },
  { href: "/groups/", label: "Groups", icon: "users" },
  { href: "/notebooks/", label: "Notebooks", icon: "notebook" },
  { href: "/interactive-auth/", label: "Interactive auth", icon: "shield" },
];

interface NavItem {
  href: string;
  label: string;
  icon: IconName;
}

const CONNECTION_LABEL = {
  live: "Live",
  polling: "Polling",
  connecting: "Connecting",
  off: "Off",
} as const;

// Shell is the signed-in frame: the navigation rail, the interactive-auth subscription
// status, and the dialog that surfaces a waiting SSH login on any page. Signed-out
// visitors are sent to /login.
//
// The rail shows icons only. Each item's name is its accessible label and appears as a
// tooltip on hover or keyboard focus.
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
  const name = displayName(me) ?? "Signed in";
  const connectionText = `Auth requests: ${CONNECTION_LABEL[connection]}`;

  return (
    <div className="shell">
      <aside className="rail">
        <Link href="/" className="rail-logo" aria-label="Apache Airavata home">
          <AiravataMark size={30} />
          <span className="tip">Apache Airavata</span>
        </Link>
        <nav className="rail-nav" aria-label="Main">
          {[...NAV, ...(isAdmin ? ADMIN_NAV : [])].map((n) => {
            const waiting = n.href === "/interactive-auth/" && !!current;
            const label = waiting ? `${n.label} — a login is waiting` : n.label;
            const on = isActive(n.href);
            return (
              <Link
                key={n.href}
                href={n.href}
                className={`rail-item${on ? " active" : ""}`}
                aria-label={label}
                aria-current={on ? "page" : undefined}
              >
                <Icon name={n.icon} />
                {waiting && <span className="rail-alert" />}
                <span className="tip">{label}</span>
              </Link>
            );
          })}
        </nav>
        <div className="rail-foot">
          <span className="rail-item static" tabIndex={0} aria-label={connectionText}>
            <span className={`dot ${connection === "live" ? "ok" : connection === "polling" ? "warn" : ""}`} />
            <span className="tip">{connectionText}</span>
          </span>
          <span
            className="rail-item static"
            tabIndex={0}
            aria-label={`Signed in as ${name}${isAdmin ? " (admin)" : ""}`}
          >
            <span className="avatar">{initials(name)}</span>
            <span className="tip">
              {name}
              {isAdmin && " · admin"}
              {me?.userId && me.userId !== name && <small>{me.userId}</small>}
            </span>
          </span>
          <button className="rail-item" onClick={signOut} aria-label="Sign out">
            <Icon name="logout" />
            <span className="tip">Sign out</span>
          </button>
        </div>
      </aside>
      <main className="main">
        <div className="main-content">{children}</div>
        <footer className="app-footer">
          Powered by{" "}
          <a href="https://airavata.apache.org" target="_blank" rel="noreferrer">
            Apache Airavata
          </a>
        </footer>
      </main>
      <InteractiveAuthDialog />
    </div>
  );
}

// initials picks up to two letters for the avatar: first and last word of a name, or
// the start of a user id.
function initials(name: string): string {
  const words = name.split("@")[0].split(/[\s._-]+/).filter(Boolean);
  if (words.length === 0) return "?";
  const letters = words.length === 1 ? words[0].slice(0, 2) : words[0][0] + words[words.length - 1][0];
  return letters.toUpperCase();
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
