import {
  Avatar,
  Box,
  HStack,
  Text,
  Menu,
  Portal,
  Button,
} from "@chakra-ui/react";
import { FaArrowRight } from "react-icons/fa6";
import { useAuth } from "react-oidc-context";

export const UserMenu = () => {
  const auth = useAuth();

  if (auth.isLoading || !auth.user || !auth.isAuthenticated)
    return (
      <Button
        colorPalette="black"
        onClick={() => {
          auth.signinRedirect({
            redirect_uri: `${window.location.origin}`,
            extraQueryParams: {
              prompt: "login",
              kc_idp_hint: "oidc",
            },
          });
        }}
      >
        Login
        <FaArrowRight />
      </Button>
    );
  const handleLogout = async () => {
    // Clear the user provider
    await auth.removeUser();
    await auth.signoutRedirect({
      post_logout_redirect_uri: `https://cilogon.org/logout?return=${window.location.origin}`,
    });
  };

  return (
    <Menu.Root>
      <Menu.Trigger asChild>
        <HStack
          gap={3}
          p={1}
          rounded="md"
          as="button"
          cursor="pointer"
          transition="all .2s"
          _hover={{ bg: "gray.200" }}
        >
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
      </Menu.Trigger>

      <Portal>
        <Menu.Positioner>
          <Menu.Content>
            <Menu.Item
              value="logout"
              onClick={handleLogout}
              _hover={{
                cursor: "pointer",
              }}
            >
              Logout
            </Menu.Item>
          </Menu.Content>
          <Menu.Arrow />
        </Menu.Positioner>
      </Portal>
    </Menu.Root>
  );
};
