<template>
  <user-storage-text-edit-viewer
    v-if="isText"
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
  <user-storage-image-edit-viewer
    v-else-if="isImage"
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
  <user-storage-audio-edit-viewer
    v-else-if="isAudio"
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
  <user-storage-video-edit-viewer
    v-else-if="isVideo"
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
  <user-storage-pdf-edit-viewer
    v-else-if="isPdf"
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
  <user-storage-default-edit-viewer
    v-else
    :file-name="fileName"
    :data-product-uri="dataProductUri"
    :mimeType="mimeType"
    :downloadUrl="downloadUrl"
    @file-content-changed="
      (fileContent) => $emit('file-content-changed', fileContent)
    "
  />
</template>

<script>
import UserStorageTextEditViewer from "./UserStorageTextEditViewer";
import UserStorageImageEditViewer from "./UserStorageImageEditViewer";
import UserStorageDefaultEditViewer from "./UserStorageDefaultEditViewer";
import UserStorageAudioEditViewer from "./UserStorageAudioEditViewer";
import UserStorageVideoEditViewer from "./UserStorageVideoEditViewer";
import UserStoragePdfEditViewer from "./UserStoragePdfEditViewer";

export default {
  name: "user-storage-file-edit-viewer",
  props: {
    fileName: {
      required: true,
    },
    dataProductUri: {
      required: true,
    },
    mimeType: {
      required: true,
    }
  },
  computed: {
    isText() {
      return /text\/.*/.test(this.mimeType);
    },
    isImage() {
      return /image\/.*/.test(this.mimeType);
    },
    isAudio() {
      return /audio\/.*/.test(this.mimeType);
    },
    isVideo() {
      return /video\/.*/.test(this.mimeType);
    },
    isPdf() {
      return /pdf/.test(this.mimeType);
    },
    downloadUrl() {
      return `/sdk/download/?data-product-uri=${this.dataProductUri}`;
    }
  },
  components: {
    UserStorageTextEditViewer,
    UserStorageImageEditViewer,
    UserStorageDefaultEditViewer,
    UserStorageAudioEditViewer,
    UserStorageVideoEditViewer,
    UserStoragePdfEditViewer,
  },
};
</script>

<style>
.user-storage-file-edit-viewer-status {
  display: flex;
  padding-bottom: 10px;
}

.user-storage-file-edit-viewer-status
.user-storage-file-edit-viewer-status-message {
  flex: 1;
  color: #919191;
  font-size: 14px;
}

.user-storage-file-edit-viewer-status
.user-storage-file-edit-viewer-status-actions
button,
.user-storage-file-edit-viewer-status
.user-storage-file-edit-viewer-status-actions
a {
  margin-right: 3px;
  margin-left: 3px;
}

.user-storage-file-edit-viewer-no-preview {
  font-size: 36px;
  color: #c0c4c7;
  text-align: center;
  padding: 20px;
}
</style>
