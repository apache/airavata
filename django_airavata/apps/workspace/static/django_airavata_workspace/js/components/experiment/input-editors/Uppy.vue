<template>
  <div class="custom-Uppy">
    <div ref="dragDrop" />
    <div ref="statusBar" />
  </div>
</template>

<script>
import { services, utils } from "django-airavata-api";

import Uppy from "@uppy/core";
import DragDrop from "@uppy/drag-drop";
import StatusBar from "@uppy/status-bar";
import Tus from "@uppy/tus";
import XHRUpload from "@uppy/xhr-upload";

import "@uppy/core/dist/style.min.css";
import "@uppy/status-bar/dist/style.min.css";
import "@uppy/drag-drop/dist/style.min.css";

export default {
  name: "uppy",
  props: {
    xhrUploadEndpoint: {
      type: String,
      required: true
    },
    // endpoint should accept POST request. Request will include form data with
    // the key uploadURL.
    tusUploadFinishEndpoint: {
      type: String,
      required: false
    },
    multiple: {
      type: Boolean,
      default: false
    }
  },
  mounted() {
    services.SettingsService.get().then(s => {
      this.settings = s;
      this.initUppy();
    });
  },
  destroyed() {
    // TODO: tear down the Uppy instance
  },
  data() {
    return {
      uppy: null
    };
  },
  methods: {
    initUppy() {
      this.uppy = Uppy({
        // TODO: set id
        autoProceed: true,
        // TODO: add maxFileSize restriction
        debug: true,
        restrictions: {
          maxNumberOfFiles: this.multiple ? null : 1,
        }
      });
      // TODO: add 'note' here as passed in through prop (Max file upload size is 64MB)
      this.uppy.use(DragDrop, { target: this.$refs.dragDrop });
      this.uppy.use(StatusBar, {
        target: this.$refs.statusBar,
        hideUploadButton: true,
        hideAfterFinish: false
      });
      if (this.settings.tusEndpoint) {
        this.uppy.use(Tus, { endpoint: this.settings.tusEndpoint });
        this.uppy.on("upload-success", (file, response) => {
          const data = new FormData();
          data.append("uploadURL", response.uploadURL);
          utils.FetchUtils.post(this.tusUploadFinishEndpoint, data, "", {
            showSpinner: false
          }).then(result => {
            this.$emit("upload-success", result);
          });
        });
      } else {
        this.uppy.use(XHRUpload, {
          endpoint: this.xhrUploadEndpoint,
          withCredentials: true,
          headers: {
            "X-CSRFToken": utils.FetchUtils.getCSRFToken()
          },
          fieldName: "file"
        });
        this.uppy.on("upload-success", (file, response) => {
          this.$emit("upload-success", response.body);
        });
      }
      this.uppy.on("upload", () => {
        this.$emit("upload-started");
      });
      this.uppy.on("complete", () => {
        this.$emit("upload-finished");
      });
    },
    reset() {
      this.uppy.reset();
    }
  }
};
</script>

<style scoped>
.custom-Uppy >>> .uppy-DragDrop-inner {
  padding: 5px 0px;
}
.custom-Uppy >>> .UppyIcon {
  display: none;
}
.custom-Uppy >>> .uppy-DragDrop-label {
  margin-bottom: 0px;
}
</style>
