// Use the following to verify that the config file is loaded
// console.log("loaded babel.config.js");
const presets = [
  [
    "@babel/preset-env",
    {
      useBuiltIns: "usage",
    },
  ],
];

const plugins = ["@babel/plugin-transform-runtime"];
module.exports = {
  presets,
  plugins,
  env: {
    test: {
      presets: ["@babel/preset-env"],
    },
  },
};
