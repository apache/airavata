import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { API_VERSION, BACKEND_URL } from './constants';

const api: AxiosInstance = axios.create({
  baseURL: `${BACKEND_URL}/api/${API_VERSION}/rf`,
  timeout: 10000, // Timeout after 10 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Example: Add authorization token
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    // Handle error globally
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;
