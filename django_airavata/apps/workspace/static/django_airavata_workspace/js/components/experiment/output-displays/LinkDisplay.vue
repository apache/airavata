<template>
  <a :href="data.url">{{ data.label }}</a>
</template>

<script>
import { models } from "django-airavata-api"

export default {
  name: "link-viewer",
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
}
</script>


