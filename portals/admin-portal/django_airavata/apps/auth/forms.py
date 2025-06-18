import logging

from django import forms
from django.core import validators

from . import iam_admin_client

logger = logging.getLogger(__name__)

USERNAME_VALIDATOR = validators.RegexValidator(
    regex=r"^[a-z0-9_-]+$",
    message="Username can only contain lowercase letters, numbers, "
            "underscores and hyphens."
)
PASSWORD_VALIDATOR = validators.RegexValidator(
    regex=r"^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@!$#*&]).*$",
    message="Password needs to contain at least (a) One lower case letter (b) "
            "One Upper case letter and (c) One number (d) One of the following"
            " special characters - !@#$&*"
)


class CreateAccountForm(forms.Form):
    error_css_class = "is-invalid"
    username = forms.CharField(
        label='Username',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'Username'}),
        min_length=6,
        validators=[USERNAME_VALIDATOR],
        help_text=USERNAME_VALIDATOR.message)
    password = forms.CharField(
        label='Password',
        widget=forms.PasswordInput(attrs={'class': 'form-control',
                                          'placeholder': 'Password'}),
        min_length=8,
        max_length=48,
        validators=[PASSWORD_VALIDATOR],
        help_text=PASSWORD_VALIDATOR.message)
    password_again = forms.CharField(
        label='Password (again)',
        widget=forms.PasswordInput(attrs={'class': 'form-control',
                                          'placeholder': 'Password (again)'}))
    email = forms.EmailField(
        label='E-mail',
        widget=forms.EmailInput(attrs={'class': 'form-control',
                                       'placeholder': 'email@example.com'}))
    email_again = forms.EmailField(
        label='E-mail (again)',
        widget=forms.EmailInput(
            attrs={
                'class': 'form-control',
                'placeholder': 'email@example.com (again)'}))
    first_name = forms.CharField(
        label='First Name',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'First Name'}))
    last_name = forms.CharField(
        label='Last Name',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'Last Name'}))

    next = forms.CharField(widget=forms.HiddenInput(), required=False)

    def clean(self):
        cleaned_data = super().clean()
        password = cleaned_data.get('password')
        password_again = cleaned_data.get('password_again')

        if password and password_again and password != password_again:
            self.add_error(
                'password',
                forms.ValidationError("Passwords do not match"))
            self.add_error(
                'password_again',
                forms.ValidationError("Passwords do not match"))

        email = cleaned_data.get('email')
        email_again = cleaned_data.get('email_again')
        if email and email_again and email != email_again:
            self.add_error(
                'email',
                forms.ValidationError("E-mail addresses do not match")
            )
            self.add_error(
                'email_again',
                forms.ValidationError("E-mail addresses do not match")
            )

        username = cleaned_data.get('username')
        try:
            if username and not iam_admin_client.is_username_available(
                    username):
                self.add_error(
                    'username',
                    forms.ValidationError("That username is not available")
                )
        except Exception as e:
            logger.exception("Failed to check if username is available")
            self.add_error(
                'username',
                forms.ValidationError("Error occurred while checking if "
                                      "username is available: " + str(e)))

        return cleaned_data


class ResendEmailVerificationLinkForm(forms.Form):
    error_css_class = "is-invalid"
    username = forms.CharField(
        label='Username',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'Username'}),
        min_length=6,
        validators=[USERNAME_VALIDATOR])


class ForgotPasswordForm(forms.Form):
    error_css_class = "is-invalid"
    username = forms.CharField(
        label='Username',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'Username'}),
        min_length=6,
        validators=[USERNAME_VALIDATOR],
        help_text=USERNAME_VALIDATOR.message)


class ResetPasswordForm(forms.Form):
    error_css_class = "is-invalid"

    password = forms.CharField(
        label='Password',
        widget=forms.PasswordInput(attrs={'class': 'form-control',
                                          'placeholder': 'Password'}),
        min_length=8,
        max_length=48,
        validators=[PASSWORD_VALIDATOR],
        help_text=PASSWORD_VALIDATOR.message)
    password_again = forms.CharField(
        label='Password (again)',
        widget=forms.PasswordInput(attrs={'class': 'form-control',
                                          'placeholder': 'Password (again)'}))

    def clean(self):
        cleaned_data = super().clean()
        password = cleaned_data.get('password')
        password_again = cleaned_data.get('password_again')

        if password and password_again and password != password_again:
            self.add_error(
                'password',
                forms.ValidationError("Passwords do not match"))
            self.add_error(
                'password_again',
                forms.ValidationError("Passwords do not match"))

        return cleaned_data
