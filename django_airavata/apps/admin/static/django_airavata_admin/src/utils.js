import Vue from 'vue'

export default {
  convertKeyValuePairObjectToValueArray:function (obj) {
    var arr=[]
    for(var prop in obj){
      arr.push(obj[prop])
    }
    return arr
  }
  ,
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
  post: function (url, body, mediaType = "application/json") {
    var headers = this.createHeader(mediaType)
    return fetch(url, {
      method: 'post',
      body: JSON.stringify(body),
      headers: headers,
      credentials: "same-origin"
    }).then((response) => {
      if (response.ok) {
        return Promise.resolve(response.json())
      } else {
        return Promise.reject(new Error(response.statusText))
      }
    })
  },
  get: function (url, queryParams = "", mediaType = "application/json") {
    if (queryParams && typeof(queryParams) != "string") {
      queryParams = Object.keys(queryParams).map(key => encodeURIComponent(key) + "=" + encodeURIComponent(queryParams[key])).join("&")
    }
    url=url+"?"+queryParams
    var headers = this.createHeader(mediaType)
    return fetch(url, {
      method: 'get',
      headers: headers,
      credentials: "same-origin"
    }).then((response) => {
      if (response.ok) {
        return Promise.resolve(response.json())
      } else {
        return Promise.reject(new Error(response.statusText))
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
  }
}

