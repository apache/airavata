<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-select v-model="value" :options="options" @change="onChange">
      <b-form-select-option :value="otherOptionValue"
        >Other (please specify)</b-form-select-option
      >
    </b-form-select>
    <b-form-input
      class="mt-2"
      v-if="showOther"
      v-model="other"
      placeholder="Please specify"
    />
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
const OTHER_OPTION = new Object(); // sentinel value

export default {
  components: { ExtendedUserProfileFieldEditor },
  props: ["extendedUserProfileField"],
  data() {
    return {
      otherOptionSelected: false,
    };
  },
  computed: {
    ...mapGetters("extendedUserProfile", [
      "getSingleChoiceValue",
      "getSingleChoiceOther",
    ]),
    value: {
      get() {
        if (this.showOther) {
          return this.otherOptionValue;
        } else {
          return this.getSingleChoiceValue(this.extendedUserProfileField.id);
        }
      },
      set(value) {
        if (value !== this.otherOptionValue) {
          this.setSingleChoiceValue({
            value,
            id: this.extendedUserProfileField.id,
          });
        }
      },
    },
    other: {
      get() {
        return this.getSingleChoiceOther(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setSingleChoiceOther({
          value,
          id: this.extendedUserProfileField.id,
        });
      },
    },
    showOther() {
      const value = this.getSingleChoiceValue(this.extendedUserProfileField.id);
      return (value === null && this.other) || this.otherOptionSelected;
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
  },
  methods: {
    ...mapMutations("extendedUserProfile", [
      "setSingleChoiceValue",
      "setSingleChoiceOther",
    ]),
    onChange(value) {
      this.otherOptionSelected = value === this.otherOptionValue;
    },
  },
};
</script>

<style></style>
