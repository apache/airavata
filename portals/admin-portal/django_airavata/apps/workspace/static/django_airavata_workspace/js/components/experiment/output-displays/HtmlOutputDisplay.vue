<template>
  <div v-html="rawOutput" />
</template>

<script>
export default {
  name: "html-output-display",
  props: {
    viewData: {
      type: Object,
      required: true,
    },
  },
  computed: {
    rawOutput() {
      return this.viewData && this.viewData.output
        ? this.viewData.output
        : null;
    },
    rawJSFile() {
      return this.viewData && this.viewData.js ? this.viewData.js : null;
    },
  },
  methods: {
    //Attaches the script to the head, the name of the script can be passed from
    //output view provider
    loadScripts() {
      return new Promise((resolve) => {
        let scriptEl = document.createElement("script");
        scriptEl.src = this.rawJSFile;
        scriptEl.type = "text/javascript";

        // Attach script to head
        document.getElementsByTagName("head")[0].appendChild(scriptEl);
        // Wait for tag to load before promise is resolved
        scriptEl.addEventListener("load", () => {
          resolve();
        });
      });
    },
  },
  watch: {
    rawJSFile() {
      // TODO: check if script is already loaded
      if (this.rawJSFile) {
        this.loadScripts();
      }
    },
  },
};
</script>
