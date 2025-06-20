import {useEffect, useState} from "react";
import {useAuth} from "react-oidc-context";
import api from "@/lib/api.ts";
import {CONTROLLER} from "@/lib/controller.ts";
import {Container, SimpleGrid, Spinner} from "@chakra-ui/react";
import {Resource} from "@/interfaces/ResourceType.ts";
import {ResourceCard} from "@/components/home/ResourceCard.tsx";
import {PageHeader} from "@/components/PageHeader.tsx";

export const LikedResourcesPage = () => {
  const [likedResources, setLikedResources] = useState([]);
  const [loading, setLoading] = useState(false);
  const auth = useAuth();

  useEffect(() => {
    if (auth.isLoading) return;

    async function getLikedResources() {
      setLoading(true);
      const resp = await api.get(`${CONTROLLER.likes}/users/${auth.user?.profile.email}/resources`)
      setLikedResources(resp.data);
      setLoading(false);
    }

    getLikedResources();
  }, [auth.isLoading]);

  return (
      <Container maxW="container.lg" mt={8}>
        <PageHeader title={"Liked Resources"}
                    description={"Resources that you have liked will show up here, for easy access."}/>
        <SimpleGrid
            columns={{base: 1, md: 2, lg: 4}}
            mt={4}
            gap={2}
            justifyContent="space-around"
        >
          {likedResources.map((resource: Resource) => {
            return (
                <ResourceCard
                    resource={resource}
                    key={resource.id}
                    removeOnUnlike={true}
                />
            );
          })}
        </SimpleGrid>
        {
            loading && (
                <Spinner/>
            )
        }
        {
            !loading && likedResources.length === 0 && (
                <>
                  No liked resources found.
                </>
            )
        }
      </Container>
  )
}