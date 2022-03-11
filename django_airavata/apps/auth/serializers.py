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

    class Meta:
        model = get_user_model()
        fields = ['id', 'username', 'first_name', 'last_name', 'email',
                  'pending_email_change', 'complete', 'username_valid']
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
                  'updated_date', 'field_type', 'other', 'choices', 'checkbox_label', 'links']
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
                        defaults=choice,
                    )
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
                        defaults=choice,
                    )
        elif instance.field_type == 'user_agreement':
            instance.user_agreement.checkbox_label = validated_data.pop('checkbox_label', instance.user_agreement.checkbox_label)

        # update links
        links = validated_data.pop('links', [])
        link_ids = [link['id'] for link in links if 'id' in link]
        instance.links.exclude(id__in=link_ids).delete()
        for link in links:
            link_id = link.pop('id', None)
            models.ExtendedUserProfileFieldLink.objects.update_or_create(
                id=link_id,
                defaults=link,
            )

        instance.save()
        return instance
