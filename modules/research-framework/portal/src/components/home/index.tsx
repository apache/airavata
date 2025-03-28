import { Box, HStack, Container } from "@chakra-ui/react";

import NavBar from "../NavBar";
import { PageHeader } from "../PageHeader";
import { AddRepositoryButton } from "./AddRepositoryButton";
import { AddZipButton } from "./AddZipButton";
import { ProjectsSection } from "./ProjectsSection";
import { ButtonWithIcon } from "./ButtonWithIcon";
import { FaPlus } from "react-icons/fa";
import { SessionsSection } from "./SessionsSection";

const Home = () => {
  return (
    <Box>
      <NavBar />

      <Container maxW="container.xl" p={4}>
        <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Notebooks"
            description="Community-Published Scientific Notebooks and Repositories."
          />

          <HStack gap={4}>
            <AddZipButton />
            <AddRepositoryButton />
          </HStack>
        </HStack>

        <ProjectsSection />

        <HStack alignItems="flex-end" justify="space-between" mt={4}>
          <PageHeader
            title="Sessions"
            description="Stop or attach to past sessions. Each session preserves your code and data."
          />

          <HStack gap={4}>
            <ButtonWithIcon
              bg="purple.500"
              _hover={{ bg: "purple.600" }}
              icon={FaPlus}
            >
              VSCode
            </ButtonWithIcon>
            <ButtonWithIcon
              bg="green.600"
              _hover={{ bg: "green.700" }}
              icon={FaPlus}
            >
              Jupyter
            </ButtonWithIcon>
          </HStack>
        </HStack>
        <SessionsSection />
      </Container>
    </Box>
  );
};

export default Home;
