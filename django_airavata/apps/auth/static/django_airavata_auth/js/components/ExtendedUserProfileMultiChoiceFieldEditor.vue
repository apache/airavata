<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-checkbox-group
      v-model="value"
      :options="options"
      stacked
      @change="onChange"
    >
      <b-form-checkbox :value="otherOptionValue"
        >Other (please specify)</b-form-checkbox
      >
    </b-form-checkbox-group>
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
  },
};
</script>

<style></style>
