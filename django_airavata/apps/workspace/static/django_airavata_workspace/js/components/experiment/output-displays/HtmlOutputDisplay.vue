<template>
  <div v-html="rawOutput"/>
</template>

<script>
import { models, utils } from "django-airavata-api";
export default {
  name: "html-output-display",
  props: {
    experimentOutput: {
      type: models.OutputDataObjectType,
      required: true
    },
    dataProducts: {
      type: Array,
      required: true
    },
    experimentId: {
      type: String,
      required: true
    },
    providerId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      rawOutput: null,
      isLoading: true,
      rawJS : `<script>console.log("This can be passed from output view providers");<\/script>;`,
    };
  },
  methods : {
    //Attaches the script to the head, the name of the script can be passed from
    //output view provider
    loadScripts() {
      return new Promise(resolve => {

        let scriptEl = document.createElement("script");
        scriptEl.src = "/static/common/js/test.js";
        scriptEl.type = "text/javascript";

        // Attach script to head
        document.getElementsByTagName("head")[0].appendChild(scriptEl);
        // Wait for tag to load before promise is resolved
        scriptEl.addEventListener('load',() => {
          resolve();
        });
      });
    },
    executeScript() {
      // This code can be used to execute scripts passed from the output
      // view providers.
      let script = this.rawJS.replace(/<\/?script>/g,"")
      eval(script)
    },
  },
  created() {
    utils.FetchUtils.get("/api/html-output", {
      "experiment-id": this.experimentId,
      "experiment-output-name": this.experimentOutput.name,
      "provider-id": this.providerId
    }).then(data => {
      this.rawOutput = data.output
      //this.rawJS = data.js
      this.isLoading = false
    });
    console.log("Created method is called for HTMLOutputDisplay");
  },
  mounted() {
    eval('console.log("Passed from output providers")');
    console.log("Mounted method is called for HTMLOutputDisplay");
  },
  watch: {
    isLoading() {
      if(!this.isLoading) {
        console.log("Data has been loaded and we can print it now. ");
        this.loadScripts().then(() => {
        this.executeScript();
        });
        console.log(this.rawOutput);
      }
    }
  }
};
</script>

<style scoped>
</style>
