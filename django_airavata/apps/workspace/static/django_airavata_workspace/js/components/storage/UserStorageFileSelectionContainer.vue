<template>
  <b-card header="Select a file">
    <user-storage-path-viewer
      :user-storage-path="userStoragePath"
      :storage-path="storagePath"
      @directory-selected="directorySelected"
      @file-selected="fileSelected"
      :include-delete-action="false"
      :include-select-file-action="true"
      :download-in-new-window="true"
      :selected-data-product-uris="selectedDataProductUris"
    >
    </user-storage-path-viewer>
    <template slot="footer">
      <div class="d-flex justify-content-end">
        <b-link
          class="text-secondary"
          @click="$emit('cancel')"
        >Cancel</b-link>
      </div>
    </template>
  </b-card>
</template>

<script>
import { services } from "django-airavata-api";
import UserStoragePathViewer from "./UserStoragePathViewer";

// Keep track of most recent path so that when user needs to select an
// additional file they are taken back to the last path
let mostRecentPath = "~";

export default {
  name: "user-storage-file-selection-container",
  computed: {
    /**
     * @returns {string} user storage path with an ending slash.
     */
    storagePath() {
      let _storagePath = /~.*$/.exec(this.$route.fullPath);
      if (_storagePath && _storagePath.length > 0) {
        _storagePath = _storagePath[0];
      } else {
        _storagePath = this.$route.path;
      }

      // Validate to have the ending slash.
      if (!_storagePath.endsWith("/")) {
        _storagePath += "/";
      }

      return _storagePath;
    }
  },
  props: {
    selectedDataProductUris: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      userStoragePath: null
    };
  },
  components: {
    UserStoragePathViewer
  },
  created() {
    return this.loadUserStoragePath(mostRecentPath);
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get({
        path
      }).then(result => (this.userStoragePath = result));
    },
    directorySelected(path) {
      mostRecentPath = "~/" + path;
      return this.loadUserStoragePath(mostRecentPath);
    },
    fileSelected(file) {
      this.$emit("file-selected", file.dataProductURI);
    }
  }
};
</script>

