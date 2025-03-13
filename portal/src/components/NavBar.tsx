import { Flex, Input, Spacer, Image, Button, HStack } from "@chakra-ui/react";
import ApacheAiravataLogo from "../assets/airavata-logo.png";
import { Link } from "react-router";
import { LuSearch } from "react-icons/lu";
import { InputGroup } from "./ui/input-group";

const NavBar = () => {
  return (
    <Flex as="nav" align="center" p={4} boxShadow="sm">
      {/* Logo */}
      <Image src={ApacheAiravataLogo} alt="Logo" boxSize="30px" />

      {/* Navigation Links */}
      <Flex ml={4} gap={6}>
        {["Notebooks", "Applications", "Repositories"].map((item) => (
          <Link key={item} to={`/${item.toLowerCase()}`} color="gray.700">
            {item}
          </Link>
        ))}
      </Flex>

      <Spacer />

      {/* Search Bar */}
      <HStack>
        <InputGroup flex="1" endElement={<LuSearch />}>
          <Input placeholder="Search" />
        </InputGroup>

        {/* Help and Login Buttons */}
        <Flex ml={4} align="center" gap={4}>
          <Link color="blue.500" to="/help">
            Help
          </Link>
          <Button size="sm" variant="outline" colorScheme="orange">
            Login
          </Button>
        </Flex>
      </HStack>
    </Flex>
  );
};

export default NavBar;
