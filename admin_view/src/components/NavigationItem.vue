<template>
  <a href="#0" class="c-nav__item is-active" data-toggle="tooltip" data-placement="right" v-bind:title="name">
    <i v-bind:class="'fa '+ class_name"></i>
    <span class="sr-only">Dashboard</span>
  </a>
</template>
<script>
  export default {
    name:"nav-item",
    props:['name','class_name']
  }
</script>
