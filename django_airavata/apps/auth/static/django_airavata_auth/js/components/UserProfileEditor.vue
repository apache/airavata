<template>
  <b-card>
    <b-form-group label="Username">
      <b-form-input disabled :value="user.username" />
    </b-form-group>
    <b-form-group label="First Name">
      <b-form-input
        v-model="$v.user.first_name.$model"
        @keydown.native.enter="save"
        :state="validateState($v.user.first_name)"
      />
    </b-form-group>
    <b-form-group label="Last Name">
      <b-form-input
        v-model="$v.user.last_name.$model"
        @keydown.native.enter="save"
        :state="validateState($v.user.last_name)"
      />
    </b-form-group>
    <b-form-group label="Email">
      <b-form-input
        v-model="$v.user.email.$model"
        @keydown.native.enter="save"
        :state="validateState($v.user.email)"
      />
      <b-form-invalid-feedback v-if="!$v.user.email.email">
        {{ user.email }} is not a valid email address.
      </b-form-invalid-feedback>
      <b-alert class="mt-1" show v-if="user.pending_email_change"
        >Once you verify your email address at
        <strong>{{ user.pending_email_change.email_address }}</strong
        >, your email address will be updated. If you didn't receive the
        verification email,
        <b-link @click="$emit('resend-email-verification')"
          >click here to resend verification link.</b-link
        ></b-alert
      >
    </b-form-group>
    <b-button variant="primary" @click="save" :disabled="$v.$invalid"
      >Save</b-button
    >
  </b-card>
</template>

<script>
import { models } from "django-airavata-api";
import { errors } from "django-airavata-common-ui";
import { validationMixin } from "vuelidate";
import { email, required } from "vuelidate/lib/validators";

export default {
  name: "user-profile-editor",
  mixins: [validationMixin],
  props: {
    value: {
      type: models.User,
      required: true,
    },
  },
  data() {
    return {
      user: this.cloneValue(),
    };
  },
  validations() {
    return {
      user: {
        first_name: {
          required,
        },
        last_name: {
          required,
        },
        email: {
          required,
          email,
        },
      },
    };
  },
  methods: {
    cloneValue() {
      return JSON.parse(JSON.stringify(this.value));
    },
    save() {
      if (!this.$v.$invalid) {
        this.$emit("save", this.user);
      }
    },
    validateState: errors.vuelidateHelpers.validateState,
  },
  watch: {
    value() {
      this.user = this.cloneValue();
    },
  },
};
</script>

<style></style>
