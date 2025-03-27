import { Badge, Box, Card, HStack, Heading } from "@chakra-ui/react";
import { ModelType } from "@/interfaces/ModelType";
import { Link } from "react-router";
export const ModelCard = ({ model }: { model: ModelType }) => {
  return (
    <Box>
      <Card.Root
        size="sm"
        _hover={{
          bg: "gray.100",
          cursor: "pointer",
        }}
      >
        <Link to={`/models/${model.appModuleId}`}>
          <Card.Header>
            <HStack
              justifyContent="space-between"
              alignItems="flex-start"
              flexWrap="wrap"
            >
              <Heading size="md"> {model.appModuleName}</Heading>
              {model.appModuleVersion && (
                <Badge size="md" colorPalette="green">
                  {model.appModuleVersion}
                </Badge>
              )}
            </HStack>
          </Card.Header>
          <Card.Body color="fg.muted">{model.appModuleDescription}</Card.Body>
        </Link>
      </Card.Root>
    </Box>
  );
};
