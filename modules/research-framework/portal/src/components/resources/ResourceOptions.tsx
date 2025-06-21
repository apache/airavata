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
import {Button, ButtonProps, Menu, VStack} from "@chakra-ui/react";
import {BsThreeDots} from "react-icons/bs";
import {DeleteResourceButton} from "@/components/resources/DeleteResourceButton.tsx";
import {StarResourceButton} from "@/components/resources/StarResourceButton.tsx";
import {useAuth} from "react-oidc-context";

export const ResourceOptions = ({resource, deleteable = true, onDeleteSuccess, onUnStarSuccess}:
                                {
                                  resource: Resource,
                                  deleteable: boolean,
                                  onDeleteSuccess: () => void,
                                  onUnStarSuccess: (resourceId: string) => void
                                }) => {

  const auth = useAuth();
  if (!auth.isAuthenticated) {
    return null;
  }

  return (
      <>
        <Menu.Root>
          <Menu.Trigger _hover={{
            cursor: 'pointer',
          }}>
            <BsThreeDots/>
          </Menu.Trigger>
          <Menu.Positioner>
            <Menu.Content>
              <VStack gap={2} alignItems={'start'}>
                <StarResourceButton resource={resource} onSuccess={onUnStarSuccess}/>
                {deleteable && <DeleteResourceButton resource={resource} onSuccess={onDeleteSuccess}/>}
              </VStack>
            </Menu.Content>
          </Menu.Positioner>
        </Menu.Root>
      </>
  )
}


type ResourceOptionButtonProps = {
  onClick: () => void;
  children?: React.ReactNode;
} & ButtonProps;

export const ResourceOptionButton = ({
                                       onClick,
                                       children,
                                       ...rest
                                     }: ResourceOptionButtonProps) => {
  return (
      <Button
          w="full"
          transition="all .2s"
          rounded="md"
          gap={2}
          onClick={onClick}
          p={0}
          display={'flex'}
          justifyContent={'flex-start'}
          bg={'transparent'}
          {...rest}
      >
        {children}
      </Button>
  );
};
