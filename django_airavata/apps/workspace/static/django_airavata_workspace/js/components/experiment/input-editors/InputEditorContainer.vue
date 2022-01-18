<template>
  <input-editor-form-group
    :label="experimentInput.name"
    :label-for="inputEditorComponentId"
    :state="validationState"
    :feedback-messages="validationFeedback"
    :description="experimentInput.userFriendlyDescription"
  >
    <component
      :is="inputEditorComponentName"
      :id="inputEditorComponentId"
      :experiment-input="experimentInput"
      :experiment="experiment"
      :read-only="experimentInput.isReadOnly"
      v-model="data"
      @invalid="recordInvalidInputEditorValue"
      @valid="recordValidInputEditorValue"
      @input="valueChanged"
      @uploadstart="uploadStart"
      @uploadend="uploadEnd"
    />
  </input-editor-form-group>
</template>

<script>
import UserFileInputEditor from "./UserFileInputEditor.vue";
import AutocompleteInputEditor from "./AutocompleteInputEditor";
import CheckboxInputEditor from "./CheckboxInputEditor.vue";
import FileInputEditor from "./FileInputEditor.vue";
import InputEditorFormGroup from "./InputEditorFormGroup.vue";
import MultiFileInputEditor from "./MultiFileInputEditor.vue";
import RadioButtonInputEditor from "./RadioButtonInputEditor.vue";
import RangeSliderInputEditor from "./RangeSliderInputEditor.vue";
import SelectInputEditor from "./SelectInputEditor.vue";
import SliderInputEditor from "./SliderInputEditor.vue";
import StringInputEditor from "./StringInputEditor.vue";
import TextareaInputEditor from "./TextareaInputEditor.vue";

import { models } from "django-airavata-api";
import { mixins, utils } from "django-airavata-common-ui";

export default {
  name: "input-editor-container",
  mixins: [mixins.VModelMixin],
  props: {
    experimentInput: {
      type: models.InputDataObjectType,
      required: true,
    },
    experiment: {
      type: models.Experiment,
      required: true,
    },
  },
  components: {
    AutocompleteInputEditor,
    CheckboxInputEditor,
    FileInputEditor,
    InputEditorFormGroup,
    MultiFileInputEditor,
    RadioButtonInputEditor,
    RangeSliderInputEditor,
    SelectInputEditor,
    SliderInputEditor,
    StringInputEditor,
    TextareaInputEditor,
    UserFileInputEditor,
  },
  created() {
    if (!this.show) {
      this.handleHidingInput();
    }
  },
  data: function () {
    return {
      state: null,
      feedbackMessages: [],
      inputHasBegun: false,
      // Store the current value when hiding input so we can restore it when shown again
      oldValue: null,
      show: this.experimentInput.show,
    };
  },
  computed: {
    inputEditorComponentName: function () {
      // If input specifices an editor UI component, use that
      if (this.experimentInput.editorUIComponentId) {
        return this.experimentInput.editorUIComponentId;
      }
      // Default UI components based on input type
      if (this.experimentInput.type === models.DataType.STRING) {
        return "string-input-editor";
      } else if (this.experimentInput.type === models.DataType.URI) {
        return "file-input-editor";
      } else if (this.experimentInput.type === models.DataType.URI_COLLECTION) {
        return "multi-file-input-editor";
      }
      // Default
      return "string-input-editor";
    },
    inputEditorComponentId: function () {
      return utils.sanitizeHTMLId(this.experimentInput.name);
    },
    validationFeedback: function () {
      // Only display validation feedback after the user has provided
      // input so that missing required value errors are only displayed
      // after interacting with the input editor
      return this.inputHasBegun ? this.feedbackMessages : null;
    },
    validationState: function () {
      return this.inputHasBegun ? this.state : null;
    },
  },
  methods: {
    recordValidInputEditorValue: function () {
      this.state = true;
      this.$emit("valid");
    },
    recordInvalidInputEditorValue: function (feedbackMessages) {
      this.feedbackMessages = feedbackMessages;
      this.state = false;
      this.$emit("invalid", feedbackMessages);
    },
    valueChanged: function () {
      this.inputHasBegun = true;
    },
    handleHidingInput: function () {
      this.oldValue = this.data;
      this.data = null;
    },
    handleShowingInput: function () {
      if (this.oldValue !== null) {
        this.data = this.oldValue;
      }
    },
    uploadStart() {
      this.$emit("uploadstart");
    },
    uploadEnd() {
      this.$emit("uploadend");
    },
  },
  watch: {
    // This is a bit of a workaround for testing purposes. Watcher for
    // "experimentInput.show" does not get triggered during unit test so sync it
    // to "show" data variable and then in the unit test manipulate "show"
    // directly.
    "experimentInput.show": function (newValue) {
      this.show = newValue;
    },
    show: function (newValue, oldValue) {
      // Hiding
      if (oldValue && !newValue) {
        this.handleHidingInput();
      }
      // Showing
      else if (newValue && !oldValue) {
        this.handleShowingInput();
      }
    },
  },
};
</script>
