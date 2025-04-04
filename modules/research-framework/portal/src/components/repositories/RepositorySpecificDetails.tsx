import { RepositoryResource } from "@/interfaces/ResourceType";
import { getGithubOwnerAndRepo } from "@/lib/util";
import {
  Box,
  Icon,
  ListItem,
  ListRoot,
  Spinner,
  Button,
  Text,
  Breadcrumb,
} from "@chakra-ui/react";
import { Fragment, useEffect, useState } from "react";
import { FiFolder, FiFile } from "react-icons/fi";

interface FileTreeItem {
  name: string;
  type: string;
  sha: string;
  path: string;
  size?: number;
}

export const RepositorySpecificDetails = ({
  dataset,
}: {
  dataset: RepositoryResource;
}) => {
  const githubUrl = dataset.repositoryUrl;
  const [fileTree, setFileTree] = useState<FileTreeItem[]>([]);
  const [fileTreeLoading, setFileTreeLoading] = useState(false);
  const [currentPath, setCurrentPath] = useState<string>("");
  const [history, setHistory] = useState<string[]>([]);
  const [fileContent, setFileContent] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!githubUrl) return;
    const ownerAndRepo = getGithubOwnerAndRepo(githubUrl);
    if (ownerAndRepo) {
      const owner = ownerAndRepo.owner;
      const repo = ownerAndRepo.repo;
      fetchFileTree(owner, repo, currentPath);
    }
  }, [githubUrl, currentPath]);

  const fetchFileTree = (owner: string, repo: string, path: string) => {
    setFileTreeLoading(true);
    fetch(`https://api.github.com/repos/${owner}/${repo}/contents/${path}`)
      .then((resp) => {
        if (!resp.ok) {
          setFileTree([]);
          console.error("Error fetching file tree:", error);
          setFileTreeLoading(false);
          setError(
            "Error fetching file tree. GitHub's API rate limit might be exceeded (60 / hour)."
          );
        }
        return resp.json();
      })
      .then((data) => {
        if (Array.isArray(data)) {
          setFileTree(data);
        } else {
          setFileTree([]);
        }
        setFileTreeLoading(false);
      });
  };

  const fetchFileContent = (owner: string, repo: string, path: string) => {
    setFileTreeLoading(true);
    fetch(`https://api.github.com/repos/${owner}/${repo}/contents/${path}`)
      .then((resp) => {
        if (!resp.ok) {
          console.error("Error fetching file tree:", error);
          setFileTreeLoading(false);
          setFileContent(null);
          setError(
            "Error fetching file tree. GitHub's API rate limit might be exceeded (60 / hour)."
          );
        }
        return resp.json();
      })
      .then((data) => {
        if (data.content) {
          setFileContent(atob(data.content.replace(/\n/g, "")));
        }
        setFileTreeLoading(false);
      });
  };

  const handleFolderClick = (path: string) => {
    setHistory((prev) => [...prev, currentPath]);
    setCurrentPath(path);
    setFileContent(null);
  };

  const handleFileClick = (path: string) => {
    setHistory((prev) => [...prev, currentPath]);
    const ownerAndRepo = getGithubOwnerAndRepo(githubUrl);
    if (ownerAndRepo) {
      const owner = ownerAndRepo.owner;
      const repo = ownerAndRepo.repo;
      fetchFileContent(owner, repo, path);
    }

    console.log("File clicked:", path);
  };

  const handleGoBack = () => {
    if (history.length > 0) {
      const previousPath = history[history.length - 1];
      setHistory((prev) => prev.slice(0, -1));
      setCurrentPath(previousPath);
      setFileContent(null);
    }
  };

  if (!githubUrl) return null;
  if (fileTreeLoading) return <Spinner />;
  if (error !== null) return null;

  return (
    <Box
      bg="white"
      p={4}
      borderRadius="md"
      shadow="md"
      overflow="auto"
      height="full"
    >
      <Button onClick={handleGoBack} mb={4}>
        Back
      </Button>
      <Breadcrumb.Root mt={2}>
        <Breadcrumb.List>
          <Breadcrumb.Item>
            <Breadcrumb.Link href="#">root</Breadcrumb.Link>
          </Breadcrumb.Item>

          {history.length > 0 &&
            currentPath
              .split("/")
              .filter(Boolean) // Remove empty strings
              .map((path, index) => (
                <Fragment key={index}>
                  <Breadcrumb.Separator />
                  <Breadcrumb.Item>
                    <Breadcrumb.Link href="#">{path}</Breadcrumb.Link>
                  </Breadcrumb.Item>
                </Fragment>
              ))}
        </Breadcrumb.List>
      </Breadcrumb.Root>{" "}
      {fileContent ? (
        <Box p={4} bg="gray.100" borderRadius="md">
          <Text whiteSpace="pre-wrap" fontSize="sm" fontFamily="monospace">
            {fileContent}
          </Text>
        </Box>
      ) : (
        <ListRoot>
          {Array.isArray(fileTree) &&
            fileTree.map((file) => (
              <ListItem
                key={file.sha}
                display="flex"
                alignItems="center"
                p={2}
                borderRadius="md"
                _hover={{ bg: "gray.100", cursor: "pointer" }}
                onClick={() =>
                  file.type === "dir"
                    ? handleFolderClick(file.path)
                    : handleFileClick(file.path)
                }
              >
                <Icon
                  as={file.type === "dir" ? FiFolder : FiFile}
                  color={file.type === "dir" ? "blue.500" : "gray.500"}
                  mr={2}
                />
                <p>{file.name}</p>
                {file.size !== undefined && file.size > 0 && (
                  <Text fontSize="xs" color="gray.500" ml={2}>
                    ({file.size} bytes)
                  </Text>
                )}
              </ListItem>
            ))}
        </ListRoot>
      )}
    </Box>
  );
};
