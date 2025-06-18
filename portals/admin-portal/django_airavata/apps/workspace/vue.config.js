const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath:
    process.env.NODE_ENV === "development"
      ? "http://localhost:9000/static/django_airavata_workspace/dist/"
      : "/static/django_airavata_workspace/dist/",
  outputDir: "./static/django_airavata_workspace/dist",
  pages: {
    "project-list": "./static/django_airavata_workspace/js/entry-project-list",
    dashboard: "./static/django_airavata_workspace/js/entry-dashboard",
    "create-experiment":
      "./static/django_airavata_workspace/js/entry-create-experiment",
    "view-experiment":
      "./static/django_airavata_workspace/js/entry-view-experiment",
    "experiment-list":
      "./static/django_airavata_workspace/js/entry-experiment-list",
    "edit-experiment":
      "./static/django_airavata_workspace/js/entry-edit-experiment",
    "edit-project": "./static/django_airavata_workspace/js/entry-edit-project",
    "user-storage": "./static/django_airavata_workspace/js/entry-user-storage",
  },
  css: {
    loaderOptions: {
      sass: {
        sassOptions: {
          // Turn off deprecation warnings for sass dependencies
          quietDeps: true,
        },
      },
    },
  },
  configureWebpack: {
    plugins:
      process.env.WC_MODE !== "true"
        ? [
            new BundleTracker({
              filename: "webpack-stats.json",
              path: "./static/django_airavata_workspace/dist/",
            }),
          ]
        : [],
  },
  devServer: {
    port: 9000,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
    hot: true,
  },
};
