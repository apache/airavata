<template>
  <b-breadcrumb>
    <b-breadcrumb-item
      v-for="item in items"
      :key="item.path"
      :text="item.text"
      :active="item.active"
      @click="directorySelected(item.path)"
    />
  </b-breadcrumb>
</template>

<script>
export default {
  name: "storage-path-breadcrumb",
  props: {
    parts: {
      type: Array,
      required: true,
    },
    rootName: {
      type: String,
      default: "Home",
    },
  },
  computed: {
    items() {
      const subparts = [];
      const partsItems = this.parts.map((part, index) => {
        subparts.push(part);
        return {
          text: part,
          path: subparts.join("/"),
          active: index === this.parts.length - 1,
        };
      });
      return [
        { text: this.rootName, path: "", active: this.parts.length === 0 },
      ].concat(partsItems);
    },
  },
  methods: {
    directorySelected(path) {
      this.$emit("directory-selected", path);
    },
  },
};
</script>
