import {
  Container,
  Heading,
  Link,
  Text,
  VStack,
  Table,
} from "@chakra-ui/react";

const agendaItems = [
  {
    time: "8:00 am – 8:15 am",
    topic: "Breakfast/ Sign in",
    session: "",
    speaker: "https://s.apache.org/cs-neuro-25",
    highlight: true,
  },
  {
    time: "8:15 am – 8:30 am",
    topic: "ARTISAN Welcome",
    session: "",
    speaker: "Suresh Marru & Giri Krishnan",
  },
  {
    time: "8:30 am – 9:30 am",
    topic: "Bio-realistic multiscale simulations of cortical circuits",
    session: ["Presentation", "Demo"],
    speaker: ["Anton Arkhipov", "Laura Green"],
  },
  {
    time: "9:30 am – 9:45 am",
    topic:
      "Apache Cerebrum: Flexible tool for constructing computational neuroscience models from large public databases and brain atlases.",
    session: "Presentation & Demo",
    speaker: "Sriram Chockalingam",
  },
  {
    time: "9:45 am – 10:45 am",
    topic: "Spatio-temporal dynamics of sleep in large-scale brain models",
    session: ["Presentation", "Hands on"],
    speaker: ["Maxim Bazhenov", "Prepared by Gabriela Navas Zuloaga"],
  },
  {
    time: "10:45 am – 11:00 am",
    topic: "Break",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "11:00 am – 11:45 am",
    topic:
      "Biologically constrained RNNs via Dale's backpropagation and topologically-informed pruning",
    session: ["Presentation", "Hands on"],
    speaker: ["Hanna Choi", "Aishwarya Balwani"],
  },
  {
    time: "11:45 am – 12:30 pm",
    topic:
      "One-hot Generalized Linear Model for Switching Brain State Discovery",
    session: ["Presentation", "Hands on"],
    speaker: ["Anqi Wu", "Li, Chengrui"],
  },
  {
    time: "12:30 pm – 1:00 pm",
    topic: "Lunch",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "1:00 pm – 2:00 pm",
    topic: "Scaling up neural data analysis with torch_brain",
    session: ["Presentation", "Hands on"],
    speaker: [
      "Eva Dyer’s Group (Vinam Arora, Mahato Shivashriganesh)",
      "Vinam Arora, Mahato Shivashriganesh",
    ],
  },
  {
    time: "2:00 pm – 2:45 pm",
    topic: "Bridge the Gap between the Structure and Function in the Brain",
    session: ["Presentation", "Hands on"],
    speaker: "Lu Mi",
  },
  {
    time: "2:45 pm – 3:00 pm",
    topic: "Break",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "3:00 pm – 3:45 pm",
    topic: "Computing with Neural Oscillators",
    session: ["Presentation", "Hands on"],
    speaker: ["Nabil Imam", "Nand Chandravadia"],
  },
  {
    time: "3:45 pm – 4:30 pm",
    topic: "Executable NeuroAI models for vision neuroscience",
    session: ["Presentation", "Hands on"],
    speaker: ["Ratan Murty", "Ratan Murty Student"],
  },
  {
    time: "4:30 pm – 4:45 pm",
    topic: "Conclusion",
    session: "Presentation",
    speaker: "Giri Krishnan",
  },
];

const indexToColor = ["black", "blue.600"];

export const Events = () => {
  return (
    <Container maxW="breakpoint-lg" my={8}>
      <Heading fontSize="3xl" lineHeight={1.2}>
        Data-Driven and Large-Scale Modeling in Neuroscience
      </Heading>
      <VStack gap={1} align="start" mt={2}>
        <KeyPair
          keyStr="When"
          valueStr="April 11, 2025 | 8:00 a.m. - 5:00 p.m."
        />
        <KeyPair
          keyStr="Where"
          valueStr="Technology Square Research Building (TSRB) 132 Banquet Hall"
        />
        <KeyPair
          keyStr="Details"
          valueStr="https://research.gatech.edu/data/NeuroData25"
        />
      </VStack>

      <Table.Root mt={4} stickyHeader={true} interactive={true}>
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>Time</Table.ColumnHeader>
            <Table.ColumnHeader>Topic</Table.ColumnHeader>
            <Table.ColumnHeader>Session</Table.ColumnHeader>
            <Table.ColumnHeader>Speaker</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {agendaItems.map((item, index) => (
            <Table.Row
              key={index}
              bg={item.highlight ? "green.100" : "transparent"}
            >
              <Table.Cell>{item.time}</Table.Cell>
              <Table.Cell>{item.topic}</Table.Cell>
              {typeof item.session === "string" ? (
                <Table.Cell>{item.session}</Table.Cell>
              ) : (
                <Table.Cell>
                  {item.session.map((session, index) => (
                    <Text key={index} color={indexToColor[index]}>
                      {session}
                    </Text>
                  ))}
                </Table.Cell>
              )}

              <Table.Cell>
                {typeof item.speaker === "string" ? (
                  <Text>
                    {item.speaker.includes("http") ? (
                      <Link
                        href={item.speaker}
                        target="_blank"
                        color="blue.600"
                        textDecor={"underline"}
                      >
                        {item.speaker}
                      </Link>
                    ) : (
                      item.speaker
                    )}
                  </Text>
                ) : (
                  <Text>
                    {item.speaker.map((speaker, index) => (
                      <Text key={index} color={indexToColor[index]}>
                        {speaker}
                      </Text>
                    ))}
                  </Text>
                )}
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
    </Container>
  );
};

const KeyPair = ({
  keyStr,
  valueStr,
}: {
  keyStr: string;
  valueStr: string;
}) => {
  const isLink = valueStr.startsWith("http");
  return (
    <Text>
      <Text as="span" fontWeight="bold">
        {keyStr}:{" "}
      </Text>

      {isLink ? (
        <Link
          href={valueStr}
          target="_blank"
          color="blue.600"
          fontWeight="normal"
        >
          {valueStr}
        </Link>
      ) : (
        <Text as="span" fontWeight="normal">
          {valueStr}
        </Text>
      )}
    </Text>
  );
};
