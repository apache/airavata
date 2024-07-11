import {
  Heading, Button, Center, Box, HStack, Table,
  Thead,
  Tbody,
  Tfoot,
  Tr,
  Th,
  Td,
  TableCaption,
  TableContainer,
  useToast,
} from "@chakra-ui/react";
import { useState, useEffect } from "react";

const DockerPage = () => {
  const [runningContainers, setRunningContainers] = useState([]); // [container1, container2, ...
  const [isLoadingStart, setIsLoadingStart] = useState(false);
  const [isLoadingStop, setIsLoadingStop] = useState(false);
  const toast = useToast();

  const handleStartNotebook = () => {
    let createOptions = {
      'Tty': false,
      'ExposedPorts': {
        '8888/tcp': {}
      },
      'HostConfig': {
        'PortBindings': {
          '8888/tcp': [
            {
              'HostPort': '6080'
            }
          ]
        }
      }
    };

    let imageName = "jupyter/datascience-notebook:latest";

    window.ipc.send("start-notebook", imageName, createOptions);
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

      console.log(err);

      if (err) {
        toast({
          title: "Error",
          description: err,
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      }

      setIsLoadingStart(false);
      getRunningContainers();
    });

    window.ipc.on("container-stopped", (containerId) => {
      console.log("Container stopped: ", containerId);
      setIsLoadingStop(false);
      getRunningContainers();
    });


    window.ipc.on("notebook-started", (containerId, err) => {
      if (err) {
        toast({
          title: "Error",
          description: err,
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      }
      getRunningContainers();
    });


    window.ipc.on("got-running-containers", (runningContainers) => {
      if (runningContainers) {
        setRunningContainers(runningContainers);
        console.log("Running containers: ", runningContainers);
      }
    });

    return () => {
      window.ipc.removeAllListeners("container-started");
      window.ipc.removeAllListeners("container-stopped");
      window.ipc.removeAllListeners("notebook-started");
      window.ipc.removeAllListeners("notebook-stopped");
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
    <Center mt={16} textAlign='center'>
      <Box>
        <Heading>Docker Page</Heading>

        <HStack justify='center'>
          <Button
            mt={2}
            onClick={handleStartNotebook}
            colorScheme='green'
          >
            Start Notebook
          </Button>
        </HStack>
        <TableContainer>
          <Table variant='simple'>
            <TableCaption>Imperial to metric conversion factors</TableCaption>
            <Thead>
              <Tr>
                <Th>Name</Th>
                <Th>Image</Th>
                <Th>Status</Th>
                <Th>Actions</Th>

              </Tr>
            </Thead>
            <Tbody>
              {
                runningContainers.map((container, index) => {
                  return (
                    <Tr key={index}>
                      <Td>{container.Names[0]}</Td>
                      <Td>{container.Image}</Td>
                      <Td>{container.State}</Td>
                      <Td>
                        {
                          container.State === "exited" && (
                            <>
                              <Button
                                mt={2}
                                onClick={() => handleStartContainer(container.Id)}
                                colorScheme='green'
                                size='sm'
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
                                size='sm'
                                isDisabled={isLoadingStop}
                              >
                                Stop
                              </Button>
                            </>
                          )
                        }

                      </Td>
                    </Tr>
                  );
                })
              }

            </Tbody>
          </Table>
        </TableContainer>
        {/* <Box mt={4}>
          {
            runningContainers.map((container, index) => {
              return <Box key={index}>
                <Button
                  mt={2}
                  onClick={() => handleStopNotebook(container.Id)}
                  colorScheme='red'
                >
                  {container.Names[0]} - {container.Id}
                </Button>
              </Box>;
            })
          }
        </Box> */}
      </Box>
    </Center>
  );
};

export default DockerPage;