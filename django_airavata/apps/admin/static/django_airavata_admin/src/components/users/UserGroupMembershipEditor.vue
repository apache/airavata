<template>
  <b-form-group label="Groups">
    <b-form-checkbox-group
      :checked="selected"
      :options="options"
      stacked
    ></b-form-checkbox-group>
  </b-form-group>
</template>

<script>
export default {
  name: "user-group-membership-editor",
  props: {
    groups: {
      type: Array,
      required: true
    },
    editableGroups: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
    }
  },
  computed: {
    selected() {
      return this.groups.map(g => g.id);
    },
    options() {
      const options = this.groups.map(g => {
        return {
          text: g.name,
          value: g.id,
          disabled: true
        }
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
          })
        }
      })
      return options;
    }
  }
};
</script>

