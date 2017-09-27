

export default {
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
