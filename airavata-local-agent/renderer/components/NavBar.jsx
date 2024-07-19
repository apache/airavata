import { Box, Text, Button, Flex, Spacer, Img } from '@chakra-ui/react';
import { MdPause, MdPlayArrow } from "react-icons/md";
export const NavBar = () => {
    const handleLogout = () => {
        window.auth.ciLogonLogout();

        setTimeout(() => {
            window.location.href = "/login";
        }, 2000);

    };
    return (
        <>
            <Flex bg='gray.100' alignItems='center' px={4} py={4}>
                <Box>
                    <Flex alignItems='center' gap={3}>
                        <Img src="/images/cs-logo.png" maxH='35px' />
                        <Text fontWeight='black' fontSize='2xl' color='blue.600'>Airavata UI</Text>
                    </Flex>
                </Box>

                <Spacer />

                <Button size='sm' colorScheme='red' onClick={handleLogout}>Logout</Button>
            </Flex>
        </>
    );
};