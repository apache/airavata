<template>
  <user-storage-text-edit-viewer
    v-if="isText"
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
  />
  <user-storage-image-edit-viewer
    v-else-if="isImage"
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
  />
  <user-storage-audio-edit-viewer
    v-else-if="isAudio"
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
  />
  <user-storage-video-edit-viewer
    v-else-if="isVideo"
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
  />
  <user-storage-pdf-edit-viewer
    v-else-if="isPdf"
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
  />
  <user-storage-default-edit-viewer
    v-else
    :file="file"
    @file-content-changed="(fileContent) => $emit('file-content-changed', fileContent)"
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
      file: {
        required: true
      }
    },
    computed: {
      isText() {
        return /text\/.*/.test(this.file.mimeType);
      },
      isImage() {
        return /image\/.*/.test(this.file.mimeType);
      },
      isAudio() {
        return /audio\/.*/.test(this.file.mimeType);
      },
      isVideo() {
        return /video\/.*/.test(this.file.mimeType);
      },
      isPdf() {
        return /pdf/.test(this.file.mimeType);
      }
    },
    components: {
      UserStorageTextEditViewer,
      UserStorageImageEditViewer,
      UserStorageDefaultEditViewer,
      UserStorageAudioEditViewer,
      UserStorageVideoEditViewer,
      UserStoragePdfEditViewer
    },
  }
</script>

<style>
  .user-storage-file-edit-viewer-status {
    display: flex;
    padding-bottom: 10px;
  }

  .user-storage-file-edit-viewer-status .user-storage-file-edit-viewer-status-message {
    flex: 1;
    color: #919191;
    font-size: 14px;
  }

  .user-storage-file-edit-viewer-status .user-storage-file-edit-viewer-status-actions button,
  .user-storage-file-edit-viewer-status .user-storage-file-edit-viewer-status-actions a {
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
