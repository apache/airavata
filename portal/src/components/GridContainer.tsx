import { SimpleGrid } from "@chakra-ui/react";

export const GridContainer = ({ children }: { children: React.ReactNode }) => {
  return (
    <SimpleGrid
      columns={{ base: 1, md: 2, lg: 3 }}
      gap={4}
      mt={2}
      maxH="50vh"
      overflow="scroll"
      p={4}
      bg="gray.50"
    >
      {children}
    </SimpleGrid>
  );
};
