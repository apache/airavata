import { Box, Button, Divider, Flex, Link, Spacer, Stack, Text, Tooltip, useToast } from '@chakra-ui/react';
import { useEffect } from 'react';

export const Footer = () => {

  const toast = useToast();
  useEffect(() => {
    window.vnc.killedAllWebsockify((event, error) => {
      console.log(error);
      toast({
        title: 'All websockify services have been stopped',
        description: "",
        status: 'success',
        duration: 3000,
        isClosable: true,
      });
    });
  }, []);
  return (
    <>
      <Divider />
      <Flex px={2} py={1} bg='gray.100' align='center'>
        <Text textAlign='center'>Developed by the Apache Airavata Team</Text>

        <Spacer />

        <Stack direction='row'>
          <Link color='blue.400' href='/tabs-view'>List Experiments</Link>

          <Text>•</Text>

          <Link color='blue.400' href='/create-namd-experiment'>Create NAMD Experiment</Link>
          {/* 
          <Text>•</Text>

          <Link color='blue.400' href='/vnc-client'>VNC Client</Link> */}
          <Text>•</Text>

          <Tooltip label="This will end all running VMD sessions. Please use cautiously."><Button colorScheme='red' size='xs' onClick={
            () => window.vnc.killAllWebsockify()
          }>stop websockify</Button></Tooltip>

        </Stack>
      </Flex >
    </>
  );
};