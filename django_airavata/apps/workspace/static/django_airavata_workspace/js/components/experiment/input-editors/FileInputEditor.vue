<template>
  <div>
    <div
      class="d-flex"
      v-if="isDataProductURI && dataProduct"
    >
      <data-product-viewer
        class="mr-auto"
        :data-product="dataProduct"
        :input-file="true"
      />
      <b-link @click="viewFile">
        View File <i class="fa fa-eye"></i>
        <span class="sr-only">View file</span>
      </b-link>
      <b-modal
        :title="dataProduct.productName"
        ref="modal"
        ok-only
      >
        <pre>{{ fileContent }}</pre>
      </b-modal>
      <delete-link
        v-if="dataProduct.isInputFileUpload"
        class="ml-2"
        @delete="deleteDataProduct"
      >
        Are you sure you want to delete input file {{ dataProduct.productName }}?
      </delete-link>
      <b-link
        v-else
        @click="unselect"
        class="ml-2 text-secondary"
      >
        Unselect
        <i
          class="fa fa-times"
          aria-hidden="true"
        ></i>
      </b-link>
    </div>
    <div v-if="isSelectingFile">
      <user-storage-file-selection-container
        @file-selected="fileSelected"
        @cancel="cancelFileSelection"
        :selected-data-product-uris="selectedDataProductURIs"
      />
    </div>
    <div
      class="d-flex align-items-baseline"
      v-if="!isSelectingFile && !isDataProductURI"
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
        <b-form-file
          :id="id"
          v-model="file"
          v-if="!isDataProductURI"
          placeholder="Upload file"
          @input="fileChanged"
          :state="fileUploadState"
        />
      </b-form-group>
    </div>
  </div>
</template>

<script>
import { models, services, utils } from "django-airavata-api";
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import { components } from "django-airavata-common-ui";
import UserStorageFileSelectionContainer from "../../storage/UserStorageFileSelectionContainer";

export default {
  name: "file-input-editor",
  mixins: [InputEditorMixin],
  components: {
    "data-product-viewer": components.DataProductViewer,
    "delete-link": components.DeleteLink,
    UserStorageFileSelectionContainer
  },
  computed: {
    isDataProductURI() {
      // Just assume that if the value is a string then it's a data product URL
      return (
        this.value &&
        typeof this.value === "string" &&
        this.value.startsWith("airavata-dp://")
      );
    },
    // When used in the MultiFileInputEditor, don't allow selecting the same
    // file more than once. This computed property creates an array of already
    // selected files.
    selectedDataProductURIs() {
      if (
        this.experimentInput.type === models.DataType.URI_COLLECTION &&
        this.experimentInput.value
      ) {
        return this.experimentInput.value.split(",");
      } else {
        return [];
      }
    },
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
      dataProduct: null,
      file: null,
      isSelectingFile: false,
      settings: null,
      fileContent: null
    };
  },
  created() {
    if (this.isDataProductURI) {
      this.loadDataProduct(this.value);
    }
    services.SettingsService.get().then(s => (this.settings = s));
  },
  methods: {
    loadDataProduct(dataProductURI) {
      services.DataProductService.retrieve({ lookup: dataProductURI }).then(
        dataProduct => (this.dataProduct = dataProduct)
      );
    },
    deleteDataProduct() {
      utils.FetchUtils.delete(
        "/api/delete-file?data-product-uri=" + encodeURIComponent(this.value),
        { ignoreErrors: true }
      )
        .then(() => {
          this.data = null;
          this.valueChanged();
        })
        .catch(err => {
          // Ignore 404 Not Found errors, file no longer exists so assume was
          // already deleted
          if (err.details.status === 404) {
            this.data = null;
            this.valueChanged();
          } else {
            throw err;
          }
        })
        .catch(utils.FetchUtils.reportError);
    },
    fileChanged() {
      if (this.file && !this.fileTooLarge) {
        let data = new FormData();
        data.append("file", this.file);
        this.$emit("uploadstart");
        utils.FetchUtils.post("/api/upload", data, "", { showSpinner: false })
          .then(result => {
            this.dataProduct = new models.DataProduct(result["data-product"]);
            this.data = this.dataProduct.productUri;
            this.valueChanged();
          })
          .finally(() => this.$emit("uploadend"));
      }
    },
    unselect() {
      this.file = null;
      this.data = null;
      this.valueChanged();
    },
    fileSelected(dataProductURI) {
      this.data = dataProductURI;
      this.isSelectingFile = false;
      this.loadDataProduct(dataProductURI);
      this.valueChanged();
    },
    cancelFileSelection() {
      this.isSelectingFile = false;
      this.unselect();
    },
    viewFile() {
      this.fileContent = null;
      fetch(this.dataProduct.downloadURL, {
        credentials: "same-origin"
      })
        .then(result => result.text())
        .then(text => {
          this.fileContent = text;
          this.$refs.modal.show();
        });
    }
  }
};
</script>

<style scoped>
.input-file-option {
  flex: 1 1 50%;
}
</style>
