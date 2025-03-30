import { useParams } from "react-router";
import NavBar from "../NavBar";
// @ts-expect-error This is fine
import { MOCK_MODELS } from "../../data/MOCK_DATA";
import { ModelType } from "@/interfaces/ModelType";
import { useState, useEffect } from "react";
import {
  Badge,
  Button,
  Code,
  Container,
  HStack,
  Heading,
  Text,
} from "@chakra-ui/react";

async function getModel(id: string | undefined) {
  return MOCK_MODELS.find((model: ModelType) => model.appModuleId === id);
}

export const ModelDetails = () => {
  const { id } = useParams();
  const [model, setModel] = useState<ModelType | null>(null);
  const [copyClicked, setCopyClicked] = useState(false);

  useEffect(() => {
    if (!id) return;

    async function getData() {
      const n = await getModel(id);
      setModel(n);
    }
    getData();
  }, [id]);

  if (!model) return null;

  const templateCode = `exp = md.AlphaFold2.initialize(
    name=...,
    input_seq=...,
    max_template_date=...,
    model_preset=...,
    multimers_per_model=...,
)
exp.create_task()
exp.plan().launch()`;

  return (
    <>
      <NavBar />

      <Container maxW="breakpoint-md" p={4}>
        <HStack
          alignItems="center"
          mt={4}
          justifyContent="space-between"
          flexWrap="wrap"
        >
          <Heading size="4xl">{model.appModuleName}</Heading>
          {model.appModuleVersion && (
            <Badge size="md" colorPalette="green">
              {model.appModuleVersion}
            </Badge>
          )}
        </HStack>
        <Text color="fg.muted" mt={2}>
          {model.appModuleDescription}
        </Text>

        <Heading size="2xl" mt={4}>
          Example
          <Button
            size="xs"
            ml={2}
            colorPalette="teal"
            onClick={() => {
              navigator.clipboard.writeText(templateCode);
              setCopyClicked(true);
              setTimeout(() => setCopyClicked(false), 2000);
            }}
          >
            {copyClicked ? "Copied!" : "Copy"}
          </Button>
        </Heading>

        <Code
          mt={2}
          p={2}
          display="block"
          whiteSpace="pre"
          children={templateCode}
        />
      </Container>
    </>
  );
};
