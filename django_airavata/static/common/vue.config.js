const BundleTracker = require("webpack-bundle-tracker");

module.exports = {
  publicPath: "/static/common/dist/",
  productionSourceMap: false,
  pages: {
    app: "./js/main.js",
    cms: "./js/cms.js",
    notices: "./js/notices.js"
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
