import { Heading, VStack, Table, Text } from "@chakra-ui/react";
import { Link } from "@chakra-ui/react";
import { KeyPair } from "../typography/KeyPair";

const agendaItems = [
  {
    time: "8:00 am – 8:30 am",
    topic: "Breakfast/ Sign in",
    session: "",
    speaker: "https://s.apache.org/hg05v",
    highlight: true,
  },
  {
    time: "8:30 am – 8:45 am",
    topic: "IDEaS Welcome",
    session: "Presentation",
    speaker: "David Sherill",
  },
  {
    time: "8:45 am – 9:15 am",
    topic: "Introduction to the workshop and Tutorial on using Cybershuttle",
    session: ["Presentation", "Tutorial"],
    speaker: "Sudhakar Pamidighantam",
  },
  {
    time: "9:15 am – 10:00 am",
    topic:
      "MatterTune: An Integrated Platform for Fine-Tuning Atomistic Foundation Models",
    session: ["Presentation", "Tutorial"],
    speaker: "Victor Fung",
  },
  {
    time: "10:00 am – 10:15 am",
    topic: "Break",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "10:15 am – 11:00 am",
    topic: "Phase-field fracture analysis using the FEniCS library",
    session: ["Presentation", "Tutorial"],
    speaker: ["Aditya Kumar", "Aarosh Dahal"],
  },
  {
    time: "11:00 am – 11:45 am",
    topic: "QC, PSI4, Delta Learning",
    session: ["Presentation", "Tutorial"],
    speaker: ["David Sherill", "Austin Wallace"],
  },
  {
    time: "11:45 am – 12:30 pm",
    topic: "From Sequence to Structure: Interpretability in AlphaFold",
    session: ["Presentation", "Demo"],
    speaker: ["Giri Krishnan", "Tyler Hayes"],
  },
  {
    time: "12:30 pm – 1:00 pm",
    topic: "Lunch",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "1:00 pm – 1:30 pm",
    topic: "Cybershuttle Catalog & Local to Remote Executions",
    session: ["Presentation", "Demo"],
    speaker: ["Yasith Jayawardana", "Dimuthu Wannipurage"],
  },
  {
    time: "1:30 pm – 2:30 pm",
    topic:
      "COmanage Registry of ACCESS for Project Management / Commercial Cloud Resources Through CloudBank / Cybershuttle Integration with CloudBank",
    session: ["Presentation", "Panel"],
    speaker: ["Jim Basney", "Shava Smallen", "Suresh Marru"],
  },
  {
    time: "2:30 pm – 2:45 pm",
    topic: "Break",
    session: "",
    speaker: "",
    highlight: true,
  },
  {
    time: "2:45 pm – 3:30 pm",
    topic:
      "Democratized computational fluid dynamics via MFC across local and broadly deployed remote HPC resources through user friendly interfaces",
    session: ["Presentation", "Tutorial"],
    speaker: ["Spencer Bryngelson", "Benjamin Wiltfong"],
  },
  {
    time: "3:30 pm – 4:00 pm",
    topic: "Amber Molecular Dynamics Workflows",
    session: ["Presentation", "Handson"],
    speaker: ["Lynn Kamerlin", "Gyula Hoffka"],
  },
  {
    time: "4:00 pm – 4:30 pm",
    topic: "Reinforcement learning",
    session: "Presentation",
    speaker: "Zsolt Kira",
  },
  {
    time: "4:30 pm – 5:00 pm",
    topic: "Envoemics Workflows",
    session: ["Presentation", "Demo"],
    speaker: "Kostas Konstantinidis",
  },
  {
    time: "5:00 pm – 5:15 pm",
    topic: "Conclusion - Wrap Up & Follow up Steps",
    session: "Open Discussion",
    speaker: "Sudhakar Pamidighantam",
  },
];

export const May7Workshop = () => {
  return (
    <>
      <Heading fontSize="3xl" lineHeight={1.2}>
        Cyberinfrastructure and Services for Science & Engineering Workshop
      </Heading>
      <VStack gap={1} align="start" mt={2}>
        <KeyPair keyStr="When" valueStr="May 7, 2025 | 8:00 a.m. - 5:00 p.m." />
        <KeyPair
          keyStr="Where"
          valueStr="Marcus Nanotechnology Building 1116 - 1118"
        />
        <KeyPair
          keyStr="Address"
          valueStr="345 Ferst Dr NW, Atlanta, GA 30332"
        />
        <KeyPair
          keyStr="Details"
          valueStr="https://research.gatech.edu/data/CIforSE"
        />
      </VStack>

      <Table.Root mt={4} stickyHeader={true} interactive={true}>
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader>Time</Table.ColumnHeader>
            <Table.ColumnHeader>Topic</Table.ColumnHeader>
            <Table.ColumnHeader>Focus Area</Table.ColumnHeader>
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
                    <Text key={index}>{session}</Text>
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
                      <Text key={index}>{speaker}</Text>
                    ))}
                  </Text>
                )}
              </Table.Cell>
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
    </>
  );
};
