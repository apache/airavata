import { Text } from "@chakra-ui/react";
import { Link } from "@chakra-ui/react";

export const KeyPair = ({
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
