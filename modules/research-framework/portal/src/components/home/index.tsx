import { Box, HStack, Container } from "@chakra-ui/react";

import NavBar from "../NavBar";
import { PageHeader } from "../PageHeader";
import { AddRepositoryButton } from "./AddRepositoryButton";
import { AddZipButton } from "./AddZipButton";
import { NotebooksAndRepositoriesSection } from "./NotebooksAndRepositoriesSection";
import { SessionsSection } from "./SessionsSection";
import {AddCodeButton} from "@/components/home/AddCodeButton.tsx";
import {AddJupyterButton} from "@/components/home/AddJupyterButton.tsx";

const Home = () => {
  return (
    <Box>
      <NavBar />

      <Container maxW="container.xl" p={4}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Notebook & Repositories"
            description="Community-Published Scientific Notebooks and Repositories."
          />

          <HStack gap={4}>
            <AddZipButton />
            <AddRepositoryButton />
          </HStack>
        </HStack>

        <NotebooksAndRepositoriesSection />

        <HStack alignItems="flex-end" justify="space-between" mt={4}>
          <PageHeader
            title="Sessions"
            description="Stop or attach to past sessions. Each session preserves your code and data."
          />

          <HStack gap={4}>
            <AddCodeButton/>
            <AddJupyterButton/>
          </HStack>
        </HStack>
        <SessionsSection />
      </Container>
    </Box>
  );
};

export default Home;
