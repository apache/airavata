<template>
  <vue-slider
    v-model="sliderValues"
    @change="onChange"
    :state="componentValidState"
    :disabled="readOnly"
    :min="min"
    :max="max"
    :interval="step"
    tooltip="always"
    :tooltip-formatter="tooltipFormatter"
    :enable-cross="false"
  />
</template>

<script>
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import VueSlider from "vue-slider-component";

export default {
  name: "range-slider-input-editor",
  mixins: [InputEditorMixin],
  props: {
    value: {
      type: String,
    },
  },
  components: {
    VueSlider,
  },
  data() {
    return {
      sliderValues: null,
    };
  },
  created() {
    // computed properties are only available at *created* step of lifecycle
    this.sliderValues = this.parseValue(this.value);
  },
  computed: {
    min: function () {
      return "min" in this.editorConfig ? this.editorConfig.min : 0;
    },
    max: function () {
      return "max" in this.editorConfig ? this.editorConfig.max : 100;
    },
    step: function () {
      return "step" in this.editorConfig ? this.editorConfig.step : 1;
    },
    delimiter() {
      return "delimiter" in this.editorConfig
        ? this.editorConfig.delimiter
        : "-";
    },
  },
  methods: {
    parseValue(value) {
      // Just remove any percentage signs
      const result = value
        .replaceAll("%", "")
        .split(this.delimiter)
        .map(parseFloat);
      return result.length === 2 && !isNaN(result[0]) && !isNaN(result[1])
        ? result
        : [this.min, this.max];
    },
    onChange(value) {
      this.data = this.formatValue(value);
      this.valueChanged();
    },
    tooltipFormatter(value) {
      if ("displayFormat" in this.editorConfig) {
        if (this.editorConfig.displayFormat.percentage) {
          return `${value}%`;
        }
      }
      return value;
    },
    formatValue(value) {
      let values = value.map(String);
      if ("valueFormat" in this.editorConfig) {
        if (this.editorConfig.valueFormat.percentage) {
          values = values.map((v) => `${v}%`);
        }
      }
      return values.join(this.delimiter);
    },
  },
  watch: {
    data(newValue) {
      this.sliderValues = this.parseValue(newValue);
    },
  },
};
</script>
