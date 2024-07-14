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
import { useEffect, useState } from "react";

const DEFAULT_CONFIG = {
  port: "6080",
  name: "",
  mountLocation: ""
};

export const AvailablePrograms = () => {
  const CreateJupyterModal = useDisclosure();
  const [startContainerConfig, setStartContainerConfig] = useState(DEFAULT_CONFIG);
  const toast = useToast();

  useEffect(() => {
    window.ipc.on("filepath-chosen", (filepath) => {
      setStartContainerConfig(prev => ({
        ...prev,
        mountLocation: filepath
      }));

      console.log("Filepath chosen: ", filepath);
    });

    return () => {
      window.ipc.removeAllListeners("filepath-chosen");
    };
  }, []);


  const handleStartNotebook = () => {
    if (startContainerConfig.port === "") {
      toast({
        title: "Error",
        description: "Please enter a port number",
        status: "error",
        duration: 9000,
        isClosable: true,
      });
      return;
    }
    // mount on host machine
    let createOptions = {
      'name': startContainerConfig.name,
      'Tty': false,
      'ExposedPorts': {
        '8888/tcp': {}
      },
      'HostConfig': {
        'PortBindings': {
          '8888/tcp': [
            {
              'HostPort': startContainerConfig.port
            }
          ]
        },
      },
    };

    if (startContainerConfig.mountLocation !== "") {
      createOptions.HostConfig.Binds = [`${startContainerConfig.mountLocation}:/home/jovyan/work`];
      createOptions.Volumes = {
        '/home/jovyan/work': {}
      };
    };

    let imageName = "jupyter/datascience-notebook";
    window.ipc.send("start-notebook", imageName, createOptions);
    setStartContainerConfig(DEFAULT_CONFIG);
    CreateJupyterModal.onClose();
  };

  return (

    <>

      <Modal isOpen={CreateJupyterModal.isOpen} onClose={CreateJupyterModal.onClose} size='4xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Create a Jupyter Notebook</ModalHeader>
          <ModalCloseButton />
          <ModalBody>

            <FormControl>
              <FormLabel>Container Name</FormLabel>
              <Input value={startContainerConfig.name} onChange={(e) => {
                setStartContainerConfig(prev => ({
                  ...prev,
                  name: e.target.value
                }));
              }}
              />
              <FormHelperText>If blank, one will automatically be generated for you.</FormHelperText>
            </FormControl>

            <FormControl mt={4}>
              <FormLabel>Port on Host Computer</FormLabel>
              <Input value={startContainerConfig.port} onChange={(e) => {
                setStartContainerConfig(prev => ({
                  ...prev,
                  port: e.target.value
                }));
              }}
              />
            </FormControl>

            <FormControl mt={4}>
              <FormLabel>Mount Location</FormLabel>
              <Stack direction='row'>
                <Input value={startContainerConfig.mountLocation} readOnly />
                <Button
                  onClick={() => {
                    window.ipc.send("choose-filepath");
                  }}
                >Choose</Button>
              </Stack>
              <FormHelperText>Mount location on host machine. Leave blank to not mount container on host machine.</FormHelperText>
            </FormControl>

            <Button
              mt={4}
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

      <SimpleGrid columns={3} spacing={10} p={4}>
        <Box shadow='md' rounded='md' p={4}
          onClick={() => {
            CreateJupyterModal.onOpen();
          }}
          _hover={{
            cursor: "pointer",
            background: "gray.100"
          }}
        >
          <Stack spacing={2}>
            <Flex align='center' gap={2}>
              <Img src="/images/jupyter_logo.png" alt="Jupyter Logo" boxSize='30px' />
              <Heading size='md'>Jupyter Notebook</Heading>

            </Flex>
            <Text>Create a new container with a dockerized Jupyter Notebook.</Text>
          </Stack>
        </Box>
      </SimpleGrid>
    </>
  );
};