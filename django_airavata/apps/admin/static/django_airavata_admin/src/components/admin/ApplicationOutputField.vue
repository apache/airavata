<template>
  <div class="main_section interface-main">
    <div class="input-field-header">
      Output Fields
      <img v-on:click="delete_event_trigger();" src="/static/images/delete.png"/>
    </div>
    <div class="entry">
      <div class="heading">Name</div>
      <input type="text" v-model="name"/>
    </div>
    <div class="entry">
      <div class="heading">Value</div>
      <input type="text" v-model="value"/>
    </div>
    <div class="entry">
      <div class="heading">Type</div>
      <select v-model="type">
        <option value="0">String</option>
        <option value="1">Integer</option>
        <option value="2">Float</option>
        <option value="3">URI</option>
      </select>
    </div>
    <div class="entry">
      <div class="heading">Application argument</div>
      <input v-model="appArg" type="text"/>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Data Movement'" v-bind:selectorId="dataMovement.fieldName"
                            v-bind:def="getAppOutputFieldValue(dataMovement)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Output Required'" v-bind:selectorId="required.fieldName"
                            v-bind:def="getAppOutputFieldValue(required)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Required on command line'" v-bind:selectorId="requiredOnCmd.fieldName"
                            v-bind:def="getAppOutputFieldValue(requiredOnCmd)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'

  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions} = createNamespacedHelpers('appInterfaceTab')

  export default {
    components: {
      BooleanRadioButton
    },
    created: function () {
      this.syncDataFromStore();
    },
    props: ['output_id'],
    methods: {
      delete_event_trigger: function () {
        this.$emit('delete_output_field');
      },
      boolValueHandler: function (selectorID, value) {
        this.updateStore(selectorID, value)
      },
      syncDataFromStore: function () {
        var val = this.getAppOutputField(this.output_id)
        this.name = val['name']
        this.value = val['value']
        this.type = val['type']
        this.appArg = val['appArg']
      },
      updateStore: function (fieldName, newValue) {
        var param = {
          'id': this.output_id,
        };
        var update = {}
        update[fieldName] = newValue
        param['update'] = update
        this.updateOutputField(param)
      },
      ...mapActions(['updateOutputField'])
    },
    mounted: function () {
      this.syncDataFromStore()
    },
    data: function () {
      return {
        'required': {
          'id': this.output_id,
          'fieldName': 'isRequired'
        },
        'requiredOnCmd':
          {
            'id': this.output_id,
            'fieldName': 'requiredToAddedToCommandLine'
          },
        'dataMovement': {
          'id': this.output_id,
          'fieldName': 'dataMovement'
        },
        name: '',
        value: '',
        type: '',
        appArg: ''
      }
    },

    computed: {
      ...mapGetters(['getAppOutputField','getAppOutputFieldValue'])
    },
    watch: {
      name: function (newValue) {
        this.updateStore('name', newValue)
      },
      value: function (newValue) {
        this.updateStore('value', newValue)

      },
      type: function (newValue) {
        this.updateStore('type', newValue)
      },
      appArg: function (newValue) {
        this.updateStore('applicationArgument', newValue)
      }
    }
  }
</script>

<style>
  .input-field-header {
    background-color: #F8F8F8;
    width: 100%;
    padding: 15px;
    border: solid 1px #dddddd;
    text-align: left;
  }

  .input-field-header img {
    float: right;
  }

  .main_section.interface-main .entry {
    margin-bottom: 40px;
    margin-left: 15px;
    margin-right: 15px;
  }

  .entry.boolean-selectors {
    display: flex;
  }

  .entry.boolean-selectors div {
    margin-right: 60px;
  }

  .entry select {
    width: 100%;
    height: 30px;
  }

  .interface-main {
    border: solid 1px #dddddd;
    border-radius: 4px;
  }

</style>
