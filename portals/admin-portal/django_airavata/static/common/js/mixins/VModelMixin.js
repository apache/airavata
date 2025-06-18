import { models } from "django-airavata-api";

export default {
  watch: {
    data: {
      handler: function (newValue, oldValue) {
        // Only emit 'input' for objects when one of their deep properties has
        // changed to prevent infinite loop since 'data' is recloned whenever
        // 'value' changes
        if (typeof this.value === "object" && newValue === oldValue) {
          this.$emit("input", newValue);
        } else if (
          (this.value === null || typeof this.value !== "object") &&
          newValue !== oldValue
        ) {
          this.$emit("input", newValue);
        }
      },
      deep: true,
    },
    value: {
      handler: function (newValue) {
        this.data = this.copyValue(newValue);
      },
      deep: true,
    },
  },
  methods: {
    copyValue(value) {
      if (value instanceof Array) {
        return value.map((item) => this.copyValue(item));
      } else {
        if (value === null) {
          return null;
        } else if (value instanceof models.BaseModel) {
          return value.clone();
        } else if (typeof value === "object") {
          return JSON.parse(JSON.stringify(value));
        } else {
          // Must be number, boolean or string
          return value;
        }
      }
    },
  },
  data: function () {
    return {
      data: this.copyValue(this.value),
    };
  },
  props: {
    value: {
      required: true,
    },
  },
};
