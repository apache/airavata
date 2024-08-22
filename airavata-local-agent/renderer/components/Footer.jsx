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
*****************************************************************/import { Button, Divider, Flex, Link, Spacer, Stack, Text, Tooltip, useToast } from '@chakra-ui/react';
import { useState, useEffect } from 'react';

export const Footer = ({ currentPage, showWarning }) => {

  const [accessToCreateExperiment, setAccessToCreateExperiment] = useState(false);

  useEffect(() => {
    async function getData() {
      const resp = await fetch("https://md.cybershuttle.org/api/applications/?format=json", {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      if (!resp.ok) {
        setAccessToCreateExperiment(false);
      }

      const data = await resp.json();
      if (!data || !Array.isArray(data)) {
        setAccessToCreateExperiment(false);
      } else {
        data.forEach((obj) => {
          if (obj.appModuleName === "NAMD") {
            console.log("Access to create experiment");
            setAccessToCreateExperiment(true);
            return;
          }
        });
      }
    }

    getData();

    if (!accessToCreateExperiment) {
      // poll every 5 seconds
      const interval = setInterval(() => {
        getData();
      }, 5000);
      return () => clearInterval(interval);
    }


  }, [accessToCreateExperiment]);

  return (
    <>
      <Divider />
      <Flex px={2} pb={2} bg='gray.100' align='center'>

        <Spacer />

        <Stack direction='row'>

          <Tooltip label={
            !accessToCreateExperiment && "You do not have access to create NAMD experiments"
          }>
            <Button colorScheme='blue' size='xs' isDisabled={('create-namd-experiment' === currentPage) || !accessToCreateExperiment} onClick={() => {
              if (showWarning) {
                confirm("Your VMD and Jupyter Notebook tabs will close if you go to the create experiment page. Are you sure?") && (window.location.href = '/create-namd-experiment');
              } else {
                window.location.href = '/create-namd-experiment';
              }
            }}>
              Create NAMD Experiment
            </Button>
          </Tooltip>
        </Stack>
      </Flex >
    </>
  );
};