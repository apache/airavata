<template>

  <span v-if="dataProduct.downloadURL">
    <a :href="dataProduct.downloadURL" class="action-link">
      <i class="fa fa-download"></i>
      {{ filename }}
    </a>
  </span>
  <span v-else>{{ filename }}</span>
</template>

<script>
import { models } from "django-airavata-api";
export default {
  name: "data-product-viewer",
  props: {
    dataProduct: {
      type: models.DataProduct,
      required: true
    },
    inputFile: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    filename() {
      if (this.inputFile) {
        // productName captures the user provided name of the file, which may
        // not match the name of the file on the storage system (for example,
        // because of file name collision)
        return this.dataProduct.productName;
      } else {
        return this.dataProduct.filename;
      }
    }
  }
};
</script>

