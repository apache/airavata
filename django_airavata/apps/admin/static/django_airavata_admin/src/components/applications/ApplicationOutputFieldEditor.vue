<template>
  <b-card>
    <div class="d-flex align-items-center" slot="header">
      <div class="mr-auto">Output Field: {{ data.name }}</div>
      <b-link
        v-if="!readonly"
        class="text-secondary"
        @click="deleteApplicationOutput"
      >
        <i class="fa fa-trash"></i>
        <span class="sr-only">Delete</span>
      </b-link>
    </div>
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
    <b-form-group label="Value" :label-for="id + '-value'">
      <b-form-input
        :id="id + '-value'"
        type="text"
        v-model="data.value"
        :disabled="readonly"
      ></b-form-input>
    </b-form-group>
    <b-form-group label="Type" :label-for="id + '-type'">
      <b-form-select
        :id="id + '-type'"
        v-model="data.type"
        :options="outputTypeOptions"
        :disabled="readonly"
      />
    </b-form-group>
    <b-form-group label="Application Argument" :label-for="id + '-argument'">
      <b-form-input
        :id="id + '-argument'"
        type="text"
        v-model="data.applicationArgument"
        :disabled="readonly"
      ></b-form-input>
    </b-form-group>
    <div class="d-flex">
      <b-form-group
        class="flex-fill"
        label="Is Required"
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
        label="Required on Command Line"
        :label-for="id + '-required-command-line'"
      >
        <b-form-radio-group
          :id="id + '-required-command-line'"
          v-model="data.requiredToAddedToCommandLine"
          :options="trueFalseOptions"
          :disabled="readonly"
        >
        </b-form-radio-group>
      </b-form-group>
    </div>
    <b-form-group
      label="Metadata"
      :label-for="id + '-metadata'"
      description="Metadata for this output, in the JSON format"
    >
      <json-editor
        :id="id + '-metadata'"
        v-model="data.metaData"
        :rows="5"
        :disabled="readonly"
      />
    </b-form-group>
    <b-button size="sm" @click="setPlainText">Plain Text</b-button>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { mixins } from "django-airavata-common-ui";
import JSONEditor from "./JSONEditor.vue";
export default {
  name: "application-output-field-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.OutputDataObjectType,
    },
    focus: {
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
    outputTypeOptions() {
      return models.OutputDataObjectType.VALID_DATA_TYPES.map((dataType) => {
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
  },
  methods: {
    doFocus() {
      this.$refs.nameInput.focus();
      this.$el.scrollIntoView({ behavior: "smooth" });
    },
    deleteApplicationOutput() {
      this.$emit("delete");
    },
    setPlainText() {
      const metadata = this.data.metaData || {};
      metadata["file-metadata"] = { "mime-type": "text/plain" };
      // Clone so that JSONEditor updates with new value
      this.data.metaData = JSON.parse(JSON.stringify(metadata));
    },
  },
  mounted() {
    if (this.focus) {
      this.doFocus();
    }
  },
};
</script>
