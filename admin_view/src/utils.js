

export default {
  addIndex:function (list){
    for(var i=0;i<list.length;i++){
      list[i].index=i;
    }
    return list;
  },
  getCSRFToken:function(){
    var csrfToken= document.cookie.split(';').map(val => val.trim()).filter(val => val.startsWith("csrftoken"+ '=')).map(val => val.split("=")[1]);
    if(csrfToken){
      return csrfToken[0];
    }else{
      return null;
    }
  },
  post:function (url,body,mediaType="application/json") {
    var csrfToken=this.getCSRFToken()
    var headers=new Headers({"Content-Type": mediaType,})
    if(csrfToken!=null){
      headers.set("X-CSRFToken",csrfToken)
    }
    return fetch(url,{
      method:'post',
      body:JSON.stringify(body),
      headers:headers,
      credentials: "same-origin"
    }).then((response)=>{
      if(response.ok){
        return Promise.resolve(response.json())
      }else{
        return Promise.reject(new Error(response.statusText))
      }
    })
  },
  mapper:function (data,map) {
    var internalMapper=function (data, map) {
      if(data == null || data == undefined || data instanceof Boolean || data instanceof String){
        return;
      }
      else if(data instanceof Array){
        for(var item in data){
          internalMapper(data[item],map)
        }
      }
      else if(data instanceof Object){
        for(var prop in data){
          var newProp=prop
          var val=data[prop]
          internalMapper(val,map)
          if(map.hasOwnProperty(prop)){
            newProp =map[prop]
            delete data[prop]
            data[newProp]=val
          }
        }
      }
    };
    var ret=Object.assign({},data)
    internalMapper(ret,map)
    return ret
  }
}

