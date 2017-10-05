
import Vue from 'vue'


export default{
  namespaced: true,
  state:{
    inputFields:{},
    outputFields:{},
    counter:0,
    initialized:false,
    enableArchiveWorkingDirectory:false,
    enableOutputFileInputs:false,
    missingFields:false
  },
  mutations:{
    createAppInterfaceInputFieldObject(state,id){
      Vue.set(state.inputFields,id,{
        input_id: id,
        name: 'nm',
        value: '',
        type: '',
        appArg: '',
        userFriendlyDescr:'',
        inpOrder:'',
        dataStaged: null,
        required: false,
        requiredOnCmd: false,
        isReadOnly:true,
        standardInput:true
      });
    },
    createAppInterfaceOutputFieldObject:function (state, id) {
      Vue.set(state.outputFields,id,{
        input_id: id,
        name: 'nm',
        value: '',
        type: '',
        appArg: '',
        required: false,
        requiredOnCmd: false,
        dataMovement:true,
      });
    },
    updateAppInterfaceField:function (state, param) {
      var id=param.id
      var updateValue=param.update
      var fields=Object.assign({},state[param.fieldType])
      console.log(fields)
      var inpField=fields[id]
      var keys=Object.keys(updateValue)
      console.log('keys',keys)
      for(var prop in updateValue){
        console.log('props',prop)
        if(inpField.hasOwnProperty(prop)){
          inpField[prop]=updateValue[prop];
          console.log('type',typeof fields[id][prop])
        }
      }
      Vue.set(state,param.fieldType,fields)
    },
    setInitialize:function (state, intitalized) {
      state.initialized=intitalized
    },
    removeAppInterfaceField:function (state, params) {
      var fields=state[params.fieldType];
      delete fields[params.id];
      Vue.set(state,params.fieldType,Object.assign({},fields))
    },
    deleteAllFields:function (state,fieldType) {
      Vue.set(state,fieldType,{})
    },
    setEnableArchiveWorkingDirectory:function (state,value) {
      state.enableArchiveWorkingDirectory=value
    },
    setEnableOutputFileInput:function (state,value) {
      state.enableOutputFileInputs=value
    },
    setMissingField:function (state,value) {
      state.missingFields=value;
    }
  },
  getters:{
    isMissing:state=>{
      return state.missingFields;
    },
    getAppInputField:state=>id=>{
      return state.inputFields[id];
    },
    getAppOutputField:state=>id=>{
      return state.outputFields[id]
    },
    isInitialized:state=>{
      return state.initialized;
    },
    getAppInputFieldValue:state=>param=>{
      var val=state.inputFields[param.id][param['fieldName']]
      console.log('Get App Input Field',param['fieldName'],param['id'])
      return val
    },
    getAppOutputFieldValue:state=>param=>{
      return state.outputFields[param.id][param['fieldName']]
    },
    isEnableArchiveWorkingDirectory:state=>state.enableArchiveWorkingDirectory,
    isEnableOutputFileInput:state=>state.enableOutputFileInputs,
    getAppInputFields:state=>{
      return state.inputFields;
    },
    getAppOutputFields:state=>{
      return state.outputFields
    },
    getAppInputFieldsIds: state=>{
      var ids=Object.getOwnPropertyNames(state.inputFields)
      ids.splice(ids.indexOf('__ob__'),1)
      return ids;
    },
    getAppOutputFieldIds:state=>{
      var ids=Object.getOwnPropertyNames(state.outputFields)
      ids.splice(ids.indexOf('__ob__'),1)
      return ids;
    }


  },
  actions:{
    createAppInterfaceInputField:function (context,id=null) {
      if(id == null || !context.state.inputFields.hasOwnProperty(id)){
        id=(context.state.counter++).toString();
        context.commit('createAppInterfaceInputFieldObject',id);
      }
      return id;
    },
    createAppInterfaceOutputField:function (context,id=null) {
      if(id == null || !context.state.inputFields.hasOwnProperty(id)){
        id=(context.state.counter++).toString();
        context.commit('createAppInterfaceOutputFieldObject',id);
      }
      return id;
    },
    deleteAppInterfaceInputField:function (context, id) {
      context.commit('removeAppInterfaceField',{'fieldType':'inputFields','id':id});
    },
    deleteAppInterfaceOutputField:function (context,id) {
      context.commit('removeAppInterfaceField',{'fieldType':'outputFields','id':id});
    },
    updateInputFieldValues:function (context, param) {
      param['fieldType']='inputFields'
      context.commit('updateAppInterfaceField',param)
    },
    updateOutputField:function (context,param) {
      param['fieldType']='outputFields'
      context.commit('updateAppInterfaceField',param)
    },
    initialized:function (context, initialize) {
      context.commit('setInitialize',initialize)
    },
    changeEnableOutputFileInput:function(context,value){
      context.commit('setEnableOutputFileInput',value)
    },
    changeEnableArchiveWorkingDirectory:function (context,value) {
      context.commit('setEnableArchiveWorkingDirectory',value)
    },
    triggerMissingField:function (context,value) {
      context.commit('setMissingField',value)
    }
  }
}
