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
        // Have to return a message for some browsers in order to trigger popup
        // asking user if they want to leave the page. I don't think any browser
        // displays the message that we return here, but a returned message is
        // still required.
        const msg =
          "You have unsaved changes. Are you sure that you want to leave this page?";
        // For Chrome, set event.returnValue
        event.returnValue = msg;
        return msg;
      }
    },
  },
};
</script>
