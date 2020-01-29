<template>
  <div>
    <data-product-viewer
      v-for="dp in dataProducts"
      :data-product="dp"
      :key="dp.productUri"
      :mime-type="fileMimeType"
    />
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "default-output-viewer",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true
    },
    dataProducts: {
      type: Array,
      required: true
    },
    data: {
      type: Object
    }
  },
  components: {
    "data-product-viewer": components.DataProductViewer
  },
  computed: {
    fileMimeType() {
      if (this.experimentOutput.fileMetadataMimeType) {
        return this.experimentOutput.fileMetadataMimeType;
      } else if (
        this.experimentOutput.type === models.DataType.STDOUT ||
        this.experimentOutput.type === models.DataType.STDERR
      ) {
        return "text/plain";
      } else {
        return null;
      }
    }
  }
};
</script>
