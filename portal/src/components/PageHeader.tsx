import { Box, HStack, Text, Heading, Icon } from "@chakra-ui/react";
import { ReactNode } from "react";

export const PageHeader = ({
  title,
  icon,
  description,
}: {
  title: string;
  icon?: ReactNode;
  description: string;
}) => {
  const fontSize = "4xl";
  return (
    <Box>
      <HStack alignItems="center" gap={4}>
        {icon && <Icon as={icon} fontSize={fontSize} />}
        <Heading as="h1" size={fontSize}>
          {title}
        </Heading>
      </HStack>

      <Text color="gray.600" mt={2}>
        {description}
      </Text>
    </Box>
  );
};
