
import Vue from 'vue'


export default{
  namespaced: true,
  state:{
    inputFields:{},
    counter:0,
    initialized:false
  },
  mutations:{
    createAppInterfaceInputFieldObject(state,id){
      Vue.set(state.inputFields,id,{
        input_id: id,
        name: 'nm',
        value: '',
        type: '',
        appArg: '',
        dataStaged: false,
        requiredId: false,
        requiredOnCmdId: false
      });
      console.log('Creating App Input Field INS: ',state.inputFields);
    },
    updateAppInterfaceInputField:function (state,param) {
      var id=param.id
      var updateValue=param.update
      var inputFields=Object.assign({},state.inputFields)
      console.log(inputFields)
      var inpField=inputFields[id]
      var keys=Object.keys(updateValue)
      console.log('keys',keys)
      for(var prop in updateValue){
        console.log('props',prop)
        if(inpField.hasOwnProperty(prop)){
          inpField[prop]=updateValue[prop];
          console.log('type',typeof inputFields[id][prop])
        }
      }
      state.inputFields=inputFields

    },
    setInitialize:function (state, intitalized) {
      state.initialized=intitalized
    },
    removeAppInterfaceInputField:function (state, id) {
      var inputFields=state.inputFields;
      delete inputFields[id];
      state.inputFields=Object.assign({},inputFields)
    },
    deleteAllInputFields:function (state) {
      state.inputFields={}
    }
  },
  getters:{
    getAppInputField:state=>id=>{
      return state.inputFields[id];
    },
    isInitialized:state=>{
      return state.initialized;
    },
    getAppInputFields:state=>{
      return state.inputFields;
    },
    getAppInputFieldsId:state=>{
      var ids=Object.getOwnPropertyNames(state.inputFields)
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
    deleteAppInterfaceInputField:function (context, id) {
      console.log('deleting',id)
      context.commit('removeAppInterfaceInputField',id);
    },
    updateFieldValues:function (context, param) {
      context.commit('updateAppInterfaceInputField',param)
    },
    initialized:function (context, initialize) {
      context.commit('setInitialize',initialize)
    }
  }
}
