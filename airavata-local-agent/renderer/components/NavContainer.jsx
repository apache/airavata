import React from 'react';


import { Center, Text, Grid, GridItem, Box, Avatar, Flex, Button, useToast, Badge } from '@chakra-ui/react';
import { BsPersonFill, BsCalendar2Week } from "react-icons/bs";
import { LinkIconBox } from '../components/LinkIconBox';
import { MdDashboard } from "react-icons/md";
import { NavBar } from './NavBar';
import { LuScreenShare } from "react-icons/lu";

export function NavContainer({ activePage, children })
{

    activePage = activePage.toLowerCase();

    return (
        <Box p='relative'>
            <Center h='30px' bg='gray.300' textAlign='center' fontSize='sm'>
                {activePage} - Airavata UI
            </Center >

            <Box bg='gray.100'>
                <NavBar />
                <Grid templateColumns='repeat(12, 1fr)'>
                    <GridItem colSpan={3} bg='gray.100' h='100%'>


                        <Box textAlign='center' mt={4}>
                            <Avatar size='xl' src='/images/temp.png' name='Jacob' />
                            <Text mt={4} fontWeight='bold'>User Name</Text>
                            <Text color='gray.600' mt={1}>Email</Text>
                        </Box>


                        <Flex alignItems='center' flexDirection='row' justifyContent='center' mt={4}>
                            <LinkIconBox icon={LuScreenShare} text='VNC Client' horizontal='left' vertical='top' active={activePage === 'vnc client' ? 1 : 0} />
                            <LinkIconBox icon={BsCalendar2Week} text='Temp' type='right' horizontal='right' vertical='bottom' active={activePage === 'temp' ? 1 : 0} />
                        </Flex>

                        {/* 
                        <Flex alignItems='center' flexDirection='row' justifyContent='center'>
                            <LinkIconBox href='/profile' icon={BsPersonFill} text='Profile' horizontal='left' vertical='bottom' active={activePage === 'profile' ? 1 : 0} />
                            <LinkIconBox icon={BsCalendar2Week} text='Temp' type='right' horizontal='right' vertical='bottom' active={activePage === 'temp' ? 1 : 0} />
                        </Flex> */}

                    </GridItem>

                    <GridItem colSpan={9} p={4} borderRadius='lg' bg='white' minH='100vh'>
                        {children}
                        {/* <Heading color='gray.600'>Welcome, Jacob.</Heading>
                    <MyCalendar mt={8} />

                    <Img src="" minH='500px' id='screenshot-image' alt='screenshot' /> */}

                    </GridItem>
                </Grid>
            </Box>
        </Box>
    );
}