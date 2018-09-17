<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">
          {{ name }}
        </h1>
        <share-button v-model="localSharedEntity" @input="sharingChanged" />
        <b-form-group label="Application Executable Path" label-for="executable-path">
          <b-form-input id="executable-path" type="text" v-model="data.executablePath" required :disabled="readonly"></b-form-input>
        </b-form-group>
        <b-form-group label="Application Parallelism Type" label-for="parallelism-type">
          <b-form-select id="parallelism-type" v-model="data.parallelism" :options="parallelismTypeOptions" :disabled="readonly" />
        </b-form-group>
        <b-form-group label="Application Deployment Description" label-for="deployment-description">
          <b-form-textarea id="deployment-description" v-model="data.appDeploymentDescription" :rows="3" :disabled="readonly"></b-form-textarea>
        </b-form-group>
        <command-objects-editor title="Module Load Commands" add-button-label="Add Module Load Command" v-model="data.moduleLoadCmds"
          :readonly="readonly" />
        <set-env-paths-editor title="Library Prepend Paths" add-button-label="Add a Library Prepend Path" v-model="data.libPrependPaths"
          :readonly="readonly" />
        <set-env-paths-editor title="Library Append Paths" add-button-label="Add a Library Append Path" v-model="data.libAppendPaths"
          :readonly="readonly" />
        <set-env-paths-editor title="Environment Variables" add-button-label="Add Environment Variable" v-model="data.setEnvironment"
          :readonly="readonly" />
        <command-objects-editor title="Pre Job Commands" add-button-label="Add Pre Job Command" v-model="data.preJobCommands" :readonly="readonly"
        />
        <command-objects-editor title="Post Job Commands" add-button-label="Add Post Job Command" v-model="data.postJobCommands"
          :readonly="readonly" />
        <b-form-group label="Default Queue Name" label-for="default-queue-name">
          <b-form-select id="default-queue-name" v-model="data.defaultQueueName" :options="queueNameOptions" @change="defaultQueueChanged"
            :disabled="readonly">
            <template slot="first">
              <option :value="null">Select a Default Queue</option>
            </template>
          </b-form-select>
        </b-form-group>
        <b-form-group label="Default Node Count" label-for="default-node-count">
          <b-form-input id="default-node-count" type="number" v-model="data.defaultNodeCount" min="0" :max="maxNodes" :disabled="defaultQueueAttributesDisabled"></b-form-input>
        </b-form-group>
        <b-form-group label="Default CPU Count" label-for="default-cpu-count">
          <b-form-input id="default-cpu-count" type="number" v-model="data.defaultCPUCount" min="0" :max="maxCPUCount" :disabled="defaultQueueAttributesDisabled"></b-form-input>
        </b-form-group>
        <b-form-group label="Default Walltime" label-for="default-walltime">
          <b-form-input id="default-walltime" type="number" v-model="data.defaultWalltime" min="0" :max="maxWalltime" :disabled="defaultQueueAttributesDisabled"></b-form-input>
        </b-form-group>
      </div>
    </div>
    <div class="row mb-4">
      <div class="col">
        <b-button variant="primary" @click="save" :disabled="readonly">
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
import CommandObjectsEditor from "./CommandObjectsEditor.vue";
import SetEnvPathsEditor from "./SetEnvPathsEditor.vue";
import { components } from "django-airavata-common-ui";

export default {
  name: "application-deployment-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: models.ApplicationDescriptionDefinition
    },
    deployment_id: {
      type: String,
      required: true
    },
    readonly: {
      type: Boolean,
      default: false
    },
    sharedEntity: {
      type: models.SharedEntity,
      required: true
    }
  },
  components: {
    CommandObjectsEditor,
    SetEnvPathsEditor,
    "share-button": components.ShareButton
  },
  data() {
    return {
      computeResource: null,
      localSharedEntity: this.sharedEntity ? this.sharedEntity.clone() : null
    };
  },
  computed: {
    name() {
      if (this.computeResource) {
        return this.computeResource.hostName;
      } else {
        return this.data.computeHostId.substring(0, 10) + "...";
      }
    },
    parallelismTypeOptions() {
      return models.ParallelismType.values.map(parType => {
        return {
          value: parType,
          text: parType.name
        };
      });
    },
    queueNameOptions() {
      if (!this.computeResource) {
        return [];
      }
      return this.computeResource.batchQueues.map(queue => {
        return {
          value: queue.queueName,
          text: queue.queueName
        };
      });
    },
    maxNodes() {
      const queue = this.computeResource
        ? this.computeResource.batchQueues.find(
            q => q.queueName === this.data.defaultQueueName
          )
        : null;
      return queue ? queue.maxNodes : 0;
    },
    maxCPUCount() {
      const queue = this.computeResource
        ? this.computeResource.batchQueues.find(
            q => q.queueName === this.data.defaultQueueName
          )
        : null;
      return queue ? queue.maxProcessors : 0;
    },
    maxWalltime() {
      const queue = this.computeResource
        ? this.computeResource.batchQueues.find(
            q => q.queueName === this.data.defaultQueueName
          )
        : null;
      return queue ? queue.maxRuntime : 0;
    },
    defaultQueueAttributesDisabled() {
      return !this.data.defaultQueueName || this.readonly;
    }
  },
  created() {
    services.ComputeResourceService.retrieve({
      lookup: this.data.computeHostId
    }).then(computeResource => {
      this.computeResource = computeResource;
    });
  },
  methods: {
    save() {
      this.$emit("save");
    },
    cancel() {
      this.$emit("cancel");
    },
    defaultQueueChanged(queueName) {
      const queue = this.computeResource.batchQueues.find(
        q => q.queueName === queueName
      );
      this.data.defaultNodeCount = queue.defaultNodeCount;
      this.data.defaultCPUCount = queue.defaultCPUCount;
      this.data.defaultWalltime = queue.defaultWalltime;
    },
    sharingChanged(newSharedEntity) {
      this.$emit("sharing-changed", newSharedEntity);
    }
  },
  watch: {
    sharedEntity(newValue, oldValue) {
      this.localSharedEntity = newValue.clone();
    }
  }
};
</script>

