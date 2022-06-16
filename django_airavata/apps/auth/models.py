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
USER_PROFILE_COMPLETED_TEMPLATE = 6


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
        (USER_PROFILE_COMPLETED_TEMPLATE, 'User Profile Completed Template'),
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

    @property
    def is_ext_user_profile_valid(self):
        fields = ExtendedUserProfileField.objects.filter(deleted=False)
        for field in fields:
            try:
                value = self.extended_profile_values.filter(ext_user_profile_field=field).get()
                if not value.valid:
                    return False
            except ExtendedUserProfileValue.DoesNotExist:
                if field.required:
                    return False
        return True

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


class ExtendedUserProfileField(models.Model):
    name = models.CharField(max_length=64)
    help_text = models.TextField(blank=True)
    order = models.IntegerField()
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)
    deleted = models.BooleanField(default=False)
    required = models.BooleanField(default=True)

    def __str__(self) -> str:
        return f"{self.name} ({self.id})"

    @property
    def field_type(self):
        if hasattr(self, 'text'):
            return 'text'
        elif hasattr(self, 'single_choice'):
            return 'single_choice'
        elif hasattr(self, 'multi_choice'):
            return 'multi_choice'
        elif hasattr(self, 'user_agreement'):
            return 'user_agreement'
        else:
            raise Exception("Could not determine field_type")


class ExtendedUserProfileTextField(ExtendedUserProfileField):
    field_ptr = models.OneToOneField(ExtendedUserProfileField,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="text")


class ExtendedUserProfileSingleChoiceField(ExtendedUserProfileField):
    field_ptr = models.OneToOneField(ExtendedUserProfileField,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="single_choice")
    other = models.BooleanField(default=False)


class ExtendedUserProfileFieldChoice(models.Model):
    display_text = models.CharField(max_length=255)
    order = models.IntegerField()
    deleted = models.BooleanField(default=False)

    class Meta:
        abstract = True

    def __str__(self) -> str:
        return f"{self.display_text} ({self.id})"


class ExtendedUserProfileSingleChoiceFieldChoice(ExtendedUserProfileFieldChoice):
    single_choice_field = models.ForeignKey(ExtendedUserProfileSingleChoiceField, on_delete=models.CASCADE, related_name="choices")


class ExtendedUserProfileMultiChoiceField(ExtendedUserProfileField):
    field_ptr = models.OneToOneField(ExtendedUserProfileField,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="multi_choice")
    other = models.BooleanField(default=False)


class ExtendedUserProfileMultiChoiceFieldChoice(ExtendedUserProfileFieldChoice):
    multi_choice_field = models.ForeignKey(ExtendedUserProfileMultiChoiceField, on_delete=models.CASCADE, related_name="choices")


class ExtendedUserProfileAgreementField(ExtendedUserProfileField):
    field_ptr = models.OneToOneField(ExtendedUserProfileField,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="user_agreement")
    # if no checkbox label, then some default text will be used
    checkbox_label = models.TextField(blank=True)


class ExtendedUserProfileFieldLink(models.Model):
    label = models.TextField()
    url = models.URLField()
    order = models.IntegerField()
    display_link = models.BooleanField(default=True)
    display_inline = models.BooleanField(default=False)
    # Technically any field can have links
    field = models.ForeignKey(ExtendedUserProfileField, on_delete=models.CASCADE, related_name="links")

    def __str__(self) -> str:
        return f"{self.label} {self.url}"


class ExtendedUserProfileValue(models.Model):
    ext_user_profile_field = models.ForeignKey(ExtendedUserProfileField, on_delete=models.SET_NULL, null=True)
    user_profile = models.ForeignKey(UserProfile, on_delete=models.CASCADE, related_name="extended_profile_values")
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)

    @property
    def value_type(self):
        if hasattr(self, 'text'):
            return 'text'
        elif hasattr(self, 'single_choice'):
            return 'single_choice'
        elif hasattr(self, 'multi_choice'):
            return 'multi_choice'
        elif hasattr(self, 'user_agreement'):
            return 'user_agreement'
        else:
            raise Exception("Could not determine value_type")

    @property
    def value_display(self):
        if self.value_type == 'text':
            return self.text.text_value
        elif self.value_type == 'single_choice':
            if self.single_choice.choice:
                try:
                    choice = self.ext_user_profile_field.single_choice.choices.get(id=self.single_choice.choice)
                    return choice.display_text
                except ExtendedUserProfileSingleChoiceFieldChoice.DoesNotExist:
                    return None
            elif self.single_choice.other_value:
                return f"Other: {self.single_choice.other_value}"
        elif self.value_type == 'multi_choice':
            result = []
            if self.multi_choice.choices:
                mc_field = self.ext_user_profile_field.multi_choice
                for choice_value in self.multi_choice.choices.all():
                    try:
                        choice = mc_field.choices.get(id=choice_value.value)
                        result.append(choice.display_text)
                    except ExtendedUserProfileMultiChoiceFieldChoice.DoesNotExist:
                        continue
            if self.multi_choice.other_value:
                result.append(f"Other: {self.multi_choice.other_value}")
            return result
        elif self.value_type == 'user_agreement':
            if self.user_agreement.agreement_value:
                return "Yes"
            else:
                return "No"
        return None

    @property
    def value_display_list(self):
        """Same as value_display except coerced always to a list."""
        value_display = self.value_display
        if value_display is not None and not isinstance(value_display, list):
            return [value_display]
        else:
            return value_display

    @property
    def valid(self):
        # if the field is deleted, whatever the value, consider it valid
        if self.ext_user_profile_field.deleted:
            return True
        if self.ext_user_profile_field.required:
            if self.value_type == 'text':
                return self.text.text_value and len(self.text.text_value.strip()) > 0
            if self.value_type == 'single_choice':
                choice_exists = (self.single_choice.choice and
                                 self.ext_user_profile_field.single_choice.choices
                                 .filter(id=self.single_choice.choice).exists())
                has_other = (self.ext_user_profile_field.single_choice.other and
                             self.single_choice.other_value and
                             len(self.single_choice.other_value.strip()) > 0)
                return choice_exists or has_other
            if self.value_type == 'multi_choice':
                choice_ids = list(map(lambda c: c.value, self.multi_choice.choices.all()))
                choice_exists = self.ext_user_profile_field.multi_choice.choices.filter(id__in=choice_ids).exists()
                has_other = (self.ext_user_profile_field.multi_choice.other and
                             self.multi_choice.other_value and
                             len(self.multi_choice.other_value.strip()) > 0)
                return choice_exists or has_other
            if self.value_type == 'user_agreement':
                return self.user_agreement.agreement_value is True
        return True


class ExtendedUserProfileTextValue(ExtendedUserProfileValue):
    value_ptr = models.OneToOneField(ExtendedUserProfileValue,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="text")
    text_value = models.TextField()


class ExtendedUserProfileSingleChoiceValue(ExtendedUserProfileValue):
    value_ptr = models.OneToOneField(ExtendedUserProfileValue,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="single_choice")
    # Only one of value or other_value should be populated, not both
    choice = models.BigIntegerField(null=True)
    other_value = models.TextField(blank=True)


class ExtendedUserProfileMultiChoiceValue(ExtendedUserProfileValue):
    value_ptr = models.OneToOneField(ExtendedUserProfileValue,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="multi_choice")
    other_value = models.TextField(blank=True)


class ExtendedUserProfileMultiChoiceValueChoice(models.Model):
    value = models.BigIntegerField()
    multi_choice_value = models.ForeignKey(ExtendedUserProfileMultiChoiceValue, on_delete=models.CASCADE, related_name="choices")


class ExtendedUserProfileAgreementValue(ExtendedUserProfileValue):
    value_ptr = models.OneToOneField(ExtendedUserProfileValue,
                                     on_delete=models.CASCADE,
                                     parent_link=True,
                                     primary_key=True,
                                     related_name="user_agreement")
    agreement_value = models.BooleanField()
