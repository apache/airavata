<template>
  <div>
    <div v-for="dp in dataProducts" :key="dp.productUri">
      <img
        v-if="dp.isImage && dp.downloadURL"
        class="image-preview rounded"
        :src="dp.downloadURL"
      />
      <data-product-viewer :data-product="dp" :mime-type="fileMimeType" />
    </div>
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
      required: true,
    },
    dataProducts: {
      type: Array,
      required: true,
    },
  },
  components: {
    "data-product-viewer": components.DataProductViewer,
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
    },
  },
};
</script>
<style scoped>
.image-preview {
  display: block;
  max-width: 100%;
  max-height: 120px;
}
</style>
