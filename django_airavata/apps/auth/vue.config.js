const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath:
    process.env.NODE_ENV === "development"
      ? "http://localhost:9000/static/django_airavata_auth/dist/"
      : "/static/django_airavata_auth/dist/",
  outputDir: "./static/django_airavata_auth/dist",
  pages: {
    "user-profile": "./static/django_airavata_auth/js/entry-user-profile",
    // additional entry points go here ...
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./static/django_airavata_auth/dist/",
      }),
    ],
  },
  devServer: {
    port: 9000,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    hot: true,
  },
};
