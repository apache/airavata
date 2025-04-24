import {
  Box,
  Button,
  Container,
  Field,
  Heading,
  Input,
  VStack,
} from "@chakra-ui/react";
import { useState } from "react";
import { useNavigate } from "react-router";
import { FaArrowLeft } from "react-icons/fa";
import { CreateProjectRequest } from "@/interfaces/Requests/CreateProjectRequest";
import RepoSearchInput from "./RepoSearch";

export const AddProjectMaster = () => {
  const navigate = useNavigate();
  const [createProjectRequest, setCreateProjectRequest] = useState(
    {} as CreateProjectRequest
  );

  return (
    <Container maxW="breakpoint-sm" mt={8}>
      <Box maxW="breakpoint-sm" mx="auto">
        <Heading
          textAlign="center"
          fontSize={{ base: "4xl", md: "5xl" }}
          fontWeight="black"
          lineHeight={1.2}
        >
          Add Project
        </Heading>
        <Button
          onClick={() => {
            navigate("/add");
          }}
          variant="ghost"
          p={0}
          mb={2}
        >
          <FaArrowLeft />
          Back
        </Button>
      </Box>

      <VStack gap={4} alignItems="flex-start">
        <Field.Root>
          <Field.Label>Project Name</Field.Label>
          <Input
            value={createProjectRequest.name}
            onChange={(e) => {
              setCreateProjectRequest({
                ...createProjectRequest,
                name: e.target.value,
              });
            }}
            placeholder="Enter project name"
          />
        </Field.Root>

        <RepoSearchInput
          createResourceRequest={createProjectRequest}
          setCreateResourceRequest={setCreateProjectRequest}
        />
      </VStack>
    </Container>
  );
};
