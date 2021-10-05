<template>
  <div>
    <b-link :href="storageFileViewRouteUrl()" @click="showFilePreview($event)">
      {{ fileName }}
    </b-link>
    <b-modal :title="fileName" ref="modal" scrollable size="lg" static lazy>
      <user-storage-file-edit-viewer
        :file-name="fileName"
        :data-product-uri="dataProductUri"
        :mime-type="mimeType"
        @file-content-changed="
          (fileContent) => $emit('file-content-changed', fileContent)
        "
      />
      <template slot="modal-footer">
        <a :href="storageFileViewRouteUrl()" target="_blank"
          >Open in a new window</a
        >
      </template>
    </b-modal>
  </div>
</template>

<script>
import UserStorageFileEditViewer from "./UserStorageEditViewer";

export default {
  name: "user-storage-link",
  components: { UserStorageFileEditViewer },
  props: {
    fileName: {
      required: true,
    },
    dataProductUri: {
      required: true,
    },
    mimeType: {
      required: true,
    },
    allowPreview: {
      default: true,
      required: false,
    },
  },
  methods: {
    showFilePreview(event) {
      if (this.allowPreview) {
        this.$refs.modal.show();
        event.preventDefault();
      }
    },
    storageFileViewRouteUrl() {
      // This endpoint can handle XHR upload or a TUS uploadURL
      return `/workspace/storage/~?dataProductUri=${this.dataProductUri}`;
    },
  },
};
</script>
