import {
  Text,
  Flex,
  Spacer,
  Image,
  HStack,
  Avatar,
  Box,
  IconButton,
  useDisclosure,
  Collapsible,
  Button,
  Stack,
  ButtonProps,
} from "@chakra-ui/react";
import ApacheAiravataLogo from "../assets/airavata-logo.png";
import { Link, useNavigate } from "react-router";
import { useAuth } from "react-oidc-context";
import { RxHamburgerMenu } from "react-icons/rx";
import { IoClose } from "react-icons/io5";

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

interface NavLinkProps extends ButtonProps {
  title: string;
  url: string;
}

const NavBar = () => {
  const auth = useAuth();
  const { open, onToggle } = useDisclosure();
  const navigate = useNavigate();

  const NavLink = ({ title, url, ...props }: NavLinkProps) => (
    <Button
      variant="plain"
      px={2}
      _hover={{ bg: "gray.200" }}
      onClick={() => {
        navigate(url);
        onToggle();
      }}
      {...props}
    >
      <Text color="gray.700" fontSize="md" textAlign="left">
        {title}
      </Text>
    </Button>
  );

  return (
    <Box position="sticky" top="0" zIndex="1000" bg="white" boxShadow="sm">
      <Flex align="center" p={4}>
        {/* Hamburger Menu (Mobile Only) */}
        <IconButton
          aria-label="Toggle Navigation"
          display={{ base: "inline-flex", md: "none" }}
          onClick={onToggle}
          variant="ghost"
          mr={2}
        >
          {open ? <IoClose size={24} /> : <RxHamburgerMenu size={24} />}
        </IconButton>

        {/* Logo */}
        <Link to="/projects">
          <Image src={ApacheAiravataLogo} alt="Logo" boxSize="30px" />
        </Link>

        {/* Desktop Nav Links */}
        <HStack ml={4} display={{ base: "none", md: "flex" }}>
          {NAV_CONTENT.map((item) => (
            <NavLink key={item.title} title={item.title} url={item.url} />
          ))}
        </HStack>

        <Spacer />

        {/* User Profile */}
        <HStack gap={3}>
          <Avatar.Root variant="subtle">
            <Avatar.Fallback name={auth.user?.profile.name} />
          </Avatar.Root>
          <Box textAlign="left">
            <Text fontSize="sm">{auth.user?.profile.name}</Text>
            <Text fontSize="xs" color="gray.500">
              {auth.user?.profile.email}
            </Text>
          </Box>
        </HStack>
      </Flex>

      {/* Mobile Nav Links (Collapse) */}
      <Collapsible.Root open={open}>
        <Collapsible.Content>
          <Stack
            direction="column"
            bg="white"
            px={4}
            pb={4}
            spaceY={2}
            display={{ md: "none" }}
          >
            {NAV_CONTENT.map((item) => (
              <Box key={item.title} w="100%">
                <NavLink
                  key={item.title}
                  title={item.title}
                  url={item.url}
                  width="100%"
                />
              </Box>
            ))}
          </Stack>
        </Collapsible.Content>
      </Collapsible.Root>
    </Box>
  );
};

export default NavBar;
