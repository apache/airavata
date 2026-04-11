import importlib

from airavata_sdk.transport.utils import (
    create_gateway_service_stub,
    create_iam_admin_service_stub,
    create_user_profile_service_stub,
)


class IamClient:
    """Gateway management, IAM admin (Keycloak users/roles), and user profiles."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._gateway = create_gateway_service_stub(channel)
        self._iam = create_iam_admin_service_stub(channel)
        self._user_profile = create_user_profile_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ================================================================
    # Gateway Service
    # ================================================================

    def is_user_exists(self, gateway_id, user_name):
        pb2 = self._svc("gateway_service_pb2")
        response = self._gateway.IsUserExists(
            pb2.IsUserExistsRequest(gateway_id=gateway_id, user_name=user_name),
            metadata=self._metadata,
        )
        return response.exists

    def add_gateway(self, gateway):
        pb2 = self._svc("gateway_service_pb2")
        response = self._gateway.AddGateway(
            pb2.AddGatewayRequest(gateway=gateway),
            metadata=self._metadata,
        )
        return response.gateway_id

    def get_all_users_in_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        response = self._gateway.GetAllUsersInGateway(
            pb2.GetAllUsersInGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return list(response.user_names)

    def update_gateway(self, gateway_id, gateway):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.UpdateGateway(
            pb2.UpdateGatewayRequest(gateway_id=gateway_id, gateway=gateway),
            metadata=self._metadata,
        )

    def get_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.GetGateway(
            pb2.GetGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_gateway(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        return self._gateway.DeleteGateway(
            pb2.DeleteGatewayRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_all_gateways(self):
        pb2 = self._svc("gateway_service_pb2")
        response = self._gateway.GetAllGateways(
            pb2.GetAllGatewaysRequest(),
            metadata=self._metadata,
        )
        return list(response.gateways)

    def is_gateway_exist(self, gateway_id):
        pb2 = self._svc("gateway_service_pb2")
        response = self._gateway.IsGatewayExist(
            pb2.IsGatewayExistRequest(gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return response.exists

    # ================================================================
    # IAM Admin Service
    # ================================================================

    def set_up_gateway(self, gateway):
        pb2 = self._svc("iam_admin_service_pb2")
        return self._iam.SetUpGateway(
            pb2.SetUpGatewayRequest(gateway=gateway),
            metadata=self._metadata,
        )

    def is_username_available(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.IsUsernameAvailable(
            pb2.IsUsernameAvailableRequest(username=username),
            metadata=self._metadata,
        )
        return resp.available

    def register_user(self, username, email_address, first_name, last_name, new_password):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.RegisterUser(
            pb2.RegisterUserRequest(
                username=username,
                email_address=email_address,
                first_name=first_name,
                last_name=last_name,
                new_password=new_password,
            ),
            metadata=self._metadata,
        )

    def enable_user(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.EnableUser(
            pb2.EnableUserRequest(username=username),
            metadata=self._metadata,
        )

    def is_user_enabled(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.IsUserEnabled(
            pb2.IsUserEnabledRequest(username=username),
            metadata=self._metadata,
        )
        return resp.enabled

    def is_user_exist(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.IsUserExist(
            pb2.IsUserExistRequest(username=username),
            metadata=self._metadata,
        )
        return resp.exists

    def get_iam_user(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        return self._iam.GetUser(
            pb2.GetIamUserRequest(username=username),
            metadata=self._metadata,
        )

    def get_iam_users(self, offset=0, limit=20, search=""):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.GetUsers(
            pb2.GetIamUsersRequest(offset=offset, limit=limit, search=search),
            metadata=self._metadata,
        )
        return list(resp.users)

    def reset_user_password(self, username, new_password):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.ResetUserPassword(
            pb2.ResetUserPasswordRequest(username=username, new_password=new_password),
            metadata=self._metadata,
        )

    def find_users(self, email, user_id=""):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.FindUsers(
            pb2.FindUsersRequest(email=email, user_id=user_id),
            metadata=self._metadata,
        )
        return list(resp.users)

    def update_iam_user_profile(self, user_details):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.UpdateUserProfile(
            pb2.UpdateIamUserProfileRequest(user_details=user_details),
            metadata=self._metadata,
        )

    def delete_iam_user(self, username):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.DeleteUser(
            pb2.DeleteUserRequest(username=username),
            metadata=self._metadata,
        )

    def add_role_to_user(self, username, role_name):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.AddRoleToUser(
            pb2.AddRoleToUserRequest(username=username, role_name=role_name),
            metadata=self._metadata,
        )

    def remove_role_from_user(self, username, role_name):
        pb2 = self._svc("iam_admin_service_pb2")
        self._iam.RemoveRoleFromUser(
            pb2.RemoveRoleFromUserRequest(username=username, role_name=role_name),
            metadata=self._metadata,
        )

    def get_users_with_role(self, role_name):
        pb2 = self._svc("iam_admin_service_pb2")
        resp = self._iam.GetUsersWithRole(
            pb2.GetUsersWithRoleRequest(role_name=role_name),
            metadata=self._metadata,
        )
        return list(resp.users)

    # ================================================================
    # User Profile Service
    # ================================================================

    def add_user_profile(self, user_profile):
        pb2 = self._svc("user_profile_service_pb2")
        resp = self._user_profile.AddUserProfile(
            pb2.AddUserProfileRequest(user_profile=user_profile),
            metadata=self._metadata,
        )
        return resp.user_id

    def update_user_profile(self, user_profile):
        pb2 = self._svc("user_profile_service_pb2")
        self._user_profile.UpdateUserProfile(
            pb2.UpdateUserProfileRequest(user_profile=user_profile),
            metadata=self._metadata,
        )

    def get_user_profile_by_id(self, user_id, gateway_id):
        pb2 = self._svc("user_profile_service_pb2")
        return self._user_profile.GetUserProfileById(
            pb2.GetUserProfileByIdRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_user_profile_by_name(self, user_name, gateway_id):
        pb2 = self._svc("user_profile_service_pb2")
        return self._user_profile.GetUserProfileByName(
            pb2.GetUserProfileByNameRequest(user_name=user_name, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_user_profile(self, user_id):
        pb2 = self._svc("user_profile_service_pb2")
        self._user_profile.DeleteUserProfile(
            pb2.DeleteUserProfileRequest(user_id=user_id),
            metadata=self._metadata,
        )

    def get_all_user_profiles_in_gateway(self, gateway_id, offset=0, limit=20):
        pb2 = self._svc("user_profile_service_pb2")
        resp = self._user_profile.GetAllUserProfilesInGateway(
            pb2.GetAllUserProfilesInGatewayRequest(
                gateway_id=gateway_id, offset=offset, limit=limit,
            ),
            metadata=self._metadata,
        )
        return list(resp.user_profiles)

    def does_user_exist(self, user_name, gateway_id):
        pb2 = self._svc("user_profile_service_pb2")
        resp = self._user_profile.DoesUserExist(
            pb2.DoesUserExistRequest(user_name=user_name, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return resp.exists
