from django.core import mail
from django.shortcuts import reverse
from django.test import RequestFactory, TestCase, override_settings

from airavata.model.group.ttypes import GroupModel
from airavata.model.user.ttypes import UserProfile
from django_airavata.apps.api.signals import user_added_to_group

from . import signals  # noqa

GATEWAY_ID = "test-gateway"
SERVER_EMAIL = "admin@test-gateway.com"
PORTAL_TITLE = "Test Gateway"


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    SERVER_EMAIL=SERVER_EMAIL,
    PORTAL_TITLE=PORTAL_TITLE
)
class EmailUserAddedToGroupSignalReceiverTests(TestCase):

    def test(self):
        factory = RequestFactory()
        request = factory.get("/")
        user = UserProfile(
            airavataInternalUserId=f"testuser@{GATEWAY_ID}",
            userId="testuser",
            gatewayId=GATEWAY_ID,
            emails=["testuser@example.com"],
            firstName="Test",
            lastName="User")
        group = GroupModel(id="abc123", name="Test Group")
        user_added_to_group.send(None,
                                 user=user,
                                 groups=[group],
                                 request=request)
        self.assertEqual(len(mail.outbox), 1)
        msg = mail.outbox[0]
        self.assertEqual(msg.subject,
                         f"You've been added to group "
                         f"[{group.name}] in {PORTAL_TITLE}")
        self.assertEqual(msg.from_email,
                         f"{PORTAL_TITLE} <{SERVER_EMAIL}>")
        self.assertSequenceEqual(
            msg.to, [f"{user.firstName} {user.lastName} <{user.emails[0]}>"])
        self.assertIn(
            request.build_absolute_uri(
                reverse("django_airavata_workspace:dashboard")),
            msg.body)
        self.assertIn(
            request.build_absolute_uri(
                reverse("django_airavata_workspace:experiments")),
            msg.body)
        self.assertIn(user.userId, msg.body)

    def test_multiple_groups(self):
        factory = RequestFactory()
        request = factory.get("/")
        user = UserProfile(
            airavataInternalUserId=f"testuser@{GATEWAY_ID}",
            userId="testuser",
            gatewayId=GATEWAY_ID,
            emails=["testuser@example.com"],
            firstName="Test",
            lastName="User")
        group1 = GroupModel(id="abc123", name="Test Group")
        group2 = GroupModel(id="group2", name="Group 2")
        user_added_to_group.send(None,
                                 user=user,
                                 groups=[group1, group2],
                                 request=request)
        self.assertEqual(len(mail.outbox), 1)
        msg = mail.outbox[0]
        self.assertEqual(msg.subject,
                         f"You've been added to groups "
                         f"[{group1.name}] and [{group2.name}] "
                         f"in {PORTAL_TITLE}")
        self.assertEqual(msg.from_email,
                         f"{PORTAL_TITLE} <{SERVER_EMAIL}>")
        self.assertSequenceEqual(
            msg.to, [f"{user.firstName} {user.lastName} <{user.emails[0]}>"])
        self.assertIn(
            request.build_absolute_uri(
                reverse("django_airavata_workspace:dashboard")),
            msg.body)
        self.assertIn(
            request.build_absolute_uri(
                reverse("django_airavata_workspace:experiments")),
            msg.body)
        self.assertIn(user.userId, msg.body)
        self.assertIn("groups Test Group and Group 2", msg.body)
