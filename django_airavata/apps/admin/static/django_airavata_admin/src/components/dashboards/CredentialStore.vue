<template>
  <div class="gen">
    <loading></loading>
    <div class="main_section interface-main" v-on:created="fetchSSHKeys()">
      <div class="input-field-header">
        Generate New SSH Key
      </div>
      <generate-s-s-h-key v-on:created="fetchSSHKeys()"></generate-s-s-h-key>
    </div>
    <div class="main_section interface-main">
      <div class="input-field-header">
        SSH Keys
      </div>
      <div>
        <div class="ssh" v-bind:id="'id_'+sshKey.token" v-for="sshKey in sshKeys">
          <h6>Description:</h6>
          <label v-if="sshKey.description">{{sshKey.description}}</label>
          <label style="color:#007BFF" v-else>Description for the SHH Key is not available</label>
          <div class="ssh-key">
            <div class="ssh-val">
              <input readonly type="text" v-bind:id="sshKey.token" v-model="sshKey.publicKey"/>
              <button class="vbtn vbtn-default" v-on:click="copySSHKey(sshKey.token)">Copy</button>
            </div>
            <button class="vbtn vbtn-cancel" v-on:click="deleteSSHKey(sshKey)">Delete</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import GenerateSSHKey from '../admin/GenerateSSHKey.vue'
  import Loading from '../Loading.vue'
  import Utils from '../../utils'

  export default {
    components: {
      GenerateSSHKey, Loading
    },
    mounted: function () {
      this.fetchSSHKeys()
    },
    data: function () {
      return {
        sshKeys: []
      }
    },
    methods: {
      fetchSSHKeys: function () {
        Utils.get('/api/credentials/ssh/keys', {success: (value) => this.sshKeys = value})
      },
      deleteSSHKey: function (sshKey) {
        var success = (value) => {
          console.log("Successfully Deleted key:", value)
          var el=document.getElementById('id_'+sshKey.token)
          el.classList.add("remove")
          this.sshKeys.splice(this.sshKeys.indexOf(sshKey), 1)
          this.sshKeys = [].concat(this.sshKeys)
        }
        Utils.post('/api/credentials/ssh/key/delete', {'token': sshKey.token}, {success: success})
      },
      copySSHKey: function (token) {
        var sshKeyElement = document.getElementById(token)
        sshKeyElement.select()
        document.execCommand('copy')
        sshKeyElement.blur()
      }

    }
  }
</script>
<style>
  .gen {
    padding: 10px;
    width: 40%;
    height: 100%;
  }

  @keyframes fadeIn {
    from{opacity: 0}
    to{opacity: 1}
  }

  @keyframes fadeOut {
    from{opacity: 1}
    to{opacity: 0}
  }

  .ssh {
    padding: 10px;
    border-bottom: solid 1px #dddddd;
    animation: fadeIn 1s;
  }
  .ssh.remove{
    animation: fadeOut 1s;
    opacity: 1;
  }


  .ssh-key {
    display: inline-flex;
    width: 100%;
  }

  .ssh-val {
    display: inline-flex;
    width: 100%;
  }

  .ssh-val button {
    margin-left: 0px;
    border-top-left-radius: 0px;
    border-bottom-left-radius: 0px;
  }

  .ssh-val input {
    border-top-right-radius: 0px;
    border-bottom-right-radius: 0px;
  }

  .ssh .vbtn.vbtn-cancel:hover {
    border-color: #ff0b03;
    background-color: #ff0b03;
    color: white;
  }

  .ssh .vbtn.vbtn-cancel:active {
    border-color: #ff0b03;
    background-color: white;
    color: #ff0b03;
  }
</style>
