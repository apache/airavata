import {Box, Button, CloseButton, Dialog, Input, Menu, Portal, Text, useDialog} from "@chakra-ui/react";
import {BsThreeDots} from "react-icons/bs";
import {FaTrash} from "react-icons/fa";
import {useAuth} from "react-oidc-context";
import {isProjectOwner} from "@/lib/util.ts";
import {useState} from "react";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {toaster} from "@/components/ui/toaster.tsx";
import {ProjectType} from "@/interfaces/ProjectType.tsx";

export const DeleteProjectButton = ({
                                      project,
                                      onSuccess,
                                    }: {
  project: ProjectType
  onSuccess: () => void;
}) => {
  const dialog = useDialog();
  const [deleteName, setDeleteName] = useState("");
  const [deleteLoading, setDeleteLoading] = useState(false);
  const auth = useAuth();
  const isOwner = isProjectOwner(auth.user?.profile.email || "INVALID", project);
  if (!isOwner || !auth.isAuthenticated) {
    return null;
  }

  const handleDeleteProject = async () => {
    setDeleteLoading(true);
    try {
      await api.delete(`${CONTROLLER.projects}/${project.id}`);
      toaster.create({
        title: "Project deleted",
        description: project.name,
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
                  <Dialog.Title>Delete Project</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                  <Text color="gray.500">
                    This action is irreversible. To confirm, please type:{" "}
                    <b>{project.name}</b>.
                  </Text>

                  <Input
                      mt={2}
                      placeholder="Project name"
                      value={deleteName}
                      onChange={(e) => setDeleteName(e.target.value)}
                  />
                </Dialog.Body>
                <Dialog.Footer>
                  <Button
                      width="100%"
                      colorPalette="red"
                      disabled={deleteName !== project.name || deleteLoading}
                      loading={deleteLoading}
                      onClick={handleDeleteProject}
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