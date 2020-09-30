<template>
  <img :src="dataUrl" />
</template>

<script>
export default {
  name: "image-output-display",
  props: {
    viewData: {
      type: Object,
      required: true,
    },
  },
  computed: {
    dataUrl() {
      if (this.viewData) {
        return `data:${this.viewData["mime-type"]};base64,${this.viewData["image"]}`;
      } else {
        return null;
      }
    },
  },
};
</script>
