<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message">
        <span v-if="saved">All the changes are saved.</span>
        <span v-if="!saved">Changes are not saved.</span>
      </div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button :data-product-uri="dataProductUri" :file-name="fileName"/>
        <b-button :disabled="saved" @click="fileContentChanged">Save</b-button>
      </div>
    </div>
    <div style="width: 100%" ref="editor"></div>
  </div>
</template>

<script>
import CodeMirror from "codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/abcdef.css";
import {utils} from "django-airavata-api";
import UserStorageDownloadButton from "./UserStorageDownloadButton";

export default {
  name: "user-storage-file-edit-viewer",
  props: {
    fileName: {
      required: true,
    },
    dataProductUri: {
      required: true,
    },
    mimeType: {
      required: true,
    },
    downloadUrl: {
      required: true,
    }
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
      if (changedFileContent) {
        utils.FetchUtils.put(`/api/data-products?product-uri=${this.dataProductUri}`, {
          fileContentText: changedFileContent,
        }).then(() => {
          this.$emit("file-content-changed", changedFileContent);
        });
      }

      this.saved = true;
    },
    setFileContent() {
      utils.FetchUtils.get(
        this.downloadUrl,
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
        extraKeys: {"Ctrl-Space": "autocomplete"},
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
