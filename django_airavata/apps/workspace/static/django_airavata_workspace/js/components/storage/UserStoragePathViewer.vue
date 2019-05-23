<template>
  <b-table
    :fields="fields"
    :items="items"
  >
    <template
      slot="name"
      slot-scope="data"
    >
      <router-link
        v-if="data.item.type === 'dir'"
        :to="'/~/' + data.item.path"
      >{{ data.item.name }}</router-link>
      <b-link
        v-else
        :href="data.item.downloadURL"
      >{{ data.item.name }}</b-link>
    </template>
  </b-table>
</template>
<script>
export default {
  name: "user-storage-path-viewer",
  props: {
    userStoragePath: {
      required: true
    }
  },
  computed: {
    fields() {
      return [
        {
          label: "Name",
          key: "name"
        }
      ];
    },
    items() {
      if (this.userStoragePath) {
        const dirs = this.userStoragePath.directories.map(d => {
          return {
            name: d.name,
            path: d.path,
            type: "dir"
          };
        });
        const files = this.userStoragePath.files.map(f => {
          return {
            name: f.name,
            type: "file",
            downloadURL: f.downloadURL
          };
        });
        return dirs.concat(files);
      } else {
        return [];
      }
    }
  }
};
</script>

