/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/
import { Link, Text } from "@chakra-ui/react";
import { useRouter } from "next/router";
import { useEffect } from "react";

const LoginCallback = () => {
  const router = useRouter();

  useEffect(() => {
    async function getToken() {
      const code = router.query.code;

      if (code) {
        // exchange code for token
        const resp = await fetch(`https://testdrive.cybershuttle.org/auth/get-token-from-code/?code=${code}`);
        const data = await resp.json();

        localStorage.setItem("accessToken", data.access_token);
        localStorage.setItem("refreshToken", data.refresh_token);

        window.ipc.send('write-file', '~/csagent/token/keys.json', JSON.stringify(data));

        router.push('/docker-home');
      }
    }

    getToken();
  });

  return (
    <>
      <Text>
        Logging in...you should be redirected shortly.
      </Text>

      <Text>
        If you are not redirected within a minute, please try <Link href="/login">logging in again</Link>.
      </Text>
    </>
  );
};

export default LoginCallback;