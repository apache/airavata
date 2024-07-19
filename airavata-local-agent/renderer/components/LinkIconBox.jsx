import { Flex, Link, Box, Text, Icon } from '@chakra-ui/react';

const defaultColor = 'gray.400';
const activeColor = 'blue.400';
const BOX_SIZE = '110px';

export const LinkIconBox = ({ vertical, horizontal, active, icon, text, href, ...props }) =>
{
    let borderRadius = [0, 0, 0, 0];

    if (vertical === 'top')
    {
        if (horizontal === 'left')
        {
            // upLeft, upRight, bottomRight, bottomLeft
            // borderRadius = '10px 1px 1px 10px';
            borderRadius[0] = 10;
        } else
        {
            // borderRadius = '1px 10px 10px 1px';
            borderRadius[1] = 10;
        }
    } else if (vertical === "bottom")
    {
        if (horizontal === 'left')
        {
            borderRadius[3] = 10;
        } else
        {
            borderRadius[2] = 10;
        }
    }

    let finalBorderString = "";
    for (let i = 0; i < borderRadius.length; i++)
    {
        finalBorderString += borderRadius[i] + "px";

        if (i != 3)
        {
            finalBorderString += " ";
        }
    }
    return (
        <Link href={href} _hover={{}}>
            <Flex alignItems='center' justifyContent='center' textAlign='center' borderWidth='1px' borderColor='gray.300' bg={active ? 'white' : 'gray.100'} borderRadius={finalBorderString} h={BOX_SIZE} w={BOX_SIZE} transition='all .2s' _hover={{ bg: 'white', color: activeColor }} color={active ? activeColor : defaultColor}  {...props} >
                <Box>
                    <Icon as={icon} fontSize='3xl' />
                    <Text >{text}</Text>
                </Box>
            </Flex>
        </Link>
    );
};
