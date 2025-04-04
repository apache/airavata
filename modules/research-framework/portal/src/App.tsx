import { useColorMode } from "./components/ui/color-mode";
import { BrowserRouter, Route, Routes } from "react-router";
import Home from "./components/home";
import { Models } from "./components/models";
import { Datasets } from "./components/datasets";
import ResourceDetails from "./components/resources/ResourceDetails";
import Notebooks from "./components/notebooks";
import Repositories from "./components/repositories";
import { Login } from "./components/auth/Login";
import ProtectedComponent from "./components/auth/ProtectedComponent";
import { useAuth } from "react-oidc-context";
import { useEffect } from "react";
import { setUserProvider } from "./lib/api";
function App() {
  const colorMode = useColorMode();
  if (colorMode.colorMode === "dark") {
    colorMode.toggleColorMode();
  }

  const user = useAuth();

  useEffect(() => {
    if (user.isAuthenticated) {
      setUserProvider(() => Promise.resolve(user.user ?? null));
    }
  }, [user]);

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />

          <Route
            path="/projects"
            element={<ProtectedComponent Component={Home} />}
          />
          <Route path="/resources">
            <Route
              path="notebooks"
              element={<ProtectedComponent Component={Notebooks} />}
            />
            <Route
              path="datasets"
              element={<ProtectedComponent Component={Datasets} />}
            />
            <Route
              path="repositories"
              element={<ProtectedComponent Component={Repositories} />}
            />
            <Route
              path="models"
              element={<ProtectedComponent Component={Models} />}
            />

            {/* Dynamic Route for specific resource details */}
            <Route
              path=":type/:id"
              element={<ProtectedComponent Component={ResourceDetails} />}
            />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
