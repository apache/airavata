<template>

  <div>
    <!-- TODO: rework layout here -->
    <template v-for="fileEntry in fileEntries">
      <div
        class="d-flex mb-2"
        :key="fileEntry.id"
      >
        <file-input-editor
          :value="fileEntry.value"
          :id="fileEntry.id"
          :experiment="experiment"
          :experiment-input="experimentInput"
          @input="updatedFile($event, fileEntry)"
          class="flex-grow-1"
        />
      </div>
    </template>
    <b-button @click="addFile">Add File</b-button>
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
        value: null
      });
    },
    updatedFile(newValue, fileEntry) {
      if (!newValue) {
        this.removeFile(fileEntry);
      } else {
        fileEntry.value = newValue;
      }
      this.data = this.fileEntries.map(e => e.value).join(",");
      this.valueChanged();
    },
    removeFile(fileEntry) {
      const index = this.fileEntries.findIndex(e => e.id === fileEntry.id);
      this.fileEntries.splice(index, 1);
    },
    createValueArray(value) {
      if (this.value && typeof this.value === "string") {
        return this.value.split(",");
      } else {
        return [];
      }
    },
    createFileEntries(value) {
      const valueArray = this.createValueArray(value);
      return valueArray.map(v => {
        return {
          id: this.id + "-" + v,
          value: v
        };
      });
    },
  },
  watch: {
    value(newValue) {
      this.data = this.createFileEntries(newValue);
    }
  }
};
</script>

