<template>
  <b-breadcrumb :items="items"></b-breadcrumb>
</template>

<script>
export default {
  name: "user-storage-path-breadcrumb",
  props: {
    parts: {
      type: Array,
      required: true
    }
  },
  computed: {
    items() {
      const homeItem = [
        {
          text: "Home",
          to: { path: "/~/" }
        }
      ];
      if (this.parts) {
        const subparts = [];
        const partsItems = this.parts.map(part => {
          subparts.push(part);
          return {
            text: part,
            to: { path: "/~/" + subparts.join("/") + "/" }
          };
        });
        return homeItem.concat(partsItems);
      } else {
        return homeItem;
      }
    }
  }
};
</script>

