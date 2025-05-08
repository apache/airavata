import { Button, ButtonProps, Icon } from "@chakra-ui/react";
import { IconType } from "react-icons";

interface ButtonWithIconProps extends ButtonProps {
  icon: IconType;
  children: React.ReactNode;
}

export const ButtonWithIcon = ({
  icon: UserIcon,
  children,
  ...rest
}: ButtonWithIconProps) => {
  return (
    <Button {...rest}>
      <Icon size="xs">
        <UserIcon fontWeight="solid" />
      </Icon>
      {children}
    </Button>
  );
};
