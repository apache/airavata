var path = require('path')
var webpack = require('webpack')

module.exports = {
    entry: [
        './js/index.js',
    ],
    output: {
        path: path.resolve(__dirname, './dist/'),
        filename: 'index.js',
        libraryTarget: 'commonjs'
    },
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    loaders: {},
                    // // other vue-loader options go here
                    extractCSS: true
                }
            },
            {
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            },
        ]
    },
    externals: ['django-airavata-api'],
}

if (process.env.NODE_ENV === 'production') {
    module.exports.devtool = '#source-map'
    // http://vue-loader.vuejs.org/en/workflow/production.html
    module.exports.plugins = (module.exports.plugins || []).concat([
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: '"production"'
            }
        }),
        new webpack.optimize.UglifyJsPlugin({
            sourceMap: true,
            compress: {
                warnings: false
            }
        }),
        new webpack.LoaderOptionsPlugin({
            minimize: true
        })
    ])
}
