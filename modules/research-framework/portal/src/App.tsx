import { useColorMode } from "./components/ui/color-mode";
import { BrowserRouter, Route, Routes } from "react-router";
import Home from "./components/home";
import ProjectDetails from "./components/notebooks/ProjectDetails";
import { Models } from "./components/models";
import { ModelDetails } from "./components/models/ModelDetails";
import { Datasets } from "./components/datasets";
import { DatasetDetails } from "./components/datasets/DatasetDetails";

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
