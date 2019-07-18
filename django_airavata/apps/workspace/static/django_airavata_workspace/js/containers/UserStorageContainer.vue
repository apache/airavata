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
        <b-form-group
          :description="maxFileUploadSizeMessage"
          :state="fileUploadState"
          :invalid-feedback="fileUploadInvalidFeedback"
        >
          <b-form-file
            v-model="file"
            ref="file-input"
            placeholder="Add file"
            @input="fileChanged"
            class="mb-2"
            :state="fileUploadState"
          ></b-form-file>
        </b-form-group>
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
import { notifications } from "django-airavata-common-ui";

export default {
  name: "user-storage-container",
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
    fileTooLarge() {
      return (
        this.settings &&
        this.settings.fileUploadMaxFileSize &&
        this.file &&
        this.file.size > this.settings.fileUploadMaxFileSize
      );
    },
    fileUploadState() {
      if (this.fileTooLarge) {
        return false;
      } else {
        return null;
      }
    },
    fileUploadInvalidFeedback() {
      if (this.fileTooLarge) {
        return (
          "File selected is larger than " + this.maxFileUploadSizeMB + " MB"
        );
      } else {
        return null;
      }
    }
  },
  data() {
    return {
      userStoragePath: null,
      file: null,
      dirName: null,
      settings: null
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
    services.SettingsService.get().then(s => (this.settings = s));
  },
  watch: {
    $route() {
      this.loadUserStoragePath(this.storagePath);
    }
  }
};
</script>
