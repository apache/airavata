import { Button, Code, Input, Text, VStack } from "@chakra-ui/react";
import { useState } from "react";
import yaml from "js-yaml";

export const AddGitUrl = ({
  nextStage,
  repoData,
  setRepoData,
}: {
  nextStage: () => void;
  repoData: any;
  setRepoData: React.Dispatch<React.SetStateAction<any>>;
}) => {
  const [githubUrl, setGithubUrl] = useState("");
  const [loadingPull, setLoadingPull] = useState(false);

  const onPullCybershuttleYml = async () => {
    try {
      setLoadingPull(true);
      const match = githubUrl.match(/github\.com\/([^\/]+)\/([^\/]+)(\.git)?/);
      if (!match) {
        alert("Invalid GitHub URL format.");
        return;
      }

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

      const parsed = yaml.load(fileContent);
      console.log("Parsed cybershuttle.yml:", parsed);
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
