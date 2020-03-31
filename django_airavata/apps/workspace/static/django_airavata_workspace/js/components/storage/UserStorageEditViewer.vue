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
    mounted() {
      const value = this.getFileContent();
      CodeMirror(document.getElementById("code"), {
        theme: "abcdef",
        mode: "text/plain",
        lineNumbers: true,
        lineWrapping: true,
        scrollbarStyle: "native",
        extraKeys: {"Ctrl-Space": "autocomplete"},
        value: value
      });
    },
    components: {},
    computed: {},
    methods: {
      getFileContent() {
        // TODO
        return "aaa bbb ccc dd ee"
      }
    }
  };

</script>

