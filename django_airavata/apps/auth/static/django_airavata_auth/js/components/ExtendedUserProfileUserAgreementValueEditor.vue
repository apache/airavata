<template>
  <extended-user-profile-value-editor v-bind="$props">
    <b-form-checkbox
      v-model="value"
      :unchecked-value="false"
      :value="true"
      :state="validateStateErrorOnly($v.value)"
    >
      {{ extendedUserProfileField.checkbox_label }}
    </b-form-checkbox>
    <b-form-invalid-feedback :state="validateState($v.value)"
      >This field is required.</b-form-invalid-feedback
    >
  </extended-user-profile-value-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import { validationMixin } from "vuelidate";
import { errors } from "django-airavata-common-ui";
import ExtendedUserProfileValueEditor from "./ExtendedUserProfileValueEditor.vue";

export default {
  mixins: [validationMixin],
  components: { ExtendedUserProfileValueEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getUserAgreementValue"]),
    value: {
      get() {
        return this.getUserAgreementValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setUserAgreementValue({
          value,
          id: this.extendedUserProfileField.id,
        });
        this.$v.value.$touch();
      },
    },
    valid() {
      return !this.$v.$invalid;
    },
    required() {
      return this.extendedUserProfileField.required;
    },
  },
  validations() {
    const validations = {
      value: {
        mustBeTrue: this.mustBeTrue,
      },
    };
    return validations;
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setUserAgreementValue"]),
    mustBeTrue(value) {
      if (this.required) {
        return value === true;
      } else {
        // If not required, always valid
        return true;
      }
    },
    validateState: errors.vuelidateHelpers.validateState,
    validateStateErrorOnly: errors.vuelidateHelpers.validateStateErrorOnly,
    touch() {
      this.$v.$touch();
    },
  },
  watch: {
    valid: {
      handler(valid) {
        this.$emit(valid ? "valid" : "invalid");
      },
      immediate: true,
    },
  },
};
</script>
