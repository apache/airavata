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
      class="d-flex"
      v-if="!isSelectingFile && !isDataProductURI"
    >
      <!-- TODO: fix layout -->
      <b-button @click="isSelectingFile=true">Select user file</b-button>
      <span class="text-muted">OR</span>
      <b-form-file
        :id="id"
        v-model="file"
        v-if="!isDataProductURI"
        :state="componentValidState"
        @input="fileChanged"
      />
    </div>
  </div>
</template>

<script>
import { models, services, utils } from "django-airavata-api";
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import DataProductViewer from "../DataProductViewer.vue";
import { components } from "django-airavata-common-ui";
import UserStorageFileSelectionContainer from "../../storage/UserStorageFileSelectionContainer";

export default {
  name: "file-input-editor",
  mixins: [InputEditorMixin],
  components: {
    DataProductViewer,
    "delete-link": components.DeleteLink,
    UserStorageFileSelectionContainer
  },
  computed: {
    isDataProductURI() {
      // Just assume that if the value is a string then it's a data product URL
      return this.value && typeof this.value === "string";
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
    }
  },
  data() {
    return {
      dataProduct: null,
      file: null,
      isSelectingFile: false
    };
  },
  created() {
    if (this.isDataProductURI) {
      this.loadDataProduct(this.value);
    }
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
      if (this.file) {
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
    }
  }
};
</script>
