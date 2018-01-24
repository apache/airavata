<template>
  <div class="new_app">
    <h3>{{title}}</h3>
    <div class="main">
      <div class="tabs">
        <div class="tab" v-bind:class="tabs[index]" v-for="(tabName,index) in tabNames">
          <label class="lbl" v-on:click="tabClickHandler(index)">{{tabName}}</label>
        </div>
        <div class="tab" style="width: 100%"></div>
      </div>
      <transition name="fade">
        <slot></slot>
      </transition>
    </div>
  </div>
</template>
<script>


  export default {
    components: {
    },
    mounted:function () {
      this.activeTabId=this.defaultActiveTab
    }
    ,
    data: function () {
      return {
        activeTabId: 0,
      }
    },
    props: {
      title:{
        type:String,
        default:"Tabs"
      },
      tabNames:{
        type:Array,
      },
      defaultActiveTab:{
        type:Number,
        default:0
      }
    },
    computed: {
      tabs: function () {
        var tabs = this.tabNames.map((value, index) => {
          if (index == this.activeTabId) {
            return "active"
          } else {
            return ""
          }
        })
        return tabs;
      }
    },
    methods: {
      tabClickHandler: function (tabId) {
        if(this.activeTabId!=tabId){
           this.$emit("tabchange",tabId,this.activeTabId)
           this.activeTabId = tabId
        }
      }
    }
  }
</script>
<style>
  .new_app {
    margin: 45px;
    width: 70%;
  }

  .main {
    width: 100%;
    margin-top: 50px;
  }

  .tab {
    text-align: center;
    width: 120px;
    margin-bottom: 15px;
    border-bottom: solid #999999 1px;
    color: #007BFF;
  }

  .tab .lbl:hover {
    cursor: pointer;
  }

  .active .lbl:hover {
    cursor: default;
  }

  .lbl {
    margin: 10px;
    color: inherit;
  }

  .link {
    color: inherit;
  }

  .link {
    color: inherit;
  }

  .active {
    color: #333333;
    border-top: solid #999999 1px;
    border-left: solid #999999 1px;
    border-right: solid #999999 1px;
    border-bottom: hidden;
    border-top-right-radius: 3px;
    border-top-left-radius: 3px;
  }

  .tabs {
    display: flex;
    width: 100%;
  }


</style>
