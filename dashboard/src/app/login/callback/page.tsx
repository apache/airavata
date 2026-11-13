"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { Alert } from "@/components/ui";
import { errorText } from "@/lib/api";
import { finishCILogon } from "@/lib/cilogon";
import { useSession } from "@/lib/session";

// CILogon returns here with ?code=…&state=… (or ?error=…). The code is redeemed once,
// then the user goes to the dashboard.
export default function CILogonCallbackPage() {
  const { signInWithCILogon } = useSession();
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  // A code can be redeemed only once; development mode runs effects twice.
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;
    const params = new URLSearchParams(window.location.search);
    // Keep the code out of history and of anything that reads the address bar.
    window.history.replaceState(null, "", window.location.pathname);
    (async () => {
      await signInWithCILogon(await finishCILogon(params));
      router.replace("/");
    })().catch((e) => setError(errorText(e)));
  }, [signInWithCILogon, router]);

  return (
    <div className="center">
      <div className="card" style={{ width: "min(460px, 100%)" }}>
        <div className="card-head">
          <h2>Signing in with CILogon</h2>
        </div>
        <div className="card-body form">
          {error ? (
            <>
              <Alert>{error}</Alert>
              <Link className="btn primary" href="/login/">
                Back to sign in
              </Link>
            </>
          ) : (
            <p className="muted">Finishing sign-in…</p>
          )}
        </div>
      </div>
    </div>
  );
}
