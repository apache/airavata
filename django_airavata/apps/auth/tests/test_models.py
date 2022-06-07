from django.contrib.auth import get_user_model
from django.test import TestCase

from django_airavata.apps.auth import models


class ExtendedUserProfileValueTestCase(TestCase):

    def setUp(self) -> None:
        User = get_user_model()
        user = User.objects.create_user("testuser")
        self.user_profile = models.UserProfile.objects.create(user=user)

    def test_value_display_of_text_value(self):
        field = models.ExtendedUserProfileTextField.objects.create(
            name="test", order=1)
        value = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            text_value="Some random answer.")
        self.assertEqual(value.value_display, value.text_value)

    def test_value_display_of_single_choice_with_choice(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        choice_two = field.choices.get(display_text="Choice #2")
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            choice=choice_two.id)
        self.assertEqual(value.value_display, choice_two.display_text)

    def test_value_display_of_single_choice_with_non_existent_choice(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            choice=-1)
        self.assertEqual(value.value_display, None)

    def test_value_display_of_single_choice_with_other(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Write-in value")
        self.assertEqual(value.value_display, "Other: Write-in value")

    def test_value_display_of_multi_choice_with_choices(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1)
        choice_one = field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        choice_three = field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        value.choices.create(value=choice_one.id)
        value.choices.create(value=choice_three.id)
        self.assertListEqual(value.value_display, [choice_one.display_text, choice_three.display_text])

    def test_value_display_of_multi_choice_with_other(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Some write-in value.")
        self.assertListEqual(value.value_display, ["Other: Some write-in value."])

    def test_value_display_of_multi_choice_with_choices_and_other(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1)
        choice_one = field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        choice_three = field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Some write-in value.")
        value.choices.create(value=choice_one.id)
        value.choices.create(value=choice_three.id)
        self.assertListEqual(value.value_display, [choice_one.display_text, choice_three.display_text, "Other: Some write-in value."])

    def test_value_display_of_multi_choice_with_non_existent_choices(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1)
        choice_one = field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        choice_three = field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        value.choices.create(value=choice_one.id)
        value.choices.create(value=choice_three.id)
        value.choices.create(value=-1)
        self.assertListEqual(value.value_display, [choice_one.display_text, choice_three.display_text])

    def test_value_display_of_user_agreement_with_no(self):
        field = models.ExtendedUserProfileAgreementField.objects.create(
            name="test", order=1)
        value = models.ExtendedUserProfileAgreementValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            agreement_value=False)
        self.assertEqual(value.value_display, "No")

    def test_value_display_of_user_agreement_with_yes(self):
        field = models.ExtendedUserProfileAgreementField.objects.create(
            name="test", order=1)
        value = models.ExtendedUserProfileAgreementValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            agreement_value=True)
        self.assertEqual(value.value_display, "Yes")

    def test_valid_of_text(self):
        field = models.ExtendedUserProfileTextField.objects.create(
            name="test", order=1, required=True)
        value = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            text_value="Some value")
        self.assertTrue(value.valid)

    def test_valid_of_text_empty(self):
        field = models.ExtendedUserProfileTextField.objects.create(
            name="test", order=1, required=True)
        value = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            text_value="")
        self.assertFalse(value.valid)

    def test_valid_of_text_empty_no_required(self):
        field = models.ExtendedUserProfileTextField.objects.create(
            name="test", order=1, required=False)
        value = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            text_value="")
        self.assertTrue(value.valid)

    def test_valid_of_single_choice_none(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1, required=True)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        self.assertFalse(value.valid)

    def test_valid_of_single_choice_with_choice(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1, required=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        choice_two = field.choices.get(display_text="Choice #2")
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            choice=choice_two.id)
        self.assertTrue(value.valid)

    def test_valid_of_single_choice_with_non_existent_choice(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1, required=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            choice=-1)
        self.assertFalse(value.valid)

    def test_valid_of_single_choice_with_other(self):
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1, required=True, other=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Some write-in value.")
        self.assertTrue(value.valid)

    def test_valid_of_single_choice_with_other_but_not_allowed(self):
        # Configure field so that 'Other' isn't an option
        field = models.ExtendedUserProfileSingleChoiceField.objects.create(
            name="test", order=1, required=True, other=False)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileSingleChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Some write-in value.")
        self.assertFalse(value.valid)

    def test_valid_of_multi_choice_with_none(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1, required=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        self.assertFalse(value.valid)

    def test_valid_of_multi_choice_with_some(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1, required=True)
        choice_one = field.choices.create(display_text="Choice #1", order=1)
        choice_two = field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        value.choices.create(value=choice_one.id)
        value.choices.create(value=choice_two.id)
        self.assertTrue(value.valid)

    def test_valid_of_multi_choice_with_non_existent_choice(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1, required=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile)
        value.choices.create(value=-1)
        self.assertFalse(value.valid)

    def test_valid_of_multi_choice_with_other(self):
        field = models.ExtendedUserProfileMultiChoiceField.objects.create(
            name="test", order=1, required=True, other=True)
        field.choices.create(display_text="Choice #1", order=1)
        field.choices.create(display_text="Choice #2", order=2)
        field.choices.create(display_text="Choice #3", order=3)
        value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            other_value="Some write-in value.")
        self.assertTrue(value.valid)
