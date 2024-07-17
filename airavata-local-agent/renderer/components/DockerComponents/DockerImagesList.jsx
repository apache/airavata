import { TableContainer, Table, Thead, Tr, Th, Tbody, Td } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { bytesToSize } from "../../lib/utilityFuncs";
import { useInterval } from "usehooks-ts";

const stripPrefix = (str, prefix) => {
  if (str.startsWith(prefix)) {
    return str.slice(prefix.length);
  }
  return str;
};

const IMAGES_FETCH_INTERVAL = 5000;

export const DockerImagesList = () => {
  const [images, setImages] = useState([]);

  useEffect(() => {
    window.ipc.send("get-all-images");

    window.ipc.on("got-all-images", (images) => {
      console.log(images);
      setImages(images);
    });

    return () => {
      window.ipc.removeAllListeners("got-all-images");
    };
  }, []);

  useInterval(() => {
    window.ipc.send("get-all-images");
  }, IMAGES_FETCH_INTERVAL);

  return (
    <TableContainer>
      <Table>
        <Thead>
          <Tr>
            <Th>Name</Th>
            <Th>ID</Th>
            <Th>Size</Th>
          </Tr>
        </Thead>
        <Tbody>
          {
            images?.map((image) => (
              <Tr key={image.Id}>
                <Td>{image.RepoTags[0]}</Td>
                <Td>{stripPrefix(image.Id, "sha256:").slice(0, 12)}</Td>
                <Td>{bytesToSize(image.Size)}</Td>
              </Tr>
            ))
          }
        </Tbody>
      </Table>
    </TableContainer>
  );
};