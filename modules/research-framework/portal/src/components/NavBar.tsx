import {
  Text,
  Flex,
  Spacer,
  Image,
  HStack,
  Avatar,
  Box,
} from "@chakra-ui/react";
import ApacheAiravataLogo from "../assets/airavata-logo.png";
import { Link } from "react-router";
import { useAuth } from "react-oidc-context";

const NAV_CONTENT = [
  {
    title: "Projects",
    url: "/projects",
  },
  {
    title: "Datasets",
    url: "/resources/datasets",
  },
  {
    title: "Repositories",
    url: "/resources/repositories",
  },
  {
    title: "Notebooks",
    url: "/resources/notebooks",
  },
  {
    title: "Models",
    url: "/resources/models",
  },
];

const NavBar = () => {
  const auth = useAuth();

  console.log(auth);
  return (
    <Flex
      as="nav"
      align="center"
      p={4}
      boxShadow="sm"
      position="sticky"
      top="0"
      zIndex="1000"
      bg="white" // Ensure background is not transparent
    >
      {/* Logo */}
      <Link to="/projects">
        <Image src={ApacheAiravataLogo} alt="Logo" boxSize="30px" />
      </Link>

      {/* Navigation Links */}
      <Flex ml={4} gap={6}>
        {NAV_CONTENT.map((item) => (
          <Link key={item.title} to={item.url} color="gray.700">
            <Text
              _hover={{
                color: "blue.400",
                textDecoration: "underline",
              }}
            >
              {item.title}
            </Text>
          </Link>
        ))}
      </Flex>

      <Spacer />

      {/* Search Bar */}
      <HStack>
        <Avatar.Root variant="subtle">
          <Avatar.Fallback name={auth.user?.profile.name} />
        </Avatar.Root>
        <Box>
          <Text>{auth.user?.profile.name}</Text>
          <Text fontSize="sm" color="gray.500">
            {auth.user?.profile.email}
          </Text>
        </Box>
      </HStack>
    </Flex>
  );
};
export default NavBar;
