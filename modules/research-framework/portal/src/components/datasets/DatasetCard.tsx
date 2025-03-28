import { DatasetType } from "@/interfaces/DatasetType";
import {
  Text,
  Box,
  Card,
  HStack,
  Avatar,
  Image,
  Badge,
} from "@chakra-ui/react";
import { Link } from "react-router";

export const DatasetCard = ({ dataset }: { dataset: DatasetType }) => {
  return (
    <Box>
      <Card.Root overflow="hidden">
        <Image
          src={dataset.metadata.images.headerImage}
          maxH={100}
          alt="Green double couch with wooden legs"
        />
        <Link to={`/datasets/${dataset.metadata.slug}`}>
          <Card.Body
            gap="2"
            _hover={{
              bg: "gray.100",
            }}
          >
            <HStack justifyContent="space-between" mb={1}>
              <Card.Title>{dataset.metadata.title}</Card.Title>
              <Badge
                colorPalette={dataset.private ? "red" : "green"}
                rounded="md"
              >
                {dataset.private ? "Private" : "Public"}
              </Badge>
            </HStack>
            <HStack flexWrap={"wrap"}>
              <Badge colorPalette={"purple"} fontWeight="bold" size="md">
                {dataset.metadata.type}
              </Badge>
              {dataset.metadata.tags.map((tag: string) => (
                <Badge size="md" key={tag}>
                  {tag}
                </Badge>
              ))}
            </HStack>
            <Text color="fg.muted" lineClamp={2}>
              {dataset.metadata.description}.
            </Text>
          </Card.Body>
        </Link>

        <Card.Footer justifyContent="space-between">
          <HStack mt={4}>
            <Avatar.Root shape="full" size="xl">
              <Avatar.Fallback name={dataset.metadata.author.name} />
              <Avatar.Image src={dataset.metadata.author.avatar} />
            </Avatar.Root>

            <Box>
              <Text fontWeight="bold">{dataset.metadata.author.name}</Text>
              <Text color="gray.500">{dataset.metadata.author.role}</Text>
            </Box>
          </HStack>
        </Card.Footer>
      </Card.Root>
    </Box>
  );
};
