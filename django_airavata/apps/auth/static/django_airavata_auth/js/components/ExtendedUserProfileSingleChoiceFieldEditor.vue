<template>
  <b-form-group
    :label="extendedUserProfileField.name"
    :description="extendedUserProfileField.help_text"
  >
    <b-form-select v-model="value" :options="options"></b-form-select>
  </b-form-group>
</template>

<script>
import { mapGetters, mapMutations } from "vuex";
export default {
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
