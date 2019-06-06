<template>
  <span :class="{'font-italic': notAvailable}">{{ applicationName }}</span>
</template>
<script>
import { services } from "django-airavata-api";
export default {
  name: "application-name",
  props: {
    applicationInterfaceId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      applicationInterface: null,
      notAvailable: false
    };
  },
  created() {
    services.ApplicationInterfaceService.retrieve(
      { lookup: this.applicationInterfaceId },
      { ignoreErrors: true, cache: true }
    )
      .then(appInterface => (this.applicationInterface = appInterface))
      .catch(() => (this.notAvailable = true));
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
    }
  }
};
</script>
