"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Fragment, useEffect, useState } from "react";
import { useBrand } from "@/lib/branding";
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

interface NavGroup {
  heading: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    heading: "Workspace",
    items: [
      { href: "/", label: "Overview", icon: "overview" },
      { href: "/storages/", label: "Storages", icon: "database" },
      { href: "/data/", label: "Data products", icon: "file" },
      { href: "/groups/", label: "Groups", icon: "users" },
      { href: "/notebooks/", label: "Notebooks", icon: "notebook" },
    ],
  },
  {
    heading: "Compute",
    items: [
      { href: "/compute/", label: "Compute clusters", icon: "server" },
      { href: "/credentials/", label: "SSH credentials", icon: "key" },
      { href: "/interactive-auth/", label: "Interactive auth", icon: "shield" },
    ],
  },
  {
    heading: "Applications",
    items: [
      { href: "/applications/", label: "Applications", icon: "app" },
      { href: "/processes/", label: "Processes", icon: "activity" },
    ],
  },
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
  const brand = useBrand();
  const pathname = usePathname();
  const router = useRouter();
  const onLogin = pathname.startsWith("/login");
  const [collapsed, setCollapsed] = useState(false);

  // The collapsed state is a per-browser preference; read it after mount so the
  // statically exported page matches on first paint.
  useEffect(() => {
    try {
      setCollapsed(localStorage.getItem("sidebar-collapsed") === "1");
    } catch {}
  }, []);
  const toggleSidebar = () =>
    setCollapsed((c) => {
      try {
        localStorage.setItem("sidebar-collapsed", c ? "0" : "1");
      } catch {}
      return !c;
    });

  useEffect(() => {
    if (token === null && !onLogin) router.replace("/login/");
  }, [token, onLogin, router]);

  if (onLogin) return <>{children}</>;
  if (!token) return null;
  if (!active) return <AwaitingActivation />;

  const isActive = (href: string) => (href === "/" ? pathname === "/" : pathname.startsWith(href.replace(/\/$/, "")));
  const name = displayName(me) ?? "Signed in";
  const connectionText = `Auth requests: ${CONNECTION_LABEL[connection]}`;

  const items = [...NAV_GROUPS.flatMap((g) => g.items), ...(isAdmin ? ADMIN_NAV : [])];
  const here = items.find((n) => isActive(n.href));
  const crumbs = [...(here && ADMIN_NAV.includes(here) ? ["Admin"] : []), here?.label ?? "Dashboard"];

  const link = (n: NavItem) => {
    const waiting = n.href === "/interactive-auth/" && !!current;
    const on = isActive(n.href);
    return (
      <Link
        key={n.href}
        href={n.href}
        className={`rail-item${on ? " active" : ""}`}
        aria-label={waiting ? `${n.label} — a login is waiting` : n.label}
        title={collapsed ? n.label : undefined}
        aria-current={on ? "page" : undefined}
      >
        <Icon name={n.icon} />
        <span className="rail-label">{n.label}</span>
        {waiting && <span className="rail-alert" />}
      </Link>
    );
  };

  return (
    <div className={`shell${collapsed ? " collapsed" : ""}`}>
      <aside className="rail">
        <Link href="/" className="rail-logo" aria-label={`${brand.name} home`}>
          {brand.logoUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img className="rail-logo-img" src={brand.logoUrl} alt="" />
          ) : (
            <AiravataMark size={30} />
          )}
          <span>{brand.name}</span>
        </Link>
        <nav className="rail-nav" aria-label="Main">
          {NAV_GROUPS.map((g) => (
            <Fragment key={g.heading}>
              <div className="rail-heading">{g.heading}</div>
              {g.items.map(link)}
            </Fragment>
          ))}
          {isAdmin && (
            <>
              <div className="rail-heading">Site administration</div>
              {ADMIN_NAV.map(link)}
            </>
          )}
        </nav>
        <div className="rail-foot">
          <span className="rail-item static" tabIndex={0} title={collapsed ? connectionText : undefined}>
            <span className={`dot ${connection === "live" ? "ok" : connection === "polling" ? "warn" : ""}`} />
            <span className="rail-label">{connectionText}</span>
          </span>
          <button className="rail-item" onClick={signOut} title={collapsed ? "Sign out" : undefined}>
            <Icon name="logout" />
            <span className="rail-label">Sign out</span>
          </button>
        </div>
      </aside>
      <div className="main">
        <header className="topbar">
          <button
            className="topbar-toggle"
            onClick={toggleSidebar}
            aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-expanded={!collapsed}
            title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          >
            <Icon name="sidebar" size={18} />
          </button>
          <div className="topbar-nav">
            <button onClick={() => router.back()} aria-label="Back">
              <Icon name="chevron-left" size={18} />
            </button>
            <button onClick={() => window.history.forward()} aria-label="Forward">
              <Icon name="chevron-right" size={18} />
            </button>
          </div>
          <nav className="crumbs" aria-label="Breadcrumb">
            {crumbs.map((c, i) => (
              <span key={c} style={{ display: "contents" }}>
                {i > 0 && <span className="sep">/</span>}
                <span className={i === crumbs.length - 1 ? "current" : undefined}>{c}</span>
              </span>
            ))}
          </nav>
          <span className="spacer" />
          <div className="session" title={me?.userId}>
            <div>
              <strong>{name}</strong>
              <small>{isAdmin ? "Administrator" : "Signed in"}</small>
            </div>
            <span className="avatar">{initials(name)}</span>
          </div>
        </header>
        <main className="main-content">{children}</main>
        <footer className="app-footer">
          Powered by{" "}
          <a href="https://airavata.apache.org" target="_blank" rel="noreferrer">
            Apache Airavata
          </a>
        </footer>
      </div>
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
