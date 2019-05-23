<template>
  <b-table :fields="fields" :items="items">
    <template slot="name" slot-scope="data">
      <router-link v-if="data.item.type === 'dir'" :to="'/~/' + data.item.path">{{ data.item.name }}</router-link>
      <b-link v-else :href="data.item.downloadURL">{{ data.item.name }}</b-link>
    </template>
  </b-table>
</template>
<script>
import { services } from "django-airavata-api";

export default {
  name: "user-storage-path-viewer",
  props: {
    path: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      userStoragePath: null,
      fields: [
        {
          label: "Name",
          key: "name"
        }
      ]
    };
  },
  computed: {
    items() {
      if (this.userStoragePath) {

        const dirs = this.userStoragePath.directories.map(d => {
          return {
            name: d.name,
            path: d.path,
            type: "dir"
          }
        });
        const files = this.userStoragePath.files.map(f => {
          return {
            name: f.name,
            type: "file",
            downloadURL: f.downloadURL
          }
        })
        return dirs.concat(files);
      } else {
        return [];
      }
    }
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get({ path }).then(result => {
        this.userStoragePath = result;
      });
    }
  },
  created() {
    this.loadUserStoragePath(this.path);
  },
  watch: {
    path(newValue) {
      this.loadUserStoragePath(newValue);
    }
  }
};
</script>

