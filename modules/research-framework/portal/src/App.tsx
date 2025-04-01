import { useColorMode } from "./components/ui/color-mode";
import { BrowserRouter, Route, Routes } from "react-router";
import Home from "./components/home";
import ProjectDetails from "./components/notebooks/ProjectDetails";
import { Models } from "./components/models";
import { ModelDetails } from "./components/models/ModelDetails";
import { Datasets } from "./components/datasets";
import { DatasetDetails } from "./components/datasets/DatasetDetails";
import ResourceDetails from "./components/resources/ResourceDetails";
import Notebooks from "./components/notebooks";
import Repositories from "./components/repositories";

function App() {
  const colorMode = useColorMode();
  if (colorMode.colorMode === "dark") {
    colorMode.toggleColorMode();
  }

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route index element={<Home />} />
          <Route path="/resources">
            <Route path="notebooks" element={<Notebooks />} />
            <Route path="datasets" element={<Datasets />} />
            <Route path="repositories" element={<Repositories />} />
            <Route path="models" element={<Models />} />

            {/* Dynamic Route for specific resource details */}
            <Route path=":type/:id" element={<ResourceDetails />} />
          </Route>

          <Route path="/notebook">
            <Route path=":slug" element={<ProjectDetails />} />
          </Route>
          <Route path="/repository">
            <Route path=":slug" element={<ProjectDetails />} />
          </Route>
          <Route path="/models">
            <Route index element={<Models />} />
            <Route path=":id" element={<ModelDetails />} />
          </Route>
          <Route path="/datasets">
            <Route index element={<Datasets />} />
            <Route path=":slug" element={<DatasetDetails />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
