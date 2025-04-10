import { RepositoryResource } from "@/interfaces/ResourceType";
import {
  Box,
  HStack,
  Heading,
  Button,
  Breadcrumb,
  ListRoot,
  ListItem,
  Icon,
  Spinner,
  Text,
} from "@chakra-ui/react";
import { FaGithub } from "react-icons/fa";
import { FiFolder, FiFile } from "react-icons/fi";
import { Fragment, useEffect, useState } from "react";
import { getGithubOwnerAndRepo } from "@/lib/util";

interface FileTreeItem {
  name: string;
  type: string;
  sha: string;
  path: string;
  size?: number;
}

export const GitHubFileTree = ({
  repository,
}: {
  repository: RepositoryResource;
}) => {
  const githubUrl = repository.repositoryUrl;
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

  // const fetchFileContent = (owner: string, repo: string, path: string) => {
  //   setFileTreeLoading(true);
  //   fetch(`https://api.github.com/repos/${owner}/${repo}/contents/${path}`)
  //     .then((resp) => {
  //       if (!resp.ok) {
  //         console.error("Error fetching file tree:", error);
  //         setFileTreeLoading(false);
  //         setFileContent(null);
  //         setError(
  //           "Error fetching file tree. GitHub's API rate limit might be exceeded (60 / hour)."
  //         );
  //       }
  //       return resp.json();
  //     })
  //     .then((data) => {
  //       if (data.content) {
  //         setFileContent(atob(data.content.replace(/\n/g, "")));
  //       } else {
  //         throw new Error("File may be too large to display");
  //       }
  //     })
  //     .catch((error) => {
  //       console.log("Error fetching file content:", error);
  //       toaster.create({
  //         title: "Toast Title",
  //         description: "Toast Description",
  //       });
  //     })
  //     .finally(() => {
  //       setFileTreeLoading(false);
  //     });
  // };

  const handleFolderClick = (path: string) => {
    setHistory((prev) => [...prev, currentPath]);
    setCurrentPath(path);
    setFileContent(null);
  };

  const handleFileClick = (path: string) => {
    // setHistory((prev) => [...prev, currentPath]);
    // const ownerAndRepo = getGithubOwnerAndRepo(githubUrl);
    // if (ownerAndRepo) {
    //   const owner = ownerAndRepo.owner;
    //   const repo = ownerAndRepo.repo;
    //   fetchFileContent(owner, repo, path);
    // }

    const urlWithoutGit = repository.repositoryUrl.replace(".git", "");
    window.open(urlWithoutGit + "/tree/main/" + path, "_blank");
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
    <Box>
      <HStack alignItems="center" justifyContent="space-between">
        <Heading fontWeight="bold" size="2xl">
          GitHub Repository
        </Heading>
        <Button
          mt={2}
          size="sm"
          as="a"
          // @ts-expect-error This is fine
          target="_blank"
          href={repository.repositoryUrl}
        >
          <FaGithub />
          Open in GitHub
        </Button>
      </HStack>

      <Box
        mt={4}
        bg="white"
        p={4}
        borderRadius="md"
        shadow="md"
        overflow="auto"
        height="full"
        width="full"
      >
        {/* Open in GitHub button */}
        <Button onClick={handleGoBack} mb={4}>
          Back
        </Button>
        <Breadcrumb.Root mt={2}>
          <Breadcrumb.List>
            <Breadcrumb.Item>
              <Breadcrumb.Link
                as="button"
                onClick={(e) => {
                  e.preventDefault();
                  setHistory([]);
                  setCurrentPath("");
                  setFileContent(null);
                }}
              >
                root
              </Breadcrumb.Link>
            </Breadcrumb.Item>

            {currentPath
              .split("/")
              .filter(Boolean)
              .map((segment, index, arr) => {
                const fullPath = arr.slice(0, index + 1).join("/");
                return (
                  <Fragment key={index}>
                    <Breadcrumb.Separator />
                    <Breadcrumb.Item>
                      <Breadcrumb.Link
                        as="button"
                        onClick={(e) => {
                          e.preventDefault();
                          setHistory((prev) =>
                            prev.slice(
                              0,
                              prev.indexOf(fullPath) !== -1
                                ? prev.indexOf(fullPath)
                                : 0
                            )
                          );
                          setCurrentPath(fullPath);
                          setFileContent(null);
                        }}
                      >
                        {segment}
                      </Breadcrumb.Link>
                    </Breadcrumb.Item>
                  </Fragment>
                );
              })}
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
                  onClick={() => {
                    if (file.type === "dir") {
                      handleFolderClick(file.path);
                    } else {
                      handleFileClick(file.path);
                    }
                  }}
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
    </Box>
  );
};
