import {
  Dialog,
  Button,
  Field,
  Input,
  CloseButton,
  Portal,
  useDialog,
  Code,
  VStack,
  SimpleGrid,
  Textarea,
} from "@chakra-ui/react";
import { FaPlus } from "react-icons/fa";
import { ButtonWithIcon } from "./ButtonWithIcon";
import React from "react";

export const AddRepositoryButton = () => {
  const dialog = useDialog();
  // useEffect(() => {
  //   dialog.setOpen(true);
  // }, []);
  return (
    <>
      <Dialog.RootProvider value={dialog} size="lg">
        <Dialog.Trigger asChild>
          <ButtonWithIcon
            bg="cyan.600"
            _hover={{ bg: "cyan.700" }}
            icon={FaPlus}
          >
            Repository
          </ButtonWithIcon>
        </Dialog.Trigger>
        <Portal>
          <Dialog.Backdrop />
          <Dialog.Positioner>
            <Dialog.Content>
              <Dialog.Body p="4">
                <Dialog.Title>Add Git Repository</Dialog.Title>
                <Dialog.Description mt="2">
                  Start by providing a GitHub URL. If the repository contains a
                  <Code colorPalette="blue">cybershuttle.yml</Code> file, the
                  author/tag/workspace information will be pulled from it. If
                  not, please provide them manually.
                </Dialog.Description>

                <VStack gap={4} mt="4">
                  <Field.Root>
                    <Field.Label>URL</Field.Label>
                    <Input
                      type="url"
                      flexGrow={1}
                      placeholder="https://github.com/example/repo.git"
                    />
                  </Field.Root>
                  <Field.Root>
                    <Field.Label>Authors</Field.Label>
                    <Input
                      flexGrow={1}
                      placeholder="John Doe <johndoe@test.edu>; Jane Doe <janedoe@test.edu>"
                    />
                  </Field.Root>

                  <Field.Root>
                    <Field.Label>Tags</Field.Label>
                    <Input
                      flexGrow={1}
                      placeholder="bmtk-workshop; neuroscience; notebooks"
                    />
                  </Field.Root>
                  <SimpleGrid columns={5} gap={4}>
                    <Field.Root>
                      <Field.Label>Dir</Field.Label>
                      <Input flexGrow={1} placeholder="/workspace" />
                    </Field.Root>
                    <Field.Root>
                      <Field.Label>Nodes</Field.Label>
                      <Input flexGrow={1} placeholder="1" />
                    </Field.Root>

                    <Field.Root>
                      <Field.Label>Min CPU</Field.Label>
                      <Input flexGrow={1} placeholder="1" />
                    </Field.Root>
                    <Field.Root>
                      <Field.Label>Min GPU</Field.Label>
                      <Input flexGrow={1} placeholder="1" />
                    </Field.Root>
                    <Field.Root>
                      <Field.Label>Min RAM</Field.Label>
                      <Input flexGrow={1} placeholder="1" />
                    </Field.Root>
                  </SimpleGrid>

                  <Field.Root>
                    <Field.Label>Data</Field.Label>
                    <Textarea
                      flexGrow={1}
                      rows={4}
                      placeholder="cyber://321aed:/workspace/data/A
allen://8a8b2f:/workspace/data/B
dropbox://4a34d1:/workspace/data/C"
                    />
                  </Field.Root>
                  <Button
                    bg="cyan.600"
                    _hover={{ bg: "cyan.700" }}
                    width="100%"
                    type="submit"
                  >
                    Add Git Repository
                  </Button>
                </VStack>
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
