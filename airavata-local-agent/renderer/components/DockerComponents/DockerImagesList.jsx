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
import { TableContainer, Table, Thead, Tr, Th, Tbody, Td } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { bytesToSize } from "../../lib/utilityFuncs";
import { useInterval } from "usehooks-ts";

const stripPrefix = (str, prefix) => {
  if (str.startsWith(prefix)) {
    return str.slice(prefix.length);
  }
  return str;
};

const IMAGES_FETCH_INTERVAL = 5000;

export const DockerImagesList = () => {
  const [images, setImages] = useState([]);

  useEffect(() => {
    window.ipc.send("get-all-images");

    window.ipc.on("got-all-images", (images) => {
      setImages(images);
    });

    return () => {
      window.ipc.removeAllListeners("got-all-images");
    };
  }, []);

  useInterval(() => {
    window.ipc.send("get-all-images");
  }, IMAGES_FETCH_INTERVAL);

  return (
    <TableContainer>
      <Table>
        <Thead>
          <Tr>
            <Th>Name</Th>
            <Th>ID</Th>
            <Th>Size</Th>
          </Tr>
        </Thead>
        <Tbody>
          {
            images?.map((image) => (
              <Tr key={image.Id}>
                <Td
                  _hover={{
                    cursor: "pointer",
                    color: "blue.500",
                  }}
                  onClick={() => {
                    window.ipc.send("inspect-image", image.Id);
                  }}
                >{image.RepoTags[0]}</Td>
                <Td>{stripPrefix(image.Id, "sha256:").slice(0, 12)}</Td>
                <Td>{bytesToSize(image.Size)}</Td>
              </Tr>
            ))
          }
        </Tbody>
      </Table>
    </TableContainer>
  );
};