<template>
  <div>
    <user-storage-create-view
      v-if="includeCreateFileAction && userStoragePath && isDir"
      :user-storage-path="userStoragePath"
      :storage-path="storagePath"
      @upload-finished="$emit('upload-finished')"
      @add-directory="(dirName) => $emit('add-directory', dirName)"
    />
    <user-storage-path-breadcrumb
      v-if="userStoragePath && isDir"
      :parts="userStoragePath.parts"
      @directory-selected="$emit('directory-selected', $event)"
    />

    <user-storage-edit-viewer
      v-if="userStoragePath && isFile"
      :file-name="file.name"
      :data-product-uri="file.dataProductURI"
      :mime-type="file.mimeType"
      @file-content-changed="
        (fileContent) => $emit('file-content-changed', fileContent)
      "
    />

    <b-table
      v-if="userStoragePath && isDir"
      :fields="fields"
      :items="items"
      sort-by="name"
    >
      <template slot="cell(name)" slot-scope="data">
        <b-link
          v-if="data.item.type === 'dir'"
          @click="directorySelected(data.item)"
        >
          <i class="fa fa-folder-open"></i> {{ data.item.name }}
        </b-link>
        <user-storage-link
          v-else
          :data-product-uri="data.item.dataProductURI"
          :mime-type="data.item.mimeType"
          :file-name="data.item.name"
          :allow-preview="allowPreview"
        />
      </template>
      <template slot="cell(modifiedTimestamp)" slot-scope="data">
        <human-date :date="data.item.modifiedTime" />
      </template>
      <template slot="cell(actions)" slot-scope="data">
        <b-button
          v-if="includeSelectFileAction && data.item.type === 'file'"
          @click="$emit('file-selected', data.item)"
          :disabled="isAlreadySelected(data.item)"
          variant="primary"
        >
          Select
        </b-button>

        <b-link
          v-if="data.item.type === 'file'"
          class="action-link"
          :href="`${data.item.downloadURL}&download`"
        >
          Download File
          <i class="fa fa-download" aria-hidden="true"></i>
        </b-link>
        <!-- max download directory size is 1GB, see airavata_django_portal_sdk/views.py -->
        <b-link
          v-if="data.item.type === 'dir'"
          class="action-link"
          :href="`/sdk/download-dir/?path=${data.item.path}`"
          :disabled="data.item.size >= Math.pow(10, 9)"
        >
          Download Zip
          <i class="fa fa-file-archive" aria-hidden="true"></i>
        </b-link>
        <delete-link v-if="includeDeleteAction" @delete="deleteItem(data.item)">
          Are you sure you want to delete <strong>{{ data.item.name }}</strong
          >?
        </delete-link>
      </template>
    </b-table>
  </div>
</template>
<script>
import UserStoragePathBreadcrumb from "./StoragePathBreadcrumb.vue";
import { components } from "django-airavata-common-ui";
import UserStorageCreateView from "./UserStorageCreateView";
import UserStorageEditViewer from "./storage-edit/UserStorageEditViewer";
import UserStorageLink from "./storage-edit/UserStorageLink";

export default {
  name: "user-storage-path-viewer",
  props: {
    allowPreview: {
      default: true,
      required: false,
    },
    userStoragePath: {
      required: true,
    },
    storagePath: {
      required: true,
    },
    includeDeleteAction: {
      type: Boolean,
      default: true,
    },
    includeSelectFileAction: {
      type: Boolean,
      default: false,
    },
    includeCreateFileAction: {
      type: Boolean,
      default: true,
    },
    downloadInNewWindow: {
      type: Boolean,
      default: false,
    },
    selectedDataProductUris: {
      type: Array,
      default: () => [],
    },
  },
  components: {
    UserStorageLink,
    "delete-link": components.DeleteLink,
    "human-date": components.HumanDate,
    UserStoragePathBreadcrumb,
    UserStorageCreateView,
    UserStorageEditViewer,
  },
  computed: {
    isDir() {
      return this.userStoragePath.isDir;
    },
    isFile() {
      return !this.userStoragePath.isDir;
    },

    // Return the first file available. This is assuming the path is a file.
    file() {
      return this.userStoragePath.files[0];
    },

    fields() {
      return [
        {
          label: "Name",
          key: "name",
          sortable: true,
        },
        {
          label: "Size",
          key: "size",
          sortable: true,
          formatter: (value) => this.getFormattedSize(value),
        },
        {
          label: "Last Modified",
          key: "modifiedTimestamp",
          sortable: true,
        },
        {
          label: "Actions",
          key: "actions",
        },
      ];
    },
    items() {
      if (this.userStoragePath) {
        const dirs = this.userStoragePath.directories
          .filter((d) => !d.hidden)
          .map((d) => {
            return {
              name: d.name,
              path: d.path,
              type: "dir",
              modifiedTime: d.modifiedTime,
              modifiedTimestamp: d.modifiedTime.getTime(), // for sorting
              size: d.size,
            };
          });
        const files = this.userStoragePath.files.map((f) => {
          return {
            name: f.name,
            mimeType: f.mimeType,
            type: "file",
            dataProductURI: f.dataProductURI,
            downloadURL: f.downloadURL,
            modifiedTime: f.modifiedTime,
            modifiedTimestamp: f.modifiedTime.getTime(), // for sorting
            size: f.size,
          };
        });
        return dirs.concat(files);
      } else {
        return [];
      }
    },
    downloadTarget() {
      return this.downloadInNewWindow ? "_blank" : "_self";
    },
  },
  methods: {
    getFormattedSize(size) {
      if (size > Math.pow(2, 30)) {
        return Math.round(size / Math.pow(2, 30)) + " GB";
      } else if (size > Math.pow(2, 20)) {
        return Math.round(size / Math.pow(2, 20)) + " MB";
      } else if (size > Math.pow(2, 10)) {
        return Math.round(size / Math.pow(2, 10)) + " KB";
      } else {
        return size + " bytes";
      }
    },
    deleteItem(item) {
      if (item.type === "dir") {
        this.$emit("delete-dir", item.path);
      } else if (item.type === "file") {
        this.$emit("delete-file", item.dataProductURI);
      }
    },
    directorySelected(item) {
      this.$emit("directory-selected", item.path);
    },
    isAlreadySelected(item) {
      return (
        this.selectedDataProductUris.find(
          (uri) => item.type === "file" && uri === item.dataProductURI
        ) !== undefined
      );
    },
  },
};
</script>
<style scoped>
.action-link + .delete-link {
  margin-left: 0.25rem;
}
a.disabled {
  pointer-events: none;
  color: var(--secondary);
}
</style>
