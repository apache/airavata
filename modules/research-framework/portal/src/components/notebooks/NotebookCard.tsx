import {
  Box,
  Image,
  Icon,
  Text,
  Card,
  HStack,
  Avatar,
  Button,
} from "@chakra-ui/react";
import { FaPlay } from "react-icons/fa";
import { Link } from "react-router";

export const NotebookCard = ({ notebook }: { notebook: any }) => {
  return (
    <Box>
      <Card.Root overflow="hidden">
        <Image
          src={notebook.images.headerImage}
          alt="Green double couch with wooden legs"
        />
        <Link to={notebook.slug}>
          <Card.Body
            gap="2"
            _hover={{
              bg: "gray.100",
            }}
          >
            <Card.Title>{notebook.title}</Card.Title>
            <HStack>
              {notebook.tags.map((tag: string) => (
                <Text key={tag} bg="gray.200" p={1} rounded="md">
                  {tag}
                </Text>
              ))}
            </HStack>
            <Card.Description>{notebook.description}.</Card.Description>
          </Card.Body>
        </Link>

        <Card.Footer justifyContent="space-between">
          <HStack mt={4}>
            <Avatar.Root shape="full" size="xl">
              <Avatar.Fallback name={notebook.author.name} />
              <Avatar.Image src={notebook.author.avatar} />
            </Avatar.Root>

            <Box>
              <Text fontWeight="bold">{notebook.author.name}</Text>
              <Text color="gray.500">{notebook.author.role}</Text>
            </Box>
          </HStack>

          <Button>
            Run
            <Icon ml={1} size={"sm"}>
              <FaPlay />
            </Icon>
          </Button>
        </Card.Footer>
      </Card.Root>
    </Box>
  );
};
