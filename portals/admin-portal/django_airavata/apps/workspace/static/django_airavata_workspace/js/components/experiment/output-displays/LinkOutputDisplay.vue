<template>
  <a :href="viewData.url">{{ viewData.label }}</a>
</template>

<script>
export default {
  name: "link-output-display",
  props: {
    viewData: {
      type: Object,
      required: true,
    },
  },
};
</script>
