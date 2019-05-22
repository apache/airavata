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
        class="ml-2"
        @delete="deleteDataProduct"
      >
        Are you sure you want to delete input file {{ dataProduct.filename }}?
      </delete-link>
      <b-link
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
        "/api/delete-file?data-product-uri=" + encodeURIComponent(this.value)
      ).then(() => {
        this.data = null;
        this.valueChanged();
      });
    },
    fileChanged() {
      if (this.file) {
        let data = new FormData();
        data.append("file", this.file);
        data.append("project-id", this.experiment.projectId);
        data.append("experiment-name", this.experiment.experimentName);
        this.$emit("uploadstart");
        // TODO: use the experimentDataDir off the experiment model as the path
        // to upload to
        utils.FetchUtils.post("/api/user-storage/~/tmp/", data, "", { showSpinner: false })
          .then(result => {
            this.dataProduct = new models.DataProduct(result["uploaded"]);
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
