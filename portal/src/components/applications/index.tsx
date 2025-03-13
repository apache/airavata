import NavBar from "../NavBar";
import { MOCK_APPLICATIONS } from "../../data/MOCK_DATA";
import { Container, SimpleGrid, VStack } from "@chakra-ui/react";
import { BsGithub } from "react-icons/bs";
import { PageHeader } from "../PageHeader";
import { AiFillExperiment } from "react-icons/ai";
import { ApplicationCard } from "./ApplicationCard";

export const Applications = () => {
  return (
    <>
      <NavBar />

      <Container maxW="container.lg" mt={8}>
        <PageHeader
          title="Applications"
          icon={<AiFillExperiment />}
          description="Start an experiment from this page."
        />

        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4} mt={8}>
          {
            // This is the code that will be replaced by the code snippet
            // from the instructions
            MOCK_APPLICATIONS.map((application) => {
              return (
                <ApplicationCard
                  application={application}
                  key={application.appModuleId}
                />
              );
            })
          }
        </SimpleGrid>
      </Container>
    </>
  );
};
