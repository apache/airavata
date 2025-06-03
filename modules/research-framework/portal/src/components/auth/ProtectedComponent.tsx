import { useEffect, useRef } from "react";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router";

function ProtectedComponent({ Component }: { Component: React.FC }) {
  const auth = useAuth();
  const navigate = useNavigate();
  const initialPathRef = useRef(window.location.pathname); // store once

  useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated) {
      navigate(`/login?redirect=${initialPathRef.current}`, { replace: true });
    }
  }, [auth, navigate]);

  if (auth.isLoading || !auth.isAuthenticated) {
    return null;
  }

  return <Component />;
}

export default ProtectedComponent;
