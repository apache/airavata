import { Button, Divider, Flex, Link, Spacer, Stack, Text, Tooltip, useToast } from '@chakra-ui/react';
import { useState, useEffect } from 'react';

export const Footer = ({ currentPage, showWarning }) => {

  const [accessToCreateExperiment, setAccessToCreateExperiment] = useState(false);

  useEffect(() => {
    async function getData() {
      const resp = await fetch("https://md.cybershuttle.org/api/applications/?format=json", {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      });

      if (!resp.ok) {
        setAccessToCreateExperiment(false);
      }

      const data = await resp.json();
      if (!data || !Array.isArray(data)) {
        setAccessToCreateExperiment(false);
      } else {
        data.forEach((obj) => {
          if (obj.appModuleName === "NAMD") {
            console.log("Access to create experiment");
            setAccessToCreateExperiment(true);
            return;
          }
        });
      }
    }

    getData();

    if (!accessToCreateExperiment) {
      // poll every 5 seconds
      const interval = setInterval(() => {
        getData();
      }, 5000);
      return () => clearInterval(interval);
    }


  }, [accessToCreateExperiment]);

  return (
    <>
      <Divider />
      <Flex px={2} pb={2} bg='gray.100' align='center'>

        <Spacer />

        <Stack direction='row'>

          <Tooltip label={
            !accessToCreateExperiment && "You do not have access to create NAMD experiments"
          }>
            <Button colorScheme='blue' size='xs' isDisabled={('create-namd-experiment' === currentPage) || !accessToCreateExperiment} onClick={() => {
              if (showWarning) {
                confirm("Your VMD and Jupyter Notebook tabs will close if you go to the create experiment page. Are you sure?") && (window.location.href = '/create-namd-experiment');
              } else {
                window.location.href = '/create-namd-experiment';
              }
            }}>
              Create NAMD Experiment
            </Button>
          </Tooltip>
        </Stack>
      </Flex >
    </>
  );
};