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
    <b-form-file
      :id="id"
      v-model="file"
      v-if="!isDataProductURI"
      :placeholder="experimentInput.userFriendlyDescription"
      :state="componentValidState"
      @input="fileChanged"
    />
  </div>
</template>

<script>
import { models, services, utils } from "django-airavata-api";
import { InputEditorMixin } from "django-airavata-workspace-plugin-api";
import DataProductViewer from "../DataProductViewer.vue";
import { components } from "django-airavata-common-ui";

export default {
  name: "file-input-editor",
  mixins: [InputEditorMixin],
  components: {
    DataProductViewer,
    "delete-link": components.DeleteLink
  },
  computed: {
    isDataProductURI() {
      // Just assume that if the value is a string then it's a data product URL
      return this.value && typeof this.value === "string";
    }
  },
  data() {
    return {
      dataProduct: null,
      file: null
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
        .catch(err => utils.FetchUtils.reportError);
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
    }
  }
};
</script>
