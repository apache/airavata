<script>
import * as linkify from "linkifyjs";

export default {
  name: "linkify",

  render: function (createElement) {
    // Find top level text nodes and run linkify on the text, converting them
    // into an array of links and text nodes
    const children = this.$slots.default
      .map((node) => {
        if (node.text) {
          const tokens = linkify.tokenize(node.text);
          return tokens.map((t) => {
            if (t.isLink) {
              return createElement(
                "a",
                {
                  attrs: { href: t.toHref("https"), target: "_blank" },
                  on: {
                    click: this.clickHandler,
                  },
                },
                t.toString()
              );
            } else {
              return t.toString();
            }
          });
        } else {
          return node;
        }
      })
      // Flatten array since text nodes are mapped to arrays
      .flat();
    return createElement("span", null, children);
  },
  methods: {
    clickHandler(e) {
      // stop click event from bubbling up
      e.stopPropagation();
    },
  },
};
</script>
