import {
  Heading, Button, Center, Box, HStack, Table,
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

} from "@chakra-ui/react";
import { useState, useEffect } from "react";
import { DockerInspectModal } from "../components/DockerInspectModal";
import { DeleteIcon } from '@chakra-ui/icons';

const DOCKER_ID_LENGTH = 12;

const DockerPage = () => {
  const [runningContainers, setRunningContainers] = useState([]); // [container1, container2, ...
  const [isLoadingStart, setIsLoadingStart] = useState(false);
  const [isLoadingStop, setIsLoadingStop] = useState(false);
  const [isLoadingDelete, setIsLoadingDelete] = useState(false);
  const InspectModal = useDisclosure();
  const CreateModal = useDisclosure();
  const [activeContainer, setActiveContainer] = useState("");
  const [portEntered, setPortEntered] = useState("6080");
  const toast = useToast();


  const handleStartNotebook = () => {
    if (portEntered === "") {
      toast({
        title: "Error",
        description: "Please enter a port number",
        status: "error",
        duration: 9000,
        isClosable: true,
      });
      return;
    }
    let createOptions = {
      'Tty': false,
      'ExposedPorts': {
        '8888/tcp': {}
      },
      'HostConfig': {
        'PortBindings': {
          '8888/tcp': [
            {
              'HostPort': portEntered
            }
          ]
        }
      }
    };

    let imageName = "jupyter/datascience-notebook:latest";
    window.ipc.send("start-notebook", imageName, createOptions);

    CreateModal.onClose();
  };

  const handleDeleteContainer = (containerId) => {
    setIsLoadingDelete(true);
    window.ipc.send("delete-container", containerId);
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

    window.ipc.on("container-deleted", (containerId) => {
      console.log("Container deleted: ", containerId);
      toast({
        title: "Success",
        description: "Container deleted successfully",
        status: "success",
        duration: 9000,
        isClosable: true,
      });
      setIsLoadingDelete(false);
    });

    window.ipc.on("got-running-containers", (runningContainers) => {
      console.log(runningContainers);
      setRunningContainers(runningContainers);
    });

    return () => {
      window.ipc.removeAllListeners("container-started");
      window.ipc.removeAllListeners("container-stopped");
      window.ipc.removeAllListeners("container-stopped");
      window.ipc.removeAllListeners("container-deleted");
      window.ipc.removeAllListeners("got-running-containers");

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
    <Box mt={16} textAlign='center'>

      <Modal isOpen={InspectModal.isOpen} onClose={InspectModal.onClose} size='4xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Inspecting Container ({activeContainer.name})</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <DockerInspectModal containerId={activeContainer.Id} />
          </ModalBody>

          <ModalFooter>
            <Button colorScheme='blue' mr={3} onClick={InspectModal.onClose}>
              Close
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>

      <Modal isOpen={CreateModal.isOpen} onClose={CreateModal.onClose} size='4xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Create a Jupyter Notebook</ModalHeader>
          <ModalCloseButton />
          <ModalBody>

            <FormControl>
              <FormLabel>Port on Host Computer</FormLabel>
              <Input value={portEntered} onChange={(e) => setPortEntered(e.target.value)} />
            </FormControl>

            <Button
              mt={2}
              onClick={() => {
                handleStartNotebook();
              }}
              colorScheme='green'
            >
              Start Jupyter Notebook
            </Button>
          </ModalBody>
        </ModalContent>
      </Modal>



      <Box>
        <HStack justify='center'>
          <Button
            mt={2}
            onClick={() => {
              CreateModal.onOpen();
            }}
            colorScheme='green'
          >
            Start Jupyter Notebook
          </Button>
        </HStack>
        <TableContainer>
          <Table variant='simple'>
            <Thead>
              <Tr>
                <Th>Name</Th>
                <Th>ID</Th>
                <Th>Image</Th>
                <Th>Status</Th>
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
                      </Td>
                      <Td>{container.Image}</Td>
                      <Td>{container.Status}</Td>
                      <Td>

                        <Stack direction='row' spacing={2}>
                          {
                            (container.State === "exited" || container.State === "created") && (
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
                            container.State === "running" && (
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
                            onClick={() => handleDeleteContainer(container.Id)}
                            colorScheme='red'
                            variant='outline'
                            isDisabled={container.State === "running"}
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
    </Box>
  );
};

export default DockerPage;