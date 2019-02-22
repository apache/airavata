<template>
  <div class="share-button btn-container">
    <b-button :variant="'outline-primary'" :title="title" :disabled="!shareButtonEnabled" @click="openSharingSettingsModal">
      Share
      <b-badge>{{ totalCount }}</b-badge>
    </b-button>
    <b-modal class="modal-share-settings" title="Sharing Settings" ref="sharingSettingsModal" ok-title="Save" @ok="saveSharedEntity"
      @cancel="cancelEditSharedEntity" no-close-on-esc no-close-on-backdrop hide-header-close @show="showSharingSettingsModal">
      <shared-entity-editor v-if="localSharedEntity && users && groups" v-model="localSharedEntity" :users="users"
        :groups="groups" :disallow-editing-admin-groups="disallowEditingAdminGroups" />
    </b-modal>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import SharedEntityEditor from "./SharedEntityEditor.vue";

export default {
  name: "share-button",
  props: {
    entityId: String,
    sharedEntity: models.SharedEntity,
    autoAddDefaultGatewayUsersGroup: {
      type: Boolean,
      default: true
    },
    disallowEditingAdminGroups: {
      type: Boolean,
      default: true
    }
  },
  components: {
    SharedEntityEditor
  },
  data: function() {
    return {
      localSharedEntity: null,
      sharedEntityCopy: null,
      defaultGatewayUsersGroup: null,
      users: null,
      groups: null
    };
  },
  computed: {
    title: function() {
      return (
        "Shared with " +
        this.groupsCount +
        " groups" +
        (this.groupsCount > 0 ? " (" + this.groupNames.join(", ") + ")" : "") +
        " and " +
        this.usersCount +
        " users" +
        (this.usersCount > 0 ? " (" + this.userNames.join(", ") + ")" : "")
      );
    },
    usersCount: function() {
      return this.localSharedEntity && this.localSharedEntity.userPermissions
        ? this.localSharedEntity.userPermissions.length
        : 0;
    },
    userNames: function() {
      return this.localSharedEntity && this.localSharedEntity.userPermissions
        ? this.localSharedEntity.userPermissions.map(
            userPerm => userPerm.user.firstName + " " + userPerm.user.lastName
          )
        : null;
    },
    filteredGroupPermissions: function() {
      if (this.localSharedEntity && this.localSharedEntity.groupPermissions) {
        return this.disallowEditingAdminGroups ? this.localSharedEntity.nonAdminGroupPermissions : this.localSharedEntity.groupPermissions;
      } else {
        return [];
      }
    },
    groupNames: function() {
      return this.filteredGroupPermissions.map(
        groupPerm => groupPerm.group.name
      );
    },
    groupsCount: function() {
      return this.filteredGroupPermissions.length;
    },
    totalCount: function() {
      return this.usersCount + this.groupsCount;
    },
    shareButtonEnabled: function() {
      // Enable share button if new entity or user is the entity's owner
      return (
        this.localSharedEntity &&
        (!this.localSharedEntity.entityId || this.localSharedEntity.isOwner)
      );
    }
  },
  methods: {
    initialize: function() {
      // First loaded needed data and then process it. This is to prevent one
      // call to initialize clobbering a later call to initialize. That is, do
      // all of the async stuff first and then make decisions based on the
      // values of the props.
      const promises = [];
      let loadedSharedEntity = null;
      if (this.entityId) {
        promises.push(
          this.loadSharedEntity(this.entityId).then(
            sharedEntity => (loadedSharedEntity = sharedEntity)
          )
        );
      }
      if (
        !this.entityId &&
        (!this.sharedEntity || !this.sharedEntity.entityId) &&
        !this.defaultGatewayUsersGroup
      ) {
        promises.push(
          services.GroupService.list({ limit: -1 }).then(groups => {
            this.groups = groups;
            // If a new sharedEntity, automatically add the defaultGatewayUsersGroup
            groups
              .filter(group => group.isDefaultGatewayUsersGroup)
              .forEach(group => (this.defaultGatewayUsersGroup = group));
          })
        );
      }
      Promise.all(promises).then(() => {
        if (this.sharedEntity) {
          this.localSharedEntity = this.sharedEntity.clone();
        } else if (this.entityId) {
          this.localSharedEntity = loadedSharedEntity;
        } else {
          this.localSharedEntity = new models.SharedEntity();
        }
        if (
          !this.localSharedEntity.entityId &&
          this.autoAddDefaultGatewayUsersGroup
        ) {
          this.localSharedEntity.addGroup(this.defaultGatewayUsersGroup);
          this.emitUnsavedEvent();
        }
      });
    },
    loadSharedEntity(entityId) {
      return services.SharedEntityService.retrieve({ lookup: entityId });
    },
    /**
     * Merge the persisted SharedEntity with the local SharedEntity
     * instance and save it, returning a Promise.
     */
    mergeAndSave: function(entityId) {
      return services.SharedEntityService.merge({
        lookup: entityId,
        data: this.localSharedEntity
      }).then(sharedEntity => {
        this.localSharedEntity = sharedEntity;
        this.emitSavedEvent();
      });
    },
    saveSharedEntity: function() {
      // If we don't have an entityId we can't create a SharedEntity. Instead,
      // we'll just emit 'unsaved' to let parent know that sharing has changed.
      // It will be up to parent to call `mergeAndSave(entityId)` once there is
      // an entityId or merge the sharedEntity itself.
      if (this.localSharedEntity.entityId) {
        services.SharedEntityService.update({
          data: this.localSharedEntity,
          lookup: this.localSharedEntity.entityId
        }).then(sharedEntity => {
          this.localSharedEntity = sharedEntity;
          this.emitSavedEvent();
        });
      } else {
        this.emitUnsavedEvent();
      }
    },
    emitSavedEvent() {
      this.$emit("saved", this.localSharedEntity);
    },
    emitUnsavedEvent() {
      this.$emit("unsaved", this.localSharedEntity);
    },
    cancelEditSharedEntity: function() {
      this.localSharedEntity = this.sharedEntityCopy;
    },
    openSharingSettingsModal: function() {
      this.$refs.sharingSettingsModal.show();
    },
    showSharingSettingsModal: function() {
      this.sharedEntityCopy = this.localSharedEntity.clone();
      if (!this.users) {
        services.ServiceFactory.service("UserProfiles")
          .list()
          .then(users => (this.users = users));
      }
      if (!this.groups) {
        services.GroupService.list({ limit: -1 }).then(groups => {
          this.groups = groups;
        });
      }
    }
  },
  mounted: function() {
    // Only run initialize when mounted since it may add the default gateways
    // group automatically (autoAddDefaultGatewayUsersGroup)
    this.initialize();
  },
  watch: {
    sharedEntity(newSharedEntity) {
      this.localSharedEntity = newSharedEntity
        ? newSharedEntity.clone()
        : new models.SharedEntity();
    },
    entityId(newEntityId, oldEntityId) {
      if (newEntityId && newEntityId !== oldEntityId) {
        this.loadSharedEntity(newEntityId).then(
          sharedEntity => (this.localSharedEntity = sharedEntity)
        );
      }
    }
  }
};
</script>

<style scoped>
button {
  background-color: white;
  white-space: nowrap;
}
.share-button {
  display: inline-block;
}
.share-button >>> .modal-share-settings .modal-body {
  max-height: 50vh;
  min-height: 300px;
  overflow: auto;
}
.share-button >>> .modal-dialog {
  max-width: 800px;
  width: 60vw;
}
</style>
