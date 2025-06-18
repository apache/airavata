// InputEditorMixin: mixin for experiment InputEditors, provides basic v-model
// and validation functionality and defines the basic props interface
// (experimentInput and id).
import { models } from "django-airavata-api";
export default {
  props: {
    value: {
      type: String,
    },
    experimentInput: {
      type: models.InputDataObjectType,
      required: true,
    },
    experiment: {
      type: models.Experiment,
      required: false,
    },
    id: {
      type: String,
      required: true,
    },
    readOnly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      data: this.value,
      inputHasBegun: false,
    };
  },
  asyncComputed: {
    validationResults: {
      get () {
        let results = this.experimentInput.validate(this.data);
        let value = []
        if ("value" in results) {
          value = Promise.all(results["value"]).then(
            arr => arr.filter(x => x !== null)
          )
        }
        return {
          "value": value
        };
      },
      default () {
        return {
          "value": []
        }
      }
    },
    validationMessages: function () {
      return "value" in this.validationResults
        ? this.validationResults["value"]
        : [];
    },
    valid: function () {
      if (this.validationMessages)
        return this.validationMessages.length === 0;
      else
        return false;
    },
    componentValidState: function () {
      if (this.inputHasBegun) {
        return this.valid;
      } else {
        return null;
      }
    },
  },
  computed: {
    editorConfig: function () {
      return this.experimentInput.editorConfig;
    },
  },
  methods: {
    valueChanged: function () {
      this.inputHasBegun = true;
      this.$emit("input", this.data);
    },
    checkValidation: function () {
      if (this.valid) {
        this.$emit("valid");
      } else {
        this.$emit("invalid", this.validationMessages);
      }
    },
  },
  created: function () {
    this.checkValidation();
  },
  watch: {
    value(newValue) {
      this.data = newValue;
    },
    valid() {
      this.checkValidation();
    },
    validationMessages() {
      this.checkValidation();
    }
  },
};
