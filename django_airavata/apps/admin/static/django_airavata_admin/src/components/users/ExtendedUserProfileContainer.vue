<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">Extended User Profile Editor</h1>
      </div>
    </div>
    <div v-for="field in fields" class="row" :key="field.id">
      <div class="col">
        <b-card :title="'Field: ' + field.name">
          <b-form-group label="Name">
            <b-form-input v-model="field.name" />
          </b-form-group>
          <b-form-group label="Description">
            <b-form-input v-model="field.description" />
          </b-form-group>
          <b-form-group label="Required">
            <b-form-checkbox v-model="field.required" />
          </b-form-group>
          <b-form-group label="Type">
            <b-form-select v-model="field.type" :options="fieldTypeOptions" />
          </b-form-group>
          <b-card
            title="Options"
            v-if="
              field.type === 'single-select' || field.type === 'multi-select'
            "
          >
            <template v-for="option in field.options">
              <b-input-group :key="option.id">
                <b-form-input v-model="option.name" />
                <b-input-group-append>
                  <b-button @click="deleteOption(field, option)"
                    >Delete</b-button
                  >
                </b-input-group-append>
              </b-input-group>
            </template>
            <b-button @click="addOption(field)">Add Option</b-button>
          </b-card>
          <template v-if="field.links && field.links.length > 0">
            <b-card title="Links" v-for="link in field.links" :key="link.id">
              <b-form-group label="Title">
                <b-form-input v-model="link.title" />
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
          <template v-if="field.conditional">
            <b-card title="Conditional">
              When
              <br />
              <template v-for="condition in field.conditional.conditions">
                <div :key="condition.id">
                  <b-form-select
                    :options="getConditionFieldOptions(field)"
                    v-model="condition.field"
                  />
                  =
                  <b-form-select
                    v-if="
                      (condition.field &&
                        condition.field.type === 'multi-select') ||
                      condition.field.type === 'single-select' ||
                      condition.field.type === 'user-agreement'
                    "
                    :disabled="condition.field"
                    :options="getConditionFieldValueOptions(condition.field)"
                    v-model="condition.value"
                  />
                  <b-form-input
                    v-else-if="
                      condition.field && condition.field.type === 'text'
                    "
                    v-model="condition.value"
                  />
                </div>
              </template>
            </b-card>
          </template>
          <b-button v-if="!field.conditional" @click="addConditional(field)"
            >Add Conditional</b-button
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
    <pre>{{ JSON.stringify(fields, null, 4) }}</pre>
  </div>
</template>

<script>
export default {
  data() {
    return {
      fields: [],
    };
  },
  methods: {
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
    fieldTypeOptions() {
      return [
        { value: "text", text: "Text" },
        { value: "single-select", text: "Single Select" },
        { value: "multi-select", text: "Multi Select" },
        { value: "user-agreement", text: "User Agreement" },
      ];
    },
  },
};
</script>
