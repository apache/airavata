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

import {Box, Button, CloseButton, Dialog, Input, Portal, Text, useDialog} from "@chakra-ui/react";
import {Resource} from "@/interfaces/ResourceType.ts";
import {useAuth} from "react-oidc-context";
import {isResourceOwner} from "@/lib/util.ts";
import {useState} from "react";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {toaster} from "@/components/ui/toaster.tsx";
import {FaTrash} from "react-icons/fa";
import {ResourceOptionButton} from "@/components/resources/ResourceOptions.tsx";

export const DeleteResourceButton = ({
                                       resource,
                                       onSuccess,
                                     }: {
  resource: Resource
  onSuccess: () => void;
}) => {
  const dialog = useDialog();
  const [deleteName, setDeleteName] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);
  const auth = useAuth();
  const isOwner = isResourceOwner(auth.user?.profile.email || "INVALID", resource);
  if (!isOwner || !auth.isAuthenticated) {
    return null;
  }

  const handleDeleteResource = async () => {
    setDeleteLoading(true);
    try {
      await api.delete(`${CONTROLLER.resources}/${resource.id}`);
      toaster.create({
        title: "Resource deleted",
        description: resource.name,
        type: "success",
      })
      onSuccess();
      dialog.setOpen(false);
    } catch {
      toaster.create({
        title: "Error deleting session",
        type: "error",
      });
    } finally {
      setDeleteLoading(false)
    }
  }

  return (
      <>
        <ResourceOptionButton
            gap={2}
            color={'red.600'}
            onClick={() => {
              dialog.setOpen(true)
            }}
            _hover={{
              cursor: 'pointer',
              bg: 'red.200',
            }}
        >
          <FaTrash/>
          <Box>Delete</Box>
        </ResourceOptionButton>


        <Dialog.RootProvider size="sm" value={dialog}>
          <Portal>
            <Dialog.Backdrop/>
            <Dialog.Positioner>
              <Dialog.Content>
                <Dialog.Header>
                  <Dialog.Title>Delete Resource</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                  <Text color="gray.500">
                    This action is irreversible. To confirm, please type:{" "}
                    <b>{resource.name}</b>.
                  </Text>

                  <Input
                      mt={2}
                      placeholder="Resource name"
                      value={deleteName}
                      onChange={(e) => setDeleteName(e.target.value)}
                  />
                </Dialog.Body>
                <Dialog.Footer>
                  <Button
                      width="100%"
                      colorPalette="red"
                      disabled={deleteName !== resource.name || deleteLoading}
                      loading={deleteLoading}
                      onClick={handleDeleteResource}
                  >
                    Delete
                  </Button>
                </Dialog.Footer>
                <Dialog.CloseTrigger asChild>
                  <CloseButton size="sm"/>
                </Dialog.CloseTrigger>
              </Dialog.Content>
            </Dialog.Positioner>
          </Portal>
        </Dialog.RootProvider>

      </>
  )
}