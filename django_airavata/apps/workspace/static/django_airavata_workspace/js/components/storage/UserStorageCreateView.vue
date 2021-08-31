<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4">Storage</h1>
        <p>
          <small class="text-muted"
            ><i class="fa fa-folder-open"></i> {{ username }}</small
          >
        </p>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <uppy
          class="mb-1"
          ref="file-upload"
          :xhr-upload-endpoint="uploadEndpoint"
          :tus-upload-finish-endpoint="uploadEndpoint"
          @upload-finished="uploadFinished"
          multiple
        />
      </div>
      <div class="col">
        <b-input-group>
          <b-form-input
            v-model="dirName"
            placeholder="New directory name"
            @keydown.native.enter="addDirectory"
          ></b-form-input>
          <b-input-group-append>
            <b-button @click="addDirectory" :disabled="!this.dirName"
              >Add directory
            </b-button>
          </b-input-group-append>
        </b-input-group>
      </div>
    </div>
  </div>
</template>

<script>
import { components } from "django-airavata-common-ui";
import { session } from "django-airavata-api";

export default {
  name: "user-storage-create-view",
  components: {
    uppy: components.Uppy,
  },
  computed: {
    uploadEndpoint() {
      // This endpoint can handle XHR upload or a TUS uploadURL
      return "/api/user-storage/" + this.storagePath;
    },
    username() {
      return session.Session.username;
    },
  },
  data() {
    return {
      dirName: null,
    };
  },
  props: {
    userStoragePath: {
      required: true,
    },
    storagePath: {
      required: true,
    },
  },
  methods: {
    uploadFinished() {
      this.$refs["file-upload"].reset();
      this.$emit("upload-finished");
    },
    addDirectory() {
      this.$emit("add-directory", this.dirName);
      this.dirName = null;
    },
  },
};
</script>
