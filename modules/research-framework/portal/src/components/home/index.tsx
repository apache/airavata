import { Box, HStack, Container } from "@chakra-ui/react";

import { PageHeader } from "../PageHeader";
import { ButtonWithIcon } from "./ButtonWithIcon";
import { FaPlus } from "react-icons/fa";
import { SessionsSection } from "./SessionsSection";
import { Tooltip } from "../ui/tooltip";

const Home = () => {
  return (
    <Box>
      <Container maxW="container.xl" mt={8}>
        {/* <HStack alignItems="flex-end" justify="space-between">
          <PageHeader
            title="Projects"
            description="Projects are a combination of repositories, notebooks, models, and datasets."
          />

          <HStack gap={4}>
            <AddZipButton />
            <AddRepositoryButton />
          </HStack>
        </HStack>

        <ProjectsSection /> */}

        <HStack alignItems="flex-end" justify="space-between" mt={4}>
          <PageHeader
            title="Sessions"
            description="Stop or attach to past sessions. Each session preserves your code and data."
          />

          <HStack gap={4}>
            <Tooltip content="Currently in development. In the future you'll be able to create new VSCode sessions">
              <ButtonWithIcon
                disabled={true}
                colorPalette="black"
                icon={FaPlus}
                size="sm"
              >
                VSCode
              </ButtonWithIcon>
            </Tooltip>
            <Tooltip content="Currently in development. In the future you'll be able to create new Jupyter Lab sessions">
              <ButtonWithIcon
                disabled={true}
                colorPalette="black"
                icon={FaPlus}
                size="sm"
              >
                Jupyter
              </ButtonWithIcon>
            </Tooltip>
          </HStack>
        </HStack>
        <SessionsSection />

        <Box my={8} />
      </Container>
    </Box>
  );
};

export default Home;
