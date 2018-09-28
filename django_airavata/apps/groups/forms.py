# Create your forms here.
from django import forms


class CreateForm(forms.Form):
    domain_id = forms.CharField(required=True, widget=forms.HiddenInput)
    group_name = forms.CharField(required=True)
    description = forms.CharField(
        required=True,
        widget=forms.Textarea
    )
    group_owner = forms.CharField(required=True, widget=forms.HiddenInput)
    CHOICES1 = (('1', 'User Level',), ('2', 'Admin Level',))
    group_type = forms.ChoiceField(
        widget=forms.HiddenInput, choices=CHOICES1, required=True)
    CHOICES2 = (('0', 'Single User',), ('1', 'Multi User',))
    group_cardinality = forms.ChoiceField(
        widget=forms.HiddenInput, choices=CHOICES2, required=True)

# class AddForm(forms.Form):
#    users = forms.MultipleChoiceField(widget=forms.CheckboxSelectMultiple, required=False)

# class RemoveForm(forms.Form):
#    members = forms.MultipleChoiceField(widget=forms.CheckboxSelectMultiple, required=False)


class AddForm(forms.Form):
    def __init__(self, data=None, user_choices=None):
        super().__init__(data=data)
        self.fields["users"] = forms.MultipleChoiceField(
            widget=forms.CheckboxSelectMultiple, choices=user_choices)


class RemoveForm(forms.Form):
    def __init__(self, data=None, user_choices=None):
        super().__init__(data=data)
        self.fields["members"] = forms.MultipleChoiceField(
            widget=forms.CheckboxSelectMultiple, choices=user_choices)
