<template>
  <div>
    User storage edit {{storagePath}}
    <br/>

    <div style="width: 100%;" id="code">
    </div>
  </div>
</template>
<script>

  import CodeMirror from 'codemirror'
  import 'codemirror/lib/codemirror.css'
  import 'codemirror/theme/abcdef.css'
  import './UserStorageEditViewer.css'

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
        fileContent: ""
      };
    },
    mounted() {
      this.setFileContent();
    },
    components: {},
    computed: {},
    methods: {
      setFileContent() {
        if (this.userStoragePath && this.userStoragePath.files && this.userStoragePath.files.length > 0) {
          fetch(this.userStoragePath.files[0].downloadURL).then((res) => {
            res.text().then((text) => {
              this.fileContent = text;
              this.setFileContentEditor(this.fileContent);
            });
          });
        }
      },
      setFileContentEditor(value) {
        CodeMirror(document.getElementById("code"), {
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

