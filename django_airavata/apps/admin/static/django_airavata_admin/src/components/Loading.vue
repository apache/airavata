<template>
<div v-if="display">

</div>
</template>
<script>
  import { createNamespacedHelpers } from 'vuex'

  const {mapGetters} = createNamespacedHelpers('loading')
  export default {
    computed:{
      ...mapGetters(['display'])
    }
  }

</script>
