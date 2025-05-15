import { Box, Button, Container, Heading } from "@chakra-ui/react";
import { useState } from "react";
import { AddGitUrl } from "./AddGitUrl";
import { useNavigate } from "react-router";
import { FaArrowLeft } from "react-icons/fa";
import { CreateResourceRequest } from "@/interfaces/Requests/CreateResourceRequest";
import { ConfirmRepoDetails } from "./ConfirmRepoDetails";

export const AddRepoMaster = () => {
  const [addStage, setAddStage] = useState(0);
  const [createResourceRequest, setCreateResourceRequest] = useState({
    privacy: "PUBLIC",
  } as CreateResourceRequest);
  const [githubUrl, setGithubUrl] = useState("");
  const navigate = useNavigate();

  const nextStage = () => {
    setAddStage(addStage + 1);
  };

  const prevStage = () => {
    if (addStage === 0) {
      navigate("/add");
      return;
    }
    setAddStage(addStage - 1);
  };

  return (
    <Container maxW="container.lg" mt={8}>
      {/* TODO: use https://chakra-ui.com/docs/components/steps to show progress */}

      <Box maxW="breakpoint-sm" mx="auto">
        <Heading
          textAlign="center"
          fontSize={{ base: "4xl", md: "5xl" }}
          fontWeight="black"
          lineHeight={1.2}
        >
          Add Repository
        </Heading>
        <Button onClick={prevStage} variant="ghost" p={0} mb={2}>
          <FaArrowLeft />
          Back
        </Button>

        {addStage === 0 && (
          <AddGitUrl
            nextStage={nextStage}
            createResourceRequest={createResourceRequest}
            setCreateResourceRequest={setCreateResourceRequest}
            setGithubUrl={setGithubUrl}
            githubUrl={githubUrl}
          />
        )}

        {addStage === 1 && (
          <ConfirmRepoDetails
            createResourceRequest={createResourceRequest}
            setCreateResourceRequest={setCreateResourceRequest}
            githubUrl={githubUrl}
          />
        )}
      </Box>
    </Container>
  );
};
