<template>
  <div>
    <data-product-viewer v-for="dp in dataProducts"
      :data-product="dp" :key="dp.productUri"/>
  </div>
</template>

<script>
import { models } from "django-airavata-api"

export default {
  name: "download-output-viewer",
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
  }
}
</script>


