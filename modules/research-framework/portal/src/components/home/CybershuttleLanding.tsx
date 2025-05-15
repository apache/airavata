import {
  Container,
  Heading,
  Image,
  Text,
  VStack,
  Box,
  SimpleGrid,
  Link,
  HStack,
  Separator,
} from "@chakra-ui/react";
import HeroOriginal from "../../assets/Hero.original.png";
import { SectionHeading } from "../typography/SectionHeading";
import ExtensibleIcon from "../../assets/extensibleIcon.png";
import ReproIcon from "../../assets/reproIcon.png";
import ThirdPartyIcon from "../../assets/thirdPartyIcon.png";
import UserClockIcon from "../../assets/userClockIcon.png";
import MolecularDyanmics from "../../assets/MolecularDynamics.png";
import NeuroScience from "../../assets/NeuroScience.png";
import GeorgiaTech from "../../assets/GTLogo.png";
import IULogo from "../../assets/IULogo.png";
import UIUCLogo from "../../assets/UIUCLogo.png";
import UCSDLogo from "../../assets/UCSDLogo.png";
import AllenLogo from "../../assets/AllenLogo.png";
import NSFLogo from "../../assets/NSFLogo.png";
import { FaCheckCircle } from "react-icons/fa";
import PoweredByApache from "../../assets/PoweredByApache.png";
import AccessLogoFooter from "../../assets/access-logo-footer.svg";

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

            <SimpleGrid columns={{ base: 1, md: 2, lg: 4 }} gap={8} mt={2}>
              <FeatureCard
                image={UserClockIcon}
                title="User Productivity"
                description="Enable individual researchers to provide an individual view into research computing resources at all scales."
              />
              <FeatureCard
                image={ExtensibleIcon}
                title="Extensibility & Collaborations"
                description="Instead of learning from scratch, modify an existing experiment and run it through CyberShuttle."
              />
              <FeatureCard
                image={ThirdPartyIcon}
                title="Third-Party App Integrations"
                description="Access to synchronized cloud tools to gain deeper insights from the data."
              />
              <FeatureCard
                image={ReproIcon}
                title="Reproducible Computations"
                description="Shared associated data and experiments captured in the system generating quick results."
              />
            </SimpleGrid>
          </Box>

          <Box>
            <SectionHeading>Science-Centrist Extensions</SectionHeading>
            <Text fontSize="lg" color="gray.500" mt={4}>
              We create and integrate tailored science extensions to optimize
              workflows and meet domain-specific requirements through
              collaboration with experts.
            </Text>

            <VStack mt={8} gap={4} alignItems="flex-start">
              <ExtensionCard
                image={MolecularDyanmics}
                title="Molecular Dynamics"
                description="Integrate with molecular dynamics simulations for enhanced analysis."
              />
              <ExtensionCard
                image={NeuroScience}
                title="Neuroscience"
                description="Integrate with neuroscience data analysis tools for advanced research."
              />
            </VStack>
          </Box>

          <Box>
            <SectionHeading>Collaboration</SectionHeading>
            <Text fontSize="lg" color="gray.500" mt={4}>
              We collaborate with institutions to enhance research computing
              capabilities and foster innovation.
            </Text>

            <SimpleGrid
              columns={{ base: 3, md: 5 }}
              gap={8}
              mt={8}
              alignItems="flex-start"
            >
              <CollaborationCard image={GeorgiaTech} title="Georgia Tech" />
              <CollaborationCard image={IULogo} title="Indiana University" />
              <CollaborationCard
                image={UIUCLogo}
                title="University of Illinois"
              />
              <CollaborationCard
                image={UCSDLogo}
                title="University of California San Diego"
              />
              <CollaborationCard
                image={AllenLogo}
                title="Allen Institute for Brain Sciences"
              />
            </SimpleGrid>
          </Box>

          <Box>
            <SectionHeading>Thanks to our Sponsors</SectionHeading>

            <SimpleGrid
              columns={{ base: 1, md: 2 }}
              gap={8}
              mt={8}
              alignItems="center"
            >
              <Box>
                <Text fontSize="xl" mb={4}>
                  Cybershuttle is supported by the{" "}
                  <Text as="span" fontWeight="bold">
                    National Science Foundation (NSF)
                  </Text>{" "}
                  under the following grants.
                </Text>

                <SimpleGrid columns={2} gap={2}>
                  <GrantBox
                    title="CSSI-2209874"
                    href="https://nsf.gov/awardsearch/showAward?AWD_ID=2209872&HistoricalAwards=false"
                  />
                  <GrantBox
                    title="CSSI-2209872"
                    href="https://nsf.gov/awardsearch/showAward?AWD_ID=2209874&HistoricalAwards=false"
                  />
                  <GrantBox
                    title="CSSI-2209875"
                    href="https://nsf.gov/awardsearch/showAward?AWD_ID=2209873&HistoricalAwards=false"
                  />
                  <GrantBox
                    title="CSSI-2209873"
                    href="https://nsf.gov/awardsearch/showAward?AWD_ID=2209875&HistoricalAwards=false"
                  />
                </SimpleGrid>
              </Box>
              <Image src={NSFLogo} alt="NSF Logo" maxH="180px" mx="auto" />
            </SimpleGrid>
          </Box>
        </VStack>
      </Container>

      <Separator my={16} borderColor="red.700" borderWidth="4px" />

      <SimpleGrid
        columns={{ base: 2, md: 4 }}
        gap={8}
        maxW="breakpoint-lg"
        mx="auto"
        alignItems="center"
        justifyContent="center"
        mb={8}
      >
        <Image
          src={PoweredByApache}
          alt="Powered by Apache"
          maxH="180px"
          mx="auto"
        />
        <Image
          src={AccessLogoFooter}
          alt="Access Logo"
          maxH="180px"
          mx="auto"
        />
        <Image src={NSFLogo} alt="NSF Logo" maxH="180px" mx="auto" />
        <Image src={GeorgiaTech} alt="GT Logo" maxH="180px" mx="auto" />
      </SimpleGrid>
    </>
  );
};

const FeatureCard = ({
  image,
  title,
  description,
}: {
  image: string;
  title: string;
  description: string;
}) => {
  return (
    <VStack
      alignItems="flex-start"
      gap={0}
      borderWidth={1}
      borderRadius="md"
      padding={4}
    >
      <Image src={image} alt={title} maxH="90px" />
      <Text fontSize="2xl" fontWeight="bold">
        {title}
      </Text>
      <Text>{description}</Text>
    </VStack>
  );
};

const ExtensionCard = ({
  image,
  title,
  description,
}: {
  image: string;
  title: string;
  description: string;
}) => {
  return (
    <HStack columns={{ base: 1, md: 2 }} gap={4} mt={2} alignItems="center">
      <Image src={image} alt={title} borderWidth={1} borderRadius="md" />

      <VStack alignItems="flex-start" gap={0}>
        <Text fontSize="2xl" fontWeight="bold">
          {title}
        </Text>
        <Text>{description}</Text>
      </VStack>
    </HStack>
  );
};

const CollaborationCard = ({
  image,
  title,
}: {
  image: string;
  title: string;
}) => {
  return (
    <VStack gap={2} borderRadius="md" textAlign="center">
      <Image src={image} alt={title} maxH="90px" mx="auto" />
      <Text fontSize="lg">{title}</Text>
    </VStack>
  );
};

const GrantBox = ({ title, href }: { title: string; href: string }) => {
  return (
    <HStack>
      <FaCheckCircle color="green" />
      <Link href={href} target="_blank">
        <Text>{title}</Text>
      </Link>
    </HStack>
  );
};
