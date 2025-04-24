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
import { DatasetSearchInput } from "./DatasetSearch";
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import { toaster } from "../ui/toaster";
import { useAuth } from "react-oidc-context";

export const AddProjectMaster = () => {
  const navigate = useNavigate();
  const [createProjectRequest, setCreateProjectRequest] = useState(
    {} as CreateProjectRequest
  );
  const [loading, setLoading] = useState(false);
  const auth = useAuth();
  console.log(auth);

  const handleSubmit = async () => {
    try {
      setLoading(true);
      createProjectRequest.ownerId = auth.user?.profile.email || "";
      if (createProjectRequest.ownerId.length === 0) {
        toaster.create({
          title: "Error creating project",
          description: "Please log in again.",
          type: "error",
        });
        return;
      }
      if (
        !createProjectRequest.name ||
        createProjectRequest.name.length === 0
      ) {
        toaster.create({
          title: "Error creating project",
          description: "Please enter a project name.",
          type: "error",
        });
        return;
      } else if (
        !createProjectRequest.repositoryId ||
        createProjectRequest.repositoryId.length === 0
      ) {
        toaster.create({
          title: "Error creating project",
          description: "Please select a repository.",
          type: "error",
        });
        return;
      }

      await api.post(`${CONTROLLER.projects}/`, createProjectRequest);

      toaster.create({
        title: "Project Created",
        description: `Project ${createProjectRequest.name} with ${createProjectRequest.datasetIds.length} datasets and 1 repository!`,
        type: "success",
      });
      navigate("/sessions");
    } catch (error) {
      console.error("Error creating project:", error);
      toaster.create({
        title: "Error creating project",
        description: "Please check your input and try again.",
        type: "error",
      });
    } finally {
      setLoading(false);
    }
  };

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

        <DatasetSearchInput
          createResourceRequest={createProjectRequest}
          setCreateResourceRequest={setCreateProjectRequest}
        />

        <Button onClick={handleSubmit} loading={loading} w="full">
          Submit
        </Button>
      </VStack>
    </Container>
  );
};
