<template>
  <div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <b-table
              hover
              :fields="fields"
              :items="items"
            >
              <template
                slot="creationTime"
                slot-scope="data">
                <human-date :date="data.value"/>
              </template>
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
import { components } from "django-airavata-common-ui";
import { services } from "django-airavata-api";
export default {
  name: "unverified-email-user-management-container",
  data() {
    return {
      usersPaginator: null,
    };
  },
  components: {
    pager: components.Pager,
    'human-date': components.HumanDate,
  },
  created() {
    services.UnverifiedEmailUserProfileService.list({ limit: 10 }).then(
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
          label: "Email Verified",
          key: "emailVerified"
        },
        {
          label: "Created",
          key: "creationTime"
        },
      ];
    },
    items() {
      return this.usersPaginator
        ? this.usersPaginator.results
        : [];
    },
  },
  methods: {
    next() {
      this.usersPaginator.next();
    },
    previous() {
      this.usersPaginator.previous();
    },
  }
};
</script>

