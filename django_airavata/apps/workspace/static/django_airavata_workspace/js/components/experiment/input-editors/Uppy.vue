<template>
  <div>
    <div ref="fileInput" />
    <div ref="statusBar" />
  </div>
</template>

<script>
import { services, utils } from "django-airavata-api";

import Uppy from "@uppy/core";
import FileInput from "@uppy/file-input";
import StatusBar from "@uppy/status-bar";
import Tus from "@uppy/tus";
import XHRUpload from "@uppy/xhr-upload";

import "@uppy/core/dist/style.css";
import "@uppy/status-bar/dist/style.css";
import "@uppy/file-input/dist/style.css";

// TODO: dispatch upload start
// TODO: maybe use dragdrop UI?
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
    }
  },
  methods: {
    initUppy() {
      this.uppy = Uppy({
        // TODO: set id
        autoProceed: true,
        // TODO: add maxFileSize restriction
        debug: true
      });
      this.uppy.use(FileInput, { target: this.$refs.fileInput, pretty: false });
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
            'X-CSRFToken': utils.FetchUtils.getCSRFToken()
          },
          fieldName: 'file'
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
