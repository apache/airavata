import React, { Component } from "react";
import { Alert, Spinner, Text } from "@chakra-ui/react";

export default class JupyterLab extends Component {
  constructor(props) {
    super(props);
    this.state = {
      rendering: false,
      msg: "",
      windowClosed: false,
      serverPort: null
    };
    this.associatedId = `JN_${this.props.experimentId}`;
    this.interval = null;
    this.interval2 = null;
  }

  shouldComponentUpdate(nextProps, nextState) {
    console.log("state: ", this.state, nextState);
    console.log("props: ", this.props, nextProps);

    return this.state.rendering !== nextState.rendering || this.state.windowClosed !== nextState.windowClosed || this.state.msg !== nextState.msg;

    // return this.state.rendering !== nextState.rendering || this.state.windowClosed !== nextState.windowClosed || this.state.msg !== nextState.msg;
    // return false;
  }

  componentDidMount() {
    const { reqPort, applicationId, headers } = this.props;

    window.ipc.on("window-has-been-closed", (windowAssociatedId) => {
      console.log("firing...");
      if (windowAssociatedId === this.associatedId) {
        console.log("Matching associated Ids, closing window");
        this.setState({ windowClosed: true });
      }
    });

    if (!reqPort) {
      this.interval = setInterval(async () => {
        const resp = await fetch(`https://api.cybershuttle.org/api/v1/application/${applicationId}/connect`, {
          method: "POST",
          headers: headers,
        });

        if (!resp.ok) {
          console.log("Error fetching the application status");
          clearInterval(this.interval);
          return;
        }

        const data = await resp.json();

        if (data.status === "PENDING") {
          console.log("Waiting for the application to launch...");
        } else if (data.status === "COMPLETED") {
          let serverPortFromData = data.allocatedPorts[0];
          this.setState({ serverPort: serverPortFromData }, () => {
            this.tryAndLaunchServer(serverPortFromData);
          });
          clearInterval(this.interval);
        }
      }, 5000);
    }
  }

  componentWillUnmount() {
    console.log("unmounting component...");
    clearInterval(this.interval);
    clearInterval(this.interval2);
    this.setState({ rendering: false });
  }

  tryAndLaunchServer = async (port) => {
    console.log("polling the jupyter server...");

    console.log("the associated id is", this.associatedId);
    try {
      console.log("in the first try");
      const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
      this.setState({ rendering: true });
      console.log("the associated id is", this.associatedId);
      window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, this.associatedId);

      console.log("trying to show the window...");
      this.setState({ msg: "JupyterLab is ready to use in a new window" });

    } catch (e) {
      console.log("in the first catch", e);
      this.interval2 = setInterval(async () => {
        try {
          console.log("in the second try");
          const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
          this.setState({ rendering: true });
          clearInterval(this.interval2);
          window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, this.associatedId);
          console.log("trying to show the window...");
          this.setState({ msg: "JupyterLab is ready to use in a new window" });
        } catch (ex) {
          console.log("in the second catch", ex);
        }
      }, 5000);
    }
  };

  render() {
    const { rendering, msg, windowClosed } = this.state;

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
  }
}


// import React, { useState, useEffect } from "react";
// import { Alert, Spinner, Text } from "@chakra-ui/react";

// const JupyterLab = ({ headers, applicationId, reqPort, experimentId }) => {
//   const [rendering, setRendering] = useState(false);
//   const [msg, setMsg] = useState("");
//   const associatedId = `JN_${experimentId}`;
//   const [windowClosed, setWindowClosed] = useState(false);

//   console.log("the associated id is", associatedId);

//   useEffect(() => {
//     let interval;
//     let interval2;

//     const tryAndLaunchServer = async (port) => {
//       console.log("polling the jupyter server...");

//       console.log("the associated id is", associatedId);
//       try {
//         console.log("in the first try");
//         const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
//         setRendering(true);
//         console.log("the associated id is", associatedId);
//         window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, associatedId);

//         console.log("trying to show the window...");
//         setMsg("JupyterLab is ready to use in a new window");

//       } catch (e) {
//         console.log("in the first catch", e);
//         interval2 = setInterval(async () => {
//           try {
//             console.log("in the second try");
//             const resp = await fetch(`http://18.217.79.150:${port}/lab?token=1234`);
//             setRendering(true);
//             clearInterval(interval2);
//             window.jn.showWindow(`http://18.217.79.150:${port}/lab?token=1234`, associatedId);
//             console.log("trying to show the window...");
//             setMsg("JupyterLab is ready to use in a new window");
//           } catch (ex) {
//             console.log("in the second catch", ex);

//           }
//         }, 5000);
//       }

//     };

//     window.ipc.on("window-has-been-closed", (windowAssociatedId) => {
//       console.log("firing...");
//       if (windowAssociatedId === associatedId) {
//         console.log("Matching associated Ids, closing window");
//         setWindowClosed(true);
//       }


//     });

//     if (!reqPort) {
//       // create the interval
//       interval = setInterval(async () => {
//         const resp = await fetch(`https://api.cybershuttle.org/api/v1/application/${applicationId}/connect`, {
//           method: "POST",
//           headers: headers,
//         });

//         if (!resp.ok) {
//           console.log("Error fetching the application status");
//           clearInterval(interval);
//           return;
//         }

//         const data = await resp.json();

//         if (data.status === "PENDING") {
//           console.log("Waiting for the application to launch...");
//         } else if (data.status === "COMPLETED") {
//           let severPortFromData = data.allocatedPorts[0];

//           setServerPort(severPortFromData);

//           console.log("Calling tryAndLaunchServer...");
//           tryAndLaunchServer(severPortFromData);


//           clearInterval(interval);
//         }

//       }, 5000);
//     }



//     return () => {
//       console.log("unmounting component...");
//       clearInterval(interval);
//       clearInterval(interval2);
//       setRendering(false);
//     };
//   }, []);

//   if (!rendering) {
//     return (
//       <Alert status='info' rounded='md'>
//         <Spinner mr={2} />
//         <Text>
//           We're currently starting the Jupyter Notebook, this may take a few minutes. Please wait...
//         </Text>
//       </Alert>
//     );
//   }

//   if (windowClosed) {
//     return (
//       <Alert status='error' rounded='md'>
//         <Text>
//           Please close this tab, your jupyter session is no longer active.
//         </Text>
//       </Alert>
//     );
//   }

//   return (
//     <>
//       {
//         msg && (
//           <Alert status='success' rounded='md'>
//             <Text>
//               {msg}
//             </Text>
//           </Alert>
//         )
//       }

//       <h1>Note: If you close this tab, your jupyter session will no longer save any changes.</h1>
//     </>
//   );
// };

// export default React.memo(JupyterLab);