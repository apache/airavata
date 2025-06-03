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
  Text,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalBody,
  SimpleGrid,
  Flex,
} from "@chakra-ui/react";
import { JupyterProgram } from "./Programs/JupyterProgram";
import ReactLoading from "react-loading";
import { useState } from "react";

export const AvailablePrograms = ({ isDisabled, loadingText, progress }) => {
  const [showProgress, setShowProgress] = useState(false);

  return (
    <Box p={4}>

      <Modal isOpen={isDisabled}>
        <ModalOverlay />
        <ModalContent>
          <ModalBody>
            <Flex justifyContent='center'>
              <ReactLoading type='bubbles' color="black" />
            </Flex>
            <Text textAlign='center'>Opening Jupyter Notebook</Text>

            <Box mx='auto' textAlign='center' mt={4}>
              <Button
                size='xs'
                onClick={() => {
                  setShowProgress(!showProgress);
                }}>
                {
                  showProgress ? "Hide Progress" : "Show Progress"
                }
              </Button>

              {
                showProgress && (
                  <>
                    <Text>{loadingText}</Text>
                    <Text mt={2}>{progress}</Text>
                  </>
                )
              }
            </Box>

          </ModalBody>
        </ModalContent>
      </Modal>

      <Text>
        Select the program you want to run.
      </Text>

      <SimpleGrid columns={3} spacing={10}
        mt={4}
        pointerEvents={isDisabled ? "none" : "auto"}
        opacity={isDisabled ? 0.4 : 1}
      >
        <JupyterProgram />
      </SimpleGrid>
    </Box>
  );
};