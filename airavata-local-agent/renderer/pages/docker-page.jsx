import { Heading, Button, Center, Box, HStack } from "@chakra-ui/react";
import { useState, useEffect } from "react";

const DockerPage = () => {
  const [runningContainers, setRunningContainers] = useState([]); // [container1, container2, ...

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

  const handleStopNotebook = (containerId) => {
    window.ipc.send("stop-notebook", containerId);
  };

  const getRunningContainers = () => {
    window.ipc.send("get-running-containers");
  };

  useEffect(() => {
    window.ipc.on("notebook-started", () => {
      console.log("Notebook started");
    });

    window.ipc.on("notebook-stopped", () => {
      console.log("Notebook stopped: ");
    });

    window.ipc.on("got-running-containers", (runningContainers) => {
      console.log("Got running containers: ", runningContainers);

      if (runningContainers) {
        setRunningContainers(runningContainers);
      }
    });



    return () => {
      window.ipc.removeAllListeners("notebook-started");
      window.ipc.removeAllListeners("notebook-stopped");
      window.ipc.removeAllListeners("got-running-containers");
    };
  }, []);

  useEffect(() => {

    let interval = setInterval(() => {
      getRunningContainers();
    }, 1000);

    return () => {
      clearInterval(interval);
    };

  });

  return (
    <Center h='100vh' textAlign='center'>
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

          {/* <Button
            mt={2}
            onClick={handleStopNotebook} colorScheme='red'
          >
            Stop Notebook
          </Button> */}
        </HStack>

        <Box mt={4}>
          {
            runningContainers.map((container, index) => {
              return <Box key={index}>
                <Button
                  mt={2}
                  onClick={() => handleStopNotebook(container.id)}
                  colorScheme='red'
                >
                  {container.name} - {container.id}
                </Button>
              </Box>;
            })
          }
        </Box>
      </Box>
    </Center>
  );
};

export default DockerPage;