<template>
  <span :class="{ 'font-italic': notAvailable }">{{ applicationName }}</span>
</template>
<script>
import { errors, services, utils } from "django-airavata-api";
export default {
  name: "application-name",
  props: {
    applicationInterfaceId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      applicationInterface: null,
      notAvailable: false,
    };
  },
  created() {
    this.loadApplicationInterface();
  },
  methods: {
    loadApplicationInterface() {
      services.ApplicationInterfaceService.retrieve(
        { lookup: this.applicationInterfaceId },
        { ignoreErrors: true, cache: true }
      )
        .then((appInterface) => (this.applicationInterface = appInterface))
        .catch((error) => {
          if (errors.ErrorUtils.isNotFoundError(error)) {
            this.notAvailable = true;
          } else {
            throw error;
          }
        })
        .catch(utils.FetchUtils.reportError);
    },
  },
  computed: {
    applicationName() {
      if (this.notAvailable) {
        return "N/A";
      } else {
        return this.applicationInterface
          ? this.applicationInterface.applicationName
          : "";
      }
    },
  },
  watch: {
    applicationInterfaceId() {
      this.loadApplicationInterface();
    },
  },
};
</script>
