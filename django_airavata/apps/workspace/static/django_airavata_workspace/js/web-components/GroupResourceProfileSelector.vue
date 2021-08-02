<template>
  <b-form-group :label="label" label-for="group-resource-profile">
    <b-form-select
      id="group-resource-profile"
      v-model="groupResourceProfileId"
      :options="groupResourceProfileOptions"
      required
      @change="groupResourceProfileChanged"
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
import {
  getDefaultGroupResourceProfileId,
  getGroupResourceProfiles,
} from "./store";
import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
Vue.use(BootstrapVue);

export default {
  name: "group-resource-profile-selector",
  props: {
    value: {
      type: String,
    },
    label: {
      type: String,
      default: "Allocation",
    },
  },
  data() {
    return {
      groupResourceProfileId: this.value,
      groupResourceProfiles: [],
      defaultGroupResourceProfileId: null,
    };
  },
  async mounted() {
    this.defaultGroupResourceProfileId = await getDefaultGroupResourceProfileId();
    this.groupResourceProfiles = await getGroupResourceProfiles();
    this.init();
  },
  computed: {
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
    init() {
      // Default the selected group resource profile
      if (
        (!this.value ||
          !this.selectedValueInGroupResourceProfileList(
            this.groupResourceProfiles
          )) &&
        this.groupResourceProfiles &&
        this.groupResourceProfiles.length > 0
      ) {
        // automatically select the last one user selected
        this.groupResourceProfileId = this.defaultGroupResourceProfileId;
        this.emitValueChanged();
      }
    },
    groupResourceProfileChanged: function (groupResourceProfileId) {
      this.groupResourceProfileId = groupResourceProfileId;
      this.emitValueChanged();
    },
    emitValueChanged: function () {
      const inputEvent = new CustomEvent("input", {
        detail: [this.groupResourceProfileId],
        composed: true,
        bubbles: true,
      });
      this.$el.dispatchEvent(inputEvent);
    },
    selectedValueInGroupResourceProfileList(groupResourceProfiles) {
      return (
        groupResourceProfiles
          .map((grp) => grp.groupResourceProfileId)
          .indexOf(this.value) >= 0
      );
    },
  },
  watch: {
    value() {
      this.groupResourceProfileId = this.value;
    },
  },
};
</script>

<style>
@import url("./styles.css");
</style>
