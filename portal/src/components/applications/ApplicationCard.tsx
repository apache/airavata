import React from "react";
import { Badge, Button, Card, HStack, Heading, Icon } from "@chakra-ui/react";
import { BiPlay } from "react-icons/bi";
export const ApplicationCard = ({ application }) => {
  return (
    <Card.Root size="sm">
      <Card.Header>
        <HStack justifyContent="space-between" alignItems="flex-start">
          <Heading size="md"> {application.appModuleName}</Heading>
          <Badge size="md" colorPalette="green">
            {application.appModuleVersion}
          </Badge>
        </HStack>
      </Card.Header>
      <Card.Body color="fg.muted">{application.appModuleDescription}</Card.Body>
      <Card.Footer>
        <Button colorPalette="teal">
          Run
          <Icon>
            <BiPlay />
          </Icon>
        </Button>
      </Card.Footer>
    </Card.Root>
  );
};
