<template>
  <div v-html="rawOutput"/>
</template>

<script>
import { models, utils } from "django-airavata-api";
export default {
  name: "html-output-display",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true
    },
    dataProducts: {
      type: Array,
      required: true
    },
    experimentId: {
      type: String,
      required: true
    },
    providerId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      rawOutput: null,
      isLoading: true,
      rawJSFile : null,
    };
  },
  methods : {
    //Attaches the script to the head, the name of the script can be passed from
    //output view provider
    loadScripts() {
      return new Promise(resolve => {

        let scriptEl = document.createElement("script");
        scriptEl.src = this.rawJSFile;
        scriptEl.type = "text/javascript";

        // Attach script to head
        document.getElementsByTagName("head")[0].appendChild(scriptEl);
        // Wait for tag to load before promise is resolved
        scriptEl.addEventListener('load',() => {
          resolve();
        });
      });
    },
  },
  created() {
    utils.FetchUtils.get("/api/html-output", {
      "experiment-id": this.experimentId,
      "experiment-output-name": this.experimentOutput.name,
      "provider-id": this.providerId
    }).then(data => {
      this.rawOutput = data.output
      this.rawJSFile = data.js
      this.isLoading = false
    });
  },
  watch: {
    isLoading() {
      if(!this.isLoading) {
        this.loadScripts();
      }
    }
  }
};
</script>

<style scoped>
</style>
