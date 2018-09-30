const presets = [
  [
    "@babel/env",
    {
      useBuiltIns: "usage"
    }
  ]
];

const plugins = ["@babel/plugin-transform-runtime"];
module.exports = { presets, plugins };
