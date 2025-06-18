<template>
  <list-layout
    @add-new-item="newGroupResourcePreference"
    :items="groupResourceProfiles"
    title="Group Resource Profiles"
    new-item-button-text="New Group Resource Profile"
  >
    <template slot="item-list" slot-scope="slotProps">
      <b-table striped hover :fields="fields" :items="slotProps.items">
        <template slot="cell(updatedTime)" slot-scope="data">
          <human-date :date="data.value" />
        </template>
        <template slot="cell(action)" slot-scope="data">
          <router-link
            class="action-link"
            v-if="data.item.userHasWriteAccess"
            :to="{
              name: 'group_resource_preference',
              params: {
                value: data.item,
                id: data.item.groupResourceProfileId,
              },
            }"
          >
            Edit
            <i class="fa fa-edit" aria-hidden="true"></i>
          </router-link>
          <router-link
            class="action-link"
            v-if="!data.item.userHasWriteAccess"
            :to="{
              name: 'group_resource_preference',
              params: {
                value: data.item,
                id: data.item.groupResourceProfileId,
              },
            }"
          >
            View
            <i class="fa fa-eye" aria-hidden="true"></i>
          </router-link>
          <delete-link
            v-if="data.item.userHasWriteAccess"
            class="action-link"
            @delete="removeGroupResourceProfile(data.item)"
          >
            Are you sure you want to delete Group Resource Profile
            <strong>{{ data.item.groupResourceProfileName }}</strong
            >?
          </delete-link>
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
    "delete-link": components.DeleteLink,
    "human-date": components.HumanDate,
    "list-layout": layouts.ListLayout,
  },
  data: function () {
    return {
      groupResourceProfiles: [],
      fields: [
        {
          label: "Name",
          key: "groupResourceProfileName",
        },
        {
          label: "Updated",
          key: "updatedTime",
        },
        {
          label: "Action",
          key: "action",
        },
      ],
    };
  },
  methods: {
    newGroupResourcePreference: function () {
      this.$router.push({
        name: "new_group_resource_preference",
      });
    },
    loadGroupResourceProfiles: function () {
      services.GroupResourceProfileService.list().then(
        (groupResourceProfiles) => {
          this.groupResourceProfiles = groupResourceProfiles;
        }
      );
    },
    removeGroupResourceProfile: function (groupResourceProfile) {
      services.GroupResourceProfileService.delete({
        lookup: groupResourceProfile.groupResourceProfileId,
      })
        .then(() => services.GroupResourceProfileService.list())
        .then(
          (groupResourceProfiles) =>
            (this.groupResourceProfiles = groupResourceProfiles)
        );
    },
  },
  mounted: function () {
    this.loadGroupResourceProfiles();
  },
};
</script>
