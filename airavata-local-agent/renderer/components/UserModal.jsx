import React, { useEffect, useState } from "react";
import { Box, Text, Input, FormControl, FormLabel, Stack, FormHelperText } from "@chakra-ui/react";

export const UserModal = ({ email, accessToken }) => {
  const [user, setUser] = useState({});



  useEffect(() => {
    async function getUserInfo() {
      await fetch(`https://md.cybershuttle.org/api/user-profiles/ganning.xu@gatech.edu/?format=json`, {
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
        <Input value={user.userId || ""} isDisabled />
        <FormHelperText>Only administrators can update a username.</FormHelperText>
      </FormControl>

      <FormControl>
        <FormLabel>First Name</FormLabel>
        <Input value={user.firstName || ""} isReadOnly />
      </FormControl>

      <FormControl>
        <FormLabel>Last Name</FormLabel>
        <Input value={user.lastName || ""} isReadOnly />
      </FormControl>

      <FormControl>
        <FormLabel>Email</FormLabel>
        <Input value={user.emails && user.emails[0] || ""} isReadOnly />
      </FormControl>

      <FormControl>
        <FormLabel>Gateway Id</FormLabel>
        <Input value={user.gatewayId || ""} isReadOnly />
      </FormControl>

      <FormControl>
        <FormLabel>Account Creation Time</FormLabel>
        <Input value={new Date(user.creationTime).toLocaleString() || ""} isDisabled />
      </FormControl>


    </Stack>
  );
};