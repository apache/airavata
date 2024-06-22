import { useState, useEffect } from "react";
import { Alert, Spinner, Text } from "@chakra-ui/react";

export const JupyterLab = ({ headers, accessToken, applicationId, reqHost, reqPort, experimentId }) => {
  const [loading, setLoading] = useState(false);
  const [rendering, setRendering] = useState(false);
  const [serverPort, setServerPort] = useState("loading");
  const [random, setRandom] = useState(Math.random());
  const [msg, setMsg] = useState("");


  useEffect(() => {
    setLoading(true);

    let interval;
    let interval2;

    const tryAndLaunchServer = async (port) => {
      console.log("trying to launch the server...");
      try {
        const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
      } catch (e) {
        interval2 = setInterval(async () => {
          try {
            const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
            setRendering(true);
            clearInterval(interval2);
            // setRandom(random + 1)
            window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`);
            console.log("trying to show the window...");
            setMsg("JupyterLab is ready to use in a new window");
          } catch (ex) {

          }
        }, 2000);
      }

    };

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
          tryAndLaunchServer(severPortFromData);

          console.log("trying to open up the server...");
          clearInterval(interval);
        }

      }, 5000);
    }

    const exitingFunction = async () => {
      console.log("running stop on", experimentId);
      await fetch(`http://20.51.202.251:9001/api/v1/application/${applicationId}/terminate`, {
        method: "POST",
        headers: headers,
      });
    };

    return () => {
      console.log("unmounting component...");
      clearInterval(interval);
      clearInterval(interval2);
      exitingFunction();
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


      {/* <iframe key={random} src={`http://18.217.79.150:${serverPort}/lab?token=1234`} width='100%' height='600px' /> */}
    </>
  );
};