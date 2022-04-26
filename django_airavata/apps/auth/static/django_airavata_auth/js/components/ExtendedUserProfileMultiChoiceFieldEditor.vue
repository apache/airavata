<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-checkbox-group
      v-model="value"
      :options="options"
      stacked
    ></b-form-checkbox-group>
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
export default {
  components: { ExtendedUserProfileFieldEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getMultiChoiceValue"]),
    value: {
      get() {
        return this.getMultiChoiceValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setMultiChoiceValue({
          value,
          id: this.extendedUserProfileField.id,
        });
      },
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
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setMultiChoiceValue"]),
  },
};
</script>

<style></style>
