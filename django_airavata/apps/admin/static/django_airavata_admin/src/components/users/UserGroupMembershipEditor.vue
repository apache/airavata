<template>
  <b-form-group label="Groups">
    <b-form-checkbox-group
      :checked="selected"
      :options="userDefinedGroupOptions"
      @input="groupsUpdated"
      stacked
    >
      <template slot="first">
        <b-form-checkbox
          v-if="gatewayUsersGroupOption"
          :value="gatewayUsersGroupOption.value"
          :disabled="gatewayUsersGroupOption.disabled"
        >{{ gatewayUsersGroupOption.text }} <b-badge>Default</b-badge>
        </b-form-checkbox>
        <b-form-checkbox
          v-if="adminsGroupOption"
          :value="adminsGroupOption.value"
          :disabled="adminsGroupOption.disabled"
        >{{ adminsGroupOption.text }} <b-badge>Admins</b-badge>
        </b-form-checkbox>
        <b-form-checkbox
          v-if="readOnlyAdminsGroupOption"
          :value="readOnlyAdminsGroupOption.value"
          :disabled="readOnlyAdminsGroupOption.disabled"
        >{{ readOnlyAdminsGroupOption.text }} <b-badge>Read Only Admins</b-badge>
        </b-form-checkbox>
      </template>
    </b-form-checkbox-group>
  </b-form-group>
</template>

<script>
import { utils } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";
export default {
  name: "user-group-membership-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: Array,
      required: true
    },
    airavataInternalUserId: {
      type: String,
      required: true
    },
    editableGroups: {
      type: Array,
      required: true
    }
  },
  computed: {
    selected() {
      return this.data.map(g => g.id);
    },
    combinedGroups() {
      const groups = {};
      this.value.concat(this.editableGroups).forEach(g => {
        groups[g.id] = g;
      });
      return Object.values(groups);
    },
    userDefinedGroups() {
      return this.combinedGroups
        ? this.combinedGroups.filter(g => {
            return (
              !g.isDefaultGatewayUsersGroup &&
              !g.isGatewayAdminsGroup &&
              !g.isReadOnlyGatewayAdminsGroup
            );
          })
        : [];
    },
    userDefinedGroupOptions() {
      const options = this.userDefinedGroups.map(g =>
        this.createGroupOption(g)
      );
      return utils.StringUtils.sortIgnoreCase(options, o => o.text);
    },
    gatewayUsersGroupOption() {
      const group = this.combinedGroups.find(g => g.isDefaultGatewayUsersGroup);
      return group ? this.createGroupOption(group) : null;
    },
    adminsGroupOption() {
      const group = this.combinedGroups.find(g => g.isGatewayAdminsGroup);
      return group ? this.createGroupOption(group) : null;
    },
    readOnlyAdminsGroupOption() {
      const group = this.combinedGroups.find(
        g => g.isReadOnlyGatewayAdminsGroup
      );
      return group ? this.createGroupOption(group) : null;
    }
  },
  methods: {
    groupsUpdated(checkedGroups) {
      // Check for added groups
      for (const checkedGroupId of checkedGroups) {
        if (!this.data.find(g => g.id === checkedGroupId)) {
          const addedGroup = this.editableGroups.find(
            g => g.id === checkedGroupId
          );
          this.data.push(addedGroup);
        }
      }
      // Check for removed groups
      for (const group of this.data) {
        if (!checkedGroups.find(groupId => groupId === group.id)) {
          const groupIndex = this.data.findIndex(g => g.id === group.id);
          this.data.splice(groupIndex, 1);
        }
      }
    },
    createGroupOption(group) {
      return {
        text: group.name,
        value: group.id,
        disabled: !group.userHasWriteAccess || group.ownerId === this.airavataInternalUserId
      };
    }
  }
};
</script>

