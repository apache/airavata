import { HStack, Text, Heading, Icon } from "@chakra-ui/react";
import { ReactNode } from "react";

export const PageHeader = ({
  title,
  icon,
  description,
}: {
  title: string;
  icon: ReactNode;
  description: string;
}) => {
  const fontSize = "5xl";
  return (
    <>
      <HStack alignItems="center" gap={4}>
        <Icon fontSize={fontSize}>{icon}</Icon>
        <Heading as="h1" size={fontSize}>
          {title}
        </Heading>
      </HStack>

      <Text color="gray.600" mt={4}>
        {description}
      </Text>
    </>
  );
};
