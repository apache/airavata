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
      rawOutput: null
    };
  },
  created() {
    utils.FetchUtils.get("/api/html-output", {
      "experiment-id": this.experimentId,
      "experiment-output-name": this.experimentOutput.name,
      "provider-id": this.providerId
    }).then(data => {
      this.rawOutput = data.output
    })
  }
};
</script>

<style scoped>
</style>
