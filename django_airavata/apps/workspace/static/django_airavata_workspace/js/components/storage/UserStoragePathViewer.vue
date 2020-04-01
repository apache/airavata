<template>
  <div>
    <user-storage-create-view
      v-if="isDir"
      :user-storage-path="userStoragePath"
      :storage-path="storagePath"
      :username="username"
      @upload-success="$emit('upload-success')"
      @add-directory="(dirName) => $emit('add-directory', dirName)"
    />
    <user-storage-path-breadcrumb
      v-if="userStoragePath"
      :parts="userStoragePath.parts"
      @directory-selected="$emit('directory-selected', $event)"
    />
    <user-storage-text-edit-viewer
      v-if="isFile && isText"
      :file="file"
      @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
    />
    <user-storage-image-edit-viewer
      v-if="isFile && isImage"
      :file="file"
      @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
    />
    <b-table
      v-if="isDir"
      :fields="fields"
      :items="items"
      sort-by="name"
    >
      <template
        slot="name"
        slot-scope="data"
      >
        <b-link
          v-if="data.item.type === 'dir'"
          @click="directorySelected(data.item)"
        > <i class="fa fa-folder-open"></i> {{ data.item.name }}</b-link>
        <b-link
          v-else
          :href="storageFileViewRouteUrl(data.item)"
        > {{ data.item.name }}</b-link>
      </template>
      <template
        slot="createdTimestamp"
        slot-scope="data"
      >
        <human-date :date="data.item.createdTime" />
      </template>
      <template
        slot="actions"
        slot-scope="data"
      >
        <b-button
          v-if="includeSelectFileAction && data.item.type === 'file'"
          @click="$emit('file-selected', data.item)"
          :disabled="isAlreadySelected(data.item)"
          variant="primary"
        >
          Select
        </b-button>
        <delete-button
          v-if="includeDeleteAction"
          @delete="deleteItem(data.item)"
        >
          Are you sure you want to delete {{ data.item.name }}?
        </delete-button>
      </template>
    </b-table>
  </div>
</template>
<script>
import UserStoragePathBreadcrumb from "./UserStoragePathBreadcrumb.vue";
import { components } from "django-airavata-common-ui";
import UserStorageCreateView from "./UserStorageCreateView";
import UserStorageTextEditViewer from "./storage-edit/UserStorageTextEditViewer";
import UserStorageImageEditViewer from "./storage-edit/UserStorageImageEditViewer";

export default {
  name: "user-storage-path-viewer",
  props: {
    userStoragePath: {
      required: true
    },
    username: {
      required: true
    },
    storagePath: {
      required: true
    },
    includeDeleteAction: {
      type: Boolean,
      default: true
    },
    includeSelectFileAction: {
      type: Boolean,
      default: false
    },
    downloadInNewWindow: {
      type: Boolean,
      default: false
    },
    selectedDataProductUris: {
      type: Array,
      default: () => []
    }
  },
  components: {
    "delete-button": components.DeleteButton,
    "human-date": components.HumanDate,
    UserStoragePathBreadcrumb,
    UserStorageCreateView,
    UserStorageTextEditViewer,
    UserStorageImageEditViewer
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
      return this.userStoragePath.files[0]
    },

    isText() {
      return /text\/.*/.test(this.file.mimeType);
    },
    isImage() {
      return /image\/.*/.test(this.file.mimeType);
    },

    fields() {
      return [
        {
          label: "Name",
          key: "name",
          sortable: true
        },
        {
          label: "Size",
          key: "size",
          sortable: true,
          formatter: value => this.getFormattedSize(value)
        },
        {
          label: "Created Time",
          key: "createdTimestamp",
          sortable: true
        },
        {
          label: "Actions",
          key: "actions"
        }
      ];
    },
    items() {
      if (this.userStoragePath) {
        const dirs = this.userStoragePath.directories
          .filter(d => !d.hidden)
          .map(d => {
            return {
              name: d.name,
              path: d.path,
              type: "dir",
              createdTime: d.createdTime,
              createdTimestamp: d.createdTime.getTime(), // for sorting
              size: d.size
            };
          });
        const files = this.userStoragePath.files.map(f => {
          return {
            name: f.name,
            type: "file",
            dataProductURI: f.dataProductURI,
            downloadURL: f.downloadURL,
            createdTime: f.createdTime,
            createdTimestamp: f.createdTime.getTime(), // for sorting
            size: f.size
          };
        });
        return dirs.concat(files);
      } else {
        return [];
      }
    },
    downloadTarget() {
      return this.downloadInNewWindow ? "_blank" : "_self";
    }
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
          uri => item.type === "file" && uri === item.dataProductURI
        ) !== undefined
      );
    },
    storageFileViewRouteUrl(item) {
      // This endpoint can handle XHR upload or a TUS uploadURL
      return `/workspace/storage/${this.storagePath}${item.name}`;
    }
  }
};
</script>

