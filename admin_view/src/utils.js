

export default {
  addIndex:function (list){
    for(var i=0;i<list.length;i++){
      list[i].index=i;
    }
    return list;
  },
  post:function (url,body) {
    return fetch(url,{
      method:'post',
      body:JSON.stringify()
    }).then((response)=>{
      if(response.ok()){
        return Promise.resolve(response.json())
      }else{
        return Promise.reject(new Error(response.statusText))
      }
    })
  },
  mapper:function (data,map) {

  }
}

