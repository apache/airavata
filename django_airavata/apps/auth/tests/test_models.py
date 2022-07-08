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

    def test_valid_of_text_empty_deleted(self):
        """Invalid value but field is deleted so valid should be true."""
        field = models.ExtendedUserProfileTextField.objects.create(
            name="test", order=1, required=True, deleted=True)
        value = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field, user_profile=self.user_profile,
            text_value="")
        self.assertTrue(value.valid, "Although value is empty but required, since the field is deleted, consider it not invalid")

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


class UserProfileTestCase(TestCase):

    def setUp(self) -> None:
        User = get_user_model()
        user = User.objects.create_user("testuser")
        self.user_profile: models.UserProfile = models.UserProfile.objects.create(user=user)

    def test_is_ext_user_profile_valid_no_fields(self):
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)

    def test_is_ext_user_profile_valid_some_fields_some_values_valid(self):
        """Values for all fields, but only some are valid"""
        field1 = models.ExtendedUserProfileTextField.objects.create(
            name="test1", order=1, required=True)
        field2 = models.ExtendedUserProfileTextField.objects.create(
            name="test2", order=2, required=True)
        field3 = models.ExtendedUserProfileTextField.objects.create(
            name="test3", order=3, required=True)
        value1 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        value2 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field2, user_profile=self.user_profile,
            text_value="Answer #2"
        )
        value3 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field3, user_profile=self.user_profile,
            text_value=""  # intentionally blank
        )
        self.assertTrue(value1.valid)
        self.assertTrue(value2.valid)
        self.assertFalse(value3.valid)
        self.assertFalse(self.user_profile.is_ext_user_profile_valid)

    def test_is_ext_user_profile_valid_some_fields_all_values_valid(self):
        """Values for all fields, and all values are valid."""
        field1 = models.ExtendedUserProfileTextField.objects.create(
            name="test1", order=1, required=True)
        field2 = models.ExtendedUserProfileTextField.objects.create(
            name="test2", order=2, required=True)
        field3 = models.ExtendedUserProfileTextField.objects.create(
            name="test3", order=3, required=True)
        value1 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        value2 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field2, user_profile=self.user_profile,
            text_value="Answer #2"
        )
        value3 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field3, user_profile=self.user_profile,
            text_value="Answer #3"
        )
        self.assertTrue(value1.valid)
        self.assertTrue(value2.valid)
        self.assertTrue(value3.valid)
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)

    def test_is_ext_user_profile_valid_some_fields_some_not_required_values_missing(self):
        """Some values are missing but they are optional."""
        field1 = models.ExtendedUserProfileTextField.objects.create(
            name="test1", order=1, required=True)
        field2 = models.ExtendedUserProfileTextField.objects.create(
            name="test2", order=2, required=False)
        field3 = models.ExtendedUserProfileTextField.objects.create(
            name="test3", order=3, required=False)
        value1 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        self.assertTrue(value1.valid)
        self.assertFalse(models.ExtendedUserProfileValue.objects.filter(
            user_profile=self.user_profile, ext_user_profile_field=field2).exists(),
            "No value for field2")
        self.assertFalse(models.ExtendedUserProfileValue.objects.filter(
            user_profile=self.user_profile, ext_user_profile_field=field3).exists(),
            "No value for field3")
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)

    def test_is_ext_user_profile_valid_some_fields_some_required_values_missing(self):
        """Some required values are missing."""
        field1 = models.ExtendedUserProfileTextField.objects.create(
            name="test1", order=1, required=True)
        field2 = models.ExtendedUserProfileTextField.objects.create(
            name="test2", order=2, required=True)
        field3 = models.ExtendedUserProfileTextField.objects.create(
            name="test3", order=3, required=False)
        value1 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        self.assertTrue(value1.valid)
        self.assertFalse(models.ExtendedUserProfileValue.objects.filter(
            user_profile=self.user_profile, ext_user_profile_field=field2).exists(),
            "No value for field2, but field2 is required")
        self.assertFalse(models.ExtendedUserProfileValue.objects.filter(
            user_profile=self.user_profile, ext_user_profile_field=field3).exists(),
            "No value for field3")
        self.assertFalse(self.user_profile.is_ext_user_profile_valid)

    def test_is_ext_user_profile_valid_some_fields_invalid_value_but_field_deleted(self):
        """Value is invalid but field is deleted so it shouldn't count."""
        field1 = models.ExtendedUserProfileTextField.objects.create(
            name="test1", order=1, required=True)
        field2 = models.ExtendedUserProfileTextField.objects.create(
            name="test2", order=2, required=True)
        field3 = models.ExtendedUserProfileTextField.objects.create(
            name="test3", order=3, required=True)

        value1 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        value2 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field2, user_profile=self.user_profile,
            text_value="Answer #2"
        )
        value3 = models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field3, user_profile=self.user_profile,
            text_value=""
        )
        self.assertTrue(value1.valid)
        self.assertTrue(value2.valid)
        self.assertFalse(value3.valid)
        self.assertFalse(self.user_profile.is_ext_user_profile_valid)

        field3.deleted = True
        field3.save()
        self.assertTrue(value3.valid)
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)
