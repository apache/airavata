from airavata.model.group.ttypes import GroupModel
from airavata.model.user.ttypes import UserProfile
from django.core import mail
from django.shortcuts import reverse
from django.test import RequestFactory, TestCase, override_settings

from django_airavata.apps.api.signals import user_added_to_group
from django_airavata.apps.auth import signals  # noqa

GATEWAY_ID = "test-gateway"
SERVER_EMAIL = "admin@test-gateway.com"
PORTAL_TITLE = "Test Gateway"
PORTAL_ADMINS = [('Portal Admin', 'admin@test-gateway.com')]


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    SERVER_EMAIL=SERVER_EMAIL,
    PORTAL_TITLE=PORTAL_TITLE,
    PORTAL_ADMINS=PORTAL_ADMINS
)
class EmailUserAddedToGroupSignalReceiverTests(TestCase):

    def setUp(self):
        factory = RequestFactory()
        self.request = factory.get("/")
        self.user = UserProfile(
            airavataInternalUserId=f"testuser@{GATEWAY_ID}",
            userId="testuser",
            gatewayId=GATEWAY_ID,
            emails=["testuser@example.com"],
            firstName="Test",
            lastName="User")

    def test(self):
        group = GroupModel(id="abc123", name="Test Group")
        user_added_to_group.send(None,
                                 user=self.user,
                                 groups=[group],
                                 request=self.request)
        self.assertEqual(len(mail.outbox), 1)
        msg = mail.outbox[0]
        self._assert_common_email_attributes(msg)
        self.assertEqual(msg.subject,
                         f"You've been added to group "
                         f"[{group.name}] in {PORTAL_TITLE}")

    def test_multiple_groups(self):
        group1 = GroupModel(id="abc123", name="Test Group")
        group2 = GroupModel(id="group2", name="Group 2")
        user_added_to_group.send(None,
                                 user=self.user,
                                 groups=[group1, group2],
                                 request=self.request)
        self.assertEqual(len(mail.outbox), 1)
        msg = mail.outbox[0]
        self._assert_common_email_attributes(msg)
        self.assertEqual(msg.subject,
                         f"You've been added to groups "
                         f"[{group1.name}] and [{group2.name}] "
                         f"in {PORTAL_TITLE}")
        self.assertIn("groups Test Group and Group 2", msg.body)

    def _assert_common_email_attributes(self, msg):
        self.assertEqual(msg.from_email,
                         f"\"{PORTAL_TITLE}\" <{SERVER_EMAIL}>")
        self.assertEqual(
            msg.reply_to,
            [f"\"{PORTAL_ADMINS[0][0]}\" <{PORTAL_ADMINS[0][1]}>"])
        self.assertSequenceEqual(
            msg.to, [f"\"{self.user.firstName} {self.user.lastName}\" "
                     f"<{self.user.emails[0]}>"])
        self.assertIn(
            self.request.build_absolute_uri(
                reverse("django_airavata_workspace:dashboard")),
            msg.body)
        self.assertIn(
            self.request.build_absolute_uri(
                reverse("django_airavata_workspace:experiments")),
            msg.body)
        self.assertIn(self.user.userId, msg.body)
