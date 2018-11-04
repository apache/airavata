const BundleTracker = require("webpack-bundle-tracker");
const path = require("path");

module.exports = {
  baseUrl: "/static/django_airavata_admin/dist/",
  outputDir: "./static/django_airavata_admin/dist",
  css: {
    extract: true,
    loaderOptions: {
      postcss: {
        config: {
          path: __dirname
        }
      }
    }
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./static/django_airavata_admin/dist/"
      })
    ]
  },
  chainWebpack: config => {
    /*
     * Specify the eslint config file otherwise it complains of a missing
     * config file for the ../api and ../../static/common packages
     *
     * See: https://github.com/vuejs/vue-cli/issues/2539#issuecomment-422295246
     */
    config.module
      .rule("eslint")
      .use("eslint-loader")
      .tap(options => {
        options.configFile = path.resolve(__dirname, "package.json");
        return options;
      });
  }
};
