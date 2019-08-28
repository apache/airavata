<template>
  <img :src="url" />
</template>

<script>
import { models } from "django-airavata-api";
export default {
  name: "image-output-display",
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
  computed: {
    url() {
      return (
        "/api/image-output?" +
        "experiment-id=" +
        encodeURIComponent(this.experimentId) +
        "&experiment-output-name=" +
        encodeURIComponent(this.experimentOutput.name) +
        "&provider-id=" +
        encodeURIComponent(this.providerId)
      );
    }
  }
};
</script>
