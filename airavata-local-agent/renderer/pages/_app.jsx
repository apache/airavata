// pages/_app.jsx
import { useState } from 'react';
import { ChakraProvider } from '@chakra-ui/react';
import { AuthContext, AuthProvider } from '../lib/Contexts';

function MyApp({ Component, pageProps }) {
  return (
    <ChakraProvider>
      <AuthProvider>
        <Component {...pageProps} />
      </AuthProvider>
    </ChakraProvider>
  );
}

export default MyApp;