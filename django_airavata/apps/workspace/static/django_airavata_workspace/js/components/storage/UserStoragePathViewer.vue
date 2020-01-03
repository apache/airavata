<template>
  <div>
    <user-storage-path-breadcrumb
      v-if="userStoragePath"
      :parts="userStoragePath.parts"
      @directory-selected="$emit('directory-selected', $event)"
    />
    <b-table
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
          :href="data.item.downloadURL"
          :target="downloadTarget"
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

export default {
  name: "user-storage-path-viewer",
  props: {
    userStoragePath: {
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
    UserStoragePathBreadcrumb
  },
  computed: {
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
    }
  }
};
</script>

