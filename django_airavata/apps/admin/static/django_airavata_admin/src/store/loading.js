export default{
  namespaced: true,
  state:{
    count:0
  },
  getters:{
    display:function (state) {
      if(state.count>0){
        return true
      }else{
        return false
      }
    }
  },
  actions:{
    loadingStarted:function ({ state, commit ,rootState}) {
      state.count=state.count+1
    },
    loadingCompleted:function ({ state, commit ,rootState}) {
      if (state.count!=0){
        state.count=state.count-1
      }else {
        throw "cannot decrement the  loading count"
      }
    }
  }
}
