import { PrivacyEnum } from "@/interfaces/PrivacyEnum";
import { CreateResourceRequest } from "@/interfaces/Requests/CreateResourceRequest";
import api from "@/lib/api";
import { CONTROLLER } from "@/lib/controller";
import {
  Button,
  Code,
  Field,
  HStack,
  Input,
  Portal,
  Select,
  Text,
  Textarea,
  VStack,
  createListCollection,
} from "@chakra-ui/react";
import { toaster } from "../ui/toaster";
import { useNavigate } from "react-router";
import { useState } from "react";

const privacyOptions = createListCollection({
  items: Object.keys(PrivacyEnum).map((key) => ({
    label: PrivacyEnum[key as keyof typeof PrivacyEnum],
    value: PrivacyEnum[key as keyof typeof PrivacyEnum],
  })),
});

export const ConfirmRepoDetails = ({
  createResourceRequest,
  setCreateResourceRequest,
  githubUrl,
}: {
  createResourceRequest: CreateResourceRequest;
  setCreateResourceRequest: (data: CreateResourceRequest) => void;
  githubUrl: string;
}) => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onSubmit = async () => {
    try {
      setLoading(true);
      await api.post(
        `${CONTROLLER.resources}/repository?githubUrl=${githubUrl}`,
        createResourceRequest
      );

      toaster.create({
        title: "Success",
        description: "Repository added successfully",
        type: "success",
      });
      navigate("/resources");
    } catch (error) {
      console.error("Error adding repository:", error);
      toaster.create({
        title: "Error",
        description: "Failed to add repository",
        type: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <VStack gap={4}>
        <Text>
          To make any changes, please modify the <Code>cybershuttle.yml</Code>{" "}
          file in your GitHub repository.
        </Text>
        <Field.Root disabled>
          <Field.Label>Repository Name</Field.Label>
          <Input value={createResourceRequest.name} />
        </Field.Root>
        <Field.Root disabled>
          <Field.Label>Repository URL</Field.Label>
          <Input value={githubUrl} />
        </Field.Root>
        <Field.Root disabled>
          <Field.Label>Description</Field.Label>
          <Textarea maxH="5lh" value={createResourceRequest.description} />
        </Field.Root>
        <Field.Root disabled>
          <Field.Label>Tags</Field.Label>
          <HStack flexWrap={"wrap"} gap={2}>
            {createResourceRequest.tags.map((tag) => (
              <Code key={tag} colorScheme="blue">
                {tag}
              </Code>
            ))}
          </HStack>
        </Field.Root>
        <Field.Root disabled>
          <Field.Label>Authors</Field.Label>
          <HStack flexWrap={"wrap"} gap={2}>
            {createResourceRequest.authors.map((author) => (
              <Code key={author} colorScheme="blue">
                {author}
              </Code>
            ))}
          </HStack>
        </Field.Root>
        <Field.Root>
          <Select.Root
            value={[createResourceRequest.privacy]} // ✅ value must be an array
            onValueChange={(value) => {
              console.log(value);
              setCreateResourceRequest({
                ...createResourceRequest,
                privacy: value.value[0] as PrivacyEnum, // ✅ value is a string[]
              });
            }}
            collection={privacyOptions}
            width="full"
            disabled={true}
          >
            <Select.HiddenSelect />
            <Select.Label>Privacy</Select.Label>
            <Select.Control width="full">
              <Select.Trigger width="full">
                <Select.ValueText placeholder="Select privacy" width="full" />
              </Select.Trigger>
              <Select.IndicatorGroup>
                <Select.Indicator />
              </Select.IndicatorGroup>
            </Select.Control>
            <Portal>
              <Select.Positioner>
                <Select.Content width="full">
                  {privacyOptions.items.map((item) => (
                    <Select.Item item={item} key={item.value} width="full">
                      {item.label}
                    </Select.Item>
                  ))}
                </Select.Content>
              </Select.Positioner>
            </Portal>
          </Select.Root>
        </Field.Root>

        <Button w="full" loading={loading} onClick={onSubmit}>
          Add Repository
        </Button>
      </VStack>
    </>
  );
};
