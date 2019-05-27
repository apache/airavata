<template>
  <div>
    <user-storage-path-breadcrumb
      v-if="userStoragePath"
      :parts="userStoragePath.parts"
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
        <router-link
          v-if="data.item.type === 'dir'"
          :to="'/~/' + data.item.path"
        > <i class="fa fa-folder-open"></i> {{ data.item.name }}</router-link>
        <b-link
          v-else
          :href="data.item.downloadURL"
        > <i class="fa fa-download"></i> {{ data.item.name }}</b-link>
      </template>
      <template
        slot="createdTimestamp"
        slot-scope="data"
      >
        <span :title="data.item.createdTime.toString()">{{ fromNow(data.item.createdTime)}}</span>
      </template>
      <template
        slot="actions"
        slot-scope="data"
      >
        <delete-button @delete="deleteItem(data.item)">
          Are you sure you want to delete {{ data.item.name }}?
        </delete-button>
      </template>
    </b-table>
  </div>
</template>
<script>
import UserStoragePathBreadcrumb from "./UserStoragePathBreadcrumb.vue";
import { components } from "django-airavata-common-ui";
import moment from "moment";

export default {
  name: "user-storage-path-viewer",
  props: {
    userStoragePath: {
      required: true
    }
  },
  components: {
    "delete-button": components.DeleteButton,
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
    }
  },
  methods: {
    fromNow(date) {
      return moment(date).fromNow();
    },
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
    }
  }
};
</script>

