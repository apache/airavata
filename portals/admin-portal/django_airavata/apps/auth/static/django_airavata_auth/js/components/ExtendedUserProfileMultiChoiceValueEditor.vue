<template>
  <extended-user-profile-value-editor v-bind="$props">
    <b-form-checkbox-group
      v-model="value"
      :options="options"
      stacked
      @change="onChange"
      :state="validateStateErrorOnly($v.value)"
    >
      <b-form-checkbox
        :value="otherOptionValue"
        v-if="extendedUserProfileField.other"
        >Other (please specify)</b-form-checkbox
      >

      <b-form-invalid-feedback :state="validateState($v.value)"
        >This field is required.</b-form-invalid-feedback
      >
    </b-form-checkbox-group>
    <template v-if="showOther">
      <b-form-input
        class="mt-2"
        v-model="other"
        placeholder="Please specify"
        :state="validateState($v.other)"
        @input="onInput"
      />
      <b-form-invalid-feedback :state="validateState($v.other)"
        >Please specify a value for 'Other'.</b-form-invalid-feedback
      >
    </template>
  </extended-user-profile-value-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import { validationMixin } from "vuelidate";
import { required, requiredIf } from "vuelidate/lib/validators";
import { errors } from "django-airavata-common-ui";
import ExtendedUserProfileValueEditor from "./ExtendedUserProfileValueEditor.vue";
const OTHER_OPTION = new Object(); // sentinel value
export default {
  mixins: [validationMixin],
  components: { ExtendedUserProfileValueEditor },
  props: ["extendedUserProfileField"],
  data() {
    return {
      otherOptionSelected: false,
    };
  },
  computed: {
    ...mapGetters("extendedUserProfile", [
      "getMultiChoiceValue",
      "getMultiChoiceOther",
    ]),
    value: {
      get() {
        const copy = this.getMultiChoiceValue(
          this.extendedUserProfileField.id
        ).slice();
        if (this.showOther) {
          copy.push(this.otherOptionValue);
        }
        return copy;
      },
      set(value) {
        const values = value.filter((v) => v !== this.otherOptionValue);
        this.setMultiChoiceValue({
          value: values,
          id: this.extendedUserProfileField.id,
        });
        this.$v.value.$touch();
      },
    },
    other: {
      get() {
        return this.getMultiChoiceOther(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setMultiChoiceOther({
          value,
          id: this.extendedUserProfileField.id,
        });
        this.$v.other.$touch();
      },
    },
    showOther() {
      return this.other || this.otherOptionSelected;
    },
    options() {
      return this.extendedUserProfileField &&
        this.extendedUserProfileField.choices
        ? this.extendedUserProfileField.choices.map((choice) => {
            return {
              value: choice.id,
              text: choice.display_text,
            };
          })
        : [];
    },
    otherOptionValue() {
      return OTHER_OPTION;
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
        required: requiredIf("required"),
      },
      other: {},
    };
    if (this.showOther) {
      validations.other = { required };
    }
    return validations;
  },
  methods: {
    ...mapMutations("extendedUserProfile", [
      "setMultiChoiceValue",
      "setMultiChoiceOther",
    ]),
    onChange(value) {
      this.otherOptionSelected = value.includes(this.otherOptionValue);
      if (!this.otherOptionSelected) {
        this.other = "";
      }
    },
    onInput() {
      // Handle case where initially there is an other value. If the user
      // deletes the other value, then we still want to keep the other text box
      // until the user unchecks the other option.
      this.otherOptionSelected = true;
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

<style></style>
