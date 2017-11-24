<template>
  <div class="main_section interface-main">
    <div class="input-field-header">
      Input Fields
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
      <boolean-radio-button v-bind:heading="'Standard input'" v-bind:selectorId="standardInput.fieldName"
                            v-bind:def="getAppInputFieldValue(standardInput)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Is read only'" v-bind:selectorId="isReadOnly.fieldName"
                            v-bind:def="getAppInputFieldValue(isReadOnly)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
    <div class="entry">
      <div class="heading">User friendly description</div>
      <textarea style="height: 80px;" type="text" v-model="userFriendlyDescr"/>
    </div>
    <div class="entry">
      <div class="heading">Input order</div>
      <input v-model="inpOrder" type="text"/>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Data is staged'" v-bind:selectorId="dataStaged.fieldName"
                            v-bind:def="getAppInputFieldValue(dataStaged)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
      <boolean-radio-button v-bind:heading="'Required'" v-bind:selectorId="required.fieldName"
                            v-bind:def="getAppInputFieldValue(required)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
    <div class="entry boolean-selectors">
      <boolean-radio-button v-bind:heading="'Required on command line'" v-bind:selectorId="requiredOnCmd.fieldName"
                            v-bind:def="getAppInputFieldValue(requiredOnCmd)"
                            v-on:bool_selector="boolValueHandler"></boolean-radio-button>
    </div>
  </div>
</template>
<script>
  import BooleanRadioButton from './BooleanRadioButton.vue'

  import {createNamespacedHelpers} from 'vuex'

  const {mapGetters, mapActions} = createNamespacedHelpers('newApplication/appInterfaceTab')

  export default {
    components: {
      BooleanRadioButton
    },
    created: function () {
      this.syncDataFromStore();
    },
    methods: {
      delete_event_trigger: function () {
        this.$emit('delete_input_field');
      },
      boolValueHandler: function (selectorID, value) {
        if (typeof(value) != "boolean"){
          throw "event value not boolean: "
        }
        this.updateStore(selectorID, value)
      },
      syncDataFromStore: function () {
        var val = this.getAppInputField(this.input_id)
        this.name = val['name']
        this.value = val['value']
        this.type = val['type']
        this.appArg = val['appArg']
        this.userFriendlyDescr = val['userFriendlyDescr']
        this.inpOrder = val['inpOrder']

      },
      updateStore: function (fieldName, newValue) {
        var param = {
          'id': this.input_id,
        };
        var update = {}
        update[fieldName] = newValue
        param['update'] = update
        this.updateInputFieldValues(param)
      },
      ...mapActions(['updateInputFieldValues'])
    },
    mounted: function () {
      this.syncDataFromStore()
    },
    data: function () {
      return {
        'dataStaged': {
          'id': this.input_id,
          'fieldName': 'dataStaged'
        },
        'required':
          {
            'id': this.input_id,
            'fieldName': 'isRequired'
          },
        'requiredOnCmd':
          {
            'id': this.input_id,
            'fieldName': 'requiredToAddedToCommandLine'
          },
        'standardInput':
          {
            'id': this.input_id,
            'fieldName': 'standardInput'
          },
        'isReadOnly':
          {
            'id': this.input_id,
            'fieldName': 'isReadOnly'
          },
        name: '',
        value: '',
        type: '',
        appArg: '',
        userFriendlyDescr: '',
        inpOrder: ''
      }
    },
    props: ['input_id'],
    computed: {
      ...mapGetters(['getAppInputField', 'getAppInputFieldValue'])
    },
    watch: {
      inpOrder: function (newValue) {
        this.updateStore('inputOrder', newValue)
      },
      userFriendlyDescr: function (newValue) {
        this.updateStore('userFriendlyDescription', newValue)
      },
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
