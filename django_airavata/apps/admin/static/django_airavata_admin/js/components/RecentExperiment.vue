<template>
  <li class="feed__list-item">
    <span class="feed__label text-secondary">{{experiment.name}}</span>
    <h2 class="feed__title mb-2"><a href="#0">{{experiment.description}}</a></h2>
    <span class="badge badge-success" v-if="experiment.status == 'Completed'">{{experiment.status}}</span>
    <span class="badge badge-primary" v-else-if="experiment.status == 'Created'">{{experiment.status}}</span>
    <span class="badge badge-danger" v-else="experiment.status == 'Failed'">{{experiment.status}}</span>
    <div class="feed__item-meta text-secondary mt-1">
      <span>Updated</span>
      <time>{{experiment.updated}}</time>
    </div>
  </li>
</template>
<script>
  export  default {
    name:'recent-experiment',
    props:['experiment']
  }
</script>
