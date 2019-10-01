<?php

use Airavata\Model\User\Status;
use Airavata\Model\User\UserProfile;

class UserProfileUtilities
{

    public static function does_user_profile_exist($userId) {
        $gatewayId = Session::get('gateway_id');
        return UserProfileService::doesUserExist(Session::get('authz-token'), $userId, $gatewayId);
    }

    public static function create_basic_user_profile($username, $userEmail, $firstName, $lastName) {
        $gatewayId = Session::get("gateway_id");
        $userProfileData = array();
        $userProfileData["airavataInternalUserId"] = $username . '@' . $gatewayId;
        $userProfileData["userId"] = $username;
        $userProfileData["gatewayId"] = $gatewayId;
        $userProfileData["emails"] = array($userEmail);
        $userProfileData["firstName"] = $firstName;
        $userProfileData["lastName"] = $lastName;

        Log::info("creating basic user profile for user", array($userProfileData));
        return UserProfileUtilities::add_user_profile($userProfileData);
    }

    public static function add_user_profile($userProfileData) {

        $userProfile = new UserProfile($userProfileData);
        $userProfile->creationTime = time();
        $userProfile->lastAccessTime = time();
        $userProfile->validUntil = -1;
        $userProfile->State = Status::ACTIVE;
        return UserProfileService::addUserProfile(Session::get('authz-token'), $userProfile);
    }

    public static function get_user_profile($userId) {

        $gatewayId = Session::get('gateway_id');
        return UserProfileService::getUserProfileById(Session::get('authz-token'), $userId, $gatewayId);
    }

    public static function update_user_profile($userProfile) {

        return UserProfileService::updateUserProfile(Session::get('authz-token'), $userProfile);
    }

    public static function get_all_user_profiles($offset, $limit) {
        return UserProfileService::getAllUserProfilesInGateway(
            Session::get('authz-token'), Session::get('gateway_id'), $offset, $limit);
    }
}

?>
