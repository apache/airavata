"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Alert, Field } from "@/components/ui";
import { errorText } from "@/lib/api";
import { useSession } from "@/lib/session";

export default function LoginPage() {
  const { token, signIn } = useSession();
  const router = useRouter();
  const [value, setValue] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (token) router.replace("/");
  }, [token, router]);

  return (
    <div className="center">
      <div className="card" style={{ width: "min(460px, 100%)" }}>
        <div className="card-head">
          <h2>Sign in to Airavata</h2>
        </div>
        <form
          className="card-body form"
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
              autoFocus
              autoComplete="off"
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder="Root token or CILogon ID token"
            />
          </Field>
          <p className="small muted" style={{ margin: 0 }}>
            The root token is printed in the server log at startup (pin it with <code>AIRAVATA_ROOT_ACCOUNT_TOKEN</code>
            ). A CILogon ID token works too. The token is kept in this browser&apos;s local storage until you sign
            out.
          </p>
          <Alert>{error}</Alert>
          <button className="primary" type="submit" disabled={busy || !value.trim()}>
            {busy ? "Checking…" : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}
