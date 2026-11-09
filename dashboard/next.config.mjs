import { createRequire } from "node:module";

const require = createRequire(import.meta.url);

/**
 * The dashboard is a static export served by the Go server under /dashboard/
 * (internal/server/dashboard.go), so it starts and stops with the server and talks
 * to the API on the same origin. basePath must match the prefix the server mounts.
 *
 * It builds with webpack (`next build --webpack`) rather than Turbopack: the Notebooks
 * page uses @datalayer/jupyter-react, whose JupyterLab packages need the loaders and
 * shims below, following the library's Next.js example.
 *
 * @type {import('next').NextConfig}
 */
const nextConfig = {
  output: "export",
  basePath: "/dashboard",
  trailingSlash: true,
  images: { unoptimized: true },
  transpilePackages: ["@jupyterlab/settingregistry", "@jupyterlite/settings"],
  webpack: (config, { webpack }) => {
    // JupyterLab code expects Node's Buffer as a global.
    config.resolve.fallback = { ...config.resolve.fallback, buffer: require.resolve("buffer/") };
    config.plugins.push(new webpack.ProvidePlugin({ Buffer: ["buffer", "Buffer"] }));
    config.resolve.alias = {
      ...config.resolve.alias,
      json5: require.resolve("json5/lib/index.js"),
    };
    // JupyterLab stylesheets import with a leading "~" (the old webpack convention).
    config.plugins.push(
      new webpack.NormalModuleReplacementPlugin(/^~(.*)/, (resource) => {
        resource.request = resource.request.replace(/^~/, "");
      }),
    );
    config.module.rules.push(
      { test: /\.js.map$/, type: "asset/resource" },
      // SVGs are data URIs inside CSS and raw strings inside JS (JupyterLab's icons).
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        issuer: /\.css$/,
        use: { loader: "svg-url-loader", options: { encoding: "none", limit: 10000 } },
      },
      { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, issuer: /\.js$/, type: "asset/source" },
      // The in-browser (Pyodide) kernel's service worker, wheels and settings schema.
      { resourceQuery: /text/, type: "asset/resource", generator: { filename: "[name][ext]" } },
      { test: /pypi\/.*/, type: "asset/resource", generator: { filename: "pypi/[name][ext][query]" } },
      { test: /\.whl$/, type: "asset/resource", generator: { filename: "pypi/[name][ext][query]" } },
      {
        test: /pyodide-kernel-extension\/schema\/.*/,
        type: "asset/resource",
        generator: { filename: "schema/[name][ext][query]" },
      },
    );
    return config;
  },
};

export default nextConfig;
