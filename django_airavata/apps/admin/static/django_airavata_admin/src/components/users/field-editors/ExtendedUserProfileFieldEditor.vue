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
    <b-card title="Options" v-if="extendedUserProfileField.supportsChoices">
      <transition-group name="fade">
        <template v-for="(choice, index) in extendedUserProfileField.choices">
          <b-input-group :key="choice.key">
            <b-form-input
              :value="choice.display_text"
              @input="handleChoiceDisplayTextChanged(choice, $event)"
            />
            <b-input-group-append>
              <b-button
                @click="handleChoiceMoveUp(choice)"
                :disabled="index === 0"
                v-b-tooltip.hover.left
                title="Move Up"
              >
                <i class="fa fa-arrow-up" aria-hidden="true"></i>
              </b-button>
              <b-button
                @click="handleChoiceMoveDown(choice)"
                :disabled="
                  index === extendedUserProfileField.choices.length - 1
                "
                v-b-tooltip.hover.left
                title="Move Down"
              >
                <i class="fa fa-arrow-down" aria-hidden="true"></i>
              </b-button>
              <b-button
                @click="handleChoiceDeleted(choice)"
                variant="danger"
                v-b-tooltip.hover.left
                title="Delete Option"
              >
                <i class="fa fa-trash" aria-hidden="true"></i>
              </b-button>
            </b-input-group-append>
          </b-input-group>
        </template>
        <b-input-group :key="'other'" v-if="extendedUserProfileField.other">
          <b-form-input placeholder="Please specify" disabled />
          <b-input-group-append>
            <b-button disabled>
              <i class="fa fa-arrow-up" aria-hidden="true"></i>
            </b-button>
            <b-button disabled>
              <i class="fa fa-arrow-down" aria-hidden="true"></i>
            </b-button>
            <b-button
              @click="other = false"
              variant="danger"
              v-b-tooltip.hover.left
              title="Remove Other option"
            >
              <i class="fa fa-trash" aria-hidden="true"></i>
            </b-button>
          </b-input-group-append>
        </b-input-group>
      </transition-group>
      <div>
        <b-button @click="addChoice({ field: extendedUserProfileField })"
          >Add Option</b-button
        >
      </div>
      <b-form-checkbox v-model="other" switch>
        Allow user to type in an "Other" option
      </b-form-checkbox>
    </b-card>

    <template
      v-if="
        extendedUserProfileField.links &&
        extendedUserProfileField.links.length > 0
      "
    >
      <transition-group name="fade">
        <b-card
          :title="`Link: ${link.label}`"
          v-for="link in extendedUserProfileField.links"
          :key="link.key"
        >
          <b-form-group label="Label">
            <b-form-input
              :value="link.label"
              @input="handleLinkLabelChanged(link, $event)"
            />
          </b-form-group>
          <b-form-group label="URL">
            <b-form-input
              :value="link.url"
              @input="handleLinkURLChanged(link, $event)"
            />
          </b-form-group>
          <b-form-group label="Show as link?">
            <b-form-checkbox
              :checked="link.display_link"
              @input="handleLinkDisplayLinkChanged(link, $event)"
            />
          </b-form-group>
          <b-form-group label="Show inline?">
            <b-form-checkbox
              :checked="link.display_inline"
              @input="handleLinkDisplayInlineChanged(link, $event)"
            />
          </b-form-group>
          <b-button @click="handleLinkDeleted(link)" variant="danger">
            Delete Link
          </b-button>
        </b-card>
      </transition-group>
    </template>
    <b-button @click="addLink({ field: extendedUserProfileField })"
      >Add Link</b-button
    >
    <b-button
      @click="handleMoveUp({ field: extendedUserProfileField })"
      :disabled="
        extendedUserProfileFields.indexOf(extendedUserProfileField) === 0
      "
      >Move Up</b-button
    >
    <b-button
      @click="handleMoveDown({ field: extendedUserProfileField })"
      :disabled="
        extendedUserProfileFields.indexOf(extendedUserProfileField) ===
        extendedUserProfileFields.length - 1
      "
      >Move Down</b-button
    >
    <b-button @click="handleDelete" variant="danger">Delete</b-button>
  </b-card>
</template>

<script>
import { mapActions, mapGetters, mapMutations } from "vuex";
export default {
  props: ["extendedUserProfileField"],
  computed: {
    ...mapGetters("extendedUserProfile", ["extendedUserProfileFields"]),
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
    other: {
      get() {
        return this.extendedUserProfileField.other;
      },
      set(value) {
        this.setOther({ value, field: this.extendedUserProfileField });
      },
    },
    title() {
      const fieldTypes = {
        text: "Text",
        single_choice: "Single Choice",
        multi_choice: "Multi Choice",
        user_agreement: "User Agreement",
      };
      return `${fieldTypes[this.extendedUserProfileField.field_type]}: ${
        this.name
      }`;
    },
  },
  methods: {
    ...mapMutations("extendedUserProfile", [
      "setName",
      "setHelpText",
      "setRequired",
      "setOther",
      "addChoice",
      "updateChoiceDisplayText",
      "deleteChoice",
      "updateChoiceIndex",
      "addLink",
      "updateLinkLabel",
      "updateLinkURL",
      "updateLinkDisplayLink",
      "updateLinkDisplayInline",
      "deleteLink",
      "updateFieldIndex",
      "deleteField",
    ]),
    handleChoiceDisplayTextChanged(choice, display_text) {
      this.updateChoiceDisplayText({ choice, display_text });
    },
    handleChoiceDeleted(choice) {
      this.deleteChoice({ field: this.extendedUserProfileField, choice });
    },
    handleChoiceMoveUp(choice) {
      let index = this.extendedUserProfileField.choices.indexOf(choice);
      index--;
      this.updateChoiceIndex({
        field: this.extendedUserProfileField,
        choice,
        index,
      });
    },
    handleChoiceMoveDown(choice) {
      let index = this.extendedUserProfileField.choices.indexOf(choice);
      index++;
      this.updateChoiceIndex({
        field: this.extendedUserProfileField,
        choice,
        index,
      });
    },
    handleLinkLabelChanged(link, label) {
      this.updateLinkLabel({ link, label });
    },
    handleLinkURLChanged(link, url) {
      this.updateLinkURL({ link, url });
    },
    handleLinkDisplayLinkChanged(link, display_link) {
      this.updateLinkDisplayLink({ link, display_link });
    },
    handleLinkDisplayInlineChanged(link, display_inline) {
      this.updateLinkDisplayInline({ link, display_inline });
    },
    handleLinkDeleted(link) {
      this.deleteLink({ field: this.extendedUserProfileField, link });
    },
    handleMoveUp({ field }) {
      let index = this.extendedUserProfileFields.indexOf(field);
      index--;
      this.updateFieldIndex({ field, index });
    },
    handleMoveDown({ field }) {
      let index = this.extendedUserProfileFields.indexOf(field);
      index++;
      this.updateFieldIndex({ field, index });
    },
    handleDelete() {
      this.deleteField({
        field: this.extendedUserProfileField,
      });
    },
  },
};
</script>

<style></style>
