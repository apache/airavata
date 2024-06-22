import { Divider, Flex, Link, Spacer, Stack, Text, useToast } from '@chakra-ui/react';
import { useState, useEffect } from 'react';

export const Footer = () => {

  const [accessToCreateExperiment, setAccessToCreateExperiment] = useState(false);

  useEffect(() => {
    async function getData() {
      const resp = await fetch("https://md.cybershuttle.org/api/group-resource-profiles/?format=json", {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      const data = await resp.json();
      if (!data || data.length === 0) {
        setAccessToCreateExperiment(false);
      } else {
        setAccessToCreateExperiment(true);
      }
      console.log("the data is:", data);
    }

    getData();
  }, []);


  return (
    <>
      <Divider />
      <Flex px={2} py={1} bg='gray.100' align='center'>
        {/* <Text textAlign='center'>Developed by the Apache Airavata Team</Text> */}

        <Spacer />

        <Stack direction='row'>
          <Link color='blue.400' href='/tabs-view'>List Experiments</Link>
          {
            accessToCreateExperiment && <>
              <Text>•</Text>

              <Link color='blue.400' href='/create-namd-experiment'>Create NAMD Experiment</Link>
            </>}
        </Stack>
      </Flex >
    </>
  );
};