import { Heading, HeadingProps } from "@chakra-ui/react";

interface SectionHeadingProps extends HeadingProps {
  children: React.ReactNode;
}
export const SectionHeading = ({ children, ...props }: SectionHeadingProps) => {
  return (
    <Heading
      fontSize={{
        base: "2xl",
        md: "3xl",
        lg: "4xl",
      }}
      fontWeight="bold"
      lineHeight={1.2}
      {...props}
    >
      {children}
    </Heading>
  );
};
