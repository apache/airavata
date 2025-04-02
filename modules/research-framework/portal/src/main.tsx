import { Provider } from "@/components/ui/provider";
import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { AuthProvider, AuthProviderProps } from "react-oidc-context";
import { WebStorageStateStore } from "oidc-client-ts";
import {
  APP_REDIRECT_URI,
  BACKEND_URL,
  CLIENT_ID,
  OPENID_CONFIG_URL,
} from "./lib/constants";

const Index = () => {
  const [oidcConfig, setOidcConfig] = useState<AuthProviderProps | null>(null);

  useEffect(() => {
    const fetchOidcConfig = async () => {
      try {
        const response = await fetch(OPENID_CONFIG_URL);
        const data = await response.json();

        const redirectUri = APP_REDIRECT_URI;

        const theConfig: AuthProviderProps = {
          authority: `${BACKEND_URL}/api/v1/identity-management/`,
          client_id: CLIENT_ID,
          redirect_uri: redirectUri,
          response_type: "code",
          scope: "openid email",
          metadata: {
            authorization_endpoint: data.authorization_endpoint,
            token_endpoint: data.token_endpoint,
            revocation_endpoint: data.revocation_endpoint,
            introspection_endpoint: data.introspection_endpoint,
            userinfo_endpoint: data.userinfo_endpoint,
            jwks_uri: data.jwks_uri,
          },
          userStore: new WebStorageStateStore({ store: window.localStorage }),
          automaticSilentRenew: true,
        };

        setOidcConfig(theConfig);
      } catch (error) {
        console.error("Error fetching OIDC config:", error);
      }
    };

    fetchOidcConfig();
  }, []);

  if (!oidcConfig) {
    return <div>Loading OIDC configuration...</div>; // Loading state while config is fetched
  }

  return (
    <React.StrictMode>
      <Provider>
        <AuthProvider
          {...oidcConfig}
          onSigninCallback={async (user) => {
            console.log("User signed in", user);
            window.location.href = "/projects";
          }}
        >
          <App />
        </AuthProvider>
      </Provider>
    </React.StrictMode>
  );
};

ReactDOM.createRoot(document.getElementById("root")!).render(<Index />);
