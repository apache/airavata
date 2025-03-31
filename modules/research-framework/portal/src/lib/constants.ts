let BACKEND_URL: string;
const API_VERSION = "v1";

if (process.env.NODE_ENV === 'production') {
  BACKEND_URL = "";
} else {
  BACKEND_URL = "http://localhost:18889";
}

export { BACKEND_URL, API_VERSION };