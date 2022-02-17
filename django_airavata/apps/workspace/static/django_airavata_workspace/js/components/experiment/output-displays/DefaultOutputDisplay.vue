<template>
  <div>
    <!-- Show the final data products if available, otherwise, display intermediate outputs -->
    <template v-if="dataProducts.length > 0">
      <pre v-if="finalOutputText">
        {{ finalOutputText }}
      </pre>
      <div v-else v-for="dp in dataProducts" :key="dp.productUri">
        <img
          v-if="dp.isImage && dp.downloadURL"
          class="image-preview rounded"
          :src="dp.downloadURL"
        />
        <data-product-viewer :data-product="dp" :mime-type="fileMimeType" />
      </div>
    </template>

    <template v-else-if="intermediateOutputDataProduct">
      <pre v-if="intermediateOutputText">
        {{ intermediateOutputText }}
      </pre>
      <data-product-viewer
        v-else
        :data-product="intermediateOutputDataProduct"
        :mime-type="fileMimeType"
      />
    </template>
    <template v-else-if="intermediateOutputMultipleDataProducts">
      <div
        v-for="dp in intermediateOutputMultipleDataProducts"
        :key="dp.productUri"
      >
        <data-product-viewer :data-product="dp" :mime-type="fileMimeType" />
      </div>
    </template>
    <template v-else-if="!isExecuting && dataProducts.length === 0">
      <div class="d-flex justify-content-center text-secondary">
        There are no files for this application output.
      </div>
    </template>
  </div>
</template>

<script>
import { models, utils } from "django-airavata-api";
import DataProductViewer from "django-airavata-common-ui/js/components/DataProductViewer.vue";
import { mapGetters } from 'vuex';

const MAX_DISPLAY_TEXT_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

export default {
  name: "default-output-viewer",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true,
    },
    dataProducts: {
      type: Array,
      required: true,
    },
  },
  components: {
    DataProductViewer,
  },
  data() {
    return {
      intermediateOutputText: null,
      finalOutputText: null,
    };
  },
  async created() {
    // Check and load intermediate or final output as text if available and applicable
    this.loadIntermediateOutputText();
    this.loadFinalOutputText();
  },
  computed: {
    ...mapGetters("viewExperiment", ["isExecuting"]),
    fileMimeType() {
      if (this.experimentOutput.fileMetadataMimeType) {
        return this.experimentOutput.fileMetadataMimeType;
      } else if (
        this.experimentOutput.type === models.DataType.STDOUT ||
        this.experimentOutput.type === models.DataType.STDERR
      ) {
        return "text/plain";
      } else {
        return null;
      }
    },
    intermediateOutputProcessStatusState() {
      if (
        this.experimentOutput &&
        this.experimentOutput.intermediateOutput &&
        this.experimentOutput.intermediateOutput.processStatus
      ) {
        return this.experimentOutput.intermediateOutput.processStatus.state;
      } else {
        return null;
      }
    },
    intermediateOutputDataProduct() {
      if (
        this.experimentOutput &&
        this.experimentOutput.intermediateOutput &&
        this.experimentOutput.intermediateOutput.dataProducts &&
        this.experimentOutput.intermediateOutput.dataProducts.length === 1
      ) {
        return this.experimentOutput.intermediateOutput.dataProducts[0];
      } else {
        return null;
      }
    },
    intermediateOutputMultipleDataProducts() {
      if (
        this.experimentOutput &&
        this.experimentOutput.intermediateOutput &&
        this.experimentOutput.intermediateOutput.dataProducts &&
        this.experimentOutput.intermediateOutput.dataProducts.length > 1
      ) {
        return this.experimentOutput.intermediateOutput.dataProducts;
      } else {
        return null;
      }
    },
    intermediateOutputFileSize() {
      if (this.intermediateOutputDataProduct) {
        return this.intermediateOutputDataProduct.filesize;
      } else {
        return -1;
      }
    },
    isIntermediateOutputFileDisplayable() {
      return (
        this.intermediateOutputDataProduct &&
        (this.intermediateOutputDataProduct.isText ||
          this.fileMimeType === "text/plain") &&
        this.intermediateOutputDataProduct.downloadURL &&
        this.intermediateOutputDataProduct.filesize < MAX_DISPLAY_TEXT_FILE_SIZE
      );
    },
    isFinalOutputFileDisplayable() {
      return (
        this.dataProducts &&
        this.dataProducts.length === 1 &&
        (this.dataProducts[0].isText || this.fileMimeType === "text/plain") &&
        this.dataProducts[0].downloadURL &&
        this.dataProducts[0].filesize < MAX_DISPLAY_TEXT_FILE_SIZE
      );
    },
  },
  methods: {
    async loadIntermediateOutputText() {
      if (this.isIntermediateOutputFileDisplayable) {
        this.intermediateOutputText = await utils.FetchUtils.get(
          this.intermediateOutputDataProduct.downloadURL,
          "",
          {
            responseType: "text",
          }
        );
      }
    },
    async loadFinalOutputText() {
      if (this.isFinalOutputFileDisplayable) {
        this.finalOutputText = await utils.FetchUtils.get(
          this.dataProducts[0].downloadURL,
          "",
          {
            responseType: "text",
          }
        );
      }
    },
  },
  watch: {
    intermediateOutputFileSize() {
      this.loadIntermediateOutputText();
    },
    dataProducts(value, oldValue) {
      if ((!oldValue || oldValue.length === 0) && value && value.length > 0) {
        this.loadFinalOutputText();
      }
    },
  },
};
</script>
<style scoped>
.image-preview {
  display: block;
  max-width: 100%;
  max-height: 120px;
}
pre {
  max-height: 340px;
  overflow: auto;
  max-width: 100%;
  margin-bottom: 0;
  /* background-color: #efefef; */
  background-color: var(--light);
  border-style: solid;
  border-width: 1px;
  border-color: var(--gray);
  border-radius: 3px;
}
</style>
