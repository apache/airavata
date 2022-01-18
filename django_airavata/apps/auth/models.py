import uuid

from django.conf import settings
from django.core.exceptions import ValidationError
from django.db import models

from . import forms

VERIFY_EMAIL_TEMPLATE = 1
NEW_USER_EMAIL_TEMPLATE = 2
PASSWORD_RESET_EMAIL_TEMPLATE = 3
USER_ADDED_TO_GROUP_TEMPLATE = 4
VERIFY_EMAIL_CHANGE_TEMPLATE = 5


class EmailVerification(models.Model):
    username = models.CharField(max_length=64)
    verification_code = models.CharField(
        max_length=36, unique=True, default=uuid.uuid4)
    created_date = models.DateTimeField(auto_now_add=True)
    verified = models.BooleanField(default=False)
    next = models.CharField(max_length=255, null=True)


class EmailTemplate(models.Model):
    TEMPLATE_TYPE_CHOICES = (
        (VERIFY_EMAIL_TEMPLATE, 'Verify Email Template'),
        (NEW_USER_EMAIL_TEMPLATE, 'New User Email Template'),
        (PASSWORD_RESET_EMAIL_TEMPLATE, 'Password Reset Email Template'),
        (USER_ADDED_TO_GROUP_TEMPLATE, 'User Added to Group Template'),
        (VERIFY_EMAIL_CHANGE_TEMPLATE, 'Verify Email Change Template'),
    )
    template_type = models.IntegerField(
        primary_key=True, choices=TEMPLATE_TYPE_CHOICES)
    subject = models.CharField(max_length=255)
    body = models.TextField()
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)

    def __str__(self):
        for choice in self.TEMPLATE_TYPE_CHOICES:
            if self.template_type == choice[0]:
                return choice[1]
        return "Unknown"


class PasswordResetRequest(models.Model):
    username = models.CharField(max_length=64)
    reset_code = models.CharField(
        max_length=36, unique=True, default=uuid.uuid4)
    created_date = models.DateTimeField(auto_now_add=True)


class UserProfile(models.Model):
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE, related_name="user_profile")
    # This flag is only used for external IDP users. It indicates that the
    # username was properly initialized when the user logged in through the
    # external IDP. As for now that means that the username was set to the
    # user's email address. Sometimes the automatic assignment of username fails
    # and an administrator needs to intervene. When an administrator sets the
    # user's username this flag will also be set to true.
    username_initialized = models.BooleanField(default=False)

    @property
    def is_complete(self):
        return len(self.invalid_fields) == 0

    @property
    def is_username_valid(self):

        # Username was provided either by external IDP or manually set by an admin
        if self.username_initialized:
            return True

        # use forms.USERNAME_VALIDATOR
        try:
            forms.USERNAME_VALIDATOR(self.user.username)
            validates = True
        except ValidationError:
            validates = False
        return validates

    @property
    def is_first_name_valid(self):
        return self.is_non_empty(self.user.first_name)

    @property
    def is_last_name_valid(self):
        return self.is_non_empty(self.user.last_name)

    @property
    def is_email_valid(self):
        # Only checking for non-empty only; assumption is that email is verified
        # before it is set or updated
        return self.is_non_empty(self.user.email)

    @property
    def invalid_fields(self):
        result = []
        if not self.is_username_valid:
            result.append('username')
        if not self.is_email_valid:
            result.append('email')
        if not self.is_first_name_valid:
            result.append('first_name')
        if not self.is_last_name_valid:
            result.append('last_name')
        return result

    def is_non_empty(self, value: str):
        return value is not None and value.strip() != ""


class UserInfo(models.Model):
    claim = models.CharField(max_length=64)
    value = models.CharField(max_length=255)
    user_profile = models.ForeignKey(UserProfile, on_delete=models.CASCADE)
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ['user_profile', 'claim']

    def __str__(self):
        return f"{self.claim}={self.value}"


class IDPUserInfo(models.Model):
    idp_alias = models.CharField(max_length=64)
    claim = models.CharField(max_length=64)
    value = models.CharField(max_length=255)
    user_profile = models.ForeignKey(UserProfile, on_delete=models.CASCADE, related_name="idp_userinfo")
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ['user_profile', 'claim', 'idp_alias']

    def __str__(self):
        return f"{self.idp_alias}: {self.claim}={self.value}"


class PendingEmailChange(models.Model):
    user = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    email_address = models.EmailField()
    verification_code = models.CharField(
        max_length=36, unique=True, default=uuid.uuid4)
    created_date = models.DateTimeField(auto_now_add=True)
    verified = models.BooleanField(default=False)
