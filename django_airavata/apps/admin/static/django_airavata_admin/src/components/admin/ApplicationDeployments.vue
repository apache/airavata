<template>
  <div class="main_section">

    <div class="new-application-tab-main">
      <h4>Application Deployments</h4>
      <div class="entry">
        <div class="heading">Application module</div>
        <input type="text" v-model="appDeployments.appModuleId"/>
      </div>
      <div class="entry">
        <div class="heading">Application compute host</div>
        <select v-model="appDeployments.computeHostId">
          <option v-for="computeHost in computeHosts" v-bind:value="computeHost.host_id">{{computeHost.host}}</option>
        </select>
      </div>
      <div class="entry">
        <div class="heading">Application executable path</div>
        <input type="text" v-model="appDeployments.executablePath"/>
      </div>
      <div class="entry">
        <div class="heading">Application Parallelism type</div>
        <select v-model="appDeployments.parallelism">
          <option value="0">SERIAL</option>
          <option value="1">MPI</option>
          <option value="2">OPENMP</option>
          <option value="3">OPENMP MPI</option>
          <option value="4">CCM</option>
          <option value="5">CRAY MPI</option>
        </select>
      </div>
      <div class="entry">
        <div class="heading">Application deployment description</div>
        <textarea style="height: 80px;" type="text" v-model="appDeployments.appDeploymentDescription"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <h4>Module load commands</h4>
      <div class="entry">
        <div class="heading">Module load commands</div>
        <div class="entry" v-for="mdlCMD in appDeployments.moduleLoadCmds">
          <input type="text" v-model="mdlCMD.command"/>
        </div>
      </div>
      <div class="deployment-entry">
        <input type="button" class="deployment btn" value="Add command" v-on:click="addCommand('moduleLoadCmds')"/>
      </div>
    </div>

    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Library Prepend Paths</h4>
        <div class="name_value" v-for="pth in appDeployments.libPrependPaths">
          <input type="text" placeholder="Name" v-model="pth.name"/>
          <input type="text" placeholder="Value" v-model="pth.value"/>
        </div>
        <input type="button" class="deployment btn" value="Add path" v-on:click="addEnvPaths('libPrependPaths')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Library Append Paths</h4>
        <div class="name_value" v-for="pth in appDeployments.libAppendPaths">
          <input type="text" placeholder="Name" v-model="pth.name"/>
          <input type="text" placeholder="Value" v-model="pth.value"/>
        </div>
        <input type="button" class="deployment btn" value="Add path" v-on:click="addEnvPaths('libAppendPaths')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Environments</h4>
        <div class="name_value" v-for="env in appDeployments.setEnvironment">
          <input type="text" placeholder="Name" v-model="env.name"/>
          <input type="text" placeholder="Value" v-model="env.value"/>
        </div>
        <input type="button" class="deployment btn" value="Add environment" v-on:click="addEnvPaths('setEnvironment')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Pre Job Commands</h4>
        <div class="entry" v-for="cmd in appDeployments.preJobCommands">
          <input type="text" v-model="cmd.command"/>
        </div>
        <input type="button" class="deployment btn" value="Add Pre Job command"
               v-on:click="addCommand('preJobCommands')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Post Job Commands</h4>
        <div class="entry" v-for="cmd in appDeployments.postJobCommands">
          <input type="text" v-model="cmd.command"/>
        </div>
        <input type="button" class="deployment btn" value="Add Post Job command"
               v-on:click="addCommand('postJobCommands')"/>
      </div>
    </div>
    <div class="new-application-tab-main">
      <div class="deployment-entry">
        <h4>Defaults</h4>
        <div class="entry">
          <div class="heading">Default Node Count</div>
          <input type="number" value="1" min="0" v-model="appDeployments.defaultNodeCount"/>
        </div>
        <div class="entry">
          <div class="heading">Default CPU Count</div>
          <input type="number" value="1" min="0" v-model="appDeployments.defaultCPUCount"/>
        </div>
        <div class="entry">
          <div class="heading">Default Queue Name</div>
          <select v-model="appDeployments.defaultQueueName">
            <option v-bind:value="queue" v-for="queue in queues">{{queue}}</option>
          </select>
        </div>
      </div>
    </div>
    <div class="new-application-tab-main">
      <new-application-buttons v-bind:save="saveApplicationDeployment" v-bind:cancel="resetState" v-bind:sectionName="'Application Deployment'"></new-application-buttons>
    </div>
  </div>
</template>
<script>
  import {createNamespacedHelpers} from 'vuex'
  import NewApplicationButtons from './NewApplicationButtons.vue';
  import Utils from '../../utils'

  const {mapGetters, mapActions} = createNamespacedHelpers('newApplication/appDeploymentsTab')

  export default {
    components: {
      NewApplicationButtons
    },
    mounted: function () {
      this.appDeployments = this.getCompleteData
      this.computeHosts=this.fetchComputeHosts()
    },
    data: function () {
      var appDeployments = this.getCompleteData
      console.log("Application Deployment Data", appDeployments)
      return {
        "appDeployments": appDeployments,
        "computeHosts":[],
        "queues":[]
      }
    },
    computed: {
      ...mapGetters(["getCompleteData", "getAppModule", "getAppComputeHost", "getAppExecutablePath", "getAppParallelismType", "getAppDeploymentDescription", "getModuleLoadCmds",
        "getLibPrependPaths", "getLibAppendPaths", "getSetEnvironment", "getPreJobCommands", "getPostJobCommands", "getDefaultNodeCount", "getDefaultCPUCount", "getDefaultQueueName"
      ])
    },
    methods: {
      addCommand: function (fieldName) {
        var cmd = {
          "command": ""
        }
        var ob = Object.assign({}, this.appDeployments)
        var fieldValues = ob[fieldName]
        fieldValues.push(cmd)
        this.appDeployments = ob
      },
      addEnvPaths: function (fieldName) {
        var envPaths = {
          "name": "",
          "value": ""
        }
        var ob = Object.assign({}, this.appDeployments)
        var fieldValues = ob[fieldName]
        fieldValues.push(envPaths)
        this.appDeployments = ob
        console.log("Add name value pair",this.appDeployments)
      },
      fetchComputeHosts:function () {
        this.computeHosts=[{"host":"Loading...","host_id":""}]
        var callable=(value)=>this.computeHosts=value
        Utils.get('/api/compute/resources',{success:callable,failure:(value)=>this.computeHosts=[]})
      },
      saveApplicationDeployment:function ({success=null,failure=null}={}) {
        this.updateAppDeployment(this.appDeployments)
        this.save({success:success,failure:failure})
      },
      ...mapActions(["updateAppDeployment","save","resetState"])
    },
    watch: {
      '$route'(to, from) {
        this.updateAppDeployment(this.appDeployments)
      },
      "appDeployments.computeHostId":function (value) {
        if(value){
          Utils.get("/api/compute/resource/queues",{queryParams:{id:value},success:(value)=>this.queues=value})
        }

      }
    }
  }

</script>
<style>
  .heading {
    font-size: 1.0em;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .deployment.btn {
    float: right;
    text-align: center;
    border-color: #007BFF;
    border-style: solid;
    border-radius: 3px;
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 15px;
    padding-right: 15px;
    color: #007BFF;
    background-color: white;
    width: auto;
    float: left;
  }

  .deployment.btn:hover {
    color: white;
    background-color: rgba(0, 105, 217, 1);
  }

  .deployment-entry {
    display: inline-block;
    margin-top: 15px;
    width: 100%;
  }

  .name_value {
    display: inline-flex;
    width: 100%;
    margin-bottom: 5px;
  }

  .name_value input {
    width: 50%;
    display: inline-flex;
    margin-right: 5px;
  }

  .deployment-entry .entry {
    margin-bottom: 5px;
  }
</style>

