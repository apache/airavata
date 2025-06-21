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
import {GoHeart, GoHeartFill} from "react-icons/go";

export const LikeResourceButton = ({
                                     resource,
                                     onSuccess,
                                   }: {
  resource: Resource,
  onSuccess: (resourceId: string) => void,
}) => {
  const [changeLikeLoading, setChangeLikeLoading] = useState(false);
  const [initialLoad, setinitialLoad] = useState(false);
  const [liked, setLiked] = useState(false);

  useEffect(() => {
    async function getWhetherUserLiked() {
      setinitialLoad(true);
      const resp = await api.get(`${CONTROLLER.likes}/resources/${resource.id}`);
      setLiked(resp.data);
      setinitialLoad(false);
    }

    getWhetherUserLiked();
  }, []);

  const handleLikeResource = async () => {
    try {
      setChangeLikeLoading(true);
      await api.post(`${CONTROLLER.likes}/resources/${resource.id}`);
      toaster.create({
        title: liked ? "Unliked" : "Liked",
        description: resource.name,
        type: "success",
      })
      setLiked(prev => {
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
      setChangeLikeLoading(false);
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
            onClick={handleLikeResource}
            loading={changeLikeLoading}
        >
          {
            liked ? <GoHeartFill color={'red'}/> : <GoHeart/>
          }
          <Box>
            {
              liked ? "Unlike" : "Like"
            }
          </Box>
        </ResourceOptionButton>
      </>
  )
}