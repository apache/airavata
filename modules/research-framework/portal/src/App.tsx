import { useColorMode } from "./components/ui/color-mode";
import { Route, Routes, useLocation, useNavigate } from "react-router";
import Home from "./components/home";
import { Models } from "./components/models";
import { Datasets } from "./components/datasets";
import ResourceDetails from "./components/resources/ResourceDetails";
import Notebooks from "./components/notebooks";
import Repositories from "./components/repositories";
import { Login } from "./components/auth/Login";
import ProtectedComponent from "./components/auth/ProtectedComponent";
import { AuthProvider, AuthProviderProps } from "react-oidc-context";
import { useEffect, useState } from "react";
import NavBarFooterLayout from "./layouts/NavBarFooterLayout";
import { CybershuttleLanding } from "./components/home/CybershuttleLanding";
import {
  APP_REDIRECT_URI,
  BACKEND_URL,
  CLIENT_ID,
  OPENID_CONFIG_URL,
} from "./lib/constants";
import { WebStorageStateStore } from "oidc-client-ts";
import { Resources } from "./components/resources";
import { UserSet } from "./components/auth/UserSet";
import { Toaster } from "./components/ui/toaster";
function App() {
  const colorMode = useColorMode();
  const navigate = useNavigate();
  const location = useLocation();
  const [oidcConfig, setOidcConfig] = useState<AuthProviderProps | null>(null);

  if (colorMode.colorMode === "dark") {
    colorMode.toggleColorMode();
  }

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
    <>
      <AuthProvider
        {...oidcConfig}
        onSigninCallback={() => {
          navigate(location.pathname, { replace: true });
        }}
      >
        <Toaster />
        <UserSet />
        <Routes>
          {/* Public Route */}
          <Route element={<NavBarFooterLayout />}>
            <Route path="/" element={<CybershuttleLanding />} />
            <Route path="/login" element={<Login />} />
            <Route path="/resources" element={<Resources />} />
            <Route path="/resources/datasets" element={<Datasets />} />
            <Route path="/resources/notebooks" element={<Notebooks />} />
            <Route path="/resources/repositories" element={<Repositories />} />
            <Route path="/resources/models" element={<Models />} />
            <Route path="/resources/:type/:id" element={<ResourceDetails />} />
          </Route>

          {/* Protected Routes with Layout */}
          <Route
            element={<ProtectedComponent Component={NavBarFooterLayout} />}
          >
            <Route path="/sessions" element={<Home />} />
          </Route>
        </Routes>
      </AuthProvider>
    </>
  );
}

export default App;
