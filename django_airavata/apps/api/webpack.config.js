const path = require("path");

module.exports = {
  entry: "./static/django_airavata_api/js/index.js",
  output: {
    path: path.resolve(__dirname, "static/django_airavata_api/dist/"),
    filename: "airavata-api.js",
    library: "AiravataAPI",
  },
  mode: "production",
};
