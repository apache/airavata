<template>
  <div>
    <storage-path-breadcrumb
      v-if="experimentStoragePath"
      :parts="experimentStoragePath.parts"
      rootName="Exp Data Dir"
      @directory-selected="$emit('directory-selected', $event)"
    />

    <b-table
      v-if="experimentStoragePath"
      :fields="fields"
      :items="items"
      sort-by="name"
    >
      <template slot="cell(name)" slot-scope="data">
        <b-link
          v-if="data.item.type === 'dir'"
          @click="directorySelected(data.item)"
        >
          <i class="fa fa-folder-open"></i> {{ data.item.name }}</b-link
        >
        <b-link v-else :href="data.item.downloadURL" :target="downloadTarget">
          {{ data.item.name }}</b-link
        >
      </template>
      <template slot="cell(modifiedTimestamp)" slot-scope="data">
        <human-date :date="data.item.modifiedTime" />
      </template>
      <template slot="cell(actions)" slot-scope="data">
        <b-link
          v-if="data.item.type === 'file'"
          class="action-link"
          :href="`${data.item.downloadURL}&download`"
        >
          Download File
          <i class="fa fa-download" aria-hidden="true"></i>
        </b-link>
        <b-link
          v-if="data.item.type === 'dir'"
          class="action-link"
          :href="`/sdk/download-experiment-dir/${encodeURIComponent(
            experimentId
          )}/?path=${data.item.path}`"
        >
          Download Zip
          <i class="fa fa-file-archive" aria-hidden="true"></i>
        </b-link>
      </template>
    </b-table>
  </div>
</template>
<script>
import StoragePathBreadcrumb from "./StoragePathBreadcrumb.vue";
import { components } from "django-airavata-common-ui";

export default {
  name: "experiment-storage-path-viewer",
  props: {
    experimentStoragePath: {
      required: true,
    },
    downloadInNewWindow: {
      type: Boolean,
      default: false,
    },
    experimentId: {
      required: true,
    },
  },
  components: {
    "human-date": components.HumanDate,
    StoragePathBreadcrumb,
  },
  computed: {
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
      if (this.experimentStoragePath) {
        const dirs = this.experimentStoragePath.directories
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
        const files = this.experimentStoragePath.files.map((f) => {
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
    directorySelected(item) {
      this.$emit("directory-selected", item.path);
    },
  },
};
</script>
