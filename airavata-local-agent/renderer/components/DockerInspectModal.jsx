import { Box, Button, Stack, Text, IconButton } from "@chakra-ui/react";
import { DeleteIcon } from '@chakra-ui/icons';
import { useEffect, useState } from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import { docco } from 'react-syntax-highlighter/dist/cjs/styles/hljs';
import { TextWithBoldKey } from "./TextWithBoldKey";
import { canPerformAction } from "../lib/utilityFuncs";

export const DockerInspectModal = ({ containerId }) => {
  const [inspectContent, setInspectContent] = useState("");
  const [status, setStatus] = useState("");

  useEffect(() => {
    window.ipc.send("inspect-container", containerId);

    window.ipc.on("container-inspected", (data) => {
      setInspectContent(data);
      setStatus(data.State.Status);
    });

    return () => {
      window.ipc.removeAllListeners("container-inspected");
    };
  }, []);

  useEffect(() => {
    let interval = setInterval(() => {
      window.ipc.send("inspect-container", containerId);
    }, 2000);

    return () => {
      clearInterval(interval);
    };
  });

  if (inspectContent === null) {
    return (
      <Text>
        Container with ID <pre>{containerId}</pre> not found. Please close this modal and try again.
      </Text>
    );
  }

  return (
    <Stack spacing={4} direction='column'>

      <Box>
        <TextWithBoldKey keyName="Container Actions" />

        <Stack direction='row' spacing={2} mt={2}>
          {
            (canPerformAction("pause", status)) && (
              <Button
                onClick={() => window.ipc.send("pause-container", containerId)}
                colorScheme="yellow"
                size='sm'
              >
                Pause
              </Button>
            )
          }

          {
            canPerformAction("unpause", status) && (
              <Button
                onClick={() => window.ipc.send("unpause-container", containerId)}
                colorScheme="green"
                size='sm'
              >
                Unpause
              </Button>
            )
          }

          {canPerformAction("start", status) &&
            <Button
              onClick={() => window.ipc.send("start-container", containerId)}
              colorScheme="green"
              size='sm'
            >
              Start
            </Button>
          }

          {(canPerformAction("stop", status)) &&
            <Button
              onClick={() => window.ipc.send("stop-container", containerId)}
              colorScheme="red"
              size='sm'
            >
              Stop
            </Button>
          }

          <IconButton
            onClick={() => window.ipc.send("remove-container", containerId)}
            colorScheme='red'
            variant='outline'
            isDisabled={!canPerformAction("remove", status)}
            size='sm'
            icon={<DeleteIcon />}
          />
        </Stack>
      </Box>

      <TextWithBoldKey keyName="Container ID" text={containerId} />

      <TextWithBoldKey keyName="Status" text={status} />

      <Box>
        <TextWithBoldKey keyName="Inspect" />
        <Box mt={2} overflow="scroll" h='500px' resize='vertical'>
          <SyntaxHighlighter language="javascript" style={docco}>
            {JSON.stringify(inspectContent, null, 2)}
          </SyntaxHighlighter>
        </Box>
      </Box>
    </Stack>
  );
};