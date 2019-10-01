<template>
  <div v-if="isSelectingFile">
    <user-storage-file-selection-container
      @file-selected="fileSelected"
      @cancel="cancelFileSelection"
      :selected-data-product-uris="selectedDataProductURIs"
    />
  </div>
  <div
    class="d-flex align-items-baseline"
    v-else
  >
    <b-button
      @click="isSelectingFile=true"
      class="input-file-option"
    >Select file from storage</b-button>
    <span class="text-muted mx-3">OR</span>
    <b-form-group
      :description="maxFileUploadSizeMessage"
      :state="fileUploadState"
      :invalid-feedback="fileUploadInvalidFeedback"
      class="input-file-option"
    >
      <uppy
        ref="uppy"
        xhr-upload-endpoint="/api/upload"
        tus-upload-finish-endpoint="/api/tus-upload-finish"
        @upload-success="uploadSuccess"
        @upload-started="$emit('uploadstart')"
        @upload-finished="uploadFinished"
        :multiple="multiple"
      />
    </b-form-group>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import UserStorageFileSelectionContainer from "../../storage/UserStorageFileSelectionContainer";
import Uppy from "./Uppy";

export default {
  name: "input-file-selector",
  props: {
    multiple: {
      type: Boolean,
      default: false
    },
    selectedDataProductURIs: {
      type: Array,
      default: () => []
    }
  },
  components: {
    UserStorageFileSelectionContainer,
    Uppy
  },
  computed: {
    maxFileUploadSizeMB() {
      return this.settings
        ? this.settings.fileUploadMaxFileSize / 1024 / 1024
        : 0;
    },
    maxFileUploadSizeMessage() {
      if (this.maxFileUploadSizeMB) {
        return (
          "Max file upload size is " +
          Math.round(this.maxFileUploadSizeMB) +
          " MB"
        );
      } else {
        return null;
      }
    },
    fileTooLarge() {
      return (
        this.settings &&
        this.settings.fileUploadMaxFileSize &&
        this.file &&
        this.file.size > this.settings.fileUploadMaxFileSize
      );
    },
    fileUploadState() {
      if (this.fileTooLarge) {
        return false;
      } else {
        return null;
      }
    },
    fileUploadInvalidFeedback() {
      if (this.fileTooLarge) {
        return (
          "File selected is larger than " + this.maxFileUploadSizeMB + " MB"
        );
      } else {
        return null;
      }
    }
  },
  data() {
    return {
      file: null,
      isSelectingFile: false,
      settings: null
    };
  },
  created() {
    services.SettingsService.get().then(s => (this.settings = s));
  },
  methods: {
    unselect() {
      this.file = null;
    },
    fileSelected(dataProductURI) {
      this.isSelectingFile = false;
      this.$emit("selected", dataProductURI);
    },
    cancelFileSelection() {
      this.isSelectingFile = false;
      this.unselect();
    },
    uploadSuccess(result) {
      const dataProduct = new models.DataProduct(result["data-product"]);
      this.$emit("selected", dataProduct.productUri, dataProduct);
    },
    uploadFinished() {
      this.$emit('uploadend');
      this.$refs.uppy.reset();
    }
  }
};
</script>

<style scoped>
.input-file-option {
  flex: 1 1 50%;
}
</style>
