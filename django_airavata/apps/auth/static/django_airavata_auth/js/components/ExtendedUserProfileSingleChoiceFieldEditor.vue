<template>
  <extended-user-profile-field-editor v-bind="$props">
    <b-form-select v-model="value" :options="options"></b-form-select>
  </extended-user-profile-field-editor>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
import ExtendedUserProfileFieldEditor from "./ExtendedUserProfileFieldEditor.vue";
export default {
  components: { ExtendedUserProfileFieldEditor },
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["getSingleChoiceValue"]),
    value: {
      get() {
        return this.getSingleChoiceValue(this.extendedUserProfileField.id);
      },
      set(value) {
        this.setSingleChoiceValue({
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
    ...mapMutations("extendedUserProfile", ["setSingleChoiceValue"]),
  },
};
</script>

<style></style>
