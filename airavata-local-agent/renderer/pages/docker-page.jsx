import {
  Button, Tooltip, Box, HStack, Table,
  Thead,
  Tbody,
  Text,
  Tr,
  Th,
  Td,
  useDisclosure,
  TableContainer,
  useToast, Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalFooter,
  ModalBody,
  ModalCloseButton,
  Input,
  FormControl,
  FormLabel,
  Stack,
  IconButton,
  FormHelperText,
  Heading,
  SimpleGrid,
  Flex,
  Img,

} from "@chakra-ui/react";
import { useState, useEffect } from "react";
import { DockerInspectModal } from "../components/DockerComponents/DockerInspectModal";
import { DeleteIcon } from '@chakra-ui/icons';
import { canPerformAction } from "../lib/utilityFuncs";
import { DockerImagesList } from "../components/DockerComponents/DockerImagesList";
import { AvailablePrograms } from "../components/DockerComponents/AvaliablePrograms";

const DOCKER_ID_LENGTH = 12;

const DockerPage = () => {
  const [runningContainers, setRunningContainers] = useState([]); // [container1, container2, ...
  const [isLoadingStart, setIsLoadingStart] = useState(false);
  const [isLoadingStop, setIsLoadingStop] = useState(false);
  const [isLoadingDelete, setIsLoadingDelete] = useState(false);
  const InspectModal = useDisclosure();
  const [activeContainer, setActiveContainer] = useState("");
  const [portMapping, setPortMapping] = useState({});

  const toast = useToast();

  const handleRemoveContainer = (containerId) => {
    setIsLoadingDelete(true);
    window.ipc.send("remove-container", containerId);
  };

  const handleStartContainer = (containerId) => {
    setIsLoadingStart(true);
    window.ipc.send("start-container", containerId);
  };

  const handleStopContainer = (containerId) => {
    setIsLoadingStop(true);
    window.ipc.send("stop-notebook", containerId);
  };

  const getRunningContainers = () => {
    window.ipc.send("get-running-containers");
  };

  useEffect(() => {
    getRunningContainers(); // so user's don't need to wait to see containers

    window.ipc.on("container-started", (containerId, err) => {
      console.log("Container started: ", containerId);
      if (err) {
        toast({
          title: "Error",
          description: err,
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      } else {
        toast({
          title: "Success",
          description: "Container started successfully",
          status: "success",
          duration: 9000,
          isClosable: true,
        });
      }
      setIsLoadingStart(false);
    });

    window.ipc.on("container-stopped", (containerId) => {
      console.log("Container stopped: ", containerId);
      toast({
        title: "Success",
        description: "Container stopped successfully",
        status: "success",
        duration: 9000,
        isClosable: true,
      });
      setIsLoadingStop(false);
    });

    window.ipc.on("container-removed", (containerId) => {
      console.log("container-removed: ", containerId);
      toast({
        title: "Success",
        description: "Container removed successfully",
        status: "success",
        duration: 9000,
        isClosable: true,
      });
      setIsLoadingDelete(false);
    });

    window.ipc.on("got-running-containers", (runningContainers) => {
      if (runningContainers === null) {
        toast({
          title: "Error getting running containers",
          description: "Please make sure docker is installed and running properly.",
          status: "error",
          duration: 9000,
          isClosable: true,
        });
        return;
      }

      window.ipc.send("get-container-ports", runningContainers);
      setRunningContainers(runningContainers);
    });

    window.ipc.on('got-container-ports', (ports) => {
      setPortMapping(ports);
    });

    return () => {
      window.ipc.removeAllListeners("container-started");
      window.ipc.removeAllListeners("container-stopped");
      window.ipc.removeAllListeners("container-removed");
      window.ipc.removeAllListeners("got-running-containers");
      window.ipc.removeAllListeners("got-container-ports");
    };
  }, []);

  useEffect(() => {
    let interval = setInterval(() => {
      getRunningContainers();
    }, 3000);

    return () => {
      clearInterval(interval);
    };
  });

  return (
    <Box p={4}>

      <Modal isOpen={InspectModal.isOpen} onClose={InspectModal.onClose} size='4xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Container Details</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <DockerInspectModal containerId={activeContainer.Id} />
          </ModalBody>

          <ModalFooter>
          </ModalFooter>
        </ModalContent>
      </Modal>


      <Box>
        <Heading size='xl' textAlign='center'>Available Programs</Heading>
        <AvailablePrograms />
      </Box>


      <Box mt={10}>
        <Heading size='xl' textAlign='center'>Running Containers</Heading>
        <TableContainer>
          <Table variant='simple'>
            <Thead>
              <Tr>
                <Th>Name</Th>
                <Th>ID</Th>
                <Th>Image</Th>
                <Th>Status</Th>
                <Th>Ports</Th>
                <Th>Actions</Th>
              </Tr>
            </Thead>
            <Tbody>
              {
                runningContainers.map((container, index) => {
                  let theName = container.Names[0].slice(1);
                  return (
                    <Tr key={index}>
                      <Td _hover={{
                        background: "gray.100",
                        cursor: "pointer"
                      }} onClick={() => {
                        setActiveContainer({
                          Id: container.Id,
                          name: theName
                        });
                        InspectModal.onOpen();
                      }}>{theName}</Td>
                      <Td>
                        <Tooltip label={container.Id}>
                          <Text
                            _hover={{
                              cursor: "pointer",
                            }}
                            onClick={() => {
                              navigator.clipboard.writeText(container.Id);
                              toast({
                                title: "Container ID copied to clipboard",
                                description: container.Id,
                                status: "success",
                                duration: 9000,
                                isClosable: true,
                              });
                            }}
                          >{container.Id.slice(0, DOCKER_ID_LENGTH)}</Text>
                        </Tooltip>
                      </Td>
                      <Td>{container.Image}</Td>
                      <Td>{container.Status}</Td>
                      <Td>
                        {
                          portMapping[container.Id] && portMapping[container.Id].map((port, index) => {
                            return (
                              <Text key={index}>{port.hostPort}:{port.containerPort}</Text>
                            );
                          })
                        }
                      </Td>
                      <Td>

                        <Stack direction='row' spacing={2}>
                          {
                            canPerformAction("start", container.State)
                            && (
                              <>
                                <Button
                                  mt={2}
                                  onClick={() => handleStartContainer(container.Id)}
                                  colorScheme='green'
                                  size='xs'
                                  isDisabled={isLoadingStart}
                                >
                                  Start
                                </Button>
                              </>
                            )
                          }

                          {
                            canPerformAction("stop", container.State) && (
                              <>
                                <Button
                                  mt={2}
                                  onClick={() => handleStopContainer(container.Id)}
                                  colorScheme='red'
                                  size='xs'
                                  isDisabled={isLoadingStop}
                                >
                                  Stop
                                </Button>
                              </>
                            )
                          }

                          <IconButton
                            mt={2}
                            onClick={() => handleRemoveContainer(container.Id)}
                            colorScheme='red'
                            variant='outline'
                            isDisabled={!canPerformAction("remove", container.State) || isLoadingDelete}
                            size='xs'
                            icon={<DeleteIcon />}
                          />
                        </Stack>

                      </Td>
                    </Tr>
                  );
                })
              }
            </Tbody>
          </Table>
        </TableContainer>
      </Box>


      {/* EVERYTHING BELOW IS TEMPORARY */}

      <Box mt={72}>
        <Heading size='xl' textAlign='center'>Docker Images</Heading>
        <DockerImagesList />
      </Box>
    </Box>
  );
};

export default DockerPage;