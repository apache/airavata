import {ModelResource, Resource} from "@/interfaces/ResourceType";
import {Tag} from "@/interfaces/TagType";
import {isValidImaage, resourceTypeToColor} from "@/lib/util";
import {Avatar, Badge, Box, Card, HStack, Image, Text,} from "@chakra-ui/react";
import {ResourceTypeBadge} from "../resources/ResourceTypeBadge";
import {ResourceTypeEnum} from "@/interfaces/ResourceTypeEnum";
import {ModelCardButton} from "../models/ModelCardButton";
import {DeleteResourceButton} from "@/components/resources/DeleteResourceButton.tsx";
import {useState} from "react";
import {Link} from 'react-router';

export const ResourceCard = ({
                               resource,
                               size = "sm",
                               deletable = true
                             }: {
  resource: Resource;
  size?: "sm" | "md" | "lg";
  deletable?: boolean
}) => {
  const [hideCard, setHideCard] = useState(false);
  const author = resource.authors[0];

  const isValidImage = isValidImaage(resource.headerImage);

  resource.tags.sort((a, b) => a.value.localeCompare(b.value));

  const linkToWithType = `${resource.type}/${resource.id}`;

  const link = '/resources/' + linkToWithType;
  const onDeleteSuccess = () => {
    setHideCard(true);
  }

  const content = (
      <Card.Root
          overflow="hidden"
          size={size}
      >
        {isValidImage && (
            <Box position="relative" width="full">
              <ResourceTypeBadge
                  type={resource.type}
                  position="absolute"
                  top="2"
                  left="2"
                  zIndex="1"
                  boxShadow="md"
              />

              {/* Full-width Image */}
              <Image
                  src={resource.headerImage}
                  alt={resource.name}
                  width="100%" // Ensure full width
                  height="200px"
                  objectFit="cover"
              />
            </Box>
        )}

        <Card.Header>
          <HStack justifyContent={'space-between'} alignItems={'center'} flexWrap={'wrap'}>
            <Card.Title>{resource.name}</Card.Title>
            {deletable && <DeleteResourceButton resource={resource} onSuccess={onDeleteSuccess}/>}
          </HStack>
        </Card.Header>

        <Link to={link} target={"_blank"}>
          <Box
              _hover={{bg: resourceTypeToColor(resource.type) + ".100"}}
          >
            <Card.Body gap="2">
              {!isValidImage && (
                  <Box>
                    <ResourceTypeBadge type={resource.type}/>
                  </Box>
              )}

              {/* Card Content */}
              <HStack flexWrap="wrap">
                {resource.tags.map((tag: Tag) => (
                    <Badge
                        key={tag.id}
                        size="md"
                        rounded="md"
                        colorPalette={resourceTypeToColor(resource.type)}
                    >
                      {tag.value}
                    </Badge>
                ))}
              </HStack>
              <Text color="fg.muted" lineClamp={2}>
                {resource.description}
              </Text>
            </Card.Body>

            <Card.Footer justifyContent="space-between" pt={4}>
              {author && (
                  <HStack>
                    <Avatar.Root shape="full" size="sm">
                      <Avatar.Fallback name={author}/>
                      <Avatar.Image src={author}/>
                    </Avatar.Root>

                    <Box>
                      <Text fontWeight="bold">{author}</Text>
                    </Box>
                  </HStack>
              )}

              {(resource.type as ResourceTypeEnum) === ResourceTypeEnum.MODEL && (
                  <ModelCardButton model={resource as ModelResource}/>
              )}
            </Card.Footer>
          </Box>
        </Link>
      </Card.Root>
  );

  // if (clickable) {
  //   return (
  //       <Box hidden={hideCard}>
  //         <Link to={link}>{content}</Link>
  //       </Box>
  //   );
  // }
  return <Box hidden={hideCard}>{content}</Box>;
};
