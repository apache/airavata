import { MetadataType } from "@/interfaces/MetadataType";
import {
  Image,
  HStack,
  Box,
  Text,
  Heading,
  Avatar,
  Separator,
  Badge,
} from "@chakra-ui/react";

export const Metadata = ({ metadata }: { metadata: MetadataType }) => {
  return (
    <>
      <HStack
        alignItems={"flex-start"}
        mb={4}
        gap={8}
        justifyContent="space-between"
      >
        <Box>
          <Heading as="h1" size="4xl" mb={4}>
            {metadata.title}
          </Heading>

          <HStack>
            <Avatar.Root shape="full" size="xl">
              <Avatar.Fallback name={metadata.author.name} />
              <Avatar.Image src={metadata.author.avatar} />
            </Avatar.Root>

            <Box>
              <Text fontWeight="bold">{metadata.author.name}</Text>
              <Text color="gray.500">{metadata.author.role}</Text>
            </Box>
          </HStack>
        </Box>

        <Image
          src={metadata.images.headerImage}
          alt="Notebook Header"
          rounded="md"
          maxW="300px"
        />
      </HStack>

      <Separator my={6} />
      <Box>
        <Heading fontWeight="bold" size="2xl">
          About
        </Heading>

        <HStack my={2}>
          {metadata.tags.map((tag) => (
            <Badge key={tag} size="lg" rounded="md">
              {tag}
            </Badge>
          ))}
        </HStack>

        <Text>{metadata.description}</Text>
      </Box>
    </>
  );
};
