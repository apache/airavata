<template>
  <vue-slider
    v-model="sliderValues"
    @change="onChange"
    :state="componentValidState"
    :disabled="readOnly"
    :min="sliderMin"
    :max="sliderMax"
    :interval="sliderStep"
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
    min: Number,
    max: Number,
    step: Number,
    valueFormat: {
      type: String,
      validator(value) {
        return ["percentage"].indexOf(value) !== -1;
      },
    },
    displayFormat: {
      type: String,
      validator(value) {
        return ["percentage"].indexOf(value) !== -1;
      },
    },
    delimiter: String,
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
    this.initializeSliderValues();
  },
  computed: {
    sliderMin: function () {
      return typeof this.min !== "undefined"
        ? this.min
        : "min" in this.editorConfig
        ? this.editorConfig.min
        : 0;
    },
    sliderMax: function () {
      return typeof this.max !== "undefined"
        ? this.max
        : "max" in this.editorConfig
        ? this.editorConfig.max
        : 100;
    },
    sliderStep: function () {
      return typeof this.step !== "undefined"
        ? this.step
        : "step" in this.editorConfig
        ? this.editorConfig.step
        : 1;
    },
    sliderDelimiter() {
      return this.delimiter
        ? this.delimiter
        : "delimiter" in this.editorConfig
        ? this.editorConfig.delimiter
        : "-";
    },
  },
  methods: {
    initializeSliderValues() {
      this.sliderValues = this.parseValue(this.data);
      // If parsing the value resulted in it changing (failed to parse so
      // initialized to ['sliderMin', 'sliderMax']), update the value
      if (this.data !== this.formatValue(this.sliderValues)) {
        this.onChange(this.sliderValues);
      }
    },
    parseValue(value) {
      // Just remove any percentage signs
      const result = value
        ? value.replaceAll("%", "").split(this.sliderDelimiter).map(parseFloat)
        : [];
      return result.length === 2 && !isNaN(result[0]) && !isNaN(result[1])
        ? result
        : [this.sliderMin, this.sliderMax];
    },
    onChange(value) {
      this.data = this.formatValue(value);
      this.valueChanged();
    },
    tooltipFormatter(value) {
      if (this.displayFormat) {
        if (this.displayFormat === "percentage") {
          return `${value}%`;
        }
      } else if ("displayFormat" in this.editorConfig) {
        if (this.editorConfig.displayFormat.percentage) {
          return `${value}%`;
        }
      }
      return value;
    },
    formatValue(value) {
      let values = value.map(String);
      if (this.valueFormat) {
        if (this.valueFormat === "percentage") {
          values = values.map((v) => `${v}%`);
        }
      } else if ("valueFormat" in this.editorConfig) {
        if (this.editorConfig.valueFormat.percentage) {
          values = values.map((v) => `${v}%`);
        }
      }
      return values.join(this.sliderDelimiter);
    },
  },
  watch: {
    data() {
      this.initializeSliderValues();
    },
  },
};
</script>
