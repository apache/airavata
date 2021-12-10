<template>
  <b-card header="Change Username">
    <p class="card-text">
      This will change the user's username in the identity service. Typically,
      you would only change the user's username when they login through an
      external identity provider and are automatically assigned an invalid
      username. Also, after updating the username the user will need to log out
      and log back in.
    </p>
    <b-alert variant="warning" :show="airavataUserProfileExists">
      This user already has an Airavata User Profile. Giving the user a new
      username will result in the user getting a new Airavata User Profile and
      losing the old one and everything (projects, experiments, etc.) associated
      with it.
    </b-alert>
    <b-form-group label="New Username" label-for="new-username">
      <b-input-group>
        <b-form-input
          id="new-username"
          v-model="$v.newUsername.$model"
          :state="validateState($v.newUsername)"
        />
        <b-input-group-append>
          <b-button @click="newUsername = email">Copy Email Address</b-button>
        </b-input-group-append>
      </b-input-group>
      <b-form-invalid-feedback
        :state="validateState($v.newUsername)"
        v-if="!$v.newUsername.emailOrMatchesRegex"
      >
        Username can only contain lowercase letters, numbers, underscores and
        hyphens OR it can be the same as the email address.
      </b-form-invalid-feedback>
    </b-form-group>
    <confirmation-button
      variant="primary"
      @confirmed="updateUsername"
      :disabled="$v.$invalid || username === newUsername"
      dialog-title="Please confirm username change"
    >
      Please confirm that you want to change the user's username to
      <strong>{{ newUsername }}</strong
      >. After updating the username the user will need to log out and log back
      in.
      <b-alert variant="danger" :show="airavataUserProfileExists">
        This user already has an Airavata User Profile. Giving the user a new
        username will result in the user getting a new Airavata User Profile and
        <strong
          >losing the old one and everything (projects, experiments, etc.)
          associated with it</strong
        >.
      </b-alert>
    </confirmation-button>
  </b-card>
</template>

<script>
import { components, errors } from "django-airavata-common-ui";
import { validationMixin } from "vuelidate";
import { helpers, or, required, sameAs } from "vuelidate/lib/validators";
export default {
  name: "change-username-panel",
  mixins: [validationMixin],
  props: {
    username: {
      type: String,
      required: true,
    },
    email: {
      type: String,
      required: true,
    },
    airavataUserProfileExists: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    "confirmation-button": components.ConfirmationButton,
  },
  data() {
    return {
      newUsername: this.username,
    };
  },
  validations() {
    const usernameRegex = helpers.regex("newUsername", /^[a-z0-9_-]+$/);
    const emailOrMatchesRegex = or(usernameRegex, sameAs("email"));
    return {
      newUsername: {
        required,
        emailOrMatchesRegex,
      },
    };
  },
  methods: {
    updateUsername() {
      if (!this.$v.$invalid) {
        this.$emit("update-username", [this.username, this.newUsername]);
      }
    },
    validateState: errors.vuelidateHelpers.validateState,
  },
};
</script>
