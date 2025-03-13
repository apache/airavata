import { Heading } from "@chakra-ui/react";
import { useColorMode } from "./components/ui/color-mode";
import { BrowserRouter, Route, Routes } from "react-router";
import Home from "./components/home";
import Notebooks from "./components/notebooks";
import NotebookDetails from "./components/notebooks/NotebookDetails";
import Repositories from "./components/repositories";
import { Applications } from "./components/applications";

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
          <Route path="/notebooks">
            <Route index element={<Notebooks />} />
            <Route path=":slug" element={<NotebookDetails />} />
          </Route>
          <Route path="/applications" element={<Applications />} />
          <Route path="/repositories" element={<Repositories />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
