import { useEffect } from "react";
import { useAuth } from "react-oidc-context";

export const TokenHandler = () => {
  const auth = useAuth();

  useEffect(() => {
    if (auth.isAuthenticated) {
      auth.events.addAccessTokenExpiring(() => {
        console.warn("Access token is about to expire...");
      });

      auth.events.addAccessTokenExpired(() => {
        console.warn("Access token expired. Attempting silent renew...");
        auth
          .signinSilent()
          .then(() => console.log("Token renewed successfully"))
          .catch(() => {
            console.error("Silent renew failed! Redirecting to login...");
            // auth.signinRedirect();
          });
      });

      auth.events.addUserSignedOut(() => {
        console.warn("User signed out externally.");
      });
    }

    return () => {
      auth.events.removeAccessTokenExpiring(() => {});
      auth.events.removeAccessTokenExpired(() => {});
      auth.events.removeUserSignedOut(() => {});
    };
  }, [auth]);

  return null;
};
