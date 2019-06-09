<template>
  <b-card header="Select a file">
    <user-storage-path-viewer
      :user-storage-path="userStoragePath"
      @directory-selected="directorySelected"
      @file-selected="fileSelected"
      :include-delete-action="false"
      :include-select-file-action="true"
      :download-in-new-window="true"
    />
    <!-- TODO: push this right? -->
    <b-link class="card-link">Cancel</b-link>
  </b-card>
</template>

<script>
import { services } from "django-airavata-api";
import UserStoragePathViewer from "./UserStoragePathViewer";

export default {
  name: "user-storage-file-selection-container",
  data() {
    return {
      userStoragePath: null
    };
  },
  components: {
    UserStoragePathViewer
  },
  created() {
    return this.loadUserStoragePath("~");
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get({
        path
      }).then(result => (this.userStoragePath = result));
    },
    directorySelected(path) {
      return this.loadUserStoragePath("~/" + path);
    },
    fileSelected(file) {
      this.$emit('file-selected', file.dataProductURI);
    }
  }
};
</script>

