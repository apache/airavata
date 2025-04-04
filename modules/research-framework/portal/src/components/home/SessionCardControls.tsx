import { SessionType } from "@/interfaces/SessionType";
import { HStack, Button } from "@chakra-ui/react";
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import { toaster } from "../ui/toaster";
import { useState } from "react";
import { SessionStatusEnum } from "@/interfaces/SessionStatusEnum";

export const SessionCardControls = ({ session }: { session: SessionType }) => {
  const [openLoading, setOpenLoading] = useState(false);
  const [terminateLoading, setTerminateLoading] = useState(false);
  const [hideControls, setHideControls] = useState(false);

  const handleOpenSession = async () => {
    setOpenLoading(true);
    try {
      const resp = await api.get(
        `${CONTROLLER.hub}/resume/session/${session.id}`
      );
      const data = resp.data;
      window.open(data.redirectUrl, "_blank");
      toaster.create({
        title: "Session started",
        description: session.sessionName,
        type: "success",
      });
    } catch {
      toaster.create({
        title: "Error resuming session",
        type: "error",
      });
    }
    setOpenLoading(false);
  };

  const handleTerminateSession = async () => {
    setTerminateLoading(true);
    try {
      // empty request body
      await api.patch(
        `${CONTROLLER.sessions}/${session.id}`,
        {},
        {
          params: {
            status: SessionStatusEnum.TERMINATED,
          },
        }
      );

      toaster.create({
        title: "Session terminated",
        description: session.sessionName,
        type: "success",
      });
      setHideControls(true);
    } catch {
      toaster.create({
        title: "Error terminating session",
        type: "error",
      });
    }
    setTerminateLoading(false);
  };

  return (
    <HStack alignItems="center" mt={2} hidden={hideControls}>
      <Button
        size="sm"
        colorPalette="red"
        variant="subtle"
        onClick={handleTerminateSession}
        loading={terminateLoading}
        disabled={terminateLoading}
      >
        Terminate
      </Button>
      <Button
        size="sm"
        colorPalette="green"
        onClick={handleOpenSession}
        loading={openLoading}
        disabled={openLoading}
      >
        Open Session
      </Button>
    </HStack>
  );
};
