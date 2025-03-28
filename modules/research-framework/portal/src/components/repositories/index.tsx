import { Container, VStack } from "@chakra-ui/react";
import NavBar from "../NavBar";
import { BsGithub } from "react-icons/bs";
import { PageHeader } from "../PageHeader";
import { MOCK_REPOSITORIES } from "../../data/MOCK_DATA";
import { RepositoryCard } from "./RepositoryCard";

const Repositories = () => {
  return (
    <>
      <NavBar />

      <Container maxW="container.lg" mt={8}>
        <PageHeader
          title="Repositories"
          icon={<BsGithub />}
          description="View the most viewed repositories on GitHub, all central to this page."
        />

        <VStack gap={4} mt={8}>
          {
            // This is the code that will be replaced by the code snippet
            // from the instructions
            MOCK_REPOSITORIES.map((repository) => {
              return (
                <RepositoryCard
                  repository={repository}
                  key={repository.githubUrl}
                />
              );
            })
          }
        </VStack>
      </Container>
    </>
  );
};

export default Repositories;
