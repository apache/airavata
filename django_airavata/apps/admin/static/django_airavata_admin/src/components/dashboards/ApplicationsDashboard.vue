<template>
  <list-layout @add-new-item="newApplicationHandler" :items="sortedModules" title="Application Catalog" subtitle="Applications"
    new-item-button-text="New Application">
    <template slot="item-list" slot-scope="slotProps">

      <div class="row">
        <application-card v-for="item in slotProps.items" v-bind:app-module="item" v-bind:key="item.appModuleId" v-on:app-selected="clickHandler(item)">
        </application-card>
      </div>
    </template>
  </list-layout>
</template>
<script>
import { mapActions, mapState } from "vuex";

import { layouts, components as comps } from "django-airavata-common-ui";

import { utils } from "django-airavata-api";

export default {
  components: {
    "application-card": comps.ApplicationCard,
    "list-layout": layouts.ListLayout
  },
  mounted: function() {
    if (!this.modules) {
      this.loadApplications();
    }
  },
  computed: {
    ...mapState("applications/modules", ["modules"]),
    sortedModules() {
      if (this.modules) {
        return utils.StringUtils.sortIgnoreCase(
          this.modules.slice(),
          a => a.appModuleName
        );
      } else {
        return [];
      }
    }
  },
  methods: {
    clickHandler: function(item) {
      this.$router.push({
        name: "application_module",
        params: { id: item.appModuleId }
      });
    },
    newApplicationHandler: function() {
      this.$router.push({ name: "new_application_module" });
    },
    ...mapActions({
      loadApplications: "applications/modules/loadApplicationModules"
    })
  }
};
</script>
