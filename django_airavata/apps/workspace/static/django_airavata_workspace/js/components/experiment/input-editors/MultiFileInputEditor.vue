<template>
  <div>
    <div
      class="mb-2"
      v-for="fileEntry in fileEntries"
      :key="fileEntry.id"
    >
      <file-input-editor
        :value="fileEntry.value"
        :id="fileEntry.id"
        :experiment="experiment"
        :experiment-input="experimentInput"
        @input="updatedFile($event, fileEntry)"
        @uploadstart="uploadStart(fileEntry)"
        @uploadend="uploadEnd(fileEntry)"
      />
    </div>
  </div>
</template>

<script>
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import FileInputEditor from "./FileInputEditor.vue";

export default {
  name: "multi-file-input-editor",
  mixins: [InputEditorMixin],
  props: {
    value: {
      type: String
    }
  },
  components: {
    FileInputEditor
  },
  data() {
    return {
      newFileCount: 0,
      fileEntries: this.createFileEntries(this.value)
    };
  },
  computed: {},
  methods: {
    addFile() {
      this.fileEntries.push({
        id: this.id + "-" + this.newFileCount++,
        value: null,
        uploading: false
      });
    },
    updatedFile(newValue, fileEntry) {
      if (!newValue) {
        this.removeFile(fileEntry);
      } else {
        fileEntry.value = newValue;
      }
      this.data = this.fileEntries
        .filter(e => e.value) // exclude null entries
        .map(e => e.value)
        .join(",");
      this.valueChanged();
    },
    removeFile(fileEntry) {
      const index = this.fileEntries.findIndex(e => e.id === fileEntry.id);
      this.fileEntries.splice(index, 1);
    },
    createValueArray(value) {
      if (value && typeof value === "string") {
        return value.split(",");
      } else {
        return [];
      }
    },
    createFileEntries(value) {
      const valueArray = this.createValueArray(value);
      const fileEntries = valueArray.map(v => {
        return {
          id: this.id + "-" + v,
          value: v,
          uploading: false
        };
      });
      // Add a null entry to accept an additional upload
      fileEntries.push({
        id: this.id + "-" + this.newFileCount++,
        value: null,
        uploading: false
      });
      return fileEntries;
    },
    uploadStart(fileEntry) {
      fileEntry.uploading = true;
      this.$emit("uploadstart");
    },
    uploadEnd(fileEntry) {
      fileEntry.uploading = false;
      if (this.fileEntries.every(fe => !fe.uploading)) {
        this.$emit("uploadend");
      }
    }
  },
  watch: {
    value(newValue) {
      this.fileEntries = this.createFileEntries(newValue);
    }
  }
};
</script>
