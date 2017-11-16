import Vue from 'vue'

export default {
  resetData:function (obj,initialData) {
    if (initialData instanceof Function){
      initialData=initialData();
    }
    for(var prop in obj){
      Vue.set(obj,prop,initialData[prop])
      console.log('call',prop)

    }
  },
  addIndex: function (list) {
    for (var i = 0; i < list.length; i++) {
      list[i].index = i;
    }
    return list;
  },
  mapper: function (data, map) {
    var internalMapper = function (data, map) {
      if (data == null || data == undefined || data instanceof Boolean || data instanceof String) {
        return;
      }
      else if (data instanceof Array) {
        for (var item in data) {
          internalMapper(data[item], map)
        }
      }
      else if (data instanceof Object) {
        for (var prop in data) {
          var newProp = prop
          var val = data[prop]
          internalMapper(val, map)
          if (map.hasOwnProperty(prop)) {
            newProp = map[prop]
            delete data[prop]
            data[newProp] = val
          }
        }
      }
    };
    var ret = Object.assign({}, data)
    internalMapper(ret, map)
    return ret
  }
}
