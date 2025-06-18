<template>
  <b-badge :variant="badgeVariant">{{ statusName }}</b-badge>
</template>

<script>
import { models } from "django-airavata-api";

export default {
  name: "experiment-status-badge",
  props: {
    statusName: {
      type: String,
      required: true,
    },
  },
  computed: {
    experimentState: function () {
      return models.ExperimentState.byName(this.statusName);
    },
    badgeVariant: function () {
      if (this.experimentState.isProgressing) {
        return "secondary";
      } else if (this.experimentState === models.ExperimentState.COMPLETED) {
        return "success";
      } else if (
        this.experimentState === models.ExperimentState.CANCELING ||
        this.experimentState === models.ExperimentState.CANCELED
      ) {
        return "warning";
      } else if (this.experimentState === models.ExperimentState.FAILED) {
        return "danger";
      } else {
        return "info";
      }
    },
  },
};
</script>
