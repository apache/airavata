<template>
  <span>
    <gateway-groups-badge v-if="adminsGroup" :group="adminsGroup" />
    <gateway-groups-badge
      v-else-if="readOnlyAdminsGroup"
      :group="readOnlyAdminsGroup"
    />
    <gateway-groups-badge
      v-else-if="defaultUsersGroup"
      :group="defaultUsersGroup"
    />
    <b-badge v-for="group in nonGatewayGroups" :key="group.id">{{
      group.name
    }}</b-badge>
  </span>
</template>

<script>
import { components } from "django-airavata-common-ui";
export default {
  name: "group-membership-display",
  props: {
    groups: {
      type: Array,
      required: true,
    },
  },
  components: {
    "gateway-groups-badge": components.GatewayGroupsBadge,
  },
  computed: {
    adminsGroup() {
      return this.groups.find((g) => g.isGatewayAdminsGroup);
    },
    readOnlyAdminsGroup() {
      return this.groups.find((g) => g.isReadOnlyGatewayAdminsGroup);
    },
    defaultUsersGroup() {
      return this.groups.find((g) => g.isDefaultGatewayUsersGroup);
    },
    nonGatewayGroups() {
      return this.groups.filter((g) => {
        return (
          !g.isGatewayAdminsGroup &&
          !g.isReadOnlyGatewayAdminsGroup &&
          !g.isDefaultGatewayUsersGroup
        );
      });
    },
    nonGatewayGroupNames() {
      return this.nonGatewayGroups.map((g) => g.name).join(", ");
    },
  },
};
</script>
