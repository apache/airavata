import Vue from 'vue'

export default{
  namespaced: true,
  state:{
    name:'',
    version:'',
    description:''
  },
  mutations:{
    addAppDetails:function (state,update) {
      for(var prop in update){
        if(state.hasOwnProperty(prop)){
          Vue.set(state,prop,update[prop])
        }
      }
    }
  },
  getters:{
    getAppName:(state)=>{
      return state.name;
    },
    getAppVersion:(state)=>{
      return state.version;
    },
    getAppDescription:(state)=>{
      return state.description;
    }
  },
  actions:{
    updateAppDetails:function (context,update) {
      context.commit('addAppDetails',update)
    }
  }
}
