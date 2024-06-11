import { Divider, Flex, Link, Spacer, Stack, Text } from '@chakra-ui/react';

export const Footer = () => {
    return (
        <>
            <Divider />
            <Flex px={2} py={1} bg='gray.100'>
                <Text textAlign='center'>Developed by the Apache Airavata Team</Text>

                <Spacer />

                <Stack direction='row'>
                    <Link color='blue.400' href='/tabs-view'>List Experiments</Link>

                    <Text>•</Text>

                    <Link color='blue.400' href='/create-namd-experiment'>Create NAMD Experiment</Link>
                </Stack>
            </Flex >
        </>
    );
};