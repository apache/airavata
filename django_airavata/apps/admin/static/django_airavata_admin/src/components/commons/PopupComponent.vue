<template>
  <transition name="fade">
    <div class="popup-1" v-if="data">
      <div class="popup-2">
        <label class="popup-btn" v-if="enableClose" v-on:click="closeHandler">
          X
        </label>
        <div class="popup-content">
          <slot>
          </slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
  import { mixins } from "django-airavata-common-ui"

  export default {
    name: "PopupComponent",
    props: {
      enableClose: {
        type: Boolean,
        default: true
      },
    },
    mixins: [mixins.VModelMixin],
    methods: {
      closeHandler: function () {
        this.data = false;
      }
    },
    watch: {
      value: function (newValue) {
        this.data = newValue;
      }
    }
  }
</script>

<style scoped>
  .popup-1 {
    position: fixed;
    top: 0;
    left: 0;
    padding-left: 20%;
    padding-top: 20%;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.5);
    z-index: 10;
  }

  .popup-2 {
    border: 1px solid #007BFF;
    border-radius: 5px;
    position: inherit;
    width: 45%;
    background: white;
  }

  .popup-content {
    padding: 50px 40px 35px 40px;
    width: 100%
  }

  .popup-btn {
    position: absolute;
    top: 0px;
    right: 0px;
    padding: 5px 5px 5px 8px;
    border: 1px solid #007BFF;
    border-top-right-radius: 5px;
    border-bottom-left-radius: 5px;
    border-top-color: transparent;
    border-right-color: transparent;
    color: #007BFF;
    width: 30px;
    cursor: pointer;
  }

  .popup-btn:hover {
    color: white;
    background-color: #ff0b03;
    border-color: #ff0b03;
    cursor: pointer;
  }

  .popup-btn:active {
    color: #ff0b03;
    background-color: white;
    border-color: #ff0b03;
    border-top-color: transparent;
    border-right-color: transparent;
  }

  .tmp {
    width: 100%
  }
</style>
