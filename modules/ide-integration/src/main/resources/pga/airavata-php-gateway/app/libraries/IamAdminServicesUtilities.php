<?php
use Airavata\Model\User\Status;

class IamAdminServicesUtilities {

    public static function registerUser($username, $email, $first_name, $last_name, $password) {

        $admin_authz_token = IamAdminServicesUtilities::getAdminAuthzToken();
        return IamAdminServices::registerUser($admin_authz_token, $username, $email, $first_name, $last_name, $password);
    }

    public static function addInitialRoleToUser($username) {

        $admin_authz_token = IamAdminServicesUtilities::getAdminAuthzToken();
        $initialRoleName = CommonUtilities::getInitialRoleName();
        IamAdminServices::addRoleToUser($admin_authz_token, $username, $initialRoleName);
    }

    public static function enableUser($username) {

        $admin_authz_token = IamAdminServicesUtilities::getAdminAuthzToken();
        return IamAdminServices::enableUser($admin_authz_token, $username);
    }

    public static function isUserEnabled($username) {
        $admin_authz_token = IamAdminServicesUtilities::getAdminAuthzToken();
        return IamAdminServices::isUserEnabled($admin_authz_token, $username);
    }

    public static function resetUserPassword($username, $new_password) {

        $admin_authz_token = IamAdminServicesUtilities::getAdminAuthzToken();
        return IamAdminServices::resetUserPassword($admin_authz_token, $username, $new_password);
    }

    public static function getUsersWithRole($role_name) {

        $authz_token = Session::get('authz-token');
        $user_profiles = IamAdminServices::getUsersWithRole($authz_token, $role_name);
        $users = [];
        foreach ($user_profiles as $user_profile) {
            array_push($users, (object)["firstName"=>$user_profile->firstName,"lastName"=>$user_profile->lastName,"email"=>implode(",",$user_profile->emails),"userName"=>$user_profile->userId, "userEnabled"=>$user_profile->State == Status::CONFIRMED]);
        }
        return $users;
    }

    private static function getAdminAuthzToken() {
        return Keycloak::getAdminAuthzToken();
    }
}
 ?>