<template>
  <b-card>
    <div class="d-flex align-items-center" slot="header">
      <div class="drag-handle mr-1 text-muted">
        <font-awesome-icon icon="grip-vertical" />
        <span class="sr-only">Drag handle for reordering</span>
      </div>
      <div class="mr-auto">Input Field: {{ data.name }}</div>
      <b-link class="text-secondary" @click="deleteApplicationInput">
        <i class="fa fa-trash"></i>
        <span class="sr-only">Delete</span>
      </b-link>
    </div>
    <b-collapse :visible="!collapse">
      <b-form-group label="Name" :label-for="id+'-name'">
        <b-form-input :id="id+'-name'" type="text" v-model="data.name" ref="nameInput" required></b-form-input>
      </b-form-group>
      <b-form-group label="Initial Value" :label-for="id+'-value'">
        <b-form-input :id="id+'-value'" type="text" v-model="data.value"></b-form-input>
      </b-form-group>
      <b-form-group label="Type" :label-for="id+'-type'">
        <b-form-select :id="id+'-type'" v-model="data.type" :options="inputTypeOptions" />
      </b-form-group>
      <b-form-group label="Application Argument" :label-for="id+'-argument'">
        <b-form-input :id="id+'-argument'" type="text" v-model="data.applicationArgument"></b-form-input>
      </b-form-group>
      <div class="d-flex">
        <b-form-group class="flex-fill" label="Standard Input" :label-for="id+'-standard-input'">
          <b-form-radio-group :id="id+'-standard-input'" v-model="data.standardInput" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
        <b-form-group class="flex-fill" label="Read Only" :label-for="id+'-read-only'">
          <b-form-radio-group :id="id+'-read-only'" v-model="data.isReadOnly" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
      </div>
      <b-form-group label="User Friendly Description" :label-for="id+'-user-friendly-description'">
        <b-form-textarea :id="id+'-user-friendly-description'" v-model="data.userFriendlyDescription" :rows="3" />
      </b-form-group>
      <div class="d-flex">
        <b-form-group class="flex-fill" label="Data is staged" :label-for="id+'-data-staged'">
          <b-form-radio-group :id="id+'-data-staged'" v-model="data.dataStaged" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
        <b-form-group class="flex-fill" label="Required" :label-for="id+'-required'">
          <b-form-radio-group :id="id+'-required'" v-model="data.isRequired" :options="trueFalseOptions">
          </b-form-radio-group>
        </b-form-group>
      </div>
      <b-form-group class="flex-fill" label="Required on Command Line" :label-for="id+'-required-command-line'">
        <b-form-radio-group :id="id+'-required-command-line'" v-model="data.requiredToAddedToCommandLine" :options="trueFalseOptions">
        </b-form-radio-group>
      </b-form-group>
    </b-collapse>
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import vmodel_mixin from "../commons/vmodel_mixin";

export default {
  name: "application-input-field-editor",
  mixins: [vmodel_mixin],
  props: {
    value: {
      type: models.InputDataObjectType
    },
    id: {
      required: true
    },
    // Whether to put focus on the name field when mounting component
    focus: {
      type: Boolean
    },
    collapse: {
      type: Boolean
    }
  },
  computed: {
    inputTypeOptions() {
      return models.DataType.values.map(dataType => {
        return {
          value: dataType,
          text: dataType.name
        };
      });
    },
    trueFalseOptions() {
      return [{ text: "True", value: true }, { text: "False", value: false }];
    }
  },
  methods: {
    doFocus() {
      this.$refs.nameInput.focus();
      this.$el.scrollIntoView();
    },
    deleteApplicationInput() {
      this.$emit("delete");
    }
  },
  mounted() {
    if (this.focus) {
      this.doFocus();
    }
  }
};
</script>

<style scoped>
.drag-handle {
  cursor: move;
}
</style>

