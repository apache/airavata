# Airavata Web API Client

A small Next.js app that authenticates via CILogon and provides a console for
manually invoking Airavata REST API endpoints (like a minimal Postman), using
the access token from the signed-in CILogon session.

## Setup

```bash
cd dev-tools/web-api-client
npm install
```

Configuration lives in `.env.local` (already populated locally, never
committed — see `.gitignore`). Copy `.env.local.example` if you need to
recreate it elsewhere:

```
SESSION_SECRET=<openssl rand -base64 32>
CILOGON_CLIENT_ID=<CILogon client id>
CILOGON_CLIENT_SECRET=<CILogon client secret>
```

The CILogon client must have this exact redirect URI registered:

```
http://localhost:8082/callback
```

## Run

```bash
npm run dev
```

The app serves on **http://localhost:8082**.

## How it works

Auth is a hand-rolled OAuth2 Authorization Code + PKCE flow against CILogon
(not a library), because the registered redirect URI (`/callback`, no extra
path segments) doesn't match what off-the-shelf auth libraries produce by
default.

- `src/lib/cilogon.ts` — CILogon's OAuth endpoints and scope.
- `src/app/login/route.ts` — starts the flow: generates PKCE verifier/state,
  stashes them in short-lived httpOnly cookies, redirects to
  `https://cilogon.org/authorize` with `redirect_uri=http://localhost:8082/callback`.
- `src/app/callback/route.ts` — receives `code`/`state`, verifies state,
  exchanges the code for tokens at CILogon's token endpoint, fetches the
  user's profile from the userinfo endpoint, and stores the result in a
  signed session cookie.
- `src/lib/session.ts` — signs/verifies the session cookie (JWT via `jose`)
  containing the access token, its expiry, and the user's name/email.
- `src/app/logout/route.ts` — clears the session cookie.
- `src/app/page.tsx` — sign-in gate + the console UI once authenticated.
- `src/components/ApiConsole.tsx` — lets you pick a method (GET/POST/PUT/DELETE),
  enter a target URL, edit extra headers and a JSON body, send the request,
  and view the status/headers/body of the response.
- `src/app/api/proxy/route.ts` — a server-side route that actually issues the
  outbound request. It reads the CILogon access token out of the session
  cookie and attaches it as `Authorization: Bearer <token>`, so the token
  never has to be handled by the browser-side request and CORS isn't an
  issue when calling a different-origin Airavata API server.

## Notes

- CILogon access tokens are short-lived. If a call starts failing with 401s,
  sign out and sign back in to get a fresh token (the console shows a
  warning once the token's expiry has passed).
- `npm audit` flags known Next.js advisories for 14.x; fixing all of them
  requires moving to Next 16, which is a breaking change out of scope for
  this internal tool. Since this app is meant to run locally against
  localhost-only Airavata services, this was left as-is — revisit if it's
  ever exposed beyond local dev use.
