import React, { useEffect, useRef, useState } from 'react';
import {
  Box,
  Text,
  Alert,
  AlertIcon,
  useToast,
  Tooltip,
  Button,
  Code
} from '@chakra-ui/react';
import dynamic from 'next/dynamic';


const VNCItem = dynamic(() => {
  return import('../components/VNCItem').then((mod) => mod.VNCItem);
}, { ssr: false });

export const VNCViewer = ({ headers, accessToken, applicationId, reqHost, reqPort, experimentId }) => {
  // console.log("Application ID", applicationId);
  // console.log("Experiment ID", experimentId);
  // console.log("Request Host", reqHost);
  // console.log("Request Port", reqPort);

  // Can't import regularly because of SSR (next.js)

  const toast = useToast();
  const username = "";
  const password = '1234';
  const [hostname, setHostname] = useState('ws://');
  const [port, setPort] = useState('6080');
  const [rendering, setRendering] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [serverPort, setServerPort] = useState("loading");
  const [showDevSettings, setShowDevSettings] = useState(false);

  const handleOnDisconnect = (rfb) => {
    // setError("The VNC server started, but we could not connect to it. Please try again.");
    setRendering(false);

    // try again after 5 seconds delay
    setTimeout(() => {
      console.log("trying to reconnect...");
      setRendering(true);
    }, 5000);
  };

  useEffect(() => {
    setLoading(true);
    let interval;
    if (!reqPort) {
      // create the interval
      interval = setInterval(async () => {
        const resp = await fetch(`http://74.235.88.134:9001/api/v1/application/${applicationId}/connect`, {
          method: "POST",
          headers: headers,
        });

        if (!resp.ok) {
          console.log("Error fetching the application status");
          clearInterval(interval);
          setError("Error launching the VNC server");
          setServerPort("error");
          setLoading(false);
          return;
        }

        const data = await resp.json();

        if (data.status === "PENDING") {
          console.log("Waiting for the application to launch...");
        } else if (data.status === "COMPLETED") {
          let severPort = data.allocatedPorts[0];

          setServerPort(severPort);

          // start the proxy
          window.vnc.startProxy(experimentId, reqHost, severPort);
          clearInterval(interval);
        }

      }, 5000);
    } else {
      // start the proxy
      window.vnc.startProxy(experimentId, reqHost, reqPort);
    }

    // setTimeout(() => {
    //   console.log("starting proxy for", experimentId, reqHost, reqPort);
    //   window.vnc.startProxy(experimentId, reqHost, reqPort);
    // }, 10000);


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

    const exitingFunction = async () => {
      console.log("running stop on", experimentId);
      window.vnc.stopProxy(false, experimentId); // false = don't restart
      await fetch(`http://74.235.88.134:9001/api/v1/application/${applicationId}/terminate`, {
        method: "POST",
        headers: headers,

      });
    };

    return () => {
      console.log("unmounting component...");
      clearInterval(interval);
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
                We're attempting to start the server and proxy. Please make sure you have <Code>git</Code> installed on your computer This will take longer if this is your first time using the VNC client, or if your wifi connection is slower. Please wait...
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

      <Button onClick={() => setShowDevSettings(!showDevSettings)} mt={4} variant='link'>{
        showDevSettings ? "Hide" : "Show"
      } Dev Settings</Button>

      {
        showDevSettings && (
          <Box mt={4}>

            <Text><Text as='span' fontWeight='bold'>VNC Server URL: </Text>{reqHost + ":" + serverPort}</Text>

            <Text><Text as='span' fontWeight='bold'>Application ID: </Text>{applicationId}</Text>

            <Text><Text as='span' fontWeight='bold'>Experiment ID: </Text>{experimentId}</Text>
          </Box>
        )
      }
    </React.Fragment>
  );
};