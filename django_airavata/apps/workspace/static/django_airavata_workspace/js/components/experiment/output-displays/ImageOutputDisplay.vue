<template>
  <img v-if="dataUrl" :src="dataUrl" />
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
      if (this.viewData && this.viewData.image) {
        return `data:${this.viewData["mime-type"]};base64,${this.viewData["image"]}`;
      } else {
        return null;
      }
    },
  },
};
</script>
