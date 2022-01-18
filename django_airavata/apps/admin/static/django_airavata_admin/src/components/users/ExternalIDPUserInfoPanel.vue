<template>
  <b-card header="External IDP Userinfo">
    <b-table :items="items" small borderless sort-by="claim" />
    <small class="text-muted"
      >This is the user information provided by the user's authentication
      provider. The IDP alias used is
      {{ externalIDPUserInfo.idp_alias || "N/A" }}.
    </small>
  </b-card>
</template>

<script>
export default {
  name: "external-idp-user-info-panel",
  props: ["externalIDPUserInfo"],
  computed: {
    userinfo() {
      return this.externalIDPUserInfo.userinfo
        ? this.externalIDPUserInfo.userinfo
        : {};
    },
    items() {
      return Object.keys(this.userinfo).map((claim) => {
        return {
          claim: claim,
          value: this.externalIDPUserInfo.userinfo[claim],
        };
      });
    },
  },
};
</script>

<style></style>
