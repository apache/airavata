import { ProjectType } from "@/interfaces/ProjectType";
import {
  Text,
  Box,
  Card,
  HStack,
  Avatar,
  Button,
  Icon,
  Image,
  Badge,
} from "@chakra-ui/react";
import { FaPlay } from "react-icons/fa";
import { Link } from "react-router";

export const ProjectCard = ({ project }: { project: ProjectType }) => {
  return (
    <Box>
      <Card.Root overflow="hidden">
        <Image
          src={project.metadata.images.headerImage}
          maxH={100}
          alt="Green double couch with wooden legs"
        />
        <Link to={`/${project.metadata.type}/${project.metadata.slug}`}>
          <Card.Body
            gap="2"
            _hover={{
              bg: "gray.100",
            }}
          >
            <Card.Title>{project.metadata.title}</Card.Title>
            <HStack flexWrap={"wrap"}>
              <Badge
                colorPalette={
                  project.metadata.type === "notebook" ? "cyan" : "pink"
                }
                fontWeight="bold"
                size="md"
              >
                {project.metadata.type}
              </Badge>
              {project.metadata.tags.map((tag: string) => (
                <Badge size="md" key={tag}>
                  {tag}
                </Badge>
              ))}
            </HStack>
            <Text color="fg.muted" lineClamp={2}>
              {project.metadata.description}.
            </Text>
          </Card.Body>
        </Link>

        <Card.Footer justifyContent="space-between">
          <HStack mt={4}>
            <Avatar.Root shape="full" size="xl">
              <Avatar.Fallback name={project.metadata.author.name} />
              <Avatar.Image src={project.metadata.author.avatar} />
            </Avatar.Root>

            <Box>
              <Text fontWeight="bold">{project.metadata.author.name}</Text>
              <Text color="gray.500">{project.metadata.author.role}</Text>
            </Box>
          </HStack>

          <Button
            bg={project.metadata.type === "notebook" ? "cyan.500" : "pink.500"}
            _hover={{
              bg:
                project.metadata.type === "notebook" ? "cyan.600" : "pink.600",
            }}
          >
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
