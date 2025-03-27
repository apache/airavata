import {
  Dialog,
  Button,
  CloseButton,
  Portal,
  useDialog,
  Code,
  Box,
  Icon,
  FileUpload,
  Spacer,
  useFileUpload,
} from "@chakra-ui/react";
import { FaPlus } from "react-icons/fa";
import { ButtonWithIcon } from "./ButtonWithIcon";
import { LuUpload } from "react-icons/lu";

export const AddZipButton = () => {
  const dialog = useDialog();
  // useEffect(() => {
  //   dialog.setOpen(true);
  // }, []);
  const fileUpload = useFileUpload({
    maxFiles: 1,
    accept: ".zip",
  });

  return (
    <>
      <Dialog.RootProvider
        value={dialog}
        size="lg"
        onExitComplete={() => {
          fileUpload.clearFiles(); // Clear files when modal closes
        }}
      >
        <Dialog.Trigger asChild>
          <ButtonWithIcon
            bg="pink.500"
            _hover={{ bg: "pink.600" }}
            icon={FaPlus}
          >
            Zip
          </ButtonWithIcon>
        </Dialog.Trigger>
        <Portal>
          <Dialog.Backdrop />
          <Dialog.Positioner>
            <Dialog.Content>
              <Dialog.Body p="4">
                <Dialog.Title>Add Zip File</Dialog.Title>
                <Dialog.Description mt="2">
                  Upload a ZIP file containing your repository. If the
                  repository contains a{" "}
                  <Code colorPalette="blue">cybershuttle.yml</Code> file, the
                  author/tag/workspace information will be pulled from it. If
                  not, please provide them manually.
                </Dialog.Description>
                <FileUpload.RootProvider
                  alignItems="stretch"
                  mt={4}
                  value={fileUpload}
                >
                  <FileUpload.HiddenInput />
                  {fileUpload.acceptedFiles.length === 0 && (
                    <FileUpload.Dropzone>
                      <Icon size="md" color="fg.muted">
                        <LuUpload />
                      </Icon>
                      <FileUpload.DropzoneContent>
                        <Box>Drag and drop your .zip file here</Box>
                      </FileUpload.DropzoneContent>
                    </FileUpload.Dropzone>
                  )}

                  <FileUpload.ItemGroup>
                    <FileUpload.Context>
                      {({ acceptedFiles }) =>
                        acceptedFiles.map((file) => (
                          <FileUpload.Item key={file.name} file={file}>
                            <FileUpload.ItemPreview />
                            <FileUpload.ItemName />
                            <FileUpload.ItemSizeText />
                            <Spacer />
                            <FileUpload.ItemDeleteTrigger />
                          </FileUpload.Item>
                        ))
                      }
                    </FileUpload.Context>
                  </FileUpload.ItemGroup>
                </FileUpload.RootProvider>

                <Button
                  bg="pink.500"
                  _hover={{ bg: "pink.600" }}
                  width="100%"
                  type="submit"
                  mt={4}
                  disabled={fileUpload.acceptedFiles.length === 0}
                >
                  Add Zip File
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
