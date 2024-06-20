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

export const VNCViewer = ({ headers, accessToken, applicationId, reqHost, reqPort, experimentId }) => {
  console.log("Application ID", applicationId);
  console.log("Experiment ID", experimentId);
  console.log("Request Host", reqHost);
  console.log("Request Port", reqPort);

  // Can't import regularly because of SSR (next.js)

  const toast = useToast();
  const username = "";
  const password = '1234';
  const [hostname, setHostname] = useState('ws://PoopyPro.local');
  const [port, setPort] = useState('6080');
  const [rendering, setRendering] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [serverPort, setServerPort] = useState("loading");

  const handleOnDisconnect = (rfb) => {
    setError("Something went wrong, please try again.");
    setRendering(false);
  };

  useEffect(() => {
    setLoading(true);
    let interval;
    if (!reqPort) {
      // create the interval
      interval = setInterval(async () => {
        const resp = await fetch("http://74.235.88.134:9001/api/v1/application/launch", {
          method: "POST",
          headers: headers,
          body: JSON.stringify({
            expId: experimentId,
            application: "VMD"
          })
        });

        if (!resp.ok) {
          console.log("Error fetching the application status");
          clearInterval(interval);

          setError("Error fetching the application status");
          setLoading(false);

          return;

        }

        const data = await resp.json();

        if (data.status === "PENDING") {
          console.log("Waiting for the application to launch...");
        } else if (data.status === "COMPLETED") {
          let port = data.allocatedPorts[0];
          console.log("Port is", port);

          setServerPort(port);

          // start the proxy
          window.vnc.startProxy(experimentId, reqHost, port);
          clearInterval(interval);
        }

      }, 2000);
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
      await fetch(`http://74.235.88.134:9001/api/v1/application/terminate/${applicationId}`, {
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
                We're attempting to start the server and proxy. This will take longer if this is your first time using the VNC client, or if your wifi connection is slower. Please wait...
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

      <Tooltip label="VNC Server URL. This is where the application creates a proxy to."><Text textAlign='center' mt={2}>{reqHost + ":" + serverPort}</Text></Tooltip>

      {/* ApplicationID */}
      <Tooltip label="Application ID. This is the ID of the application that is running."><Text textAlign='center' mt={2}>{applicationId}</Text></Tooltip>

    </React.Fragment>
  );
};