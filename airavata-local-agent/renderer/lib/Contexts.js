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
import { useState, useContext, createContext, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [authInfo, setAuthInfo] = useState({
    accessToken: null,
    refreshToken: null,
  });

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    if (accessToken && refreshToken) {
      setAuthInfo({ accessToken, refreshToken });
    }
  }, []);

  return (
    <AuthContext.Provider value={[authInfo, setAuthInfo]}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const state = useContext(AuthContext);
  return state;
};

// Create context
const BackendUrlContext = createContext();

export const BackendUrlProvider = ({ children }) => {
  const [gateway, setGateway] = useState('mdcyber');
  const [allGateways, setAllGateways] = useState([]);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch gateways
    window.ipc.send('get-all-gateways');
    window.ipc.send('get-gateway');

    window.ipc.on('got-gateways', (all) => {
      setAllGateways(all);
    });

    window.ipc.on('gateway-got', (gateway) => {
      setGateway(gateway);
      setLoading(false);
    });

    return () => {
      window.ipc.removeAllListeners('got-gateways');
      window.ipc.removeAllListeners('gateway-got');
    };
  }, []);



  // the user might want to change their gateway in the app too
  const setGatewayId = (gateway) => {
    setGateway(gateway);
    window.ipc.send('set-gateway', gateway);
  };


  if (loading) {
    return <div>Loading...</div>;
  }
  const ourGateway = allGateways?.find(g => g.id === gateway);

  return (
    <BackendUrlContext.Provider value={{
      apiUrl: ourGateway?.gateway + '/api',
      authUrl: ourGateway?.gateway + '/auth',
      loginUrl: ourGateway?.loginUrl,
      gatewayName: ourGateway?.name,
      gateway,
      allGateways,
      setGatewayId
    }}>
      {children}
    </BackendUrlContext.Provider>
  );
};

export const useBackendUrls = () => useContext(BackendUrlContext);
