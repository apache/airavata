# Airavata Dashboard

A web UI over the Airavata API. Build it and the API server serves it under
`/dashboard/` — see [INSTALL.md](../INSTALL.md#dashboard).

```bash
npm ci
npm run build       # static export in out/, served by the Go server
npm run typecheck
```

For development with hot reload, run the API server, then:

```bash
NEXT_PUBLIC_AIRAVATA_API_URL=http://localhost:9095 npm run dev
# http://localhost:3000/dashboard/
```

Under `npm run dev` the page and the API are on different origins, so the
interactive-auth WebSocket fails its origin check and the dashboard falls back to
polling — the sidebar shows "Polling" instead of "Live". The built dashboard is
same-origin and uses the socket.

## Layout

```
src/app/             one route per page; every page is a client component
src/components/      shared UI (Shell, forms, tables, the interactive-auth dialog)
src/lib/api.ts       fetch wrapper: bearer token, error envelope, download/upload
src/lib/session.tsx  the signed-in token and whether it is an admin
src/lib/interactive-auth.tsx
                     subscription to waiting SSH logins (socket, polling fallback)
src/lib/types.ts     wire shapes, mirroring api/*/dto
src/components/NotebookView.tsx
                     the Notebooks page: a @datalayer/jupyter-react notebook on an
                     in-browser Pyodide kernel, autosaved to local storage
```

The build uses webpack (`next build --webpack`), not Turbopack: the JupyterLab packages
behind the Notebooks page need the loaders and shims in `next.config.mjs`. They are only
loaded on that page. `package.json` overrides `react` and `react-dom` because
jupyter-react pins its own versions, and two copies of React break hooks.

The app is a static export (`output: "export"`), so it has no server code of its
own: routes with an id take it as a query parameter (`/processes/view/?id=…`), and
the bearer token is held in the browser's local storage. A browser `WebSocket`
cannot send an `Authorization` header, so the token is offered as a subprotocol
instead; the server reads it in `internal/auth/middleware.go`.
