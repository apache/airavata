import { ProjectType } from "@/interfaces/ProjectType";
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import {
  useDialog,
  Dialog,
  Button,
  Portal,
  Fieldset,
  Input,
  CloseButton,
} from "@chakra-ui/react";
import { useState } from "react";
import { Toaster, toaster } from "../ui/toaster";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router";

export const StartSessionFromProjectButton = ({
  project,
}: {
  project: ProjectType;
}) => {
  const dialog = useDialog();
  const defaultName =
    new Date().toLocaleDateString() + " " + project.name + " Session";
  const [sessionName, setSessionName] = useState<string>(defaultName);
  const [loadingOpenProject, setLoadingOpenProject] = useState(false);
  const auth = useAuth();
  const shouldRedirect = auth.isLoading || !auth.user || !auth.isAuthenticated;
  const navigate = useNavigate();

  const handleClickStart = () => {
    if (shouldRedirect) {
      navigate(`/login?redirect=${window.location.pathname}`, {
        replace: true,
      });
      return;
    }
    dialog.setOpen(true);
  };

  const handleOpenProject = async () => {
    console.log(auth.user);
    if (!sessionName) {
      toaster.create({
        title: "Session name is required",
        type: "error",
      });
      return;
    }

    setLoadingOpenProject(true);
    try {
      const resp = await api.get(
        `${CONTROLLER.hub}/start/project/${project.id}`,
        {
          params: {
            sessionName: sessionName,
          },
        }
      );
      const data = resp.data;
      window.open(data.redirectUrl, "_blank");
      dialog.setOpen(false);
      toaster.create({
        title: "Session started",
        type: "success",
      });
    } catch (error) {
      console.error("Error fetching project:", error);
      toaster.create({
        title: "Error starting session",
        description:
          "This is likely because you just made an account and haven't been enabled yet. Please let us know so we can enable your account.",
        type: "error",
      });
    }
    setLoadingOpenProject(false);
  };

  return (
    <>
      <Toaster />

      <Dialog.RootProvider
        value={dialog}
        size="sm"
        lazyMount={true}
        unmountOnExit={true}
        onExitComplete={() => {
          setSessionName(defaultName); // Reset session name when modal closes
        }}
      >
        <Button colorPalette="black" size="sm" onClick={handleClickStart}>
          Start Project Session
        </Button>

        <Portal>
          <Dialog.Backdrop />
          <Dialog.Positioner>
            <Dialog.Content>
              <Dialog.Body p="4">
                <Dialog.Title>New Session</Dialog.Title>
                <Dialog.Description mt="2">
                  Project: <b>{project.name}</b>
                </Dialog.Description>

                <Fieldset.Root size="lg" mt={2}>
                  <Fieldset.Legend>Session Name</Fieldset.Legend>
                  <Input
                    placeholder="My new session"
                    mt={2}
                    value={sessionName}
                    onChange={(e) => setSessionName(e.target.value)}
                  />
                  <Fieldset.HelperText mt="2">
                    All session and session names are private and cannot be seen
                    by anyone else.
                  </Fieldset.HelperText>
                </Fieldset.Root>

                <Button
                  colorPalette="black"
                  width="100%"
                  type="submit"
                  mt={4}
                  onClick={handleOpenProject}
                  disabled={!sessionName || loadingOpenProject}
                  loading={loadingOpenProject}
                >
                  Create session
                </Button>
              </Dialog.Body>
              <Dialog.CloseTrigger asChild>
                <CloseButton _hover={{ bg: "gray.100" }} size="sm" />
              </Dialog.CloseTrigger>
            </Dialog.Content>
          </Dialog.Positioner>
        </Portal>
      </Dialog.RootProvider>
    </>
  );
};
