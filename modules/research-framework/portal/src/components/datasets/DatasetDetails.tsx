import { Metadata } from "../Metadata";
import NavBar from "../NavBar";
// @ts-expect-error This is fine
import { MOCK_DATASETS } from "../../data/MOCK_DATA";
import { DatasetType } from "@/interfaces/DatasetType";
import { useEffect, useState } from "react";
import { Badge, Box, Container, HStack, Icon, Spinner } from "@chakra-ui/react";
import { Link, useParams } from "react-router";
import { BiArrowBack } from "react-icons/bi";

async function getDataset(slug: string | undefined) {
  return MOCK_DATASETS.find(
    (dataset: DatasetType) => dataset.metadata.slug === slug
  );
}

export const DatasetDetails = () => {
  const [dataset, setDataset] = useState<DatasetType | null>(null);
  const { slug } = useParams();

  useEffect(() => {
    async function getData() {
      const n = await getDataset(slug);
      setDataset(n);
    }
    getData();
  }, []);

  if (!dataset) return <Spinner />;
  return (
    <>
      <NavBar />

      <Container maxW="breakpoint-lg" mx="auto" p={4} mt={16}>
        <Box>
          <HStack alignItems="center" mb={4}>
            <Icon>
              <BiArrowBack />
            </Icon>
            <Link to="/datasets">Back to Datasets</Link>
          </HStack>
        </Box>
        <Badge
          colorPalette={dataset.private ? "red" : "green"}
          rounded="md"
          mb={1}
        >
          {dataset.private ? "Private" : "Public"}
        </Badge>
        <Metadata metadata={dataset.metadata} />
      </Container>
    </>
  );
};
