import {Resource} from "@/interfaces/ResourceType.ts";
import {ResourceOptionButton} from "@/components/resources/ResourceOptions.tsx";
import {Box} from "@chakra-ui/react";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {toaster} from "@/components/ui/toaster.tsx";
import {useEffect, useState} from "react";
import {GoHeart, GoHeartFill} from "react-icons/go";

export const LikeResourceButton = ({
                                     resource,
                                     onSuccess,
                                   }: {
  resource: Resource,
  onSuccess: (resourceId: string) => void,
}) => {
  const [changeLikeLoading, setChangeLikeLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(false);
  const [liked, setLiked] = useState(false);

  useEffect(() => {
    async function getWhetherUserLiked() {
      setInitialLoading(true);
      const resp = await api.get(`${CONTROLLER.likes}/resources/${resource.id}`);
      setLiked(resp.data);
      setInitialLoading(false);
    }

    getWhetherUserLiked();
  }, []);

  const handleLikeResource = async () => {
    try {
      setChangeLikeLoading(true);
      await api.post(`${CONTROLLER.likes}/resources/${resource.id}`);
      toaster.create({
        title: liked ? "Unliked" : "Liked",
        description: resource.name,
        type: "success",
      })
      setLiked(prev => {
        if (prev) {
          onSuccess(resource.id || "INVALID");
        }
        return !prev;
      });
    } catch {
      toaster.create({
        title: "Error liking resource",
        type: "error",
      });
    } finally {
      setChangeLikeLoading(false);
    }
  }

  if (initialLoading) {
    return null;
  }

  return (
      <>
        <ResourceOptionButton
            gap={2}
            _hover={{
              cursor: 'pointer',
              bg: 'blue.200',
            }}
            color={'black'}
            onClick={handleLikeResource}
            loading={changeLikeLoading}
        >
          {
            liked ? <GoHeartFill color={'red'}/> : <GoHeart/>
          }
          <Box>
            {
              liked ? "Unlike" : "Like"
            }
          </Box>
        </ResourceOptionButton>
      </>
  )
}