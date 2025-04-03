let BACKEND_URL: string;
let APP_URL: string;

if (process.env.NODE_ENV === 'production') {
  BACKEND_URL = "https://api.cybershuttle.org:18889";
  APP_URL = "https://catalog.cybershuttle.org";
} else {
  BACKEND_URL = "http://localhost:18889";
  APP_URL = 'http://localhost:5173';
}

export const API_VERSION = "v1";
export const CLIENT_ID = 'data-catalog-portal';
export const APP_REDIRECT_URI = `${APP_URL}/oauth_callback`;
export const OPENID_CONFIG_URL = `https://auth.dev.cybershuttle.org/realms/default/.well-known/openid-configuration`;

export { BACKEND_URL, APP_URL };
