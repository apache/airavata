<template>
  <b-form-group :label="label" label-for="group-resource-profile">
    <b-form-select
      id="group-resource-profile"
      :value="groupResourceProfileId"
      :options="groupResourceProfileOptions"
      required
      @change="groupResourceProfileChanged"
      @input.native.stop
      :disabled="disabled"
    >
      <template slot="first">
        <option :value="null" disabled>
          <slot name="null-option">Select an allocation</slot>
        </option>
      </template>
    </b-form-select>
  </b-form-group>
</template>

<script>
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import store from "./store";
import { mapGetters } from "vuex";
Vue.use(BootstrapVue);

export default {
  name: "group-resource-profile-selector",
  props: {
    value: {
      type: String,
      default: null,
    },
    label: {
      type: String,
      default: "Allocation",
    },
    disabled: {
      type: Boolean,
      default: false,
    }
  },
  store: store,
  created() {
    this.$store.dispatch("initializeGroupResourceProfileId", {
      groupResourceProfileId: this.value,
    });
    this.$store.dispatch("loadGroupResourceProfiles");
  },
  computed: {
    ...mapGetters(["groupResourceProfileId", "groupResourceProfiles"]),
    groupResourceProfileOptions: function () {
      if (this.groupResourceProfiles && this.groupResourceProfiles.length > 0) {
        const groupResourceProfileOptions = this.groupResourceProfiles.map(
          (groupResourceProfile) => {
            return {
              value: groupResourceProfile.groupResourceProfileId,
              text: groupResourceProfile.groupResourceProfileName,
            };
          }
        );
        groupResourceProfileOptions.sort((a, b) =>
          a.text.localeCompare(b.text)
        );
        return groupResourceProfileOptions;
      } else {
        return [];
      }
    },
  },
  methods: {
    groupResourceProfileChanged: function (groupResourceProfileId) {
      this.$store.dispatch("updateGroupResourceProfileId", {
        groupResourceProfileId,
      });
    },
    emitValueChanged: function () {
      const inputEvent = new CustomEvent("input", {
        detail: [this.groupResourceProfileId],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
  },
  watch: {
    value(newValue) {
      if (newValue !== this.groupResourceProfileId) {
        this.$store.dispatch("updateGroupResourceProfileId", {
          groupResourceProfileId: newValue,
        });
      }
    },
    groupResourceProfileId() {
      this.emitValueChanged();
    },
  },
};
</script>

<style lang="scss">
@import "./styles";
:host {
  display: block;
}
</style>
