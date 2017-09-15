import Vuex from 'vuex';

const store=Vuex.Store({
  modules:{
    appInputField:{
      state:{
        inputs:{},
      },
      mutations:{
        addInputField:function(id,name='',value='',type='',applicationArgument='',dataStaged=null,requiredOnCMDLine=none){
          store.inputs[id]={
            name:name,
            value:value,
            type:type,
            applicationArgument:applicationArgument,
            dataStaged:dataStaged,
            requiredOnCMDLine:requiredOnCMDLine
          };
        },
        getInputField:function(id){
          return store.inputs[id];
        },

      }
    }
  }
});
