<template>
  <list-layout @add-new-item="newGroupResourcePreference" :items="groupResourceProfiles" title="Group Resource Profiles" new-item-button-text="New Group Resource Profile">
    <template slot="item-list" slot-scope="slotProps">

      <b-table striped hover :fields="fields" :items="slotProps.items">
        <template slot="updatedTime" slot-scope="data">
          <human-date :date="data.value"/>
        </template>
        <template slot="action" slot-scope="data">
          <router-link class="action-link" v-if="data.item.userHasWriteAccess" :to="{name: 'group_resource_preference', params: {value: data.item, id: data.item.groupResourceProfileId}}">
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </router-link>
          <a href="#" class="action-link text-danger" @click.prevent="removeGroupResourceProfile(data.item)" v-if="data.item.userHasWriteAccess">
            Delete
            <i class="fa fa-trash" aria-hidden="true"></i>
          </a>
        </template>
      </b-table>
    </template>
  </list-layout>
</template>

<script>
import { components, layouts } from "django-airavata-common-ui";
import { services } from "django-airavata-api";

export default {
  name: "compute-resource-preference",
  components: {
    "human-date": components.HumanDate,
    "list-layout": layouts.ListLayout
  },
  data: function() {
    return {
      groupResourceProfiles: [],
      fields: [
        {
          label: "Name",
          key: "groupResourceProfileName"
        },
        {
          label: "Updated",
          key: "updatedTime",
        },
        {
          label: "Action",
          key: "action"
        }
      ]
    };
  },
  methods: {
    newGroupResourcePreference: function() {
      this.$router.push({
        name: "new_group_resource_preference"
      });
    },
    loadGroupResourceProfiles: function() {
      services.GroupResourceProfileService.list().then(
        groupResourceProfiles => {
          this.groupResourceProfiles = groupResourceProfiles;
        }
      );
    },
    removeGroupResourceProfile: function(groupResourceProfile) {
      services.GroupResourceProfileService.delete({
        lookup: groupResourceProfile.groupResourceProfileId
      })
        .then(() => services.GroupResourceProfileService.list())
        .then(
          groupResourceProfiles =>
            (this.groupResourceProfiles = groupResourceProfiles)
        );
    }
  },
  mounted: function() {
    this.loadGroupResourceProfiles();
  }
};
</script>
