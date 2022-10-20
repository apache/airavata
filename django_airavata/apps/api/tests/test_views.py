from unittest.mock import MagicMock, call, patch

from airavata.model.appcatalog.gatewaygroups.ttypes import GatewayGroups
from airavata.model.group.ttypes import GroupModel
from airavata.model.user.ttypes import UserProfile
from django.contrib.auth.models import User
from django.test import TestCase, override_settings
from django.urls import reverse
# from rest_framework import status
from rest_framework.test import APIRequestFactory, force_authenticate

from django_airavata.apps.api import signals, views

GATEWAY_ID = "test-gateway"
PORTAL_ADMINS = [('Admin Name', 'admin@example.com')]


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_ADMINS=PORTAL_ADMINS
)
class GroupViewSetTests(TestCase):

    def setUp(self):
        self.user = User.objects.create_user('testuser')
        self.factory = APIRequestFactory()

    def test_create_group_sends_user_added_to_group_signal(self):

        url = reverse('django_airavata_api:group-list')
        data = {
            "id": None,
            "name": "test",
            "description": None,
            "members": [
                    f"{self.user.username}@{GATEWAY_ID}",  # owner
                    f"testuser1@{GATEWAY_ID}"],
            "admins": []
        }
        request = self.factory.post(url, data)
        force_authenticate(request, self.user)

        # Mock api clients
        group_manager_mock = MagicMock(name='group_manager')
        user_profile_mock = MagicMock(name='user_profile')
        request.profile_service = {
            'group_manager': group_manager_mock,
            'user_profile': user_profile_mock,
        }
        request.airavata_client = MagicMock(name="airavata_client")
        request.airavata_client.getGatewayGroups.return_value = GatewayGroups(
            gatewayId=GATEWAY_ID,
            adminsGroupId="adminsGroupId",
            readOnlyAdminsGroupId="readOnlyAdminsGroupId",
            defaultGatewayUsersGroupId="defaultGatewayUsersGroupId"
        )
        request.authz_token = "dummy"
        request.session = {}
        group_manager_mock.createGroup.return_value = "abc123"
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        user_profile_mock.getUserProfileById.return_value = user_profile

        # Mock signal handler to verify 'user_added_to_group' signal is sent
        user_added_to_group_handler = MagicMock()
        signals.user_added_to_group.connect(
            user_added_to_group_handler,
            sender=views.GroupViewSet)
        group_create = views.GroupViewSet.as_view({'post': 'create'})
        response = group_create(request)
        self.assertEquals(201, response.status_code)
        self.assertEquals("abc123", response.data['id'])
        user_added_to_group_handler.assert_called_once()
        args, kwargs = user_added_to_group_handler.call_args
        self.assertEquals("abc123", kwargs["groups"][0].id)
        self.assertIs(user_profile, kwargs["user"])

    def test_update_group_sends_user_added_to_group_signal(self):
        url = reverse('django_airavata_api:group-detail',
                      kwargs={'group_id': 'abc123'})
        data = {
            "id": "abc123",
            "name": "test",
            "description": None,
            "members": [
                    f"{self.user.username}@{GATEWAY_ID}",  # owner
                    f"testuser1@{GATEWAY_ID}",  # existing member
                    f"testuser3@{GATEWAY_ID}"],  # new member
            "admins": []
        }
        request = self.factory.put(url, data)
        force_authenticate(request, self.user)

        # Mock api clients
        group_manager_mock = MagicMock(name='group_manager')
        user_profile_mock = MagicMock(name='user_profile')
        request.profile_service = {
            'group_manager': group_manager_mock,
            'user_profile': user_profile_mock,
        }
        request.airavata_client = MagicMock(name="airavata_client")
        request.airavata_client.getGatewayGroups.return_value = GatewayGroups(
            gatewayId=GATEWAY_ID,
            adminsGroupId="adminsGroupId",
            readOnlyAdminsGroupId="readOnlyAdminsGroupId",
            defaultGatewayUsersGroupId="defaultGatewayUsersGroupId"
        )
        request.authz_token = "dummy"
        request.session = {}

        # mock getGroup
        group = GroupModel(id="abc123", name="My Group",
                           ownerId=f"{self.user.username}@{GATEWAY_ID}",
                           members=[
                               f"{self.user.username}@{GATEWAY_ID}",  # owner
                               f"testuser1@{GATEWAY_ID}",  # existing member
                               f"testuser2@{GATEWAY_ID}",  # new member
                           ],
                           admins=[])
        group_manager_mock.getGroup.return_value = group

        # Only user added is testuser3, so getUserProfileById will be called
        # for that user
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser3@{GATEWAY_ID}",
            userId="testuser3",
            firstName="Test",
            lastName="User3",
            emails=["testuser3@example.com"]
        )
        user_profile_mock.getUserProfileById.return_value = user_profile

        # Mock signal handler to verify 'user_added_to_group' signal is sent
        user_added_to_group_handler = MagicMock()
        signals.user_added_to_group.connect(
            user_added_to_group_handler,
            sender=views.GroupViewSet)
        group_update = views.GroupViewSet.as_view({'put': 'update'})
        response = group_update(request, group_id="abc123")
        self.assertEquals(200, response.status_code)
        self.assertEquals("abc123", response.data['id'])

        # verify addUsersToGroup
        group_manager_mock.addUsersToGroup.assert_called_once()
        args, kwargs = group_manager_mock.addUsersToGroup.call_args
        self.assertEqual(args[1], [f"testuser3@{GATEWAY_ID}"])

        # verify removeUsersFromGroup
        group_manager_mock.removeUsersFromGroup.assert_called_once()
        args, kwargs = group_manager_mock.removeUsersFromGroup.call_args
        self.assertEqual(args[1], [f"testuser2@{GATEWAY_ID}"])

        # verify updateGroup
        group_manager_mock.updateGroup.assert_called_once()

        user_added_to_group_handler.assert_called_once()
        args, kwargs = user_added_to_group_handler.call_args
        self.assertEquals("abc123", kwargs["groups"][0].id)
        self.assertIs(user_profile, kwargs["user"])


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_ADMINS=PORTAL_ADMINS
)
class IAMUserViewSetTests(TestCase):

    def setUp(self):
        self.user = User.objects.create_user('testuser')
        self.factory = APIRequestFactory()

    @patch("django_airavata.apps.api.views.iam_admin_client")
    def test_update_that_adds_user_to_group_sends_user_added_to_group_signal(
            self, iam_admin_client):

        username = "testuser1"
        url = reverse(
            'django_airavata_api:iam-user-profile-detail',
            kwargs={'user_id': username})
        data = {
            "airavataInternalUserId": f"{username}@{GATEWAY_ID}",
            "userId": username,
            "gatewayId": GATEWAY_ID,
            "email": "testuser1@example.com",
            "firstName": "Test",
            "lastName": "User1",
            "airavataUserProfileExists": True,
            "enabled": True,
            "emailVerified": True,
            "groups": [
                {"id": "group1", "name": "Group 1"},
                {"id": "group2", "name": "Group 2"}
            ]
        }
        request = self.factory.put(url, data)
        force_authenticate(request, self.user)
        request.is_gateway_admin = True

        # Mock api clients
        iam_user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        iam_admin_client.get_user.return_value = iam_user_profile
        group_manager_mock = MagicMock(name='group_manager')
        user_profile_mock = MagicMock(name='user_profile')
        request.profile_service = {
            'group_manager': group_manager_mock,
            'user_profile': user_profile_mock,
        }
        request.authz_token = "dummy"
        user_profile_mock.doesUserExist.return_value = True
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        user_profile_mock.getUserProfileById.return_value = user_profile
        group_manager_mock.getAllGroupsUserBelongs.return_value = [
            GroupModel(id="group1")]
        group = GroupModel(
            id="group2", name="Group 2"
        )
        group_manager_mock.getGroup.return_value = group
        request.airavata_client = MagicMock(name="airavata_client")
        request.airavata_client.getGatewayGroups.return_value = GatewayGroups(
            gatewayId=GATEWAY_ID,
            adminsGroupId="adminsGroupId",
            readOnlyAdminsGroupId="readOnlyAdminsGroupId",
            defaultGatewayUsersGroupId="defaultGatewayUsersGroupId"
        )
        request.session = {}

        # Mock signal handler to verify 'user_added_to_group' signal is sent
        user_added_to_group_handler = MagicMock(
            name="user_added_to_group_handler")
        signals.user_added_to_group.connect(
            user_added_to_group_handler,
            sender=views.IAMUserViewSet)
        iam_user_update = views.IAMUserViewSet.as_view({'put': 'update'})
        response = iam_user_update(request, user_id=username)
        self.assertEquals(200, response.status_code)

        user_profile_mock.doesUserExist.assert_called_once()
        group_manager_mock.getAllGroupsUserBelongs.assert_called_once()

        user_profile_mock.getUserProfileById.assert_called_once()
        args, kwargs = user_profile_mock.getUserProfileById.call_args
        self.assertSequenceEqual(
            args, [request.authz_token, "testuser1", GATEWAY_ID])

        group_manager_mock.getGroup.assert_called_once()
        args, kwargs = group_manager_mock.getGroup.call_args
        self.assertSequenceEqual(args, [request.authz_token, "group2"])

        group_manager_mock.addUsersToGroup.assert_called_once()
        args, kwargs = group_manager_mock.addUsersToGroup.call_args
        self.assertSequenceEqual(
            args, [request.authz_token, [f"testuser1@{GATEWAY_ID}"], "group2"]
        )

        user_added_to_group_handler.assert_called_once()
        args, kwargs = user_added_to_group_handler.call_args
        self.assertEqual(kwargs["sender"], views.IAMUserViewSet)
        self.assertEqual(kwargs["user"], user_profile)
        self.assertEqual(kwargs["groups"][0], group)

    @patch("django_airavata.apps.api.views.iam_admin_client")
    def test_update_that_adds_user_to_multiple_groups(
            self, iam_admin_client):

        username = "testuser1"
        url = reverse(
            'django_airavata_api:iam-user-profile-detail',
            kwargs={'user_id': username})
        data = {
            "airavataInternalUserId": f"{username}@{GATEWAY_ID}",
            "userId": username,
            "gatewayId": GATEWAY_ID,
            "email": "testuser1@example.com",
            "firstName": "Test",
            "lastName": "User1",
            "airavataUserProfileExists": True,
            "enabled": True,
            "emailVerified": True,
            "groups": [
                {"id": "group1", "name": "Group 1"},
                {"id": "group2", "name": "Group 2"},
                {"id": "group3", "name": "Group 3"},
            ]
        }
        request = self.factory.put(url, data)
        force_authenticate(request, self.user)
        request.is_gateway_admin = True

        # Mock api clients
        iam_user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        iam_admin_client.get_user.return_value = iam_user_profile
        group_manager_mock = MagicMock(name='group_manager')
        user_profile_mock = MagicMock(name='user_profile')
        request.profile_service = {
            'group_manager': group_manager_mock,
            'user_profile': user_profile_mock,
        }
        request.authz_token = "dummy"
        user_profile_mock.doesUserExist.return_value = True
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        user_profile_mock.getUserProfileById.return_value = user_profile
        group_manager_mock.getAllGroupsUserBelongs.return_value = [
            GroupModel(id="group1")]

        def side_effect(authz_token, group_id):
            if group_id == "group2":
                return GroupModel(id="group2", name="Group 2")
            elif group_id == "group3":
                return GroupModel(id="group3", name="Group 3")
            else:
                raise Exception("Unexpected group id: " + group_id)

        group_manager_mock.getGroup.side_effect = side_effect
        request.airavata_client = MagicMock(name="airavata_client")
        request.airavata_client.getGatewayGroups.return_value = GatewayGroups(
            gatewayId=GATEWAY_ID,
            adminsGroupId="adminsGroupId",
            readOnlyAdminsGroupId="readOnlyAdminsGroupId",
            defaultGatewayUsersGroupId="defaultGatewayUsersGroupId"
        )
        request.session = {}

        # Mock signal handler to verify 'user_added_to_group' signal is sent
        user_added_to_group_handler = MagicMock(
            name="user_added_to_group_handler")
        signals.user_added_to_group.connect(
            user_added_to_group_handler,
            sender=views.IAMUserViewSet)
        iam_user_update = views.IAMUserViewSet.as_view({'put': 'update'})
        response = iam_user_update(request, user_id=username)
        self.assertEquals(200, response.status_code)

        user_profile_mock.doesUserExist.assert_called_once()
        group_manager_mock.getAllGroupsUserBelongs.assert_called_once()

        user_profile_mock.getUserProfileById.assert_called_once()
        args, kwargs = user_profile_mock.getUserProfileById.call_args
        self.assertSequenceEqual(
            args, [request.authz_token, "testuser1", GATEWAY_ID])

        group_manager_mock.getGroup.assert_has_calls([
            call(request.authz_token, "group2"),
            call(request.authz_token, "group3")
        ], any_order=True)

        group_manager_mock.addUsersToGroup.assert_has_calls([
            call(request.authz_token, [f"testuser1@{GATEWAY_ID}"], "group2"),
            call(request.authz_token, [f"testuser1@{GATEWAY_ID}"], "group3"),
        ], any_order=True)

        # user_added_to_group signal should only be called once, with both
        # groups passed to it
        user_added_to_group_handler.assert_called_once()
        args, kwargs = user_added_to_group_handler.call_args
        self.assertEqual(kwargs["sender"], views.IAMUserViewSet)
        self.assertEqual(kwargs["user"], user_profile)
        self.assertSetEqual({"group2", "group3"},
                            {g.id for g in kwargs["groups"]})

    @patch("django_airavata.apps.api.views.iam_admin_client")
    def test_update_that_does_not_add_user_to_groups(
            self, iam_admin_client):

        username = "testuser1"
        url = reverse(
            'django_airavata_api:iam-user-profile-detail',
            kwargs={'user_id': username})
        data = {
            "airavataInternalUserId": f"{username}@{GATEWAY_ID}",
            "userId": username,
            "gatewayId": GATEWAY_ID,
            "email": "testuser1@example.com",
            "firstName": "Test",
            "lastName": "User1",
            "airavataUserProfileExists": True,
            "enabled": True,
            "emailVerified": True,
            "groups": [
                {"id": "group1", "name": "Group 1"},
            ]
        }
        request = self.factory.put(url, data)
        force_authenticate(request, self.user)
        request.is_gateway_admin = True

        # Mock api clients
        iam_user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        iam_admin_client.get_user.return_value = iam_user_profile
        group_manager_mock = MagicMock(name='group_manager')
        user_profile_mock = MagicMock(name='user_profile')
        request.profile_service = {
            'group_manager': group_manager_mock,
            'user_profile': user_profile_mock,
        }
        request.authz_token = "dummy"
        user_profile_mock.doesUserExist.return_value = True
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser1@{GATEWAY_ID}",
            userId="testuser1",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        user_profile_mock.getUserProfileById.return_value = user_profile
        group_manager_mock.getAllGroupsUserBelongs.return_value = [
            GroupModel(id="group1")]

        request.airavata_client = MagicMock(name="airavata_client")
        request.airavata_client.getGatewayGroups.return_value = GatewayGroups(
            gatewayId=GATEWAY_ID,
            adminsGroupId="adminsGroupId",
            readOnlyAdminsGroupId="readOnlyAdminsGroupId",
            defaultGatewayUsersGroupId="defaultGatewayUsersGroupId"
        )
        request.session = {}

        # Mock signal handler to verify 'user_added_to_group' signal is sent
        user_added_to_group_handler = MagicMock(
            name="user_added_to_group_handler")
        signals.user_added_to_group.connect(
            user_added_to_group_handler,
            sender=views.IAMUserViewSet)
        iam_user_update = views.IAMUserViewSet.as_view({'put': 'update'})
        response = iam_user_update(request, user_id=username)
        self.assertEquals(200, response.status_code)

        user_profile_mock.doesUserExist.assert_called_once()
        group_manager_mock.getAllGroupsUserBelongs.assert_called_once()

        # Since user wasn't added to a group, these all should not have been
        # called
        user_profile_mock.getUserProfileById.assert_not_called()
        group_manager_mock.getGroup.assert_not_called()
        group_manager_mock.addUsersToGroup.assert_not_called()
        user_added_to_group_handler.assert_not_called()


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_ADMINS=PORTAL_ADMINS
)
class ExceptionHandlingTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user('testuser')
        self.factory = APIRequestFactory()

    def test_unauthenticated_request(self):

        url = reverse('django_airavata_api:group-list')
        data = {}
        request = self.factory.post(url, data)
        # Deliberately not authenticating user for request
        group_create = views.GroupViewSet.as_view({'post': 'create'})
        response = group_create(request)
        self.assertEquals(403, response.status_code)
        self.assertIn('is_authenticated', response.data)
        self.assertFalse(response.data['is_authenticated'])
