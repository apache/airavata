/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import {Resource} from "@/interfaces/ResourceType.ts";
import {ResourceOptionButton} from "@/components/resources/ResourceOptions.tsx";
import {Box} from "@chakra-ui/react";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {toaster} from "@/components/ui/toaster.tsx";
import {useEffect, useState} from "react";
import {BsStar, BsStarFill} from "react-icons/bs";

export const StarResourceButton = ({
                                     resource,
                                     onSuccess,
                                   }: {
  resource: Resource,
  onSuccess: (resourceId: string) => void,
}) => {
  const [changeStarLoading, setChangeStarLoading] = useState(false);
  const [initialLoad, setinitialLoad] = useState(false);
  const [starred, setStarred] = useState(false);

  useEffect(() => {
    async function getWhetherUserStarred() {
      setinitialLoad(true);
      const resp = await api.get(`${CONTROLLER.resources}/${resource.id}/star`);
      setStarred(resp.data);
      setinitialLoad(false);
    }

    getWhetherUserStarred();
  }, []);

  const handleStarResource = async () => {
    try {
      setChangeStarLoading(true);
      await api.post(`${CONTROLLER.resources}/${resource.id}/star`);
      toaster.create({
        title: starred ? "Unstarred" : "Starred",
        description: resource.name,
        type: "success",
      })
      setStarred(prev => {
        if (prev) {
          onSuccess(resource.id || "INVALID");
        }
        return !prev;
      });
    } catch {
      toaster.create({
        title: "Error liking resource",
        type: "error",
      });
    } finally {
      setChangeStarLoading(false);
    }
  }

  if (initialLoad) {
    return null;
  }

  return (
      <>
        <ResourceOptionButton
            gap={2}
            _hover={{
              cursor: 'pointer',
              bg: 'blue.200',
            }}
            color={'black'}
            onClick={handleStarResource}
            loading={changeStarLoading}
        >
          {
            starred ? <BsStarFill color={'#EFBF04'}/> : <BsStar/>
          }
          <Box>
            {
              starred ? "Unstar" : "Star"
            }
          </Box>
        </ResourceOptionButton>
      </>
  )
}