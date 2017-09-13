<template>
    <ul id="project-list">
        <li v-for="project in projects">
            {{ project.name }}
        </li>
    </ul>
</template>

<script>
export default {
  name: 'project-list',
  data () {
    return {
      projects: [
              {"name": "Project 1"},
              {"name": "Project 2"},
              {"name": "Project 3"},
              {"name": "Project 4"},
          ]
      }
  }
}
</script>

<style>
</style>
