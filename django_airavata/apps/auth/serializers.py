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
