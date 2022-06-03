<template>
  <b-card :title="title">
    <b-form-group label="Name">
      <b-form-input v-model="name" />
    </b-form-group>
    <b-form-group label="Help text">
      <b-form-input v-model="help_text" />
    </b-form-group>
    <b-form-group label="Required">
      <b-form-checkbox v-model="required" />
    </b-form-group>
    <!-- <b-card
            title="Options"
            v-if="
              field.field_type === 'single_choice' ||
              field.field_type === 'multi_choice'
            "
          >
            <template v-for="choice in field.choices">
              <b-input-group :key="choice.id">
                <b-form-input v-model="choice.display_text" />
                <b-input-group-append>
                  <b-button @click="deleteOption(field, choice)"
                    >Delete</b-button
                  >
                </b-input-group-append>
              </b-input-group>
            </template>
            <b-button @click="addOption(field)">Add Option</b-button>
          </b-card>
          <template v-if="field.links && field.links.length > 0">
            <b-card title="Links" v-for="link in field.links" :key="link.id">
              <b-form-group label="Label">
                <b-form-input v-model="link.label" />
              </b-form-group>
              <b-form-group label="URL">
                <b-form-input v-model="link.url" />
              </b-form-group>
              <b-form-group label="Show as link?">
                <b-form-checkbox v-model="link.display_link" />
              </b-form-group>
              <b-form-group label="Show inline?">
                <b-form-checkbox v-model="link.display_inline" />
              </b-form-group>
            </b-card>
          </template>
          <b-button v-if="!field.links" @click="addLink(field)"
            >Add Link</b-button
          > -->
  </b-card>
</template>

<script>
import { mapMutations } from "vuex";
export default {
  props: ["extendedUserProfileField"],
  computed: {
    name: {
      get() {
        return this.extendedUserProfileField.name;
      },
      set(value) {
        this.setName({ value, field: this.extendedUserProfileField });
      },
    },
    help_text: {
      get() {
        return this.extendedUserProfileField.help_text;
      },
      set(value) {
        this.setHelpText({ value, field: this.extendedUserProfileField });
      },
    },
    required: {
      get() {
        return this.extendedUserProfileField.required;
      },
      set(value) {
        this.setRequired({ value, field: this.extendedUserProfileField });
      },
    },
    title() {
      const fieldTypes = {
        text: "Text",
        single_choice: "Single Choice",
        multi_choice: "Multi Choice",
        user_agreement: "User Agreement",
      }
      return `${fieldTypes[this.extendedUserProfileField.field_type]}: ${this.name}`;
    }
  },
  methods: {
    ...mapMutations("extendedUserProfile", ["setName", "setHelpText", "setRequired"]),
  },
};
</script>

<style></style>
