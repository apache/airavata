# Airavata documentation site

A [Docusaurus](https://docusaurus.io) site that renders the Markdown pages in [`../docs`](../docs).

The pages are not copied here. They stay in `docs/`, next to the code, because `docs/embed.go` embeds some of them into `airavata-mcp` and they also need to read well on GitHub. Edit them there.

## Run locally

```bash
cd website
npm install
npm start          # http://localhost:3000, reloads as you edit docs/*.md
```

## Build

```bash
npm run build      # static site in website/build
npm run serve      # preview the build
```

The build fails on broken links between pages, so a renamed page or heading is caught here.

## Adding a page

1. Add `docs/<name>.md`, starting with a `# Title` heading.
2. Add `'<name>'` to the right category in [`sidebars.js`](sidebars.js).

Pages are parsed as plain Markdown (CommonMark), not MDX, so text like `{{ inputs.x[0] }}` is safe. Link to other pages by file name (`[Applications](application.md)`). Link to files outside `docs/` with a full GitHub URL.
