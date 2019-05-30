<template>
  <b-form-group label="Groups">
    <b-form-checkbox-group
      :checked="selected"
      :options="options"
      @input="groupsUpdated"
      stacked
    ></b-form-checkbox-group>
  </b-form-group>
</template>

<script>
import { mixins } from "django-airavata-common-ui";
export default {
  name: "user-group-membership-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: Array,
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
    options() {
      const options = this.data.map(g => {
        return {
          text: g.name,
          value: g.id,
          disabled: true
        };
      });
      this.editableGroups.forEach(g => {
        const editableOption = options.find(opt => opt.value === g.id);
        if (editableOption) {
          editableOption.disabled = false;
        } else {
          options.push({
            text: g.name,
            value: g.id,
            disabled: false
          });
        }
      });
      // TODO: sort the options?
      return options;
    }
  },
  methods: {
    groupsUpdated(checkedGroups) {
      // Check for added groups
      for (const checkedGroupId of checkedGroups) {
        if (!this.data.find(g => g.id === checkedGroupId)) {
          const addedGroup = this.editableGroups.find(g => g.id === checkedGroupId);
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
    }
  }
};
</script>

