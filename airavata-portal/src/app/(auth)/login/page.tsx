"use client";

import { signIn } from "next-auth/react";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useRef } from "react";

function LoginContent() {
  const searchParams = useSearchParams();
  const callbackUrl = searchParams.get("callbackUrl") || "/default";
  const error = searchParams.get("error");
  const sessionExpired = searchParams.get("session_expired") === "1";
  const didRedirect = useRef(false);

  useEffect(() => {
    if (didRedirect.current) return;
    didRedirect.current = true;
    signIn("keycloak", { callbackUrl }, { prompt: "login" });
  }, [callbackUrl]);

  if (error || sessionExpired) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="max-w-md w-full space-y-6 p-8 bg-white rounded-xl shadow-lg">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900">Apache Airavata</h1>
            <p className="mt-1 text-gray-600">Science Gateway Portal</p>
          </div>
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg space-y-2">
            {sessionExpired && (
              <p className="text-sm font-medium">Your session expired. You will be redirected to sign in again.</p>
            )}
            {error && !sessionExpired && (
              <p className="text-sm font-medium">
                {error === "OAuthSignin" && "Unable to start authentication."}
                {error === "OAuthCallback" && "Authentication failed."}
                {error === "AccessDenied" && "Access denied."}
                {error === "Configuration" && "Authentication service is unavailable. Ensure Keycloak is running."}
                {error === "RefreshAccessTokenError" && "Session expired."}
                {!["OAuthSignin", "OAuthCallback", "OAuthCreateAccount", "Callback", "AccessDenied", "Configuration", "RefreshAccessTokenError"].includes(error) && `Error: ${error}`}
              </p>
            )}
          </div>
          <div className="text-center">
            <button
              type="button"
              onClick={() => signIn("keycloak", { callbackUrl }, { prompt: "login" })}
              className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg"
            >
              Try again
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="text-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600 mx-auto" />
        <p className="mt-4 text-gray-600">Redirecting to sign in...</p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
        </div>
      }
    >
      <LoginContent />
    </Suspense>
  );
}
