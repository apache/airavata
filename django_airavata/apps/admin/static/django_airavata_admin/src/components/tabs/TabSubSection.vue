<template>
  <div class="main_section interface-main">
    <div class="input-field-header">
      {{sectionName}}
      <img v-if="enableDeletion" v-on:click="deleteSection()" src="/static/images/delete.png"/>
    </div>
    <slot></slot>
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
      }
    },
    methods: {
      deleteSection: function () {
        if (this.sectionType != null) {
          this.$emit('delete_section', this.sectionId, this.sectionType)
        } else {
          this.$emit('delete_section', this.sectionId)
        }
      }
    }
  }
</script>

<style scoped>
  .input-field-header img {
    float: right;
  }

  .main_section.interface-main .entry {
    margin-bottom: 40px;
    margin-left: 15px;
    margin-right: 15px;
  }
</style>
