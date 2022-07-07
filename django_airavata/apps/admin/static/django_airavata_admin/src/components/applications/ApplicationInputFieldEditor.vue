<template>
  <b-card>
    <div class="d-flex align-items-center" slot="header">
      <div v-if="!readonly" class="drag-handle mr-1 text-muted">
        <i class="fa fa-grip-vertical"></i>
        <span class="sr-only">Drag handle for reordering</span>
      </div>
      <div class="mr-auto">Input Field: {{ data.name }}</div>
      <b-link
        v-if="!readonly"
        class="text-secondary"
        @click="deleteApplicationInput"
      >
        <i class="fa fa-trash"></i>
        <span class="sr-only">Delete</span>
      </b-link>
    </div>
    <b-collapse :id="id + '-collapse'" :visible="!collapse">
      <b-form-group label="Name" :label-for="id + '-name'">
        <b-form-input
          :id="id + '-name'"
          type="text"
          v-model="data.name"
          ref="nameInput"
          required
          :disabled="readonly"
        ></b-form-input>
      </b-form-group>
      <b-form-group label="Type" :label-for="id + '-type'">
        <b-form-select
          :id="id + '-type'"
          v-model="data.type"
          :options="inputTypeOptions"
          :disabled="readonly"
        />
      </b-form-group>
      <b-form-group
        label="Initial Value"
        :label-for="id + '-value'"
        v-if="showValueField"
      >
        <b-form-input
          :id="id + '-value'"
          type="text"
          v-model="data.value"
          :disabled="readonly"
        ></b-form-input>
      </b-form-group>
      <b-form-group
        label="Override Filename"
        :label-for="id + '-value'"
        v-if="showOverrideFilenameField"
      >
        <b-form-input
          :id="id + '-override-filename'"
          type="text"
          v-model="data.overrideFilename"
          :disabled="readonly"
        ></b-form-input>
      </b-form-group>
      <b-form-group label="Application Argument" :label-for="id + '-argument'">
        <b-form-input
          :id="id + '-argument'"
          type="text"
          v-model="data.applicationArgument"
          :disabled="readonly"
        ></b-form-input>
      </b-form-group>
      <b-form-group
        class="flex-fill"
        label="Required on Command Line"
        :label-for="id + '-required-command-line'"
        description="Add this input's value to the command line in the generated job script."
      >
        <b-form-radio-group
          :id="id + '-required-command-line'"
          v-model="data.requiredToAddedToCommandLine"
          :options="trueFalseOptions"
          :disabled="readonly"
        >
        </b-form-radio-group>
      </b-form-group>
      <div class="d-flex">
        <b-form-group
          class="flex-fill"
          label="Required"
          :label-for="id + '-required'"
        >
          <b-form-radio-group
            :id="id + '-required'"
            v-model="data.isRequired"
            :options="trueFalseOptions"
            :disabled="readonly"
          >
          </b-form-radio-group>
        </b-form-group>
        <b-form-group
          class="flex-fill"
          label="Read Only"
          :label-for="id + '-read-only'"
        >
          <b-form-radio-group
            :id="id + '-read-only'"
            v-model="data.isReadOnly"
            :options="trueFalseOptions"
            :disabled="readonly"
          >
          </b-form-radio-group>
        </b-form-group>
      </div>
      <b-form-group
        label="User Friendly Description"
        :label-for="id + '-user-friendly-description'"
      >
        <b-form-textarea
          :id="id + '-user-friendly-description'"
          v-model="data.userFriendlyDescription"
          :rows="3"
          :disabled="readonly"
        />
      </b-form-group>
      <b-form-group
        label="Advanced Input Field Modification Metadata"
        :label-for="id + '-metadata'"
        description="Metadata for this input, in the JSON format"
      >
        <json-editor
          :id="id + '-metadata'"
          v-model="data.metaData"
          :rows="5"
          :disabled="readonly"
        />
      </b-form-group>
    </b-collapse>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";
import JSONEditor from "./JSONEditor.vue";

export default {
  name: "application-input-field-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.InputDataObjectType,
    },
    // Whether to put focus on the name field when mounting component
    focus: {
      type: Boolean,
    },
    collapse: {
      type: Boolean,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    "json-editor": JSONEditor,
  },
  computed: {
    inputTypeOptions() {
      return models.InputDataObjectType.VALID_DATA_TYPES.map((dataType) => {
        return {
          value: dataType,
          text: dataType.name,
        };
      });
    },
    trueFalseOptions() {
      return [
        { text: "True", value: true },
        { text: "False", value: false },
      ];
    },
    id() {
      return "id-" + this.data.key;
    },
    showValueField() {
      return this.data.type.isSimpleValueType;
    },
    showOverrideFilenameField() {
      return this.data.type === models.DataType.URI;
    },
  },
  methods: {
    doFocus() {
      this.$refs.nameInput.focus();
      this.$el.scrollIntoView({ behavior: "smooth" });
    },
    deleteApplicationInput() {
      this.$emit("delete");
    },
  },
  mounted() {
    if (this.focus) {
      this.doFocus();
    }
  },
};
</script>

<style scoped>
.drag-handle {
  cursor: move;
}
</style>
