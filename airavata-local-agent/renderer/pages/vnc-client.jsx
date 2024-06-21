import React, { useEffect, useRef, useState } from 'react';
import Head from 'next/head';
import {
  Heading, FormControl,
  FormLabel,
  Box,
  Input,
  Text,
  Container,
  VStack,
  Button,
  Alert,
  AlertIcon,
  useToast
} from '@chakra-ui/react';
import dynamic from 'next/dynamic';
import { NavContainer } from '../components/NavContainer';
import { useRouter } from "next/router";

const VncClient = () => {
  // Can't import regularly because of SSR (next.js)
  const VncScreen = dynamic(() => {
    return import('react-vnc').then((mod) => mod.VncScreen);
  }, { ssr: false });


  const router = useRouter();
  const ref = useRef();
  const toast = useToast();
  const [password, setPassword] = useState('1234');
  const [hostname, setHostname] = useState('ws://PoopyPro.local');
  const [port, setPort] = useState('6080');
  const [username, setUsername] = useState('');
  const [rendering, setRendering] = useState(false);
  const [timesConnected, setTimesConnected] = useState(0);
  const [loading, setLoading] = useState(false);


  const handleOnCredentialsRequired = (rfb) => {
    console.log('Credentials required');
  };

  const handleOnDisconnect = (rfb) => {
    console.log('Disconnected');
    setRendering(false);

  };

  useEffect(() => {
    setLoading(true);

    window.vnc.startProxy();

    window.vnc.proxyStarted((event, hostname, port) => {
      console.log('Proxy started', hostname, port);
      setLoading(false);
      toast({
        title: 'Proxy started',
        description: "",
        status: 'success',
        duration: 3000,
        isClosable: true,
      });

      setHostname('ws://' + hostname);
      setPort(port);
      setRendering(true);
    });

    window.vnc.proxyStopped((event, restart) => {
      console.log('Proxy stopped');

      if (restart) window.vnc.startProxy();
    });

    const exitingFunction = () => {
      window.vnc.stopProxy(false); // false = don't restart
    };

    router.events.on("routeChangeStart", exitingFunction);

    return () => {
      console.log("unmounting component...");
      router.events.off("routeChangeStart", exitingFunction);
      setRendering(false);
    };

  }, []);

  const handleSubmitInfo = () => {
    setTimesConnected(timesConnected + 1);

    // here we need to get the hostname and port through the novnc_proxy script


    console.log("Connecting to", hostname, port);

    setRendering(true);
  };

  const handleDisconnect = () => {
    setTimesConnected(0);
    setRendering(false); // unmounting the component auto-disconnects
  };

  return (
    <React.Fragment>
      {
        loading && (
          <>
            <Alert status='info' rounded='md'>
              <AlertIcon />
              <Text>
                We're attempting to start the VNC server and proxy. This will take longer if this is your first time using the VNC client, or if your wifi connection is slower. Please wait...
              </Text>
            </Alert>
          </>
        )
      }

      {rendering && (
        <>
          <Box textAlign='center'>
            <VncScreen
              url={"ws://blah:3211"}
              scaleViewport
              background="#000000"
              style={{
                width: '100%',
                height: '75vh',
              }}
              rfbOptions={{
                credentials: {
                  username,
                  password
                }
              }}
              ref={ref}
              onCredentialsRequired={handleOnCredentialsRequired}
              onDisconnect={handleOnDisconnect}
            />

            {/* <Button onClick={handleDisconnect} colorScheme='red' mt={4}>
                Disconnect
              </Button> */}
          </Box>
        </>
      )
      }




    </React.Fragment>
  );
};

export default VncClient;