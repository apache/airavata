<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Groups</h1>
      </div>
      <div id="col-new-group" class="col-sm-2">
        <b-button href="create" :variant="'primary'"
          >Create New Group&nbsp;&nbsp;<i
            class="fa fa-plus"
            aria-hidden="true"
          ></i
        ></b-button>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <group-list v-bind:groupsForOwners="groupsOwners"></group-list>
            <pager
              v-bind:paginator="groupPaginator"
              v-on:next="nextGroups"
              v-on:previous="previousGroups"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GroupList from "../group_components/GroupList.vue";

import { services } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";

export default {
  name: "groups-manage-container",
  data() {
    return {
      groupPaginator: null,
    };
  },
  components: {
    "group-list": GroupList,
    pager: comps.Pager,
  },
  methods: {
    nextGroups: function () {
      this.groupPaginator.next();
    },
    previousGroups: function () {
      this.groupPaginator.previous();
    },
  },
  computed: {
    groupsOwners: function () {
      return this.groupPaginator ? this.groupPaginator.results : null;
    },
  },
  beforeMount: function () {
    services.GroupService.list().then(
      (result) => (this.groupPaginator = result)
    );
  },
};
</script>

<style>
#col-new-group {
  text-align: right;
}
#modal-new-group {
  text-align: left;
}
</style>
