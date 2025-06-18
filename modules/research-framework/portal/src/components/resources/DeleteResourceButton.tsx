import {Box, Button, CloseButton, Dialog, Input, Menu, Portal, Text, useDialog} from "@chakra-ui/react";
import {BsThreeDots} from "react-icons/bs";
import {FaTrash} from "react-icons/fa";
import {Resource} from "@/interfaces/ResourceType.ts";
import {useAuth} from "react-oidc-context";
import {isResourceOwner} from "@/lib/util.ts";
import {useState} from "react";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {toaster} from "@/components/ui/toaster.tsx";

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
        <Menu.Root>
          <Menu.Trigger _hover={{
            cursor: 'pointer',
          }}>
            <BsThreeDots/>
          </Menu.Trigger>
          <Menu.Positioner>
            <Menu.Content>
              <Menu.Item value={"delete"}
                         color="fg.error"
                         _hover={{bg: "bg.error", color: "fg.error", cursor: "pointer"}}
                         onClick={() => dialog.setOpen(true)}
              >
                <FaTrash/>
                <Box flex="1">Delete</Box>

              </Menu.Item>

            </Menu.Content>
          </Menu.Positioner>
        </Menu.Root>

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