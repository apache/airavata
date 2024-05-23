import { Box, Text, Button, Flex, Spacer, Img } from '@chakra-ui/react';
import { MdPause, MdPlayArrow } from "react-icons/md";
import { useRouter } from 'next/router';
export const NavBar = () =>
{
    const router = useRouter();
    const handleLogout = () =>
    {
        console.log("logging out logic...");
        router.push("/login");
    };
    return (
        <>
            <Flex bg='gray.100' alignItems='center' px={4} py={4}>
                <Box>
                    <Flex alignItems='center' gap={3}>
                        <Img src="/images/a-logo.png" maxH='35px' />
                        <Text fontWeight='black' fontSize='2xl' color='blue.600'>Airavata UI</Text>
                    </Flex>
                </Box>

                <Spacer />

                <Button size='sm' colorScheme='red' onClick={handleLogout}>Logout</Button>
            </Flex>
        </>
    );
};