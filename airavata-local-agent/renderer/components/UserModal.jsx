import React, { useEffect, useState } from "react";
import { Box, Text, Input, FormControl, FormLabel, Stack, FormHelperText, Button, useToast } from "@chakra-ui/react";

export const UserModal = ({ email, accessToken }) => {
  const [user, setUser] = useState({});
  const toast = useToast();
  const [loading, setLoading] = useState(false);

  async function handleSaveInfo() {
    setLoading(true);
    await fetch(`https://md.cybershuttle.org/auth/users/${user.id}/`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(user)
    }).then(resp => resp.json()).then(data => {
      toast({
        title: "User information updated.",
        status: "success",
        duration: 5000,
        isClosable: true,
      });

      setUser(data);
      setLoading(false);
    }).catch(err => {
      toast({
        title: "Failed to update user information.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });

      setLoading(false);
    });
  }

  useEffect(() => {

    async function getUserInfo() {

      await fetch(`https://md.cybershuttle.org/auth/users/current/`, {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      }).then(resp => resp.json()).then(data => {
        console.log(data);
        setUser(data);
      });
    }

    getUserInfo();
  }, []);

  return (
    <Stack direction='column' spacing={4}>
      <Text fontStyle='italic'>All fields are currently readonly.</Text>
      <FormControl>
        <FormLabel>Username</FormLabel>
        <Input value={user.username || ""} isDisabled />
        <FormHelperText>Only administrators can update a username.</FormHelperText>
      </FormControl>

      <FormControl>
        <FormLabel>First Name</FormLabel>
        <Input value={user.first_name || ""} onChange={
          (e) => {
            setUser({ ...user, first_name: e.target.value });
          }
        } />
      </FormControl>

      <FormControl>
        <FormLabel>Last Name</FormLabel>
        <Input value={user.last_name || ""} onChange={
          (e) => {
            setUser({ ...user, last_name: e.target.value });
          }
        } />
      </FormControl>

      <FormControl>
        <FormLabel>Email</FormLabel>
        <Input value={user.email || ""} onChange={
          (e) => {
            setUser({ ...user, email: e.target.value });
          }
        } />

        {
          user?.pending_email_change?.email_address && (
            <Text bg='blue.100' p={1} mt={4} rounded='md'>Once you verify your email address at {user.pending_email_change.email_address} your email address will be updated.</Text>
          )
        }
      </FormControl>

      <Button colorScheme='blue' size='sm'
        onClick={handleSaveInfo}
        isLoading={loading}

      >Update</Button>

      {/* <FormControl>
        <FormLabel>Gateway Id</FormLabel>
        <Input value={user.gatewayId || ""} isReadOnly />
      </FormControl> */}

      {/* <FormControl>
        <FormLabel>Account Creation Time</FormLabel>
        <Input value={new Date(user.creationTime).toLocaleString() || ""} isDisabled />
      </FormControl> */}


    </Stack>
  );
};