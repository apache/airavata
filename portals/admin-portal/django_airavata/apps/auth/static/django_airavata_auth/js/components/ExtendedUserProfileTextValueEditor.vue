<template>
  <extended-user-profile-value-editor v-bind="$props">
    <b-form-input v-model="value" :state="validateState($v.value)" />
    <b-form-invalid-feedback :state="validateState($v.value)"
      >This field is required.</b-form-invalid-feedback
    >
  </extended-user-profile-value-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import { validationMixin } from "vuelidate";
import { requiredIf } from "vuelidate/lib/validators";
import { errors } from "django-airavata-common-ui";
import ExtendedUserProfileValueEditor from "./ExtendedUserProfileValueEditor.vue";
export default {
  mixins: [validationMixin],
  components: { ExtendedUserProfileValueEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getTextValue"]),
    value: {
      get() {
        return this.getTextValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setTextValue({ value, id: this.extendedUserProfileField.id });
        this.$v.$touch();
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
    return {
      value: {
        required: requiredIf("required"),
      },
    };
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setTextValue"]),
    validateState: errors.vuelidateHelpers.validateState,
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

<style></style>
