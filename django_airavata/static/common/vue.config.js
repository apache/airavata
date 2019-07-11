const BundleTracker = require("webpack-bundle-tracker");
const staticDir = process.env.STATIC_ROOT ? process.env.STATIC_ROOT + "common/" : "./";

module.exports = {
  publicPath: "/static/common/dist/",
  pages: {
    app: "./js/main.js",
    cms: "./js/cms.js",
    notices: "./js/notices.js"
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: staticDir + "dist/"
      })
    ]
  }
};
