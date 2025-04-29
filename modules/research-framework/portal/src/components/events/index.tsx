import { Container, Accordion, Spacer, Span } from "@chakra-ui/react";
import { Apr11Workshop } from "./Apr11Workshop";
import { May7Workshop } from "./May7Workshop";

export const Events = () => {
  return (
    <Container maxW="breakpoint-lg" my={8}>
      <Accordion.Root multiple defaultValue={["May 7, 2025"]}>
        {events.map((event) => (
          <Accordion.Item key={event.id} value={event.id}>
            <Accordion.ItemTrigger>
              {event.name} <Spacer />
              <Span color="gray.400" fontSize="sm">
                {event.id}
              </Span>
              <Accordion.ItemIndicator />
            </Accordion.ItemTrigger>

            <Accordion.ItemContent>
              <Accordion.ItemBody>{event.component()}</Accordion.ItemBody>
            </Accordion.ItemContent>
          </Accordion.Item>
        ))}
      </Accordion.Root>
    </Container>
  );
};

const events = [
  {
    id: "May 7, 2025",
    name: "Cyberinfrastructure and Services for Science & Engineering Workshop",
    component: May7Workshop,
  },
  {
    id: "April 11, 2025",
    name: "Data-Driven and Large-Scale Modeling in Neuroscience",
    component: Apr11Workshop,
  },
];
