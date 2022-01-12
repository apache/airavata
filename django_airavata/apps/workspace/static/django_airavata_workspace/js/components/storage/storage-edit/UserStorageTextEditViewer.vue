<template>
  <div>
    <div class="user-storage-file-edit-viewer-status">
      <div class="user-storage-file-edit-viewer-status-message">
        <span v-if="editAvailable && saved">All the changes are saved.</span>
        <span v-if="editAvailable && !saved">Changes are not saved.</span>
      </div>
      <div class="user-storage-file-edit-viewer-status-actions">
        <user-storage-download-button
          :data-product-uri="dataProductUri"
          :file-name="fileName"
        />
        <b-button
          v-if="editAvailable"
          :disabled="saved"
          @click="fileContentChanged"
          >Save</b-button
        >
      </div>
    </div>
    <div style="width: 100%" ref="editor" v-if="editAvailable"></div>
    <div class="user-storage-file-edit-viewer-no-preview" v-else>
      Inline edit not available. Click the <strong>Download</strong> button to
      download the file.
    </div>
  </div>
</template>

<script>
import CodeMirror from "codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/abcdef.css";
import { services, utils } from "django-airavata-api";
import UserStorageDownloadButton from "./UserStorageDownloadButton";

const MAX_EDIT_FILESIZE = 1024 * 1024;

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
      dataProduct: null,
    };
  },
  mounted() {
    this.setFileContent();
  },
  destroyed() {
    // this.editor is created only when the file is small enough to be
    // previewed/edited in browser
    if (this.editor) {
      this.editor.getWrapperElement().remove();
    }
  },
  computed: {
    editAvailable() {
      return !this.dataProduct || this.dataProduct.filesize < MAX_EDIT_FILESIZE;
    },
  },
  methods: {
    fileContentChanged() {
      const changedFileContent = this.editor.getDoc().getValue();
      if (changedFileContent) {
        utils.FetchUtils.put(
          `/api/data-products?product-uri=${this.dataProductUri}`,
          {
            fileContentText: changedFileContent,
          }
        ).then(() => {
          this.$emit("file-content-changed", changedFileContent);
        });
      }

      this.saved = true;
    },
    loadDataProduct() {
      return services.DataProductService.retrieve({
        lookup: this.dataProductUri,
      }).then((dataProduct) => {
        this.dataProduct = dataProduct;
        return dataProduct;
      });
    },
    setFileContent() {
      this.loadDataProduct().then(() => {
        if (this.editAvailable) {
          utils.FetchUtils.get(this.downloadUrl, "", {
            ignoreErrors: false,
            showSpinner: true,
            responseType: "text",
          }).then((res) => {
            this.fileContent = res;
            this.setFileContentEditor(this.fileContent);
          });
        }
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
