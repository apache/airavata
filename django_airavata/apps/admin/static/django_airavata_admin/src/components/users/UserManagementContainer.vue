<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Users</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-table
              hover
              :fields="fields"
              :items="items"
            >
            </b-table>
            <pager
              v-bind:paginator="usersPaginator"
              v-on:next="next"
              v-on:previous="previous"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { services } from "django-airavata-api";
import { components } from "django-airavata-common-ui";

export default {
  name: "user-management-container",
  data() {
    return {
      usersPaginator: null
    };
  },
  components: {
    pager: components.Pager
  },
  created() {
    services.ManagedUserProfileService.list({ limit: 10 }).then(
      users => (this.usersPaginator = users)
    );
  },
  computed: {
    fields() {
      return [
        {
          label: "First Name",
          key: "firstName"
        },
        {
          label: "Last Name",
          key: "lastName"
        },
        {
          label: "Username",
          key: "userId"
        },
        {
          label: "Email",
          key: "email"
        },
        {
          label: "Enabled",
          key: "enabled"
        },
        {
          label: "Email Verified",
          key: "emailVerified"
        },
        {
          label: "Action",
          key: "action"
        }
      ];
    },
    items() {
      return this.usersPaginator ? this.usersPaginator.results : [];
    }
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    }
  }
};
</script>

