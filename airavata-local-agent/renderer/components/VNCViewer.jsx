import React, { Component } from 'react';
import {
  Box,
  Text,
  Alert,
  AlertIcon,
  Button,
  Spinner
} from '@chakra-ui/react';
import dynamic from 'next/dynamic';

const VNCItem = dynamic(() => {
  return import('../components/VNCItem').then((mod) => mod.VNCItem);
}, { ssr: false });

const ForwardedRefComponent = React.forwardRef((props, ref) => (
  <VNCItem {...props} forwardedRef={ref} />
));


export default class VNCViewer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      rendering: false,
      loading: false,
      error: "",
      serverPort: "loading",
      serverHostname: "loading",
      showDevSettings: false
    };
    this.interval = null;
    this.username = "";
    this.password = '1234';
    this.myRef = React.createRef();
  }

  handleOnDisconnect = (rfb) => {
    this.setState({ rendering: false });
    setTimeout(() => {
      console.log("trying to reconnect...");
      this.setState({ rendering: true });
    }, 2000);
  };

  async fetchServerStatus() {
    const { applicationId, headers } = this.props;

    const resp = await fetch(`http://20.51.202.251:9001/api/v1/application/${applicationId}/connect`, {
      method: "POST",
      headers: headers,
    });

    if (!resp.ok) {
      console.log("Error fetching the application status");
      clearInterval(this.interval);
      this.setState({ error: "Error launching the VNC server", serverPort: "error", loading: false });
      return;
    }

    const data = await resp.json();

    if (data.status === "PENDING") {
      console.log("Waiting for the application to launch...");
    } else if (data.status === "COMPLETED") {
      let serverPortFromData = data.allocatedPorts[0];
      let serverHostnameFromData = data.host;

      this.setState({
        serverPort: serverPortFromData,
        serverHostname: serverHostnameFromData,
        rendering: true,
        loading: false
      });
      clearInterval(this.interval);
    }
  }

  // startHeartBeat() {
  //   this.interval = setInterval(() => {
  //     let val = this.myRef.current;

  //     console.log(this.myRef.current?.rfb);


  //   }, 5000);

  // }

  componentDidMount() {
    this.setState({ loading: true });
    console.log("calling componentDidMount", this.props.reqPort);
    const { reqPort } = this.props;
    if (!reqPort) {
      this.fetchServerStatus();
      this.interval = setInterval(() => this.fetchServerStatus(), 5000);
    }
  }


  componentWillUnmount() {
    console.log("unmounting component...");
    clearInterval(this.interval);
    this.setState({ rendering: false });
  }

  toggleDevSettings = () => {
    this.setState((prevState) => ({ showDevSettings: !prevState.showDevSettings }));
  };

  render() {
    const { applicationId, experimentId } = this.props;
    const { rendering, loading, error, serverPort, showDevSettings } = this.state;

    return (
      <React.Fragment>
        {
          error && (
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
                <Spinner mr={2} />
                <Text>
                  We're currently starting the VNC server, this may take a few minutes. Please wait...
                </Text>
              </Alert>
            </>
          )
        }

        {rendering && (
          <>
            <Box textAlign='center'>
              <ForwardedRefComponent url={"ws://20.51.202.251" + ":" + serverPort} username={this.username} password={this.password} vncRef={this.myRef} handleOnDisconnect={this.handleOnDisconnect} />
            </Box>
          </>
        )}

        <Button onClick={this.toggleDevSettings} mt={4} variant='link'>
          {showDevSettings ? "Hide" : "Show"} Dev Settings
        </Button>

        {
          showDevSettings && (
            <Box mt={4}>
              <Text><Text as='span' fontWeight='bold'>Websocket URL: </Text>{"ws://20.51.202.251" + ":" + serverPort}</Text>
              <Text><Text as='span' fontWeight='bold'>Application ID: </Text>{applicationId}</Text>
              <Text><Text as='span' fontWeight='bold'>Experiment ID: </Text>{experimentId}</Text>
            </Box>
          )
        }
      </React.Fragment>
    );
  }
}



// import React, { useEffect, useState } from 'react';
// import {
//   Box,
//   Text,
//   Alert,
//   AlertIcon,
//   Button,
//   Spinner
// } from '@chakra-ui/react';
// import dynamic from 'next/dynamic';


// const VNCItem = dynamic(() => {
//   return import('../components/VNCItem').then((mod) => mod.VNCItem);
// }, { ssr: false });

// export const VNCViewer = ({ headers, accessToken, applicationId, reqHost, reqPort, experimentId }) => {
//   const username = "";
//   const password = '1234';
//   const [rendering, setRendering] = useState(false);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState("");
//   const [serverPort, setServerPort] = useState("loading");
//   const [serverHostname, setServerHostname] = useState("loading");
//   const [showDevSettings, setShowDevSettings] = useState(false);

//   const handleOnDisconnect = (rfb) => {
//     // setError("The VNC server started, but we could not connect to it. Please try again.");
//     setRendering(false);

//     // try again after 5 seconds delay
//     setTimeout(() => {
//       console.log("trying to reconnect...");
//       setRendering(true);
//     }, 2000);
//   };

//   useEffect(() => {
//     setLoading(true);
//     let interval;

//     if (!reqPort) {
//       // create the interval
//       interval = setInterval(async () => {
//         const resp = await fetch(`http://20.51.202.251:9001/api/v1/application/${applicationId}/connect`, {
//           method: "POST",
//           headers: headers,
//         });

//         if (!resp.ok) {
//           console.log("Error fetching the application status");
//           clearInterval(interval);
//           setError("Error launching the VNC server");
//           setServerPort("error");
//           setLoading(false);
//           return;
//         }

//         const data = await resp.json();

//         if (data.status === "PENDING") {
//           console.log("Waiting for the application to launch...");
//         } else if (data.status === "COMPLETED") {
//           let severPortFromData = data.allocatedPorts[0];
//           let serverHostnameFromData = data.host;

//           setServerPort(severPortFromData);
//           setServerHostname(serverHostnameFromData);
//           setRendering(true);
//           setLoading(false);

//           clearInterval(interval);
//         }

//       }, 5000);
//     }

//     return () => {
//       console.log("unmounting component...");
//       clearInterval(interval);
//       setRendering(false);
//     };

//   }, []);

//   return (
//     <React.Fragment>
//       {
//         error !== "" && (
//           <Alert status='error' rounded='md'>
//             <AlertIcon />
//             <Text>
//               {error}
//             </Text>
//           </Alert>
//         )
//       }

//       {
//         loading && (
//           <>
//             <Alert status='info' rounded='md'>
//               <Spinner mr={2} />
//               <Text>
//                 We're currently starting the VNC server, this may take a few minutes. Please wait...
//               </Text>
//             </Alert>
//           </>
//         )
//       }

//       {rendering && (
//         <>
//           <Box textAlign='center'>
//             <VNCItem url={"ws://20.51.202.251" + ":" + serverPort} username={username} password={password} handleOnDisconnect={handleOnDisconnect} />
//           </Box>
//         </>
//       )
//       }

//       <Button onClick={() => setShowDevSettings(!showDevSettings)} mt={4} variant='link'>{
//         showDevSettings ? "Hide" : "Show"
//       } Dev Settings</Button>

//       {
//         showDevSettings && (
//           <Box mt={4}>
//             <Text><Text as='span' fontWeight='bold'>Websocket URL: </Text>{"ws://20.51.202.251" + ":" + serverPort}</Text>
//             <Text><Text as='span' fontWeight='bold'>Application ID: </Text>{applicationId}</Text>
//             <Text><Text as='span' fontWeight='bold'>Experiment ID: </Text>{experimentId}</Text>
//           </Box>
//         )
//       }
//     </React.Fragment>
//   );
// };