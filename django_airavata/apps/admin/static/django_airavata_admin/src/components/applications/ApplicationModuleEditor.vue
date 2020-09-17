<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Application Details</h1>
        <b-form-group
          label="Application Name"
          label-for="application-name"
          :invalid-feedback="validationFeedback.appModuleName.invalidFeedback"
          :state="validationFeedback.appModuleName.state"
        >
          <b-form-input
            id="application-name"
            type="text"
            v-model="data.appModuleName"
            required
            :disabled="readonly"
            :state="validationFeedback.appModuleName.state"
          ></b-form-input>
        </b-form-group>
        <b-form-group
          label="Application Version"
          label-for="application-version"
        >
          <b-form-input
            id="application-version"
            type="text"
            v-model="data.appModuleVersion"
            :disabled="readonly"
          ></b-form-input>
        </b-form-group>
        <b-form-group
          label="Application Description"
          label-for="application-description"
        >
          <b-form-textarea
            id="application-description"
            v-model="data.appModuleDescription"
            :rows="3"
            :disabled="readonly"
          ></b-form-textarea>
        </b-form-group>
      </div>
    </div>
  </div>
</template>

<script>
import { models } from "django-airavata-api";
import { components, errors, mixins } from "django-airavata-common-ui";

export default {
  name: "application-module-editor",
  mixins: [mixins.VModelMixin],
  props: {
    value: {
      type: models.ApplicationModule,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    validationErrors: {
      type: Object,
    },
  },
  components: {
    "delete-button": components.DeleteButton,
  },
  computed: {
    validationFeedback() {
      return errors.ValidationErrors.createValidationFeedback(
        this.data,
        this.validationErrors
      );
    },
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    deleteApplicationModule() {
      this.$emit("delete", this.data);
    },
  },
};
</script>
