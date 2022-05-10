<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-input v-model="value" :state="validateState($v.value)" />
    <b-form-invalid-feedback :state="validateState($v.value)"
      >This field is required.</b-form-invalid-feedback
    >
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import { validationMixin } from "vuelidate";
import { required } from "vuelidate/lib/validators";
import { errors } from "django-airavata-common-ui";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
export default {
  mixins: [validationMixin],
  components: { ExtendedUserProfileFieldEditor },
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
  },
  validations() {
    return {
      value: {
        required,
      },
    };
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setTextValue"]),
    validateState: errors.vuelidateHelpers.validateState,
  },
};
</script>

<style></style>
