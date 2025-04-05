import { setUserProvider } from "@/lib/api";
import { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router";

function ProtectedComponent({ Component }: { Component: React.FC }) {
  const auth = useAuth();
  const navigate = useNavigate();
  const path = window.location.pathname;

  useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated) {
      navigate(`/login?redirect=${path}`, { replace: true });
    }

    if (auth.isAuthenticated) {
      setUserProvider(() => Promise.resolve(auth.user ?? null));
    }
  }, [auth]);

  if (auth.isLoading || !auth.isAuthenticated) {
    return;
  }

  return <Component />;
}

export default ProtectedComponent;
