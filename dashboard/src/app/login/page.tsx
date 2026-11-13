"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Alert, Field } from "@/components/ui";
import { errorText } from "@/lib/api";
import { useBrand } from "@/lib/branding";
import { beginCILogon, getCILogonConfig } from "@/lib/cilogon";
import type { CILogonConfig } from "@/lib/cilogon";
import { useSession } from "@/lib/session";

export default function LoginPage() {
  const { token, signIn } = useSession();
  const router = useRouter();
  const brand = useBrand();
  // undefined while the server is asked whether CILogon sign-in is configured.
  const [cilogon, setCILogon] = useState<CILogonConfig | null | undefined>(undefined);
  const [redirecting, setRedirecting] = useState(false);
  const [value, setValue] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (token) router.replace("/");
  }, [token, router]);

  useEffect(() => {
    getCILogonConfig()
      .then(setCILogon)
      .catch(() => setCILogon(null));
  }, []);

  const tokenForm = (
    <form
      className="form"
      onSubmit={async (e) => {
        e.preventDefault();
        setBusy(true);
        setError(null);
        try {
          await signIn(value);
          router.replace("/");
        } catch (err) {
          setError(errorText(err));
        } finally {
          setBusy(false);
        }
      }}
    >
      <Field label="Bearer token">
        <input
          type="password"
          autoFocus={!cilogon?.enabled}
          autoComplete="off"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="Root, service account or CILogon access token"
        />
      </Field>
      <p className="small muted" style={{ margin: 0 }}>
        The root token is printed in the server log at startup (pin it with <code>AIRAVATA_ROOT_ACCOUNT_TOKEN</code>).
        The token is kept in this browser&apos;s local storage until you sign out.
      </p>
      <button className={cilogon?.enabled ? undefined : "primary"} type="submit" disabled={busy || !value.trim()}>
        {busy ? "Checking…" : "Sign in with token"}
      </button>
    </form>
  );

  return (
    <div className="center">
      <div className="card" style={{ width: "min(460px, 100%)" }}>
        <div className="card-head">
          <h2>Sign in to {brand.name}</h2>
        </div>
        <div className="card-body form">
          {cilogon === undefined ? (
            <p className="muted">Loading…</p>
          ) : cilogon?.enabled ? (
            <>
              <button
                className="primary"
                type="button"
                disabled={redirecting}
                onClick={async () => {
                  setRedirecting(true);
                  setError(null);
                  try {
                    await beginCILogon(cilogon);
                  } catch (err) {
                    setError(errorText(err));
                    setRedirecting(false);
                  }
                }}
              >
                {redirecting ? "Redirecting to CILogon…" : "Sign in with CILogon"}
              </button>
              <p className="small muted" style={{ margin: 0 }}>
                Use your institution, ORCID, GitHub or Google account through CILogon.
              </p>
              <Alert>{error}</Alert>
              <details className="login-token">
                <summary>Sign in with a token instead</summary>
                {tokenForm}
              </details>
            </>
          ) : (
            <>
              {tokenForm}
              <Alert>{error}</Alert>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
