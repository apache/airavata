import React, { useEffect, useRef, useState } from 'react';
import {
  Box,
  Text,
  Alert,
  AlertIcon,
  useToast,
  Tooltip
} from '@chakra-ui/react';
import dynamic from 'next/dynamic';


const VNCItem = dynamic(() => {
  return import('../components/VNCItem').then((mod) => mod.VNCItem);
}, { ssr: false });

export const VNCViewer = ({ reqHost, reqPort, experimentId }) => {
  // console.log(reqHost, reqPort);
  // Can't import regularly because of SSR (next.js)

  const toast = useToast();
  const username = "";
  const password = '1234';
  const [hostname, setHostname] = useState('ws://PoopyPro.local');
  const [port, setPort] = useState('6080');
  const [rendering, setRendering] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleOnDisconnect = (rfb) => {
    setError("Something went wrong, please try again.");
    setRendering(false);
  };

  useEffect(() => {
    setLoading(true);

    setTimeout(() => {
      console.log("starting proxy for", experimentId, reqHost, reqPort);
      window.vnc.startProxy(experimentId, reqHost, reqPort);
    }, 10000);


    window.vnc.proxyStarted((event, hostname, port, theExperimentId) => {
      console.log('Proxy started for', hostname, port, theExperimentId);
      if (experimentId !== theExperimentId) {
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
      exitingFunction();
      setRendering(false);
    };

  }, []);

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
            <VNCItem url={hostname + ':' + port} username={username} password={password} handleOnDisconnect={handleOnDisconnect} />
          </Box>
        </>
      )
      }

      <Tooltip label="VNC Server URL. This is where the application creates a proxy to."><Text textAlign='center' mt={2}>{reqHost + ":" + reqPort}</Text></Tooltip>

    </React.Fragment>
  );
};