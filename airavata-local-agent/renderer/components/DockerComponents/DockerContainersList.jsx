/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/
import {
  Button,
  Box,
  Table,
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
  Stack,
  IconButton,
  Flex,
  Switch,
  Link,
  Icon,
} from "@chakra-ui/react";
import { useState, useEffect } from "react";
import { DockerInspectModal } from "./DockerInspectModal";
import { DeleteIcon } from '@chakra-ui/icons';
import { canPerformAction } from "../../lib/utilityFuncs";
import { useInterval } from 'usehooks-ts';
import { MdContentCopy } from 'react-icons/md';
const DOCKER_ID_LENGTH = 12;
const CONTAINER_FETCH_INTERVAL = 3000;

export const DockerContainersList = ({ setTabIndex }) => {
  const [runningContainers, setRunningContainers] = useState([]); // [container1, container2, ...
  const [isLoadingStart, setIsLoadingStart] = useState(false);
  const [isLoadingStop, setIsLoadingStop] = useState(false);
  const [isLoadingDelete, setIsLoadingDelete] = useState(false);
  const [activeContainer, setActiveContainer] = useState("");
  const [portMapping, setPortMapping] = useState({});
  const [showOnlyCybershuttle, setShowOnlyCybershuttle] = useState(true);
  const [deleteContainer, setDeleteContainer] = useState({});
  const [shouldShowMapping, setShouldShowMapping] = useState({});
  const InspectModal = useDisclosure();
  const DeleteModal = useDisclosure();
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
    window.ipc.send("stop-container", containerId);
  };

  const getContainers = () => {
    window.ipc.send("get-containers");
  };

  useEffect(() => {

    getContainers(); // so user's don't need to wait to see containers

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
          description: "Container started successfully. The program may take a few seconds to open.",
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

    window.ipc.on("got-containers", (allContainers) => {
      if (allContainers === null) {
        toast({
          title: "Error getting running containers",
          description: "Please make sure docker is installed and running properly.",
          status: "error",
          duration: 3000,
          isClosable: true,
        });
        return;
      }

      window.ipc.send("get-container-ports", allContainers);
      window.ipc.send("get-should-show-runs", allContainers);

      if (showOnlyCybershuttle) {
        allContainers = allContainers.filter(container => {
          return container.Labels['cybershuttle-local-agent'] === 'true';
        });
      };

      setRunningContainers(allContainers);
    });

    window.ipc.on('got-container-ports', (ports) => {
      setPortMapping(ports);
    });

    window.ipc.on('got-should-show-runs', (shouldShows) => {
      setShouldShowMapping(shouldShows);
    });

    return () => {
      window.ipc.removeAllListeners("container-started");
      window.ipc.removeAllListeners("container-stopped");
      window.ipc.removeAllListeners("container-removed");
      window.ipc.removeAllListeners("got-containers");
      window.ipc.removeAllListeners("got-container-ports");
      window.ipc.removeAllListeners("got-should-show-runs");
    };
  }, [showOnlyCybershuttle]);

  useInterval(() => {
    getContainers();
  }, CONTAINER_FETCH_INTERVAL);

  return (
    <>
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

      <Modal isOpen={DeleteModal.isOpen} onClose={DeleteModal.onClose}>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Remove Container</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Text>Are you sure you want to remove this container?</Text>
            <Text mt={2} fontWeight='bold'>{deleteContainer.Names && deleteContainer?.Names[0]?.slice(1)}</Text>

            <Button
              mt={4}
              colorScheme='red'
              onClick={() => {
                handleRemoveContainer(deleteContainer.Id);
                DeleteModal.onClose();
              }}
              isLoading={isLoadingDelete}
            >
              Remove
            </Button>

            <Button
              mt={4}
              ml={4}
              onClick={DeleteModal.onClose}
            >
              Cancel
            </Button>
          </ModalBody>
        </ModalContent>
      </Modal>

      <Box>
        <Flex alignItems='center' gap={2} justifyContent='flex-end' mb={4}>
          <Switch colorScheme='blue' size='md'
            isChecked={showOnlyCybershuttle}
            onChange={() => setShowOnlyCybershuttle(!showOnlyCybershuttle)}
          />
          <Text>Show only Cybershuttle Containers</Text>
        </Flex>
      </Box>

      <TableContainer>
        <Table variant='simple'>
          <Thead>
            <Tr>
              <Th>Name</Th>
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
                    <Td>
                      <Text
                        _hover={{
                          background: "blue.50",
                          cursor: "pointer"
                        }} onClick={() => {
                          setActiveContainer({
                            Id: container.Id,
                            name: theName
                          });
                          InspectModal.onOpen();
                        }}
                        rounded='md'
                        color='blue.500'
                      >
                        {theName}
                      </Text>


                      <Flex align='center'
                        color='gray.600'
                        gap={1}
                        mt={1}
                      >
                        <Icon as={MdContentCopy}

                          _hover={{
                            cursor: "pointer"
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
                          }} />
                        <Text
                          fontSize='xs'

                        >{container.Id.slice(0, DOCKER_ID_LENGTH)}</Text>
                      </Flex>

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
                          shouldShowMapping[container.Id] && (
                            <Button
                              mt={2}
                              onClick={() => {
                                window.ipc.send("show-window-from-id", container.Id);
                              }}
                              colorScheme='blue'
                              size='xs'
                            >
                              Show
                            </Button>
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
                          onClick={() => {
                            setDeleteContainer(container);
                            DeleteModal.onOpen();
                          }}
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

      {
        runningContainers.length === 0 && (
          <Box mx='auto' textAlign='center' color='gray.500' mt={4}>
            <Text>No {
              showOnlyCybershuttle ? "application" : ""
            } containers are currently running.
            </Text>

            <Text mt={2} fontSize='sm'>
              <Text as={Link} bg='blue.100' px={2} pb={1} rounded='md' color='blue.800' onClick={() => setTabIndex(0)}>Launch a local app</Text> to start using Cybershuttle!
            </Text>
          </Box >
        )
      }
    </>
  );
};