<template>
  <div v-if="user">
    <b-form-group
      label="Username"
      :disabled="true"
      description="Only administrators can update a username."
    >
      <b-form-input v-model="user.username" />
    </b-form-group>
    <b-form-group label="First Name" :disabled="disabled">
      <b-form-input
        v-model="$v.first_name.$model"
        @keydown.native.enter="save"
        :state="validateState($v.first_name)"
      />
    </b-form-group>
    <b-form-group label="Last Name" :disabled="disabled">
      <b-form-input
        v-model="$v.last_name.$model"
        @keydown.native.enter="save"
        :state="validateState($v.last_name)"
      />
    </b-form-group>
    <b-form-group label="Email" :disabled="disabled">
      <b-form-input
        v-model="$v.email.$model"
        @keydown.native.enter="save"
        :state="validateState($v.email)"
      />
      <b-form-invalid-feedback v-if="!$v.email.email">
        {{ email }} is not a valid email address.
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
  </div>
</template>

<script>
import { errors } from "django-airavata-common-ui";
import { validationMixin } from "vuelidate";
import { email, required } from "vuelidate/lib/validators";
import { mapGetters, mapMutations } from "vuex";

export default {
  name: "user-profile-editor",
  mixins: [validationMixin],
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  created() {
    if (!this.disabled) {
      this.$v.$touch();
    }
  },
  data() {
    return {};
  },
  computed: {
    ...mapGetters("userProfile", ["user"]),
    first_name: {
      get() {
        return this.user.first_name;
      },
      set(first_name) {
        this.setFirstName({ first_name });
      },
    },
    last_name: {
      get() {
        return this.user.last_name;
      },
      set(last_name) {
        this.setLastName({ last_name });
      },
    },
    email: {
      get() {
        return this.user.email;
      },
      set(email) {
        this.setEmail({ email });
      },
    },
    valid() {
      return !this.$v.$invalid;
    },
  },
  validations() {
    return {
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
    };
  },
  methods: {
    ...mapMutations("userProfile", ["setFirstName", "setLastName", "setEmail"]),
    save() {
      this.$emit("save");
    },
    validateState: errors.vuelidateHelpers.validateState,
  },
};
</script>

<style></style>
