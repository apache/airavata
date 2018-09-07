<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          {{ name }}
        </h1>
        <b-form-group label="Application Executable Path" label-for="executable-path">
          <b-form-input id="executable-path" type="text" v-model="data.executablePath" required></b-form-input>
        </b-form-group>
        <b-form-group label="Application Parallelism Type" label-for="parallelism-type">
          <b-form-select id="parallelism-type" v-model="data.parallelism" :options="parallelismTypeOptions" />
        </b-form-group>
        <b-form-group label="Application Deployment Description" label-for="deployment-description">
          <b-form-textarea id="deployment-description" v-model="data.appDeploymentDescription" :rows="3"></b-form-textarea>
        </b-form-group>
        <b-card title="Module Load Commands">
          <b-input-group v-for="moduleLoadCmd in data.moduleLoadCmds" :key="moduleLoadCmd.key" class="mb-1">
            <b-form-input type="text" v-model="moduleLoadCmd.command" required ref="moduleLoadCmdInputs" />
            <b-input-group-append>
              <b-button variant="secondary" @click="deleteModuleLoadCmd(moduleLoadCmd)">
                <i class="fa fa-trash"></i>
                <span class="sr-only">Delete</span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <b-button variant="secondary" @click="addModuleLoadCmd">Add Module Load Command</b-button>
        </b-card>
      </div>
    </div>
    <div class="row mb-4">
      <div class="col">
        <b-button variant="primary" @click="save">
          Save
        </b-button>
        <b-button variant="secondary" @click="cancel">
          Cancel
        </b-button>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import vmodel_mixin from "../commons/vmodel_mixin";

export default {
  name: "application-deployment-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: models.ApplicationDescriptionDefinition
    },
    id: {
      type: String,
      required: true
    },
    deployment_id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      computeResource: null
    };
  },
  computed: {
    name() {
      if (this.computeResource) {
        return this.computeResource.hostName;
      } else {
        return this.data.computeHostId.substring(0, 10);
      }
    },
    parallelismTypeOptions() {
      return models.ParallelismType.values.map(parType => {
        return {
          value: parType,
          text: parType.name
        };
      });
    }
  },
  created() {
    services.ComputeResourceService.retrieve({
      lookup: this.data.computeHostId
    }).then(computeResource => (this.computeResource = computeResource));
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    addModuleLoadCmd() {
      if (!this.data.moduleLoadCmds) {
        this.data.moduleLoadCmds = [];
      }
      this.data.moduleLoadCmds.push(new models.CommandObject());
      this.$nextTick(() =>
        this.$refs.moduleLoadCmdInputs[
          this.$refs.moduleLoadCmdInputs.length - 1
        ].focus()
      );
    },
    deleteModuleLoadCmd(moduleLoadCmd) {
      const index = this.data.moduleLoadCmds.findIndex(
        cmd => cmd.key === moduleLoadCmd.key
      );
      this.data.moduleLoadCmds.splice(index, 1);
    }
  }
};
</script>

