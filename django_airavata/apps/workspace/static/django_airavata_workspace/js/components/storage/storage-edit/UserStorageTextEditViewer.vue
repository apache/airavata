<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message">
        <span v-if="saved">All the changes are saved.</span>
        <span v-if="!saved">Changes are not saved.</span>
      </div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :file="file" />
        <b-button :disabled="saved" @click="fileContentChanged">Save </b-button>
      </div>
    </div>
    <div style="width: 100%" ref="editor"></div>
  </div>
</template>

<script>
import CodeMirror from "codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/abcdef.css";
import { utils } from "django-airavata-api";
import UserStorageDownloadButton from "./UserStorageDownloadButton";

export default {
  name: "user-storage-file-edit-viewer",
  props: {
    file: {
      required: true,
    },
  },
  components: {
    UserStorageDownloadButton: UserStorageDownloadButton,
  },
  data() {
    return {
      fileContent: "",
      saved: true,
      editor: null,
    };
  },
  mounted() {
    this.setFileContent();
  },
  destroyed() {
    this.editor.getWrapperElement().remove();
  },
  methods: {
    fileContentChanged() {
      const changedFileContent = this.editor.getDoc().getValue();
      this.$emit("file-content-changed", changedFileContent);
      this.saved = true;
    },
    setFileContent() {
      utils.FetchUtils.get(
        this.file.downloadURL,
        "",
        {
          ignoreErrors: false,
          showSpinner: true,
        },
        "text"
      ).then((res) => {
        this.fileContent = res;
        this.setFileContentEditor(this.fileContent);
      });
    },
    setFileContentEditor(value = "") {
      this.editor = CodeMirror(this.$refs.editor, {
        theme: "abcdef",
        mode: "text/plain",
        lineNumbers: true,
        lineWrapping: true,
        scrollbarStyle: "native",
        extraKeys: { "Ctrl-Space": "autocomplete" },
        value: value,
      });
      this.editor.on("change", () => {
        this.saved = false;
      });
    },
  },
};
</script>

<style>
.CodeMirror {
  height: auto;
  min-height: 600px;
}
</style>
