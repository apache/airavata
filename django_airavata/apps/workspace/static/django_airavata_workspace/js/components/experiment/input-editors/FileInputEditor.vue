<template>
  <div>
    <div
      class="row"
      v-if="isDataProductURI && dataProduct"
    >
      <div class="col mr-auto">
        <data-product-viewer :data-product="dataProduct" :input-file="true"/>
      </div>
      <div class="col-auto">
        <delete-link @delete="deleteDataProduct">
          Are you sure you want to delete input file {{ dataProduct.filename }}?
        </delete-link>
      </div>
    </div>
    <div class="row">
      <div class="col">

        <b-form-file
          :id="id"
          v-model="data"
          v-if="!isDataProductURI"
          :placeholder="experimentInput.userFriendlyDescription"
          :state="componentValidState"
          @input="valueChanged"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
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
      dataProduct: null
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
      // Just null out the 'data' field. Backend will delete the file from storage
      this.data = null;
      this.valueChanged();
    }
  }
};
</script>
