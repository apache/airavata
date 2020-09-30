<template>
  <span :class="{ 'font-italic': notAvailable }">{{ name }}</span>
</template>
<script>
import { services } from "django-airavata-api";
export default {
  name: "compute-resource-name",
  props: {
    computeResourceId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      computeResource: null,
      notAvailable: false,
    };
  },
  created() {
    this.loadComputeResource();
  },
  methods: {
    loadComputeResource() {
      services.ComputeResourceService.retrieve(
        { lookup: this.computeResourceId },
        { ignoreErrors: true, cache: true }
      )
        .then((computeResource) => (this.computeResource = computeResource))
        .catch(() => (this.notAvailable = true));
    },
  },
  computed: {
    name() {
      if (this.notAvailable) {
        return "N/A";
      } else {
        return this.computeResource ? this.computeResource.hostName : "";
      }
    },
  },
  watch: {
    computeResourceId() {
      this.loadComputeResource();
    },
  },
};
</script>
