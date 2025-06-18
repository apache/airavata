<template>
  <b-form-select
    :id="id"
    v-model="data"
    :options="selectOptions"
    stacked
    :disabled="readOnly"
    :state="componentValidState"
    @input="valueChanged"
  />
</template>

<script>
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";

const CONFIG_OPTION_TEXT_KEY = "text";
const CONFIG_OPTION_VALUE_KEY = "value";

export default {
  name: "select-input-editor",
  mixins: [InputEditorMixin],
  props: {
    value: {
      type: String,
    },
    options: {
      type: Array,
    },
  },
  computed: {
    selectOptions: function () {
      const options = this.options || this.editorConfig.options || [];
      return options.map((option) => {
        return {
          text: option[CONFIG_OPTION_TEXT_KEY],
          value: option[CONFIG_OPTION_VALUE_KEY],
        };
      });
    },
  },
};
</script>
