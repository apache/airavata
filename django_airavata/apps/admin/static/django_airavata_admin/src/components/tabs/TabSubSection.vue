<template>
  <div class="tab-section-1 interface-main">
    <div class="input-field-header">
      {{sectionName}}
      <img v-if="enableDeletion" v-on:click="deleteSection()" src="/static/images/delete.png"/>
    </div>
    <div class="tab-view">
      <slot></slot>
    </div>
  </div>
</template>

<script>
  export default {
    name: "tab-sub-section",
    props: {
      sectionName: {
        type: String
      },
      sectionId: {
        default: "na"
      },
      sectionType: {
        type: String,
        default: null
      },
      enableDeletion: {
        type: Boolean,
        default: true
      },
      deleteAction: {
        type: Function,
        default: null
      }
    },
    methods: {
      deleteSection: function () {
        if (this.deleteAction) {
          this.deleteAction();
        } else {
          if (this.sectionType != null) {
            this.$emit('delete_section', this.sectionId, this.sectionType)
          } else {
            this.$emit('delete_section', this.sectionId)
          }
        }
      }
    }
  }
</script>

<style scoped>
  .input-field-header img {
    float: right;
  }

  .tab-section-1 {
    width: 100%;
    display: block;
    margin-top: 10px;
  }

  .tab-section-1.interface-main .entry {
    margin-bottom: 40px;
    margin-left: 15px;
    margin-right: 15px;
  }
</style>
