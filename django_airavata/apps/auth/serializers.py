import logging
from urllib.parse import urlencode

from django.conf import settings
from django.contrib.auth import get_user_model
from django.db.transaction import atomic
from django.template import Context
from django.urls import reverse
from rest_framework import serializers

from django_airavata.apps.auth import iam_admin_client

from . import models, utils

logger = logging.getLogger(__name__)


class PendingEmailChangeSerializer(serializers.ModelSerializer):

    class Meta:
        model = models.PendingEmailChange
        fields = ['email_address', 'created_date']


class UserSerializer(serializers.ModelSerializer):

    pending_email_change = serializers.SerializerMethodField()
    complete = serializers.SerializerMethodField()
    username_valid = serializers.SerializerMethodField()
    ext_user_profile_valid = serializers.SerializerMethodField()

    class Meta:
        model = get_user_model()
        fields = ['id', 'username', 'first_name', 'last_name', 'email',
                  'pending_email_change', 'complete', 'username_valid',
                  'ext_user_profile_valid']
        read_only_fields = ('username',)

    def get_pending_email_change(self, instance):
        request = self.context['request']
        pending_email_change = models.PendingEmailChange.objects.filter(user=request.user, verified=False).first()
        if pending_email_change is not None:
            serializer = PendingEmailChangeSerializer(instance=pending_email_change, context=self.context)
            return serializer.data
        else:
            return None

    def get_complete(self, instance):
        return instance.user_profile.is_complete

    def get_username_valid(self, instance):
        return instance.user_profile.is_username_valid

    def get_ext_user_profile_valid(self, instance):
        return instance.user_profile.is_ext_user_profile_valid

    @atomic
    def update(self, instance, validated_data):
        request = self.context['request']
        instance.first_name = validated_data['first_name']
        instance.last_name = validated_data['last_name']
        if instance.email != validated_data['email']:
            # Delete any unverified pending email changes
            models.PendingEmailChange.objects.filter(user=request.user, verified=False).delete()
            # Email doesn't get updated until it is verified. Create a pending
            # email change record in the meantime
            pending_email_change = models.PendingEmailChange.objects.create(user=request.user, email_address=validated_data['email'])
            self._send_email_verification_link(request, pending_email_change)
        instance.save()
        # save in the user profile service too
        user_profile_client = request.profile_service['user_profile']

        # update the Airavata profile if it exists
        if user_profile_client.doesUserExist(request.authz_token,
                                             request.user.username,
                                             settings.GATEWAY_ID):
            airavata_user_profile = user_profile_client.getUserProfileById(
                request.authz_token, request.user.username, settings.GATEWAY_ID)
            airavata_user_profile.firstName = instance.first_name
            airavata_user_profile.lastName = instance.last_name
            user_profile_client.updateUserProfile(request.authz_token, airavata_user_profile)
        # otherwise, update in Keycloak user store
        else:
            iam_admin_client.update_user(request.user.username,
                                         first_name=instance.first_name,
                                         last_name=instance.last_name)
        return instance

    def _send_email_verification_link(self, request, pending_email_change):

        verification_uri = (
            request.build_absolute_uri(reverse('django_airavata_auth:user_profile')) +
            '?' + urlencode({"code": pending_email_change.verification_code}))
        logger.debug(
            "verification_uri={}".format(verification_uri))

        context = Context({
            "username": pending_email_change.user.username,
            "email": pending_email_change.email_address,
            "first_name": pending_email_change.user.first_name,
            "last_name": pending_email_change.user.last_name,
            "portal_title": settings.PORTAL_TITLE,
            "url": verification_uri,
        })
        utils.send_email_to_user(models.VERIFY_EMAIL_CHANGE_TEMPLATE, context)


class ExtendedUserProfileFieldChoiceSerializer(serializers.Serializer):
    id = serializers.IntegerField(required=False)
    display_text = serializers.CharField()
    order = serializers.IntegerField()


class ExtendedUserProfileFieldLinkSerializer(serializers.Serializer):
    id = serializers.IntegerField(required=False)
    label = serializers.CharField()
    url = serializers.URLField()
    order = serializers.IntegerField()
    display_link = serializers.BooleanField(default=True)
    display_inline = serializers.BooleanField(default=False)


class ExtendedUserProfileFieldSerializer(serializers.ModelSerializer):
    field_type = serializers.ChoiceField(choices=["text", "single_choice", "multi_choice", "user_agreement"])
    other = serializers.BooleanField(required=False)
    choices = ExtendedUserProfileFieldChoiceSerializer(required=False, many=True)
    checkbox_label = serializers.CharField(allow_blank=True, required=False)
    links = ExtendedUserProfileFieldLinkSerializer(required=False, many=True)

    class Meta:
        model = models.ExtendedUserProfileField
        fields = ['id', 'name', 'help_text', 'order', 'created_date',
                  'updated_date', 'field_type', 'other', 'choices', 'checkbox_label', 'links', 'required']
        read_only_fields = ('created_date', 'updated_date')

    def to_representation(self, instance):
        result = super().to_representation(instance)
        if instance.field_type == 'single_choice':
            result['other'] = instance.single_choice.other
            result['choices'] = ExtendedUserProfileFieldChoiceSerializer(instance.single_choice.choices.filter(deleted=False).order_by('order'), many=True).data
        if instance.field_type == 'multi_choice':
            result['other'] = instance.multi_choice.other
            result['choices'] = ExtendedUserProfileFieldChoiceSerializer(instance.multi_choice.choices.filter(deleted=False).order_by('order'), many=True).data
        if instance.field_type == 'user_agreement':
            result['checkbox_label'] = instance.user_agreement.checkbox_label
        result['links'] = ExtendedUserProfileFieldLinkSerializer(instance.links.order_by('order'), many=True).data
        return result

    def create(self, validated_data):
        field_type = validated_data.pop('field_type')
        other = validated_data.pop('other', False)
        choices = validated_data.pop('choices', [])
        checkbox_label = validated_data.pop('checkbox_label', '')
        links = validated_data.pop('links', [])
        if field_type == 'text':
            instance = models.ExtendedUserProfileTextField.objects.create(**validated_data)
        elif field_type == 'single_choice':
            instance = models.ExtendedUserProfileSingleChoiceField.objects.create(**validated_data, other=other)
            # add choices
            for choice in choices:
                choice.pop('id', None)
                instance.choices.create(**choice)
        elif field_type == 'multi_choice':
            instance = models.ExtendedUserProfileMultiChoiceField.objects.create(**validated_data, other=other)
            # add choices
            for choice in choices:
                choice.pop('id', None)
                instance.choices.create(**choice)
        elif field_type == 'user_agreement':
            instance = models.ExtendedUserProfileAgreementField.objects.create(**validated_data, checkbox_label=checkbox_label)
        else:
            raise Exception(f"Unrecognized field type: {field_type}")
        # create links
        for link in links:
            link.pop('id', None)
            instance.links.create(**link)
        return instance

    def update(self, instance, validated_data):
        instance.name = validated_data['name']
        instance.help_text = validated_data['help_text']
        instance.order = validated_data['order']
        instance.required = validated_data.get('required', instance.required)
        # logger.debug(f"instance.field_type={instance.field_type}, validated_data={validated_data}")
        if instance.field_type == 'single_choice':
            instance.single_choice.other = validated_data.get('other', instance.single_choice.other)
            choices = validated_data.pop('choices', None)
            if choices:
                choice_ids = [choice['id'] for choice in choices if 'id' in choice]
                # Soft delete any choices that are not in the list
                instance.single_choice.choices.exclude(id__in=choice_ids).update(deleted=True)
                for choice in choices:
                    choice_id = choice.pop('id', None)
                    models.ExtendedUserProfileSingleChoiceFieldChoice.objects.update_or_create(
                        id=choice_id,
                        defaults={**choice, "single_choice_field": instance.single_choice},
                    )
            instance.single_choice.save()
        elif instance.field_type == 'multi_choice':
            instance.multi_choice.other = validated_data.get('other', instance.multi_choice.other)
            choices = validated_data.pop('choices', None)
            if choices:
                choice_ids = [choice['id'] for choice in choices if 'id' in choice]
                # Soft delete any choices that are not in the list
                instance.multi_choice.choices.exclude(id__in=choice_ids).update(deleted=True)
                for choice in choices:
                    choice_id = choice.pop('id', None)
                    models.ExtendedUserProfileMultiChoiceFieldChoice.objects.update_or_create(
                        id=choice_id,
                        defaults={**choice, "multi_choice_field": instance.multi_choice},
                    )
            instance.multi_choice.save()
        elif instance.field_type == 'user_agreement':
            instance.user_agreement.checkbox_label = validated_data.pop('checkbox_label', instance.user_agreement.checkbox_label)
            instance.user_agreement.save()

        # update links
        links = validated_data.pop('links', [])
        link_ids = [link['id'] for link in links if 'id' in link]
        instance.links.exclude(id__in=link_ids).delete()
        for link in links:
            link_id = link.pop('id', None)
            link['field'] = instance
            models.ExtendedUserProfileFieldLink.objects.update_or_create(
                id=link_id,
                defaults=link,
            )

        instance.save()
        return instance


class ExtendedUserProfileValueSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField(label='ID', required=False)
    text_value = serializers.CharField(required=False, allow_blank=True)
    # choices must be write_only so that DRF ignores trying to deserialized this related field
    # deserialization is handled explicitly in to_representation, see below
    choices = serializers.ListField(child=serializers.IntegerField(), required=False, write_only=True)
    other_value = serializers.CharField(required=False, allow_blank=True)
    agreement_value = serializers.BooleanField(required=False)

    class Meta:
        model = models.ExtendedUserProfileValue
        fields = ['id', 'value_type', 'ext_user_profile_field', 'text_value',
                  'choices', 'other_value', 'agreement_value', 'valid', 'value_display']
        read_only_fields = ['value_type', 'value_display']

    def to_representation(self, instance):
        result = super().to_representation(instance)
        if instance.value_type == 'text':
            result['text_value'] = instance.text.text_value
        elif instance.value_type == 'single_choice':
            choices = []
            if instance.single_choice.choice is not None:
                choices.append(instance.single_choice.choice)
            result['choices'] = choices
            result['other_value'] = instance.single_choice.other_value
        elif instance.value_type == 'multi_choice':
            result['choices'] = list(map(lambda c: c.value, instance.multi_choice.choices.all()))
            result['other_value'] = instance.multi_choice.other_value
        elif instance.value_type == 'user_agreement':
            result['agreement_value'] = instance.user_agreement.agreement_value
        return result

    def create(self, validated_data):
        request = self.context['request']
        user = request.user
        user_profile = user.user_profile

        # Support create/update in the many=True situation. When many=True and
        # .save() is called, .create() will be called on each value. Here we
        # need to see if there is an id and if so call .update() instead.
        if "id" in validated_data:
            instance = models.ExtendedUserProfileValue.objects.get(id=validated_data["id"])
            return self.update(instance, validated_data)

        ext_user_profile_field = validated_data.pop('ext_user_profile_field')
        if ext_user_profile_field.field_type == 'text':
            text_value = validated_data.pop('text_value')
            return models.ExtendedUserProfileTextValue.objects.create(
                ext_user_profile_field=ext_user_profile_field,
                user_profile=user_profile,
                text_value=text_value)
        elif ext_user_profile_field.field_type == 'single_choice':
            choices = validated_data.pop('choices', [])
            choice = choices[0] if len(choices) > 0 else None
            other_value = validated_data.pop('other_value', '')
            return models.ExtendedUserProfileSingleChoiceValue.objects.create(
                ext_user_profile_field=ext_user_profile_field,
                user_profile=user_profile,
                choice=choice,
                other_value=other_value,
            )
        elif ext_user_profile_field.field_type == 'multi_choice':
            choices = validated_data.pop('choices', [])
            other_value = validated_data.pop('other_value', '')
            value = models.ExtendedUserProfileMultiChoiceValue.objects.create(
                ext_user_profile_field=ext_user_profile_field,
                user_profile=user_profile,
                other_value=other_value,
            )
            for choice in choices:
                models.ExtendedUserProfileMultiChoiceValueChoice.objects.create(
                    value=choice,
                    multi_choice_value=value
                )
            return value
        elif ext_user_profile_field.field_type == 'user_agreement':
            agreement_value = validated_data.get('agreement_value')
            return models.ExtendedUserProfileAgreementValue.objects.create(
                ext_user_profile_field=ext_user_profile_field,
                user_profile=user_profile,
                agreement_value=agreement_value
            )

    def update(self, instance, validated_data):
        if instance.value_type == 'text':
            text_value = validated_data.pop('text_value')
            instance.text.text_value = text_value
            instance.text.save()
        elif instance.value_type == 'single_choice':
            choices = validated_data.pop('choices', [])
            choice = choices[0] if len(choices) > 0 else None
            other_value = validated_data.pop('other_value', '')
            instance.single_choice.choice = choice
            instance.single_choice.other_value = other_value
            instance.single_choice.save()
        elif instance.value_type == 'multi_choice':
            choices = validated_data.pop('choices', [])
            other_value = validated_data.pop('other_value', '')
            # Delete any that are no longer in the set
            instance.multi_choice.choices.exclude(value__in=choices).delete()
            # Create records as needed for new entries
            for choice in choices:
                models.ExtendedUserProfileMultiChoiceValueChoice.objects.update_or_create(
                    value=choice, multi_choice_value=instance.multi_choice)
            instance.multi_choice.other_value = other_value
            instance.multi_choice.save()
        elif instance.value_type == 'user_agreement':
            agreement_value = validated_data.pop('agreement_value')
            instance.user_agreement.agreement_value = agreement_value
            instance.user_agreement.save()
        instance.save()
        return instance

    def validate(self, attrs):
        ext_user_profile_field = attrs['ext_user_profile_field']
        # validate that id_value is only provided for choice fields, and 'text_value' only for the others
        if ext_user_profile_field.field_type == 'single_choice':
            choices = attrs.get('choices', [])
            other_value = attrs.get('other_value', '')
            # Check that choices are valid
            for choice in choices:
                if not ext_user_profile_field.single_choice.choices.filter(id=choice, deleted=False).exists():
                    raise serializers.ValidationError({'choices': 'Invalid choice.'})
            if len(choices) > 1:
                raise serializers.ValidationError({'choices': "Must specify only a single choice."})
            if len(choices) == 1 and other_value != '':
                raise serializers.ValidationError("Must specify only a single choice or the other choice, but not both.")
            if len(choices) == 0 and other_value == '':
                raise serializers.ValidationError("Must specify one of a single choice or the other choice (but not both).")
        elif ext_user_profile_field.field_type == 'multi_choice':
            choices = attrs.get('choices', [])
            other_value = attrs.get('other_value', '')
            # Check that choices are valid
            for choice in choices:
                if not ext_user_profile_field.multi_choice.choices.filter(id=choice, deleted=False).exists():
                    raise serializers.ValidationError({'choices': 'Invalid choice.'})
        return attrs
