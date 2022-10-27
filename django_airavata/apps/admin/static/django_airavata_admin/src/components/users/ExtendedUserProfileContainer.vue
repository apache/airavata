<template>
  <div class="has-fixed-footer">
    <div class="row mb-2">
      <div class="col-auto mr-auto">
        <h1 class="h4">Extended User Profile Editor</h1>
        <p class="text-muted small">
          Add and edit additional user profile fields for gateway users to
          complete.
        </p>
      </div>
    </div>
    <transition-group name="fade">
      <div
        v-for="field in extendedUserProfileFields"
        class="row"
        :key="field.key"
      >
        <div class="col">
          <extended-user-profile-field-editor
            :extendedUserProfileField="field"
            @valid="recordValidChildComponent(field)"
            @invalid="recordInvalidChildComponent(field)"
          />
        </div>
      </div>
    </transition-group>
    <div ref="bottom" />
    <div class="fixed-footer">
      <div class="d-flex">
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
        <b-button
          variant="primary"
          @click="save"
          :disabled="!valid"
          class="ml-2"
          >Save</b-button
        >
        <b-button variant="secondary" class="ml-auto" href="/admin/users"
          >Return to Manage Users</b-button
        >
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions, mapGetters } from "vuex";
import ExtendedUserProfileFieldEditor from "./field-editors/ExtendedUserProfileFieldEditor.vue";
import { mixins } from "django-airavata-common-ui";
export default {
  mixins: [mixins.ValidationParent],
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
      this.addExtendedUserProfileField({ field_type });
      this.$nextTick(() => {
        this.$refs.bottom.scrollIntoView();
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
    save() {
      this.saveExtendedUserProfileFields();
    },
  },
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
    valid() {
      return this.childComponentsAreValid;
    },
  },
};
</script>
