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
import {
  FormControl,
  FormLabel,
  FormErrorMessage,
  Flex,
  RadioGroup,
  Radio,
  FormHelperText,
  Box, Container, Img, Input, Text, Select, Heading, Link, HStack, VStack, Stack,
  Button,
  Textarea,
  Checkbox,
  Spacer,
  useToast,
  Spinner,
  Badge,
} from "@chakra-ui/react";
import { HeaderBox } from "../components/HeaderBox";
import { useEffect, useState } from "react";
import { Footer } from "../components/Footer";
import tus from 'tus-js-client';
import { useRouter } from 'next/router';

const Home = () => {
  const router = useRouter();

  const toast = useToast();
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');

  const [name, setName] = useState("NAMD on " + new Date().toLocaleDateString() + " at " + new Date().toLocaleTimeString());

  const [desc, setDesc] = useState("Enter description here");
  const [descOpen, setDescOpen] = useState(false);

  const [project, setProject] = useState("option1");
  const [executionType, setExecutionType] = useState("CPU");

  const [contPrev, setContPrev] = useState(false);
  const [prevJobId, setPrevJobId] = useState("");

  const [replicate, setReplicate] = useState(false);
  const [numReplicas, setNumReplicas] = useState(0);

  const [allocation, setAllocation] = useState("default");
  const [computeResource, setComputeResource] = useState("expanse_34f71d6b-765d-4bff-be2e-30a74f5c8c32");

  const [nodeCount, setNodeCount] = useState(1);
  const [coreCount, setCoreCount] = useState(10);
  const [timeLimit, setTimeLimit] = useState(2);
  const [physMemory, setPhysMemory] = useState(null);

  const [emailNotif, setEmailNotif] = useState(false);

  const [settingsOpen, setSettingsOpen] = useState(false);
  const [queue, setQueue] = useState("gpu-shared");
  const [accessToken, setAccessToken] = useState("");
  const [mdInstructionsUri, setMdInstructionsUri] = useState("");
  const [proteinPSFUri, setProteinPSFUri] = useState("");
  const [coordinatesUri, setCoordinatesUri] = useState("");
  const [fParamUri, setFParamUri] = useState([]);
  const [constraintsUri, setConstraintsUri] = useState("");
  const [optionalUri, setOptionalUri] = useState([]);
  const [replicasList, setReplicasList] = useState("");

  const [loading, setLoading] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [linkedCoreAndNode, setLinkedCoreAndNode] = useState(true); // TODO: fix this

  // INPUT STATES
  const [projectObjArray, setProjectObjArray] = useState([]);
  const [resourcesObjArray, setResourcesObjArray] = useState([]);
  const [allocationObjArray, setAllocationObjArray] = useState([]);

  const getAllocationObjById = (id) => {
    return allocationObjArray.find(item => item.allocationId === id);
  };

  const getComputeResourcePolicesByComputeResourceId = (id) => {
    let allocationObj = getAllocationObjById(allocation);

    let policyObj = allocationObj.computeResourcePolicies.find(item => item.computeResourceId === id);
    return policyObj;
  };

  const tusAndFetchUpload = (file, setHandler, type = "single") => {
    if (!file || !(type === "single" || type === "multiple")) {
      console.error("Invalid arguments passed to tusAndFetchUpload.");
      return;
    }
    let upload = new tus.Upload(file, {
      endpoint: "https://tus.airavata.org/files/",
      metadata: {
        filename: file.name,
        filetype: file.type || "text/plain",
        "mime-type": file.type || "text/plain"
      },

      onError: function (error) {
        console.log("Failed because: " + error);
      },
      onProgress: function (bytesUploaded, bytesTotal) {
        var percentage = (bytesUploaded / bytesTotal * 100).toFixed(2);
        console.log(bytesUploaded, bytesTotal, percentage + "%");
      },
      onSuccess: function () {
        console.log("Download %s from %s", upload.file.name, upload.url);

        const url = upload.url;

        let form_data = new FormData();
        form_data.append('uploadURL', url);

        fetch("https://md.cybershuttle.org/api/tus-upload-finish", {
          method: "POST",
          body: form_data,
          credentials: "include",
          headers: {
            'Authorization': 'Bearer ' + accessToken
          }
        }).then((resp) => resp.json())
          .then((data) => {
            const uri = data["data-product"]["productUri"];
            console.log("Saving...", uri);

            if (type === "single") {
              setHandler(uri);
            } else if (type === "multiple") {
              setHandler((prev) => {
                // this will also keep the old one
                return [...prev, uri];
              });
            }
          })
          .catch((error) => {
            toast({
              title: "Error",
              description: error.message,
              status: "error",
              duration: 9000,
              isClosable: true,
            });
          });

      }
    });

    upload.start();
  };

  const uploadMultipleFiles = (files, setHandler) => {
    if (!files || files.length === 0) {
      setHandler([]);
      return;
    }

    // clear the previous files in setHandler
    setHandler([]);

    Array.prototype.forEach.call(files, function (file) {
      tusAndFetchUpload(file, setHandler, "multiple");
    });
  };

  const uploadFile = (file, setHandler) => {
    console.log("Uploading...", file);
    if (!file) {
      setHandler("");
      return;
    }

    tusAndFetchUpload(file, setHandler, "single");
  };

  const showToast = (title, description, status = "error") => {
    toast({
      title: title,
      description: description,
      status: status,
      duration: 9000,
      isClosable: true,
    });
  };


  useEffect((e) => {
    try {
      const theAccessToken = localStorage.getItem('accessToken');
      const obj = JSON.parse(atob(theAccessToken.split('.')[1]));

      setUserName(obj.name);
      setEmail(obj.email);
      setAccessToken(theAccessToken);

      async function getProjects() {
        const resp = await fetch("https://md.cybershuttle.org/api/projects/?format=json&limit=100", {
          headers: {
            "Authorization": "Bearer " + theAccessToken
          }
        });

        const data = await resp.json();
        let items = [];

        data.results.forEach((e) => {
          items.push({
            "projectID": e.projectID,
            "projectName": e.name
          });
        });
        setProjectObjArray(items);
        setProject(items[0].projectID);
      }

      async function getGroupResourceProfileList() {
        const resp = await fetch("https://md.cybershuttle.org/api/group-resource-profiles/?format=json", {
          headers: {
            "Authorization": "Bearer " + theAccessToken
          }
        });

        const data = await resp.json();
        let items = [];

        data.forEach((e) => {
          items.push({
            "allocationName": e.groupResourceProfileName,
            "allocationId": e.groupResourceProfileId,
            "computePreferences": e.computePreferences, // is an array
            "computeResourcePolicies": e.computeResourcePolicies
          });
        });

        setAllocationObjArray(items);
        setAllocation(items[0].allocationId);
        // setComputeResource(items[0].computePreferences[0].computeResourceId);
        setComputeResource("expanse_34f71d6b-765d-4bff-be2e-30a74f5c8c32");
      }

      async function getData() {
        await getProjects();

        await getGroupResourceProfileList();
      }

      getData().then(() => {
        setLoading(false);
        setNodeCount(1);
        setTimeLimit(30);
        setExecutionType("GPU");
      }).catch((error) => {
        console.log(error);
        window.location.href = "/login";
      });


    } catch (error) {
      console.log(error);
      window.location.href = "/login";
    }
  }, []);

  const handleSave = async () => {
    let idx0 = {
      "name": "Execution_Type",
      "value": executionType,
      "type": 0,
      "applicationArgument": "-t",
      "standardInput": false,
      "userFriendlyDescription": "CPU or GPU executable to be used. If you chose GPU please make sure GPU partitions are selected at the Resource selection below.",
      "metaData": {
        "editor": {
          "ui-component-id": "radio-button-input-editor",
          "config": {
            "options": [
              {
                "value": "CPU",
                "text": " CPU"
              },
              {
                "value": "GPU",
                "text": "GPU"
              }
            ]
          }
        }
      },
      "inputOrder": 0,
      "isRequired": true,
      "requiredToAddedToCommandLine": true,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "d3fc9f5e-864c-4d57-b9f5-d0aa8366ab17",
      "show": true
    };
    let idx1 = {
      "name": "Continue_from_Previous_Run?",
      "value": contPrev ? "yes" : "no",
      "type": 0,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": null,
      "metaData": {
        "editor": {
          "ui-component-id": "checkbox-input-editor",
          "config": {
            "options": [
              {
                "value": "yes",
                "text": "Yes"
              }
            ]
          }
        }
      },
      "inputOrder": 1,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "cdc7da81-7c5a-46b7-9390-523acc812b03",
      "show": true
    };
    let idx2 = {
      "name": "Previous_JobID",
      "value": prevJobId,
      "type": 0,
      "applicationArgument": "-r",
      "standardInput": false,
      "userFriendlyDescription": "JobID from the previous run from which the restart/reuse data is to be extracted.",
      "metaData": {
        "editor": {
          "dependencies": {
            "show": {
              "Continue_from_Previous_Run?": {
                "comparison": "equals",
                "value": "yes"
              }
            },
            "showOptions": {
              "isRequired": true
            }
          }
        }
      },
      "inputOrder": 2,
      "isRequired": false,
      "requiredToAddedToCommandLine": true,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "d8946d70-de6a-42dd-8516-0fff7934f822",
      "show": false
    };
    let idx3 = {
      "name": "MD-Instructions-Input",
      "value": mdInstructionsUri,
      "type": 3,
      "applicationArgument": "-i",
      "standardInput": false,
      "userFriendlyDescription": "NAMD conf file/QuickMD conf file",
      "metaData": null,
      "inputOrder": 3,
      "isRequired": true,
      "requiredToAddedToCommandLine": true,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "8dcbe73c-050a-4d7b-a3fc-60709f8d5ae1",
      "show": true
    };
    let idx4 = {
      "name": "Coordinates-PDB-File",
      "value": coordinatesUri,
      "type": 3,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "PDB coordinates files needed but could be uploaded using optional upload below together with other needed files",
      "metaData": null,
      "inputOrder": 4,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "ac9275a8-3ebd-4629-a383-3ba7784a6a10",
      "show": true
    };
    let idx5 = {
      "name": "Protein-Structure-File_PSF",
      "value": proteinPSFUri,
      "type": 3,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "Protein structure file (psf) needed but could be uploaded using optional upload below together with other needed files",
      "metaData": null,
      "inputOrder": 5,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "cc9f6cff-5d49-45b4-a549-2955c4205694",
      "show": true
    };
    let idx6 = {
      "name": "FF-Parameter-Files",
      "value": fParamUri.join(","),
      "type": 4,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "Force field parameter and related files (e.g, *.prm and *.str files) needed but could be uploaded using optional upload below together with other needed files",
      "metaData": null,
      "inputOrder": 6,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "a3407d94-a944-43e6-83a4-7f71ac292fe0",
      "show": true
    };
    let idx7 = {
      "name": "Constraints-PDB",
      "value": constraintsUri,
      "type": 3,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "Constraints file in pdb",
      "metaData": null,
      "inputOrder": 7,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "841373ea-65c7-422c-8391-d3efad517034",
      "show": true
    };
    let idx8 = {
      "name": "Optional_Inputs",
      "value": optionalUri.join(","),
      "type": 4,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "Any other optional and all needed inputs to be uploaded, for a modified DCD out please upload your instructions for modification in a file named ModDCD.tcl.",
      "metaData": null,
      "inputOrder": 8,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "b55a91ae-120e-4c2a-8d6b-66af6d646773",
      "show": true
    };
    let idx9 = {
      "name": "Replicate?",
      "value": replicate ? "yes" : "no",
      "type": 0,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "Optionally Specify if Replicated runs needed. Make sure the resources requested are commensurate, such as as many nodes as replicas.",
      "metaData": {
        "editor": {
          "ui-component-id": "checkbox-input-editor",
          "config": {
            "options": [
              {
                "value": "yes",
                "text": "Yes"
              }
            ]
          }
        }
      },
      "inputOrder": 9,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "4b97eb71-9905-481c-9a8a-9cbc8593a04f",
      "show": true
    };
    let idx10 = {
      "name": "Number of Replicas",
      "value": numReplicas === 0 ? "" : numReplicas,
      "type": 1,
      "applicationArgument": "-n",
      "standardInput": false,
      "userFriendlyDescription": "Specify the number of replicas. Make sure the resources requested are commensurate, such as as many nodes as replicas.",
      "metaData": {
        "editor": {
          "dependencies": {
            "show": {
              "Replicate?": {
                "comparison": "equals",
                "value": "yes"
              }
            },
            "showOptions": {
              "isRequired": true
            }
          }
        }
      },
      "inputOrder": 10,
      "isRequired": false,
      "requiredToAddedToCommandLine": true,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "b23a0853-7379-41af-b78f-d6dd0af5b8cf",
      "show": false
    };
    let idx11 = {
      "name": "Restart_Replicas_List",
      "value": replicasList,
      "type": 0,
      "applicationArgument": "-l",
      "standardInput": false,
      "userFriendlyDescription": "Optionally specify a comma delimited list of replicas to restart (incomplete runs from a previous job), Make sure the resources requested are commensurate, such as as many nodes as replicas.",
      "metaData": {
        "editor": {
          "dependencies": {
            "show": {
              "Continue_from_Previous_Run?": {
                "comparison": "equals",
                "value": "yes"
              }
            },
            "showOptions": {
              "isRequired": false
            }
          }
        }
      },
      "inputOrder": 11,
      "isRequired": false,
      "requiredToAddedToCommandLine": true,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": false,
      "overrideFilename": null,
      "_key": "48dc33a0-3a7f-410a-b1b4-350f4e762566",
      "show": false
    };
    let idx12 = {
      "name": "GPU Resource Warning",
      "value": null,
      "type": 0,
      "applicationArgument": null,
      "standardInput": false,
      "userFriendlyDescription": "The GPU Execution_Type  selected above  requires GPU partition selection. Otherwise it may lead to failed run.",
      "metaData": {
        "editor": {
          "dependencies": {
            "show": {
              "Execution_Type": {
                "comparison": "equals",
                "value": "GPU"
              }
            },
            "showOptions": {
              "isRequired": false
            }
          }
        }
      },
      "inputOrder": 12,
      "isRequired": false,
      "requiredToAddedToCommandLine": false,
      "dataStaged": false,
      "storageResourceId": null,
      "isReadOnly": true,
      "overrideFilename": null,
      "_key": "a9b3c3df-2d1c-4e1e-98d0-c3dc9305304d",
      "show": false
    };

    const payload = {
      creationTime: null,
      description: null,
      emailAddresses: null,
      enableEmailNotification: emailNotif,
      errors: null,
      executionId: "NAMD_dd041e87-1dde-4e57-8ec4-23af2ffa1ba0",
      experimentId: null,
      experimentInputs: [
        idx0, idx1, idx2, idx3, idx4, idx5, idx6, idx7, idx8, idx9, idx10, idx11, idx12
      ],
      experimentName: name,
      experimentOutputs: [
        {
          "name": "Coordinate_Files",
          "value": "*.coor",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "5ae7bfae-da14-49f1-910d-e182bc5be625"
        },
        {
          "name": "NAMD-Standard-Error",
          "value": null,
          "type": 6,
          "applicationArgument": null,
          "isRequired": true,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": {
            "file-metadata": {
              "mime-type": "text/plain"
            }
          },
          "intermediateOutput": null,
          "_key": "904618c5-0b0d-4d7b-9034-88e673248e1f"
        },
        {
          "name": "NAMD-Standard-Out",
          "value": null,
          "type": 5,
          "applicationArgument": null,
          "isRequired": true,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": {
            "file-metadata": {
              "mime-type": "text/plain"
            }
          },
          "intermediateOutput": null,
          "_key": "0117b5f8-3dda-4479-ac03-b73bd58b63f3"
        },
        {
          "name": "Output_Index_files",
          "value": "*.idx",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "b6c1e7bb-25a2-4b77-b5ca-6ff46bbfde5d"
        },
        {
          "name": "Output_PDB_Files",
          "value": "*.pdb",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "fc62735e-f3db-466a-aa04-04e8beb725f6"
        },
        {
          "name": "Output_PSF_Files",
          "value": "*.psf",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "a8866cff-5267-4aea-b077-5fb6cc47a01e"
        },
        {
          "name": "Replica_Outputs",
          "value": "*.out",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "50203f5c-9d01-4ccc-9d58-eed8356aa89e"
        },
        {
          "name": "Restart_Files",
          "value": "*.restart.*",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "6ef4347c-534e-4369-8b03-cc192a8b3e37"
        },
        {
          "name": "Trajectory-Files",
          "value": "*.dcd",
          "type": 4,
          "applicationArgument": null,
          "isRequired": false,
          "requiredToAddedToCommandLine": false,
          "dataMovement": false,
          "location": null,
          "searchQuery": null,
          "outputStreaming": false,
          "storageResourceId": null,
          "metaData": null,
          "intermediateOutput": null,
          "_key": "ba62f8a0-0b3a-42ad-bc94-a4cbe5ad03d7"
        }
      ],
      experimentStatus: null,
      experimentType: 0,
      gatewayId: null,
      processes: null,
      projectId: project,
      userHasWriteAccess: true,
      userName: null,
      workflow: null,
      userConfigurationData: {
        airavataAutoSchedule: false,
        autoScheduledCompResourceSchedulingList: null,
        experimentDataDir: null,
        generateCert: false,
        groupResourceProfileId: "f47130f7-33a8-4856-8ac9-19967724c1b8",
        overrideManualScheduledParams: false,
        shareExperimentPublicly: false,
        storageId: null,
        throttleResources: false,
        useUserCRPref: false,
        userDN: null,
        computationalResourceScheduling: {
          chessisNumber: null,
          nodeCount: nodeCount,
          numberOfThreads: null,
          overrideAllocationProjectNumber: null,
          overrideLoginUserName: null,
          overrideScratchLocation: null,
          queueName: queue,
          resourceHostId: computeResource,
          staticWorkingDir: null,
          totalCPUCount: coreCount,
          totalPhysicalMemory: physMemory,
          wallTimeLimit: timeLimit
        }
      }
    };

    const resp = await fetch("https://md.cybershuttle.org/api/experiments/", {
      method: "POST",
      body: JSON.stringify(payload),
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + accessToken
      }
    });

    if (resp.ok) {
      const data = await resp.json();
      return data.experimentId;
    } else {
      return null;
    }

  };

  const handleSaveAndLaunch = async () => {
    const experimentId = await handleSave();

    if (experimentId !== null) {
      // launch the experiment
      const resp1 = await fetch(`https://md.cybershuttle.org/api/experiments/${experimentId}/launch/`, {
        method: "POST",
        boody: {},
        headers: {
          "Authorization": "Bearer " + accessToken
        }
      });

      if (resp1.ok) {
        toast({
          title: 'Experiment created and launched.',
          description: "Redirecting you soon...",
          status: 'success',
          duration: 9000,
          isClosable: true,
        });

        setLoading(false);

        setTimeout(() => {
          window.location.href = '/tabs-view';
        }, 3000);
      }
    }
  };

  const shouldBeDisabled = () => {
    return executionType === "" ||
      (contPrev === true && prevJobId === "") ||
      mdInstructionsUri === "" ||
      (replicate === true && (numReplicas === 0 || numReplicas === ""));
  };

  return (
    <>
      <HeaderBox name={userName} email={email} />
      <Footer currentPage="create-namd-experiment" />

      <Container maxW='container.md' p={4} mt={4}>

        <Button mb={4} colorScheme="blue" variant='link' onClick={() => {
          confirm("You have not yet submitted your experiment, are you sure you want to go back?") && router.push('/tabs-view');
        }}>Back to Experiments</Button>

        <Stack direction='column' spacing={4}>

          <Box>
            <Badge>NAMD</Badge>
          </Box>

          <Heading mt={-4} fontSize='3xl'>Create a New Experiment</Heading>

          <FormControl>
            <FormLabel>Experiment Name</FormLabel>
            <Input type='text' value={name} onChange={(e) => setName(e.target.value)} />
          </FormControl>

          <Box>
            <Button variant="link"
              onClick={
                () => {
                  setDescOpen(!descOpen);
                }
              }>{
                descOpen ? "Hide Description" : "Add Description"
              }
            </Button>
          </Box>

          {
            descOpen && (

              <FormControl>
                <FormLabel>Description</FormLabel>
                <Textarea type='text' value={desc} onChange={(e) => setDesc(e.target.value)} />
              </FormControl>
            )
          }

          {/* Project input */}
          <FormControl>
            <FormLabel>Project</FormLabel>
            <Select placeholder='Select option' value={project} onChange={(e) => {
              setProject(e.target.value);
            }}>
              {projectObjArray.map((e) => {
                return (
                  <option key={e.projectID} value={e.projectID}>{e.projectName}</option>
                );
              })}
            </Select>
          </FormControl>

          <Box bg='blue.100' p={2} rounded='md' fontSize='sm'>
            <Text>Some needed input file can be uploaded as optional now! The restart and coordinate files are being downloaded.
            </Text>
          </Box>

          <Heading mt={4} fontSize='3xl'>Application Configuration</Heading>

          <Stack spacing={4} border='1px solid lightgray' p={4} rounded='md'>

            <Heading fontSize='xl'>Application Inputs</Heading>

            <FormControl>
              <RadioGroup onChange={setExecutionType} value={executionType}>
                <Stack direction='column'>
                  <Radio value="CPU">CPU</Radio>

                  <Radio value="GPU">GPU</Radio>
                </Stack>
              </RadioGroup>

              <FormHelperText>CPU or GPU executable to be used. If you chose GPU please make sure GPU partitions are selected at the Resource selection below.
              </FormHelperText>
            </FormControl>



            <FormControl>
              <FormLabel>Continue from Previous Run</FormLabel>
              <Checkbox isChecked={contPrev} onChange={(e) => {
                setContPrev(e.target.checked);
              }}>Yes</Checkbox>
            </FormControl>

            {
              contPrev && (
                <FormControl>
                  <FormLabel>Previous JobID</FormLabel>
                  <Input type='text' value={prevJobId} onChange={(e) => {
                    setPrevJobId(e.target.value);
                  }} />

                  <FormHelperText>JobID from the previous run from which the restart/reuse data is to be extracted.
                  </FormHelperText>
                </FormControl>

              )
            }


            <FormControl>
              <FormLabel>MD-Instructions-Input</FormLabel>
              <Input type='file' placeholder='upload file' onChange={(e) => {
                uploadFile(e.target.files[0], setMdInstructionsUri);
              }} />
              <FormHelperText>NAMD conf file/QuickMD conf file. {mdInstructionsUri && "File uploaded."}</FormHelperText>
            </FormControl>

            <FormControl>
              <FormLabel>Coordinates-PDB-File</FormLabel>
              <Input type='file' placeholder='upload file' onChange={(e) => {
                uploadFile(e.target.files[0], setCoordinatesUri);
              }} />
              <FormHelperText>PDB coordinates files needed but could be uploaded using optional upload below together with other needed files. {coordinatesUri && "File uploaded."}</FormHelperText>
            </FormControl>

            <FormControl>
              <FormLabel>Protein-Structure-File_PSF</FormLabel>
              <Input type='file' placeholder='upload file' onChange={(e) => {
                uploadFile(e.target.files[0], setProteinPSFUri);
              }} />
              <FormHelperText>Protein structure file (psf) needed but could be uploaded using optional upload below together with other needed files. {proteinPSFUri && "File uploaded"}
              </FormHelperText>
            </FormControl>


            <FormControl>
              <FormLabel>FF-Parameter-Files</FormLabel>
              <Input multiple={true} type='file' placeholder='upload file' onChange={(e) => {
                uploadMultipleFiles(e.target.files, setFParamUri);
              }} />
              <FormHelperText>Force field parameter and related files (e.g, *.prm and *.str files) needed but could be uploaded using optional upload below together with other needed files. {fParamUri.join(",") && `${fParamUri.length} files uploaded.`}
              </FormHelperText>
            </FormControl>

            <FormControl>
              <FormLabel>Constraints-PDB</FormLabel>
              <Input type='file' placeholder='upload file' onChange={(e) => {
                uploadFile(e.target.files[0], setConstraintsUri);
              }} />
              <FormHelperText>Constraints file in pdb. {constraintsUri && "File uploaded."}
              </FormHelperText>
            </FormControl>

            <FormControl>
              <FormLabel>Optional_Inputs
              </FormLabel>
              <Input multiple={true} type='file' placeholder='upload file' onChange={(e) => {
                uploadMultipleFiles(e.target.files, setOptionalUri);
              }} />
              <FormHelperText>Any other optional and all needed inputs to be uploaded, for a modified DCD out please upload your instructions for modification in a file named ModDCD.tcl. {optionalUri.join(",") && `${optionalUri.length} files uploaded.`}
              </FormHelperText>
            </FormControl>


            <FormControl>
              <FormLabel>Replicate</FormLabel>
              <Checkbox isChecked={replicate} onChange={(e) => {
                setReplicate(e.target.checked);
              }}>Yes</Checkbox>
              <FormHelperText>Optionally Specify if Replicated runs needed. Make sure the resources requested are commensurate, such as as many nodes as replicas.</FormHelperText>
            </FormControl>

            {
              replicate && (
                <>
                  <FormControl>
                    <FormLabel>Number of replicas</FormLabel>
                    <Input type='number' value={numReplicas} onChange={(e) => setNumReplicas(e.target.value)} />
                    <FormHelperText>Specify the number of replicas. Make sure the resources requested are commensurate, such as as many nodes as replicas.
                    </FormHelperText>
                  </FormControl>
                </>
              )
            }

            {contPrev &&
              <FormControl>
                <FormLabel>Restart replicas list</FormLabel>
                <Input type='text' value={replicasList} onChange={(e) => setReplicasList(e.target.value)} />
                <FormHelperText>Optionally specify a comma delimited list of replicas to restart (incomplete runs from a previous job), Make sure the resources requested are commensurate, such as as many nodes as replicas.
                </FormHelperText>
              </FormControl>}
          </Stack>


          <FormControl>
            <FormLabel>Allocation</FormLabel>

            <Select placeholder='Select an allocation' value={allocation} onChange={(e) => {
              setAllocation(e.target.value);
            }}>
              {
                allocationObjArray.map((item) => {
                  return (
                    <option key={item.allocationId} value={item.allocationId}>{item.allocationName}</option>
                  );
                })
              }
            </Select>
          </FormControl>

          <FormControl>
            <FormLabel>Compute Resource</FormLabel>

            <Select placeholder='Select a compute resource' value={computeResource} onChange={(e) => {
              setComputeResource(e.target.value);
            }}>
              {
                getAllocationObjById(allocation)?.computePreferences.map((item) => {
                  return (
                    <option key={item.computeResourceId} value={item.computeResourceId}>{item.computeResourceId.split("_")[0]}</option>
                  );
                })
              }
            </Select>
          </FormControl>

          <Box p={4} rounded='md' border='1px solid gray' _hover={{
            bg: 'gray.100',
            cursor: 'pointer'
          }} onClick={
            () => {
              setSettingsOpen(!settingsOpen);
            }
          }>
            <Heading fontSize='2xl'>Settings for queue {queue}</Heading>
            <Flex justify='space-between' mt={4}>
              <Box>
                <Heading>{nodeCount}</Heading>
                <Text>Node Count</Text>
              </Box>

              <Box>
                <Heading>{coreCount}</Heading>
                <Text>Core Count</Text>
              </Box>

              <Box>
                <Heading>{timeLimit} minutes</Heading>
                <Text>Time Limit</Text>
              </Box>

              <Box>
                <Heading>{physMemory} MB</Heading>
                <Text>Physical Memory</Text>
              </Box>

            </Flex>
          </Box>

          {
            settingsOpen && (
              <Stack direction='column' spacing={4}>

                <FormControl>
                  <FormLabel>Select a Queue</FormLabel>
                  <Select placeholder='Select a queue' value={queue} onChange={(e) => {
                    setQueue(e.target.value);
                  }}>
                    {
                      getComputeResourcePolicesByComputeResourceId(computeResource)?.allowedBatchQueues?.map((item) => {
                        return (
                          <option key={item} value={item}>{item}</option>
                        );
                      })
                    }
                  </Select>
                </FormControl>
                <FormControl>
                  <FormLabel><Text bg={linkedCoreAndNode ? "blue.100" : ""} px={2} rounded='md' display='inline-block'>Node Count

                    (<Text as='span' color='blue.500' cursor='pointer' onClick={() => {
                      setLinkedCoreAndNode(!linkedCoreAndNode);
                    }
                    }>{linkedCoreAndNode ? "Unlink" : "Link"} with core count</Text>)
                  </Text></FormLabel>
                  <Input type='number' value={nodeCount} onChange={(e) => {
                    if (linkedCoreAndNode) {
                      setCoreCount(128 * e.target.value);
                    }
                    setNodeCount(e.target.value);
                  }} />
                </FormControl>

                <FormControl>
                  <FormLabel><Text bg={linkedCoreAndNode ? "blue.100" : ""} px={2} rounded='md' display='inline-block'>Total Core Count

                    (<Text as='span' color='blue.500' cursor='pointer' onClick={() => {
                      setLinkedCoreAndNode(!linkedCoreAndNode);
                    }
                    }>{linkedCoreAndNode ? "Unlink" : "Link"} with node count</Text>)

                  </Text></FormLabel>
                  <Input type='number' value={coreCount} onChange={(e) => {
                    if (linkedCoreAndNode) {
                      setNodeCount(Math.ceil(e.target.value / 128));
                    }
                    setCoreCount(e.target.value);
                  }} />
                </FormControl>


                <FormControl>
                  <FormLabel>Wall Time Limit</FormLabel>
                  <Input type='number' value={timeLimit} onChange={(e) => setTimeLimit(e.target.value)} />

                </FormControl>


                <FormControl>
                  <FormLabel>Physical Memory</FormLabel>
                  <Input type='number' value={physMemory} onChange={(e) => setPhysMemory(e.target.value)} />
                </FormControl>
              </Stack>
            )
          }

          <FormControl>
            <FormLabel>Email Settings</FormLabel>
            <Checkbox isChecked={emailNotif} onChange={(e) => {
              setEmailNotif(e.target.checked);
            }}>Receive email notification of experiment status</Checkbox>
          </FormControl>

          <Flex alignItems='center'>
            <Box>
              <Button mb={4} colorScheme="blue" variant='link' onClick={() => {
                confirm("You have not yet submitted your experiment, are you sure you want to go back?") && router.push('/tabs-view');
              }}>Back to Experiments</Button></Box>

            <Spacer />
            <HStack>

              <Button
                colorScheme='blue'
                onClick={async () => {
                  setSaveLoading(true);
                  const experimentId = await handleSave();

                  if (experimentId) {
                    showToast("Experiment saved.", "Redirecting you soon...", "success");
                    setTimeout(() => {
                      window.location.href = '/tabs-view';
                    }, 3000);
                  }

                  setSaveLoading(false);
                }}
                isDisabled={shouldBeDisabled()}>
                {
                  saveLoading ? "Saving..." : "Save"
                }
              </Button>

              <Button
                colorScheme='green'
                onClick={async () => {
                  setLoading(true);
                  await handleSaveAndLaunch();
                  setLoading(false);

                }}
                isDisabled={shouldBeDisabled()}>
                {
                  loading ? "Saving and Launching..." : "Save and Launch"
                }
              </Button>
            </HStack>
          </Flex>

        </Stack>
      </Container>
    </>
  );
};

export default Home;;