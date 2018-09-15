const BundleTracker = require('webpack-bundle-tracker')

module.exports = {
    outputDir: './static/django_airavata_admin/dist',
    assetsDir: 'static',
    filenameHashing: false,
    css: {
        extract: true
    },
    configureWebpack: {
      plugins: [
        new BundleTracker({
          filename: 'webpack-stats.json',
          path: './static/django_airavata_admin/dist/'
        }),
      ]
    }
}
