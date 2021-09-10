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
      required: true,
    },
    label: {
      type: String,
      default: "Allocation",
    },
  },
  store: store,
  data() {
    return {
      groupResourceProfileId: this.value,
    };
  },
  async mounted() {
    await this.$store.dispatch("loadGroupResourceProfiles");
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
    ...mapGetters(["groupResourceProfiles"]),
  },
  methods: {
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
  },
  watch: {
    value() {
      this.groupResourceProfileId = this.value;
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
