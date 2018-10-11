from django import forms

from . import iam_admin_client


class CreateAccountForm(forms.Form):
    error_css_class = "is-invalid"
    username = forms.CharField(
        label='Username',
        widget=forms.TextInput(attrs={'class': 'form-control',
                                      'placeholder': 'Username'}))
    password = forms.CharField(
        label='Password',
        widget=forms.PasswordInput(attrs={'class': 'form-control',
                                          'placeholder': 'Password'}))
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

    def clean(self):
        cleaned_data = super().clean()
        password = cleaned_data.get('password')
        password_again = cleaned_data.get('password_again')

        if password != password_again:
            self.add_error(
                'password',
                forms.ValidationError("Passwords do not match"))
            self.add_error(
                'password_again',
                forms.ValidationError("Passwords do not match"))

        email = cleaned_data.get('email')
        email_again = cleaned_data.get('email_again')
        if email != email_again:
            self.add_error(
                'email',
                forms.ValidationError("E-mail addresses do not match")
            )
            self.add_error(
                'email_again',
                forms.ValidationError("E-mail addresses do not match")
            )

        username = cleaned_data.get('username')
        if not iam_admin_client.is_username_available(username):
            self.add_error(
                'username',
                forms.ValidationError("That username is not available")
            )

        return cleaned_data
