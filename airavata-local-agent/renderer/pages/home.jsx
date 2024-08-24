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
import { Button, Container, Img, Text, Flex, Heading, Link, HStack, UnorderedList, ListItem, Alert, AlertIcon, useInterval } from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { useEffect, useState } from "react";
import { isNewerVersion } from "../lib/utilityFuncs";
const Home = () => {
  const features = [
    'Connects local machines, lab servers, and workstations with remote HPC resources and cloud services.',
    'Automates the installation and execution of scientific software.',
    'Facilitates collaboration among researchers across different locations.',
    'Supports graphical interfaces, Jupyter Notebooks, and low-code Python annotations.',
    'Manages data transfer and synchronization between local and remote environments.',
  ];

  const [version, setVersion] = useState('');
  const [serverVersion, setServerVersion] = useState('');
  const [showUpdate, setShowUpdate] = useState(false);
  const [dockerRunning, setDockerRunning] = useState(false);

  useInterval(() => {
    window.ipc.send('docker-ping');
  }, 5000);

  useEffect(() => {
    window.config.getVersionNumber();

    window.ipc.send('docker-ping');

    window.ipc.on('docker-pinged', (data) => {
      if (!data) {
        setDockerRunning(false);
      } else {
        setDockerRunning(true);
      }
    });

    window.config.versionNumber(async (event, version) => {
      // make fetch here to check for updates
      const resp = await fetch("https://api.cybershuttle.org/local-agent/version");
      let respVersion = await resp.text();

      // respVersion starts with v, so we remove it
      if (respVersion.startsWith('v')) {
        respVersion = respVersion.substring(1);
      }

      setShowUpdate(isNewerVersion(respVersion, version));
      setVersion(version);
      setServerVersion(respVersion);
    });

    return () => {
      window.ipc.removeAllListeners('docker-pinged');
    };
  }, []);
  return (
    <>
      <HeaderBox />

      <Container maxW='container.sm' p={4} mt={4}>
        <Flex alignItems='center' gap={2}>
          <Img src='/images/cs-logo.png' maxH='50px' />
          <Text color='blue.600' fontWeight='bold' fontSize='3xl'>Cybershuttle Local Agent</Text>
        </Flex>

        {
          showUpdate && (
            <Alert status='info' rounded='md' mt={4}>
              <AlertIcon />
              <Text>
                A new version is available. You're currently using version {version}. Please update to the latest version {serverVersion} by downloading the latest release from <Link href='' target="_blank" color='blue.500' fontWeight='bold'>here</Link>.
              </Text>
            </Alert>
          )
        }

        {
          dockerRunning ? (
            <Alert status='success' rounded='md' mt={4}>
              <AlertIcon />
              <Text>
                Docker is running properly. You can now login.
              </Text>
            </Alert>) : (
            <Alert status='error' rounded='md' mt={4}>
              <AlertIcon />
              <Text>
                Docker is not running. Please make sure Docker is installed, launched, and running properly. If you don't have Docker installed, <Link href='https://www.docker.com/products/docker-desktop' target="_blank" color='blue.500' fontWeight='bold'>you can download it from here</Link>.
              </Text>
            </Alert>
          )
        }

        <Text mt={2}>Cybershuttle Local Agent, developed by the Cybershuttle project, empowers researchers by providing seamless access to a comprehensive range of computational resources. The agent bridges the gap between local, institutional, and national-scale computing resources, enhancing productivity and collaboration in scientific research. By integrating diverse computing environments into a unified interface, Apache Airavata eliminates traditional barriers, enabling researchers to focus on innovation and discovery.
        </Text>

        <Heading fontSize='2xl' mt={4} color='blue.500'>Features</Heading>
        <UnorderedList mt={2}>
          {
            features.map((feature, index) => {
              return <ListItem key={index}>{feature}</ListItem>;
            })
          }
        </UnorderedList>

        <HStack spacing={4} mt={2}>
          <Button as='a' href='/login' colorScheme='blue'
            isDisabled={!dockerRunning}
          >Login</Button>
        </HStack>
      </Container>
    </>
  );
};

export default Home;