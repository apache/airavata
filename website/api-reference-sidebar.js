// @ts-check

const fs = require('fs');
const path = require('path');

// Empty until `npm run gen-api-docs` writes the generated pages and their sidebar.
const generated = path.join(__dirname, 'api-reference', 'sidebar.ts');

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  reference: fs.existsSync(generated) ? require(generated).default : [],
};

module.exports = sidebars;
