<template>
  <div>Application Deployment Editor</div>
</template>

<script>
export default {
  name: "application-deployment-editor",
  props: {
    id: {
      type: String,
      required: true
    },
    deployment_id: {
      type: String,
      required: true
    }
  }
};
</script>

