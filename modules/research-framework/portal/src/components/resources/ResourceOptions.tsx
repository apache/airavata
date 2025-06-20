import {Resource} from "@/interfaces/ResourceType.ts";
import {Button, ButtonProps, Menu, VStack} from "@chakra-ui/react";
import {BsThreeDots} from "react-icons/bs";
import {DeleteResourceButton} from "@/components/resources/DeleteResourceButton.tsx";
import {LikeResourceButton} from "@/components/resources/LikeResourceButton.tsx";
import {useAuth} from "react-oidc-context";

export const ResourceOptions = ({resource, deleteable = true, onDeleteSuccess, onUnlikeSuccess}:
                                {
                                  resource: Resource,
                                  deleteable: boolean,
                                  onDeleteSuccess: () => void,
                                  onUnlikeSuccess: (resourceId: string) => void
                                }) => {

  const auth = useAuth();
  if (!auth.isAuthenticated) {
    return null;
  }

  return (
      <>
        <Menu.Root>
          <Menu.Trigger _hover={{
            cursor: 'pointer',
          }}>
            <BsThreeDots/>
          </Menu.Trigger>
          <Menu.Positioner>
            <Menu.Content>
              <VStack gap={2} alignItems={'start'}>
                <LikeResourceButton resource={resource} onSuccess={onUnlikeSuccess}/>
                {deleteable && <DeleteResourceButton resource={resource} onSuccess={onDeleteSuccess}/>}
              </VStack>
            </Menu.Content>
          </Menu.Positioner>
        </Menu.Root>
      </>
  )
}


type ResourceOptionButtonProps = {
  onClick: () => void;
  children?: React.ReactNode;
} & ButtonProps;

export const ResourceOptionButton = ({
                                       onClick,
                                       children,
                                       ...rest
                                     }: ResourceOptionButtonProps) => {
  return (
      <Button
          w="full"
          transition="all .2s"
          rounded="md"
          gap={2}
          onClick={onClick}
          p={0}
          display={'flex'}
          justifyContent={'flex-start'}
          bg={'transparent'}
          {...rest}
      >
        {children}
      </Button>
  );
};
