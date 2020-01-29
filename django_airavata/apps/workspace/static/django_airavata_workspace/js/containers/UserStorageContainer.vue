<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4">
          Storage
        </h1>
        <p>
          <small class="text-muted"><i class="fa fa-folder-open"></i> {{ username }}</small>
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
          @upload-success="uploadSuccess"
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
            <b-button
              @click="addDirectory"
              :disabled="!this.dirName"
            >Add directory</b-button>
          </b-input-group-append>
        </b-input-group>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <b-card>
          <router-view
            :user-storage-path="userStoragePath"
            @delete-dir="deleteDir"
            @directory-selected="directorySelected"
            @delete-file="deleteFile"
          ></router-view>
        </b-card>
      </div>
    </div>
  </div>
</template>

<script>
import { services, session, utils } from "django-airavata-api";
import { components, notifications } from "django-airavata-common-ui";

export default {
  name: "user-storage-container",
  components: {
    uppy: components.Uppy
  },
  computed: {
    storagePath() {
      if (this.$route.path.startsWith("/")) {
        return this.$route.path.substring(1);
      } else {
        return this.$route.path;
      }
    },
    username() {
      return session.Session.username;
    },
    uploadEndpoint() {
      // This endpoint can handle XHR upload or a TUS uploadURL
      return "/api/user-storage/" + this.storagePath;
    }
  },
  data() {
    return {
      userStoragePath: null,
      dirName: null
    };
  },
  methods: {
    loadUserStoragePath(path) {
      return services.UserStoragePathService.get(
        { path },
        { ignoreErrors: true }
      )
        .then(result => {
          this.userStoragePath = result;
        })
        .catch(err => {
          if (err.details.status === 404) {
            this.handleMissingPath(path);
          } else {
            utils.FetchUtils.reportError(err);
          }
        });
    },
    handleMissingPath(path) {
      this.$router.replace("/~/");
      // Display a transient error about the path not existing
      notifications.NotificationList.add(
        new notifications.Notification({
          type: "WARNING",
          message: "Path does not exist: " + path,
          duration: 2
        })
      );
    },
    fileChanged() {
      if (this.file && !this.fileTooLarge) {
        let data = new FormData();
        data.append("file", this.file);
        utils.FetchUtils.post(
          "/api/user-storage/" + this.storagePath,
          data
        ).then(() => {
          // this.file = null;
          this.$refs["file-input"].reset();
          this.loadUserStoragePath(this.storagePath);
        });
      }
    },
    uploadSuccess() {
      this.$refs["file-upload"].reset();
      this.loadUserStoragePath(this.storagePath);
    },
    addDirectory() {
      if (this.dirName) {
        let newDirPath = this.storagePath;
        if (!newDirPath.endsWith("/")) {
          newDirPath = newDirPath + "/";
        }
        newDirPath = newDirPath + this.dirName;
        utils.FetchUtils.post("/api/user-storage/" + newDirPath).then(() => {
          this.dirName = null;
          this.loadUserStoragePath(this.storagePath);
        });
      }
    },
    deleteDir(path) {
      utils.FetchUtils.delete("/api/user-storage/~/" + path).then(() => {
        this.loadUserStoragePath(this.storagePath);
      });
    },
    deleteFile(dataProductURI) {
      utils.FetchUtils.delete(
        "/api/delete-file?data-product-uri=" +
          encodeURIComponent(dataProductURI)
      ).then(() => {
        this.loadUserStoragePath(this.storagePath);
      });
    },
    directorySelected(path) {
      this.$router.push("/~/" + path);
    }
  },
  created() {
    if (this.$route.path === "/") {
      this.$router.replace("/~/");
    } else {
      this.loadUserStoragePath(this.storagePath);
    }
  },
  watch: {
    $route() {
      this.loadUserStoragePath(this.storagePath);
    }
  }
};
</script>
