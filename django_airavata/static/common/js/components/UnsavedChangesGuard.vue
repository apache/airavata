<template>
  <div></div>
</template>

<script>
export default {
  name: "unsaved-changes-guard",
  props: {
    dirty: {
      type: Boolean,
      default: false,
    },
  },
  mounted() {
    window.addEventListener("beforeunload", this.onBeforeUnload);
  },
  destroyed() {
    window.removeEventListener("beforeunload", this.onBeforeUnload);
  },
  methods: {
    onBeforeUnload(event) {
      if (this.dirty) {
        event.preventDefault();
        return "You have unsaved changes. Are you sure that you want to leave this page?";
      }
    },
  },
};
</script>
