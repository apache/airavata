import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router";

function ProtectedComponent({ Component }: { Component: React.FC }) {
  const auth = useAuth();
  const navigate = useNavigate();

  if (auth.isLoading) {
    return;
  }

  if (!auth.isAuthenticated) {
    navigate("/");
    return;
  }

  return <Component />;
}

export default ProtectedComponent;
