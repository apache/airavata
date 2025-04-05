import { Container, Heading, Image, Text, VStack, Box } from "@chakra-ui/react";
import HeroOriginal from "../../assets/Hero.original.png";
import { SectionHeading } from "../typography/SectionHeading";

export const CybershuttleLanding = () => {
  return (
    <>
      <Container maxW="breakpoint-lg" mt={8}>
        <Heading
          textAlign="center"
          fontSize={{ base: "3xl", md: "4xl", lg: "5xl" }}
          fontWeight="black"
          lineHeight={1.2}
        >
          Balance Local and Remote for
          <br /> Streamlined Research with{" "}
          <Text as="span" color="blue.600">
            Cybershuttle
          </Text>
        </Heading>

        <Text
          textAlign="center"
          fontSize={{ base: "lg", lg: "xl" }}
          color="gray.600"
          mt={4}
        >
          Cybershuttle expertly balances local and remote computing, seamlessly
          orchestrating tasks and data between machines. Schedule
          time-sensitive, small tasks locally, and reserve compute-intensive
          tasks for powerful remote HPC resources, with data transparently
          accessible everywhere. This approach significantly enhances the
          efficiency of scientific workflows compared to fully remote or fully
          local operations.
        </Text>

        <Image src={HeroOriginal} alt="Cybershuttle" maxW="100%" mt={8} />
        <VStack mt={18} alignItems="flex-start" gap={16}>
          <Box>
            <SectionHeading>Main Features</SectionHeading>
          </Box>

          <Box>
            <SectionHeading>Science-Centrist Extensions</SectionHeading>
          </Box>

          <Box>
            <SectionHeading>Collaboration</SectionHeading>
          </Box>

          <Box>
            <SectionHeading>Thanks to our Sponsors</SectionHeading>
          </Box>
        </VStack>

        <Box my={8} />
      </Container>
    </>
  );
};
