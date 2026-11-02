/**
 * The dashboard is a static export served by the Go server under /dashboard/
 * (internal/server/dashboard.go), so it starts and stops with the server and talks
 * to the API on the same origin. basePath must match the prefix the server mounts.
 *
 * @type {import('next').NextConfig}
 */
const nextConfig = {
  output: "export",
  basePath: "/dashboard",
  trailingSlash: true,
  images: { unoptimized: true },
  // Pin the workspace root so a lockfile elsewhere up the tree is not mistaken for it.
  turbopack: { root: import.meta.dirname },
};

export default nextConfig;
