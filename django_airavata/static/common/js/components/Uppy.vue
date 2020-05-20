<template>
  <div class="custom-Uppy">
    <div ref="dragDrop" />
    <div ref="statusBar" />
    <b-alert
      class="mt-1"
      :show="restrictionFailed"
      variant="danger"
    >{{ restrictionFailedMessage }}</b-alert>
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
    if (this.uppy) {
      this.uppy.close();
    }
  },
  data() {
    return {
      uppy: null,
      restrictionFailedMessage: null,
      settings: null
    };
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
    restrictionFailed() {
      return this.restrictionFailedMessage != null;
    }
  },
  methods: {
    initUppy() {
      this.uppy = Uppy({
        autoProceed: true,
        debug: true,
        restrictions: {
          maxNumberOfFiles: this.multiple ? null : 1,
          maxFileSize: this.settings.fileUploadMaxFileSize
        }
      });
      this.uppy.use(DragDrop, {
        target: this.$refs.dragDrop,
        note: this.maxFileUploadSizeMessage
      });
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
        this.restrictionFailedMessage = null;
      });
      this.uppy.on("restriction-failed", (file, error) => {
        this.restrictionFailedMessage = `${file.name}: ${error.message}`;
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
