<template>
  <div>
    <div class="array-view-main">
      <div class="array-view-main2">
        <input class="deployment btn left" type="button" value="Previous" v-if="data > 0"
               v-on:click.prevent="previousClickHandler()"/>
        <input class="deployment btn home-btn" type="button" v-bind:value="homeBtnName" v-on:click="homeAction"/>
        <input class="deployment btn right" type="button" value="Next" v-if="data < maxIndex"
               v-on:click.prevent="nextClickHandler()"/>
      </div>
    </div>
    <slot>

    </slot>
  </div>
</template>

<script>
  import { mixins } from "django-airavata-common-ui"

  export default {
    name: "array-component-view",
    mixins: [mixins.VModelMixin],
    props: {
      homeBtnName: {
        type: String,
        default: "Home"
      },
      maxIndex: {
        type: Number,
        default: 0
      },
      homeAction: {
        type: Function,
        default: function () {

        }
      }
    },
    methods: {
      previousClickHandler: function () {
        if (this.data != 0) {
          this.data--;
        }
      },
      nextClickHandler: function () {
        if (this.data < this.maxIndex) {
          this.data++;
        }
      }
    }
  }
</script>

<style scoped>
  .array-view-main {
    position: fixed;
    height: 50px;
    bottom: 0px;
    left: 10px;
    right: 10px;
    display: inline-block;
    border: 1px solid #9d9d9d;
    padding-top: 10px;
    background-color: #FFFFFF;
  }

  .left {
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    float: left;
  }

  .right {
    border-top-right-radius: 20px;
    border-bottom-right-radius: 20px;
    float: right;
  }

  .array-view-main2 .left, .right {
    width: 100px;
  }

  .array-view-main2 {
    padding-left: 15%;
    padding-right: 15%;
  }

  .home-btn {
    margin-left: 35%;
    margin-right: 35%;
    float: none;
  }
  .array-view-main + * {
    padding-bottom: 50px;
  }
</style>
