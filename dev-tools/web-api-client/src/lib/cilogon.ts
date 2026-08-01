export const CILOGON_AUTHORIZATION_ENDPOINT = "https://cilogon.org/authorize";
export const CILOGON_TOKEN_ENDPOINT = "https://cilogon.org/oauth2/token";
export const CILOGON_USERINFO_ENDPOINT = "https://cilogon.org/oauth2/userinfo";
export const CILOGON_SCOPE = "openid profile email org.cilogon.userinfo";

export const OAUTH_STATE_COOKIE = "cilogon_state";
export const OAUTH_VERIFIER_COOKIE = "cilogon_verifier";

export function redirectUriFor(origin: string) {
  return `${origin}/callback`;
}
