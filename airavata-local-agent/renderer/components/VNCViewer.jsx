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

let count = 0;


const VNCItem = dynamic(() => {
  return import('../components/VNCItem').then((mod) => mod.VNCItem);
}, { ssr: false });

export const VNCViewer = ({ reqHost, reqPort, experimentId }) => {
  // console.log(reqHost, reqPort);
  // Can't import regularly because of SSR (next.js)

  const vncRef = useRef(null);
  const toast = useToast();
  const [password, setPassword] = useState('1234');
  const [hostname, setHostname] = useState('ws://PoopyPro.local');
  const [port, setPort] = useState('6080');
  const [username, setUsername] = useState('');
  const [rendering, setRendering] = useState(false);
  const [timesConnected, setTimesConnected] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");


  const handleOnCredentialsRequired = (rfb) => {
    console.log('Credentials required');
  };

  const handleOnDisconnect = (rfb) => {
    console.log(experimentId, 'Disconnected');
    setError("Something went wrong, please try again.");
    setRendering(false);
  };

  count++;
  console.log("component render number: ", count);

  useEffect(() => {
    setLoading(true);

    window.vnc.startProxy(experimentId, reqHost, reqPort);

    window.vnc.proxyStarted((event, hostname, port, theExperimentId) => {
      console.log('Proxy started for', hostname, port, theExperimentId);
      if (experimentId !== theExperimentId) {
        console.log("returning...");
        return;
      }
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
      const { connect, connected, disconnect } = vncRef.current ?? {};

      connect?.();
    });

    window.vnc.proxyStopped((event, restart) => {
      console.log('Proxy stopped');

      if (restart) window.vnc.startProxy(experimentId, reqHost, reqPort);
    });

    const exitingFunction = () => {
      console.log("running stop on", experimentId);
      window.vnc.stopProxy(false, experimentId); // false = don't restart

    };

    return () => {
      console.log("unmounting component...");
      // router.events.off("routeChangeStart", exitingFunction);
      exitingFunction();
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
        error !== "" && (
          <Alert status='error' rounded='md'>
            <AlertIcon />
            <Text>
              {error}
            </Text>
          </Alert>
        )
      }

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
            <VNCItem url={hostname + ':' + port} username={username} password={password} vncRef={vncRef} onDisconnect={handleDisconnect} />

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