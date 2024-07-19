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


// import { useContext, createContext } from "react";

// const TemplateContext = createContext({});

// // Template Provider
// const TemplateProvider = ({ children }) => {
//   const [accessToken, setAccessToken] = React.useState('');

//   // Context values passed to consumer
//   const value = {
//     accessToken,    // <------ Expose Value to Consumer
//     setAccessToken  // <------ Expose Setter to Consumer
//   };

//   return (
//     <TemplateContext.Provider value={value}>
//       {children}
//     </TemplateContext.Provider>
//   );
// };

// // Template Consumer
// const TemplateConsumer = ({ children }) => {
//   return (
//     <TemplateContext.Consumer>
//       {(context) => {
//         if (context === undefined) {
//           throw new Error('TemplateConsumer must be used within TemplateProvider');
//         }
//         return children(context);
//       }}
//     </TemplateContext.Consumer>
//   );
// };

// // useTemplate Hook
// const useAuth = () => {
//   const context = useContext(AuthContext);
//   if (context === undefined) {
//     throw new Error('useTemplate must be used within TemplateProvider');
//   }
//   return context;
// };

// export {
//   TemplateProvider,
//   TemplateConsumer,
//   useAuth
// };
