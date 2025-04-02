"use client";

import {
    Dialog,
    Button,
    CloseButton,
    Portal,
    useDialog,
    VStack,
    Field,
    Input,
} from "@chakra-ui/react";
import { FaPlus } from "react-icons/fa";
import { ButtonWithIcon } from "./ButtonWithIcon";

export const AddCodeButton = () => {
    const dialog = useDialog();

    const renderInput = (label: string, placeholder: string) => (
        <Field.Root required>
            <Field.Label>
                {label} <Field.RequiredIndicator />
            </Field.Label>
            <Input placeholder={placeholder} />
        </Field.Root>
    );

    return (
        <>
            <Dialog.RootProvider value={dialog} size="lg">
                <Dialog.Trigger asChild>
                    <ButtonWithIcon
                        bg="purple.500"
                        _hover={{ bg: "purple.600" }}
                        icon={FaPlus}
                    >
                        VSCode
                    </ButtonWithIcon>
                </Dialog.Trigger>
                <Portal>
                    <Dialog.Backdrop />
                    <Dialog.Positioner>
                        <Dialog.Content>
                            <Dialog.Body p="4">
                                <Dialog.Title>Add VSCode Instance</Dialog.Title>
                                <Dialog.Description mt="2">
                                    Select configuration options for the new VSCode instance.
                                </Dialog.Description>

                                <VStack gap={4} mt="4">
                                    {renderInput("Model", "e.g. v1 or v2")}
                                    {renderInput("Dataset", "e.g. A, B, or C")}
                                    {renderInput("Nodes", "e.g. 1")}
                                    {renderInput("RAM", "e.g. 8GB")}
                                    {renderInput("Storage", "e.g. 40GB")}

                                    <Button
                                        bg="purple.500"
                                        _hover={{ bg: "purple.600" }}
                                        width="100%"
                                        type="submit"
                                    >
                                        Add VSCode Instance
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
