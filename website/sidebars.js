// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    'intro',
    {
      type: 'category',
      label: 'Concepts',
      collapsed: false,
      items: ['application', 'data-product', 'virtual-data', 'batch-clusters', 'notebooks'],
    },
    {
      type: 'category',
      label: 'Security',
      collapsed: false,
      items: ['auth', 'interactive-auth', 'sharing', 'audit'],
    },
    {
      type: 'category',
      label: 'Tools',
      collapsed: false,
      items: ['mcp'],
    },
    {
      type: 'category',
      label: 'Reference',
      collapsed: false,
      items: ['api'],
    },
  ],
};

module.exports = sidebars;
