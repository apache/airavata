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
import { Box, Text, Button, Flex, Spacer, Img } from '@chakra-ui/react';
import { MdPause, MdPlayArrow } from "react-icons/md";
export const NavBar = () => {
    const handleLogout = () => {
        window.auth.ciLogonLogout();

        setTimeout(() => {
            window.location.href = "/login";
        }, 2000);

    };
    return (
        <>
            <Flex bg='gray.100' alignItems='center' px={4} py={4}>
                <Box>
                    <Flex alignItems='center' gap={3}>
                        <Img src="/images/cs-logo.png" maxH='35px' />
                        <Text fontWeight='black' fontSize='2xl' color='blue.600'>Airavata UI</Text>
                    </Flex>
                </Box>

                <Spacer />

                <Button size='sm' colorScheme='red' onClick={handleLogout}>Logout</Button>
            </Flex>
        </>
    );
};