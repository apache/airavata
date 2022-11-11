const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath:
    process.env.NODE_ENV === "development"
      ? "http://localhost:9000/static/django_airavata_dataparsers/dist/"
      : "/static/django_airavata_dataparsers/dist/",
  outputDir: "./static/django_airavata_dataparsers/dist",
  pages: {
    "parser-details":
      "./static/django_airavata_dataparsers/js/entry-parser-details",
    "parser-list":
      "./static/django_airavata_dataparsers/js/parser-listing-entry-point.js",
    "parser-edit":
      "./static/django_airavata_dataparsers/js/parser-edit-entry-point.js",
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./static/django_airavata_dataparsers/dist/",
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
