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
import { useState } from "react";
import {
  Tr, Tooltip, Box, Td, Text, Badge, HStack, Button, Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  useDisclosure,
  ModalBody,
  ModalCloseButton,
} from "@chakra-ui/react";
import ExperimentModal from "./ExperimentModal";
import { getColorScheme, getResourceFromId } from '../lib/utilityFuncs';
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
dayjs.extend(relativeTime);


const isValidStatus = (status) => {
  let invalidStatus = ["CREATED"];
  return !invalidStatus.includes(status);
};

const getExperimentApplication = (executionId) => {
  // these are the executionIds that are used in the backend.
  if (!executionId) {
    return ("N/A");
  } else if (executionId.startsWith("AlphaFold2")) {
    return "AlphaFold2";
  } else if (executionId.startsWith("NAMD3_gpu")) {
    return "NAMD3 GPU";
  } else if (executionId.startsWith("NAMD_Diego")) {
    return "NAMD3 Single Node";
  } else if (executionId.startsWith("NAMD3")) {
    return "NAMD3";
  } else if (executionId.startsWith("NAMD")) {
    return "NAMD";
  } else if (executionId.startsWith("VMD")) {
    return "VMD";
  } else if (executionId.startsWith("JupyterLab")) {
    return "Jupyter Lab";
  }
};


export const ExperimentsList = ({ experiments, email, handleAddTab, accessToken, isOpenTab }) => {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [activeExperiment, setActiveExperiment] = useState("");

  return (
    <>
      <Modal isOpen={isOpen} onClose={onClose} size='4xl'>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Experiment Details ({activeExperiment.name})</ModalHeader>
          <ModalCloseButton />
          <ModalBody pb={8}>
            {
              isOpen && (
                <ExperimentModal activeExperiment={activeExperiment} onClose={onClose} onOpen={onOpen} accessToken={accessToken} />
              )
            }
          </ModalBody>
        </ModalContent>
      </Modal>

      {
        experiments?.results?.map((experiment) => {
          return (
            <Tr key={experiment.experimentId} fontSize='sm' alignItems='center'>
              <Td>
                <Box >
                  <Tooltip label={experiment.experimentId}>
                    <Text whiteSpace='pre-wrap'
                      _hover={{
                        cursor: "pointer",
                      }} transition='all .2s' onClick={() => {
                        setActiveExperiment(experiment);
                        onOpen();
                      }}
                    >{experiment.name}
                    </Text>
                  </Tooltip>
                </Box>
              </Td>

              <Td>
                {
                  experiment.userName === email ? <Badge colorScheme='green'>{experiment.userName}</Badge> : <Text>{experiment.userName}</Text>
                }
              </Td>

              <Td>
                <Text>{getExperimentApplication(experiment.executionId)}</Text>
              </Td>

              <Td>
                <Text >{getResourceFromId(experiment.resourceHostId)}</Text>
              </Td>

              <Td>
                <Tooltip label={new Date(experiment.creationTime).toLocaleString()}><Text>{dayjs(experiment.creationTime).fromNow(true)} ago</Text></Tooltip>
              </Td>

              <Td>
                <Badge colorScheme={getColorScheme(experiment.experimentStatus)}>{experiment.experimentStatus}</Badge>
              </Td>

              <Td>
                <HStack>
                  {
                    !experiment.executionId?.startsWith("VMD") && !experiment.executionId?.startsWith("JupyterLab") && isValidStatus(experiment.experimentStatus) &&
                    <Button colorScheme='orange' size='xs' onClick={() => {
                      handleAddTab('JN', experiment.experimentId, experiment.name);
                    }}>
                      Jupyter
                      {
                        isOpenTab('JN', experiment.experimentId) &&
                        <Spinner ml={2} />
                      }
                    </Button>}
                  {
                    // only show jupyter button if executionId starts with "NAMD_*".
                    experiment.executionId?.startsWith('NAMD_') && isValidStatus(experiment.experimentStatus) && (
                      <Button colorScheme='blue' size='xs' onClick={() => {
                        handleAddTab('VMD', experiment.experimentId, experiment.name);
                      }}
                      >
                        VMD
                        {
                          isOpenTab('VMD', experiment.experimentId) &&
                          <Spinner ml={2} />
                        }</Button>
                    )
                  }
                </HStack>
              </Td>
            </Tr>
          );
        })
      }
    </>
  );
};