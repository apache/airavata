let BACKEND_URL: string;
const API_VERSION = "v1";

if (process.env.NODE_ENV === 'production') {
  BACKEND_URL = "";
} else {
  BACKEND_URL = "http://localhost:18889";
}

export const CLIENT_ID = 'data-catalog-portal';
export const APP_URL = 'http://localhost:5173';
export const APP_REDIRECT_URI = `${APP_URL}/oauth_callback`;
export const OPENID_CONFIG_URL = `https://auth.cybershuttle.org/realms/default/.well-known/openid-configuration`;

export { BACKEND_URL, API_VERSION };
