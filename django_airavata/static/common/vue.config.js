const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  baseUrl: "/static/common/dist/",
  pages: {
    app: "./js/main.js",
    cms: "./js/cms.js"
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./dist/"
      })
    ]
  }
};
