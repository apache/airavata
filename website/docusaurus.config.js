// @ts-check
// Docusaurus site for the Airavata documentation.
//
// The pages themselves live in ../docs, next to the code, so they stay the single
// source: docs/embed.go embeds some of them into airavata-mcp, and they read fine on
// GitHub. This site only renders them.

const { themes: prismThemes } = require('prism-react-renderer');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Apache Airavata',
  tagline: 'Run research applications on the clusters and data you already have',

  url: 'https://airavata.apache.org',
  baseUrl: '/',

  organizationName: 'apache',
  projectName: 'airavata',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: {
    // The pages are plain Markdown written for GitHub, and they contain text MDX would
    // try to evaluate (Jinja placeholders such as {{ inputs.x[0] }}). Parse .md files as
    // CommonMark and keep MDX for .mdx only.
    format: 'detect',
    hooks: {
      onBrokenMarkdownLinks: 'throw',
    },
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: '../docs',
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          // ../docs also holds scripts and Go sources; only the pages are rendered.
          include: ['*.md'],
          editUrl: 'https://github.com/apache/airavata/edit/master/docs/',
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: 'Apache Airavata',
        items: [
          { type: 'docSidebar', sidebarId: 'docs', position: 'left', label: 'Docs' },
          { to: '/api', label: 'API', position: 'left' },
          { href: 'https://github.com/apache/airavata', label: 'GitHub', position: 'right' },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              { label: 'Introduction', to: '/' },
              { label: 'Applications', to: '/application' },
              { label: 'API reference', to: '/api' },
            ],
          },
          {
            title: 'Project',
            items: [
              { label: 'GitHub', href: 'https://github.com/apache/airavata' },
              { label: 'Install', href: 'https://github.com/apache/airavata/blob/master/INSTALL.md' },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} The Apache Software Foundation. Licensed under the Apache License, Version 2.0.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['bash', 'json', 'sql', 'yaml'],
      },
    }),
};

module.exports = config;
