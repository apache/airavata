<template>
  <div>
    <div class="row">
      <div class="col-auto mr-auto">
        <h1 class="h4 mb-4">Extended User Profile Editor</h1>
      </div>
    </div>
    <div v-for="field in extendedUserProfileFields" class="row" :key="field.id">
      <div class="col">
        <extended-user-profile-field-editor :extendedUserProfileField="field" />
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-dropdown text="Add Field">
          <b-dropdown-item @click="addField('text')">Text</b-dropdown-item>
          <b-dropdown-item @click="addField('single_choice')"
            >Single Choice</b-dropdown-item
          >
          <b-dropdown-item @click="addField('multi_choice')"
            >Multi Choice</b-dropdown-item
          >
          <b-dropdown-item @click="addField('user_agreement')"
            >User Agreement</b-dropdown-item
          >
        </b-dropdown>
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
import ExtendedUserProfileFieldEditor from "./field-editors/ExtendedUserProfileFieldEditor.vue";
export default {
  components: { ExtendedUserProfileFieldEditor },
  data() {
    return {};
  },
  created() {
    this.loadExtendedUserProfileFields();
  },
  methods: {
    ...mapActions("extendedUserProfile", [
      "loadExtendedUserProfileFields",
      "saveExtendedUserProfileFields",
      "addExtendedUserProfileField",
    ]),
    addField(field_type) {
      // TODO: post an empty field to the API
      this.addExtendedUserProfileField({ field_type });
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
    save() {
      this.saveExtendedUserProfileFields();
    },
  },
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
  },
};
</script>
