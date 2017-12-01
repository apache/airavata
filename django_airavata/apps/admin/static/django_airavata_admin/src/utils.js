import Vue from 'vue'
import Store from './store/store'


export default {
  resetData:function (obj,initialData) {
    if (initialData instanceof Function){
      initialData=initialData();
    }
    for(var prop in initialData){
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
  getCSRFToken: function () {
    var csrfToken = document.cookie.split(';').map(val => val.trim()).filter(val => val.startsWith("csrftoken" + '=')).map(val => val.split("=")[1]);
    if (csrfToken) {
      return csrfToken[0];
    } else {
      return null;
    }
  },
  createHeader: function (contentType = "application/json") {
    var csrfToken = this.getCSRFToken()
    var headers = new Headers({"Content-Type": contentType,})
    if (csrfToken != null) {
      headers.set("X-CSRFToken", csrfToken)
    }
    return headers;
  },
  post: function (url, body, {success=(value)=>console.log("Request Successful",value),failure=(value)=>console.log("Request Failed",value), mediaType = "application/json"}={}) {
    var headers = this.createHeader(mediaType)
    Store.dispatch('loading/loadingStarted')
    return fetch(url, {
      method: 'post',
      body: JSON.stringify(body),
      headers: headers,
      credentials: "same-origin"
    }).then((response) => {
      Store.dispatch('loading/loadingCompleted')
      if (response.ok) {
        return Promise.resolve(response.json()).then(success)
      } else {
        return Promise.reject(failure(response.statusText))
      }
    })
  },
  get: function (url, {queryParams = null, success=(value)=>console.log("Request Successful",value),failure=(value)=>console.log("Request Failed",value), mediaType = "application/json"}={}) {
    if(queryParams&& typeof(queryParams) != "string"){
      url=url+"?"+Object.keys(queryParams).map(paramName => encodeURIComponent(paramName)+"="+encodeURIComponent(queryParams[paramName])).join("&")
    }
    Store.dispatch('loading/loadingStarted')

    var headers = this.createHeader(mediaType)
    return fetch(url, {
      method: 'get',
      headers: headers,
      credentials: "same-origin"
    }).then((response) => {
      Store.dispatch('loading/loadingCompleted')
      if (response.ok) {
        return Promise.resolve(response.json()).then(success)
      } else {
        return Promise.reject(failure(response.statusText))
      }
    })

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
  },
  convertKeyValuePairObjectToValueArray:function (map) {
    var arr=[]
    for(var prop in map){
      arr.push(map[prop])
    }
    return arr
  }
}

