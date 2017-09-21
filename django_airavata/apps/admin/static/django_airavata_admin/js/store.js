
const store={
  modules:{
    appInterfaceTab:{
      state:{
        inputFields:[],
        counter:0
      },
      mutations:{
        createAppInterfaceInputField:function (state) {
          state.inputFields.push(
            {
              input_id:state.counter++,
              name:'',
              value:'',
              type:'',
              appArg:'',
              dataStaged:false,
              requiredId:false,
              requiredOnCmdId:false
            }
          );
        }
      },
      getters:{
        getAppInterface:state=>{
          return state.data;
        }
      }
    },
    appDetailsTab:{
      state:{
        data:{},
      },
      mutations:{
        addAppDetails:function (state,data) {
          state.data=data;
        }
      },
      getters:{
        getAppDetails:(state,getters)=>{
          return state.data;
        }
      }
    },
    appDeploymentsTab:{
      state:{
        data:{},
      },
      mutations:{
        addAppDeployments:function (state,data) {
          state.data=data;
        }
      },
      getters:{
        getAppDeployments:(state,getters)=>{
          return state.data;
        }
      }
    }
  }
};

export default {
  'store':store
}

