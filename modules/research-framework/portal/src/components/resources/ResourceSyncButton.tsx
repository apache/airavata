import { RepositoryResource } from "@/interfaces/ResourceType";
import {
  Box,
  Button,
  Code,
  Dialog,
  Spinner,
  Text,
  VStack,
  useDialog,
} from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { FaSync } from "react-icons/fa";
import yaml from "js-yaml";
import { CreateResourceRequest } from "@/interfaces/Requests/CreateResourceRequest";
import { CONTROLLER } from "@/lib/controller";
import api from "@/lib/api";
import { toaster } from "../ui/toaster";
import { useNavigate } from "react-router";
import { nanoid } from "nanoid";

export const ResourceSyncButton = ({
  repository,
}: {
  repository: RepositoryResource;
}) => {
  const [modifyResourceRequest, setModifyResourceRequest] =
    useState<null | ModifyResourceRequest>(null);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const dialog = useDialog();

  const onSync = async () => {
    if (!modifyResourceRequest) return;
    try {
      setLoading(true);
      await api.patch(
        `${CONTROLLER.resources}/repository`,
        modifyResourceRequest
      );

      toaster.create({
        title: "Success",
        description: "Resource synced successfully",
        type: "success",
      });

      navigate(`/resources/REPOSITORY/${repository.id}`, { state: nanoid() });
      dialog.setOpen(false);
    } catch (error) {
      console.error("Error syncing resource:", error);

      toaster.create({
        title: "Error",
        description: "Failed to sync resource",
        type: "error",
      });
    } finally {
      setLoading(false);
    }
  };
  return (
    <>
      <Dialog.RootProvider lazyMount={true} value={dialog}>
        <Dialog.Trigger asChild>
          <Button size="sm" variant="outline" colorScheme="blue">
            <FaSync />
            Sync
          </Button>
        </Dialog.Trigger>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.CloseTrigger />
            <Dialog.Header>
              <Dialog.Title>Sync {repository.name} with GitHub</Dialog.Title>
            </Dialog.Header>
            <Dialog.Body>
              <Text>
                We&apos;ll pull the <Code>cybershuttle.yml</Code> file from the
                main or master branch of <Code>{repository.repositoryUrl}</Code>
                . This will update the resource details with the latest changes
                from the repository.
              </Text>

              <Box>
                <ModalContent
                  repository={repository}
                  modifyResourceRequest={modifyResourceRequest}
                  setModifyResourceRequest={setModifyResourceRequest}
                />
              </Box>
            </Dialog.Body>
            <Dialog.Footer>
              <Dialog.ActionTrigger asChild>
                <Button variant="outline">Cancel</Button>
              </Dialog.ActionTrigger>
              <Button variant="solid" onClick={onSync} loading={loading}>
                Save
              </Button>
            </Dialog.Footer>
          </Dialog.Content>
        </Dialog.Positioner>
      </Dialog.RootProvider>
    </>
  );
};

interface ModifyResourceRequest extends CreateResourceRequest {
  id: string;
}

const ModalContent = ({
  repository,
  modifyResourceRequest,
  setModifyResourceRequest,
}: {
  repository: RepositoryResource;
  modifyResourceRequest: ModifyResourceRequest | null;
  setModifyResourceRequest: (data: ModifyResourceRequest) => void;
}) => {
  const githubUrl = repository.repositoryUrl;
  const [loadingPull, setLoadingPull] = useState(false);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoadingPull(true);
        // eslint-disable-next-line no-useless-escape
        const match = githubUrl.match(
          /github\.com\/([^\/]+)\/([^\/]+)(\.git)?/
        );
        if (!match) {
          alert("Invalid GitHub URL format.");
          return;
        }

        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const [_, owner, repo] = match;

        const tryFetch = async (branch: string) => {
          const rawUrl = `https://raw.githubusercontent.com/${owner}/${repo}/${branch}/cybershuttle.yml?ts=${Date.now()}&a=${Math.random()}`;
          const res = await fetch(rawUrl);
          if (!res.ok) throw new Error(`Branch ${branch} not found`);
          return res.text();
        };

        let fileContent = "";
        try {
          fileContent = await tryFetch("main");
        } catch {
          fileContent = await tryFetch("master");
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const parsed = yaml.load(fileContent) as any;
        console.log(parsed);
        const resourceRequest: ModifyResourceRequest = {
          id: repository.id,
          name: parsed.project.name,
          headerImage: "image.png",
          description: parsed.project.description,
          tags: parsed.project.tags,
          authors: parsed.project.authors,
          privacy: parsed.project.privacy || "PUBLIC",
        };
        setModifyResourceRequest(resourceRequest);
      } catch (error) {
        console.error("Error fetching cybershuttle.yml:", error);
        alert("Failed to fetch cybershuttle.yml. Please check the URL.");
      } finally {
        setLoadingPull(false);
      }
    }

    fetchData();
  }, []);

  if (loadingPull || !modifyResourceRequest) {
    return <Spinner />;
  }
  return (
    <>
      <VStack gap={2} alignItems="flex-start" mt={4}>
        <KeyValue label="Repository Name" value={modifyResourceRequest?.name} />
        <KeyValue label="Repository URL" value={repository.repositoryUrl} />
        <KeyValue
          label="Description"
          value={modifyResourceRequest?.description}
        />
        <KeyValue label="Tags" value={modifyResourceRequest?.tags.join(", ")} />
        <KeyValue
          label="Authors"
          value={modifyResourceRequest?.authors.join(", ")}
        />
        <KeyValue
          label="Privacy"
          value={modifyResourceRequest?.privacy?.toUpperCase()}
        />
      </VStack>
    </>
  );
};

const KeyValue = ({ label, value }: { label: string; value: string }) => {
  return (
    <Text>
      <Text as="span" fontSize="sm" fontWeight={"bold"}>
        {label}:{" "}
      </Text>
      <Text as="span" fontSize="sm">
        {value}
      </Text>
    </Text>
  );
};
