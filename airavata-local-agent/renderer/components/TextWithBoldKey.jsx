import { Text } from "@chakra-ui/react";

export const TextWithBoldKey = ({ keyName, text }) => {
    return (
        <Text>
            <strong>{keyName}:</strong> {text}
        </Text>
    );
};