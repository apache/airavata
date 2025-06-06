import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { API_VERSION, BACKEND_URL } from './constants';
import { User } from 'oidc-client-ts';

const api: AxiosInstance = axios.create({
  baseURL: `${BACKEND_URL}/api/${API_VERSION}/rf`,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Function to inject token dynamically
let getUser: (() => Promise<User | null>) | null = null;

export const setUserProvider = (provider: () => Promise<User | null>) => {
  getUser = provider;
};

// Request interceptor
api.interceptors.request.use(
  async (config) => {
    if (getUser) {
      const user = await getUser();
      if (user) {
        config.headers.Authorization = `Bearer ${user.access_token}`;
        config.headers["X-Claims"] = JSON.stringify({
          "userName": user.profile.email,
          "gatewayID": "default",
        });
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;
