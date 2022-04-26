<template>
  <b-form-group
    :label="extendedUserProfileField.name"
    :description="extendedUserProfileField.help_text"
  >
    <b-form-checkbox-group
      v-model="value"
      :options="options"
      stacked
    ></b-form-checkbox-group>
  </b-form-group>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
export default {
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
