<script>
import * as linkify from "linkifyjs/";

var getChildrenTextContent = function(children) {
  return children
    .map(function(node) {
      return node.children ? getChildrenTextContent(node.children) : node.text;
    })
    .join("");
};

export default {
  name: "linkify",

  render: function(createElement) {
    // Parse the contents of the element for links and turn them into links
    const tokens = linkify.tokenize(
      getChildrenTextContent(this.$slots.default)
    );
    const children = tokens.map(t => {
      if (t.isLink) {
        return createElement(
          "a",
          { attrs: { href: t.toHref("https"), target: "_blank" } },
          t.toString()
        );
      } else {
        return t.toString();
      }
    });
    return createElement("span", null, children);
  }
};
</script>
