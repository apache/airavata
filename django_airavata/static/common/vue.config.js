const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath: "/static/common/dist/",
  pages: {
    app: "./js/main.js",
    cms: "./js/cms.js",
    notices: "./js/notices.js",
  },
  configureWebpack: (config) => {
    if (process.env.LIBRARY_MODE === "true") {
      // Externalize the django-airavata-api library, will be available to
      // custom apps as global AiravataAPI object
      config.externals = config.externals || {};
      config.externals["django-airavata-api"] = {
        commonjs: "django-airavata-api",
        commonjs2: "django-airavata-api",
        root: "AiravataAPI",
      };
    } else {
      config.plugins.push(
        new BundleTracker({
          filename: "webpack-stats.json",
          path: "./dist/",
        })
      );
    }
  },
};
