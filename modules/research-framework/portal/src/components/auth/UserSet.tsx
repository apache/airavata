import { setUserProvider } from "@/lib/api";
import { useEffect } from "react";
import { useAuth } from "react-oidc-context";

export const UserSet = () => {
  const auth = useAuth();

  useEffect(() => {
    if (auth.isAuthenticated) {
      // Set the user provider here
      setUserProvider(() => Promise.resolve(auth.user ?? null));
    }
  }, [auth]);

  return null;
};
