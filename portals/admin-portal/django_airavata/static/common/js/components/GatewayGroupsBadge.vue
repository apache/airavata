<template>
  <b-badge :variant="variant">{{ name }}</b-badge>
</template>

<script>
import { models } from "django-airavata-api";

export default {
  name: "gateway-groups-badge",
  props: {
    group: {
      type: models.Group,
      required: true,
    },
  },
  computed: {
    variant() {
      if (this.group.isGatewayAdminsGroup) {
        return "danger";
      } else if (this.group.isReadOnlyGatewayAdminsGroup) {
        return "warning";
      } else if (this.group.isDefaultGatewayUsersGroup) {
        return "primary";
      } else {
        return "secondary";
      }
    },
    name() {
      if (this.group.isGatewayAdminsGroup) {
        return "Admins";
      } else if (this.group.isReadOnlyGatewayAdminsGroup) {
        return "Read Only Admins";
      } else if (this.group.isDefaultGatewayUsersGroup) {
        return "Default";
      } else {
        return this.group.name;
      }
    },
  },
};
</script>
