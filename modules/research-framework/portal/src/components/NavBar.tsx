import { Text, Flex, Spacer, Image, HStack, Avatar } from "@chakra-ui/react";
import ApacheAiravataLogo from "../assets/airavata-logo.png";
import { Link } from "react-router";

const NAV_CONTENT = [
  {
    title: "Home",
    url: "/",
  },
  {
    title: "Datasets",
    url: "/datasets",
  },
  {
    title: "Models",
    url: "/models",
  },
];

const NavBar = () => {
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
      <Link to="/">
        <Image src={ApacheAiravataLogo} alt="Logo" boxSize="30px" />
      </Link>

      {/* Navigation Links */}
      <Flex ml={4} gap={6}>
        {NAV_CONTENT.map((item) => (
          <Link key={item.title} to={item.url} color="gray.700">
            {item.title}
          </Link>
        ))}
      </Flex>

      <Spacer />

      {/* Search Bar */}
      <HStack _hover={{ cursor: "pointer" }}>
        <Text>John Doe</Text>
        <Avatar.Root variant="subtle">
          <Avatar.Fallback name="John Doe" />
        </Avatar.Root>
      </HStack>
    </Flex>
  );
};
export default NavBar;
