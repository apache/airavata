/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/
import React, { Component } from "react";
import { Alert, Spinner, Text } from "@chakra-ui/react";

const HOSTNAME = "https://api2.cybershuttle.org/proxy";
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

  doneWithGettingData = (port) => {
    this.setState({ rendering: true });
    console.log("the associated id is", this.associatedId);
    window.jn.showWindow(`${HOSTNAME}/${port}/lab?token=1234`, this.associatedId);

    console.log("trying to show the window...");
    this.setState({ msg: "JupyterLab is ready to use in a new window. Please enter 1234 as the password box, if prompted." });


  };

  tryAndLaunchServer = async (port) => {
    console.log("polling the jupyter server...");


    try {
      console.log("in the first try");
      const resp = await fetch(`${HOSTNAME}/${port}/lab?token=1234`);
      if (resp.ok) {
        this.doneWithGettingData(port);
      } else {
        throw new Error("Error fetching the application status");
      }

    } catch (e) {
      console.log("in the first catch", e);
      this.interval2 = setInterval(async () => {
        try {
          console.log("in the second try");
          const resp = await fetch(`${HOSTNAME}/${port}/lab?token=1234`);

          if (resp.ok) {
            clearInterval(this.interval2);
            this.doneWithGettingData(port);
          }
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