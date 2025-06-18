<template>
  <tr>
    <td>{{ project.name }}</td>
    <td>{{ project.owner }}</td>
    <td v-bind:title="project.creationTime">{{ creationTime }}</td>
    <td>
      <a :href="editLink" v-if="project.userHasWriteAccess"
        >Edit <i class="fa fa-edit" aria-hidden="true"></i
      ></a>
    </td>
  </tr>
</template>

<script>
import urls from "../../utils/urls";
import moment from "moment";

export default {
  name: "project-list-item",
  props: ["project"],
  computed: {
    creationTime: function () {
      var dt = new Date(this.project.creationTime);
      return moment(dt).fromNow();
    },
    editLink() {
      return urls.editProject(this.project);
    },
  },
};
</script>

<style></style>
