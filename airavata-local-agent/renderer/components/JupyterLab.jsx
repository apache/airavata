
import React, { useState, useEffect } from "react";
import { Alert, Spinner, Text } from "@chakra-ui/react";

export const JupyterLab = ({ headers, applicationId, reqPort, experimentId }) => {
  const [loading, setLoading] = useState(false);
  const [rendering, setRendering] = useState(false);
  const [serverPort, setServerPort] = useState("loading");
  const [msg, setMsg] = useState("");
  const associatedId = `JN_${experimentId}`;
  const [windowClosed, setWindowClosed] = useState(false);

  console.log("the associated id is", associatedId);

  useEffect(() => {
    setLoading(true);

    let interval;
    let interval2;

    const tryAndLaunchServer = async (port) => {
      console.log("polling the jupyter server...");

      console.log("the associated id is", associatedId);
      try {
        console.log("in the first try");
        const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
        setRendering(true);
        console.log("the associated id is", associatedId);
        window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, associatedId);

        console.log("trying to show the window...");
        setMsg("JupyterLab is ready to use in a new window");

      } catch (e) {
        console.log("in the first catch", e);
        interval2 = setInterval(async () => {
          try {
            console.log("in the second try");
            const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
            setRendering(true);
            clearInterval(interval2);
            window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, associatedId);
            console.log("trying to show the window...");
            setMsg("JupyterLab is ready to use in a new window");
          } catch (ex) {
            console.log("in the second catch", ex);

          }
        }, 5000);
      }

    };

    window.ipc.on("window-has-been-closed", (windowAssociatedId) => {
      console.log("firing...");
      if (windowAssociatedId === associatedId) {
        console.log("Matching associated Ids, closing window");
        setWindowClosed(true);
      }


    });

    if (!reqPort) {
      // create the interval
      interval = setInterval(async () => {
        const resp = await fetch(`http://20.51.202.251:9001/api/v1/application/${applicationId}/connect`, {
          method: "POST",
          headers: headers,
        });

        if (!resp.ok) {
          console.log("Error fetching the application status");
          clearInterval(interval);
          setLoading(false);
          return;
        }

        const data = await resp.json();

        if (data.status === "PENDING") {
          console.log("Waiting for the application to launch...");
        } else if (data.status === "COMPLETED") {
          let severPortFromData = data.allocatedPorts[0];

          setServerPort(severPortFromData);

          console.log("Calling tryAndLaunchServer...");
          tryAndLaunchServer(severPortFromData);


          clearInterval(interval);
        }

      }, 5000);
    }



    return () => {
      console.log("unmounting component...");
      clearInterval(interval);
      clearInterval(interval2);
      setRendering(false);
    };
  }, []);

  if (!rendering) {
    return (
      <Alert status='info' rounded='md'>
        <Spinner mr={2} />
        <Text>
          We're currently starting the Jupyter Notebook, this may take a few minutes. Please wait...
        </Text>
      </Alert>
    );
  }

  if (windowClosed) {
    return (
      <Alert status='error' rounded='md'>
        <Text>
          Please close this tab, your jupyter session is no longer active.
        </Text>
      </Alert>
    );
  }

  return (
    <>
      {
        msg && (
          <Alert status='success' rounded='md'>
            <Text>
              {msg}
            </Text>
          </Alert>
        )
      }

      <h1>Note: If you close this tab, your jupyter session will no longer save any changes.</h1>
    </>
  );
};