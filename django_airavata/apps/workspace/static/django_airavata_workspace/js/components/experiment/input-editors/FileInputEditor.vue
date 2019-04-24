<template>
  <div>
    <div
      class="row"
      v-if="isDataProductURI && dataProduct"
    >
      <div class="col mr-auto">
        <data-product-viewer
          :data-product="dataProduct"
          :input-file="true"
        />
      </div>
      <div class="col-auto">
        <delete-link @delete="deleteDataProduct">
          Are you sure you want to delete input file {{ dataProduct.filename }}?
        </delete-link>
        <b-link @click="unselect" class="ml-2 text-secondary">
          Unselect
          <i class="fa fa-times" aria-hidden="true"></i>
        </b-link>
      </div>
    </div>
    <div class="row">
      <div class="col">

        <b-form-file
          :id="id"
          v-model="file"
          v-if="!isDataProductURI"
          :placeholder="experimentInput.userFriendlyDescription"
          :state="componentValidState"
          @input="fileChanged"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { services, utils } from "django-airavata-api";
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
        this.$emit('uploadstart');
        utils.FetchUtils.post("/api/upload", data, "", {showSpinner: false}).then(
          result => {
            this.data = result["data-product-uri"];
            // TODO: change upload to return serialized data product
            this.loadDataProduct(this.data);
            this.valueChanged();
          }
        ).finally(() => this.$emit('uploadend'));
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
