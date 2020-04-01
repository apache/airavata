<template>
  <div style="width: 100%;" id="code">
  </div>
</template>

<script>

  import CodeMirror from 'codemirror'
  import 'codemirror/lib/codemirror.css'
  import 'codemirror/theme/abcdef.css'
  import './UserStorageEditViewer.css'
  import UserStoragePathBreadcrumb from "./UserStoragePathBreadcrumb";
  import {utils} from "django-airavata-api";

  export default {
    name: "user-storage-file-edit-viewer",
    props: {
      userStoragePath: {
        required: true
      },
      storagePath: {
        required: true
      }
    },
    data() {
      return {
        fileContent: "",
        editor: null
      };
    },
    mounted() {
      this.setFileContentEditor();
      this.setFileContent();
    },
    methods: {
      setFileContent() {
        if (this.userStoragePath && this.userStoragePath.files && this.userStoragePath.files.length > 0) {
          utils.FetchUtils.get(this.userStoragePath.files[0].downloadURL, "", {
            ignoreErrors: false,
            showSpinner: true
          }, "text").then((res) => {
            this.fileContent = res;
            this.editor.getDoc().setValue(this.fileContent);
          });
        }
      },
      setFileContentEditor(value = "") {
        this.editor = CodeMirror(document.getElementById("code"), {
          theme: "abcdef",
          mode: "text/plain",
          lineNumbers: true,
          lineWrapping: true,
          scrollbarStyle: "native",
          extraKeys: {"Ctrl-Space": "autocomplete"},
          value: value
        });
      }
    }
  };

</script>

