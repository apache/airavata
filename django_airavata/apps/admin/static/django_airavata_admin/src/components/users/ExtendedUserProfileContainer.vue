<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">Extended User Profile Editor</h1>
      </div>
    </div>
    <div v-for="field in extendedUserProfileFields" class="row" :key="field.id">
      <div class="col">
        <b-card :title="'Field: ' + field.name">
          <b-form-group label="Name">
            <b-form-input v-model="field.name" />
          </b-form-group>
          <b-form-group label="Help text">
            <b-form-input v-model="field.help_text" />
          </b-form-group>
          <b-form-group label="Required">
            <b-form-checkbox v-model="field.required" />
          </b-form-group>
          <b-form-group label="Type">
            <b-form-select
              v-model="field.field_type"
              :options="fieldTypeOptions"
            />
          </b-form-group>
          <b-card
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
          >
        </b-card>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-button variant="primary" @click="addField"> Add Field </b-button>
      </div>
    </div>
    <div class="row mt-4">
      <div class="col">
        <b-button variant="primary" @click="save">Save</b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from "vuex";
export default {
  data() {
    return {
      fields: [],
    };
  },
  created() {
    this.loadExtendedUserProfileFields();
  },
  methods: {
    ...mapActions("extendedUserProfile", ["loadExtendedUserProfileFields"]),
    addField() {
      // TODO: post an empty field to the API
      this.fields.push({
        id: null,
        name: "",
        description: "",
        type: "text",
        required: false,
        options: null,
        links: null,
      });
    },
    addOption(field) {
      if (!field.options) {
        field.options = [];
      }
      field.options.push({ id: null, name: "" });
    },
    deleteOption(field, option) {
      const i = field.options.indexOf(option);
      field.options.splice(i, 1);
    },
    addLink(field) {
      if (!field.links) {
        field.links = [];
      }
      field.links.push({
        id: null,
        url: "",
        title: "",
        display_link: true,
        display_inline: false,
      });
    },
    addConditional(field) {
      if (!field.conditional) {
        field.conditional = {
          id: null,
          conditions: [],
          require_when: true,
          show_when: true,
        };
      }
    },
    deleteLink(field, link) {
      const i = field.links.indexOf(link);
      field.links.splice(i, 1);
    },
    save() {},
  },
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
    fieldTypeOptions() {
      return [
        { value: "text", text: "Text" },
        { value: "single_choice", text: "Single Choice" },
        { value: "multi_choice", text: "Multi Choice" },
        { value: "user_agreement", text: "User Agreement" },
      ];
    },
  },
};
</script>
