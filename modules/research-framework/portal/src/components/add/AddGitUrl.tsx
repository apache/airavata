import { Button, Code, Input, Text, VStack } from "@chakra-ui/react";
import { useState } from "react";
import yaml from "js-yaml";
import { CreateResourceRequest } from "@/interfaces/Requests/CreateResourceRequest";

export const AddGitUrl = ({
  nextStage,
  createResourceRequest,
  setCreateResourceRequest,
  githubUrl,
  setGithubUrl,
}: {
  nextStage: () => void;
  createResourceRequest: CreateResourceRequest;
  setCreateResourceRequest: (data: CreateResourceRequest) => void;
  githubUrl: string;
  setGithubUrl: (url: string) => void;
}) => {
  const [loadingPull, setLoadingPull] = useState(false);

  const onPullCybershuttleYml = async () => {
    try {
      setLoadingPull(true);
      // eslint-disable-next-line no-useless-escape
      const match = githubUrl.match(/github\.com\/([^\/]+)\/([^\/]+)(\.git)?/);
      if (!match) {
        alert("Invalid GitHub URL format.");
        return;
      }

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const [_, owner, repo] = match;

      const tryFetch = async (branch: string) => {
        const rawUrl = `https://raw.githubusercontent.com/${owner}/${repo}/${branch}/cybershuttle.yml`;
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
      setCreateResourceRequest({
        ...createResourceRequest,
        name: parsed.project.name,
        headerImage: "image.png",
        description: parsed.project.description,
        tags: parsed.project.tags,
        authors: parsed.project.authors,
      });
      nextStage();
    } catch (error) {
      console.error("Error fetching cybershuttle.yml:", error);
      alert("Failed to fetch cybershuttle.yml. Please check the URL.");
    } finally {
      setLoadingPull(false);
    }
  };

  return (
    <VStack alignItems="flex-start">
      <Text>Paste GitHub Url</Text>
      <Text fontSize="sm" color="gray.500">
        We&apos;ll pull the <Code>cybershuttle.yml</Code> file from the
        repository to auto-populate the project fields.
      </Text>
      <Input
        placeholder="https://github.com/username/repo.git"
        value={githubUrl}
        onChange={(e) => setGithubUrl(e.target.value)}
        mt={2}
      />
      <Button
        width="full"
        loading={loadingPull}
        onClick={onPullCybershuttleYml}
        mt={4}
        colorScheme="blue"
        disabled={!githubUrl}
      >
        Pull cybershuttle.yml file
      </Button>
    </VStack>
  );
};
