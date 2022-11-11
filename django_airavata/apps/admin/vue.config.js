const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath:
    process.env.NODE_ENV === "development"
      ? "http://localhost:9000/static/django_airavata_admin/dist/"
      : "/static/django_airavata_admin/dist/",
  outputDir: "./static/django_airavata_admin/dist",
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./static/django_airavata_admin/dist/",
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
