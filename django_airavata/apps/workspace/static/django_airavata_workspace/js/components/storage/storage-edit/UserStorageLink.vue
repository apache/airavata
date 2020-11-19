<template>
  <b-link @click="showFilePreview($event)">
    {{ fileName }}
    <b-modal :title="fileName" ref="modal" scrollable size="lg">
      <user-storage-file-edit-viewer
        :file-name="fileName"
        :data-product-uri="dataProductUri"
        :mime-type="mimeType"
        @file-content-changed="
            (fileContent) => $emit('file-content-changed', fileContent)
          "
      />
      <template slot="modal-footer">
        <a>Open in a new window</a>
      </template>
    </b-modal>
  </b-link>
</template>

<script>
import UserStorageFileEditViewer from "./UserStorageEditViewer";

export default {
  name: "user-storage-link",
  components: {UserStorageFileEditViewer},
  props: {
    fileName: {
      required: true
    },
    dataProductUri: {
      required: true
    },
    mimeType: {
      required: true
    }
  },
  methods: {
    showFilePreview(event) {
      this.$refs.modal.show();
      event.preventDefault();
    },
  }
};
</script>
