<?php
use Airavata\Model\Group\ResourceType;
use Airavata\Model\Group\ResourcePermissionType;

class SharingUtilities {

    /**
     * Determine if the resource has been shared with any users.
     *
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return True if the resource has been shared, false otherwise.
     */
    public static function resourceIsShared($resourceId, $dataResourceType) {
        $read = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::READ);
        return (count($read) > 0 ? true : false);
    }

    /**
     * Determine if the user has read privileges on the resource.
     *
     * @param $uid                  The user to check
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return True if the user has read permission, false otherwise.
     */
    public static function userCanRead($uid, $resourceId, $dataResourceType) {

        return Airavata::userHasAccess(Session::get('authz-token'), $resourceId, ResourcePermissionType::READ);
    }

    /**
     * Determine if the user has write privileges on the resource.
     *
     * @param $uid                  The user to check
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return True if the user has write permission, false otherwise.
     */
    public static function userCanWrite($uid, $resourceId, $dataResourceType) {

        return Airavata::userHasAccess(Session::get('authz-token'), $resourceId, ResourcePermissionType::WRITE);
    }

    /**
     * Get the permissions settings for the specified resource.
     *
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return An array [$uid => [read => bool, write => bool, owner => bool]]
     */
    public static function getAllUserPermissions($resourceId, $dataResourceType) {
        $users = array();

        $read = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::READ);
        $write = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::WRITE);
        $owner = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::OWNER);

        foreach($read as $uid) {
            if ($uid !== Session::get('username') && UserProfileUtilities::does_user_profile_exist($uid)) {
                $users[$uid] = array('read' => true, 'write' => false);
            }
        }

        foreach($write as $uid) {
            if ($uid !== Session::get('username') && UserProfileUtilities::does_user_profile_exist($uid)) {
                $users[$uid]['write'] = true;
            }
        }

        foreach($owner as $uid) {
            if ($uid !== Session::get('username') && UserProfileUtilities::does_user_profile_exist($uid)) {
                $users[$uid]['owner'] = true;
            }
        }

        return $users;
    }

    /**
     * Retrieve profile information for all user in the supplied list.
     *
     * @param $uids An array of uids
     * @return An array [uid => [firstname => string, lastname => string, email => string]]
     */
    public static function getUserProfiles($uids) {
        // FIXME: instead of loading all user profiles it would be better to
        // have the user search for users and just load profiles matching the search
        $all_profiles = SharingUtilities::convertUserProfilesToSharingProfiles(UserProfileUtilities::get_all_user_profiles(0, 100000));
        // Log::debug("all_profiles", array($all_profiles));
        $uids = array_filter($uids, function($uid) use ($all_profiles) {
            return ($uid !== Session::get('username') && array_key_exists($uid, $all_profiles));
        });
        $profiles = array();
        foreach ($uids as $uid) {
            $profiles[$uid] = $all_profiles[$uid];
        }
        return $profiles;
    }

    private static function convertUserProfilesToSharingProfiles($user_profiles) {
        $sharing_profiles = array();
        foreach ($user_profiles as $user_profile) {
            $sharing_profile = array(
                'firstname' => $user_profile->firstName,
                'lastname' => $user_profile->lastName,
                'email' => $user_profile->emails[0],
            );
            $sharing_profiles[$user_profile->userId] = $sharing_profile;
        }
        return $sharing_profiles;
    }

    /**
     * Retrieve profile and permissions information for users with access to the given resource.
     *
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return An array [uid => [firstname => string, lastname => string, email => string, access => [read => bool, write => bool]]]
     */
    public static function getProfilesForSharedUsers($resourceId, $dataResourceType) {
        $perms = SharingUtilities::getAllUserPermissions($resourceId, $dataResourceType);
        $profs = SharingUtilities::getUserProfiles(array_keys($perms));

        foreach ($profs as $uid => $prof) {
            $prof["access"] = $perms[$uid];
            $profs[$uid] = $prof;
        }

        return $profs;
    }

    /**
     * Retrieve profile and permissions information for users without access to the given resource.
     *
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return An array [uid => [firstname => string, lastname => string, email => string]] of all users without access
     */
    public static function getProfilesForUnsharedUsers($resourceId, $dataResourceType) {
        $users = GrouperUtilities::getAllGatewayUsers();
        $read = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::READ);

        $unshared = array_diff($users, $read);

        return SharingUtilities::getUserProfiles($unshared);
    }

    /**
     * Retrieve profile and permissions information for all users for the given resource.
     *
     *
     * @param $resourceId           Experiment or Project ID
     * @param $dataResourceType     e.g Airavata\Model\Group\ResourceType:PROJECT,Airavata\Model\Group\ResourceType:EXPERIMENT
     * @return An array [uid => [firstname => string, lastname => string, email => string, access => [read => bool, write => bool, owner => bool]]]
     *         with access only defined for users with permissions.
     */
    public static function getAllUserProfiles($resourceId=null, $dataResourceType=null) {
        $profs = SharingUtilities::getUserProfiles(GrouperUtilities::getAllGatewayUsers());
        if ($resourceId) {
            $perms = SharingUtilities::getAllUserPermissions($resourceId, $dataResourceType);

            foreach ($perms as $uid => $access) {
                $profs[$uid]['access'] = $access;
            }
        }
        return $profs;
    }

    public static function mixProjectPermissionsWithExperiment($projectId, $expId) {
        $proj = SharingUtilities::getProfilesForSharedUsers($projectId, ResourceType::PROJECT);
        $exp = SharingUtilities::getProfilesForSharedUsers($expId, ResourceType::EXPERIMENT);

        foreach ($proj as $uid => $prof) {
            if (!array_key_exists($uid, $exp)) {
                $exp[$uid] = $prof;
            }
        }

        return $exp;
    }

    public static function updateAllUsersListWithPrivileges($shared) {
        $users = SharingUtilities::getAllUserProfiles();

        foreach ($shared as $uid => $prof) {
            $users[$uid] = $shared[$uid];
        }

        return $users;
    }

    public static function getSharedResourceOwner($resourceId, $dataResourceType) {
        $owners = GrouperUtilities::getAllAccessibleUsers($resourceId, $dataResourceType, ResourcePermissionType::OWNER);
        if (count($owners) != 1) {
            Log::error("Got more than one shared resource owner!", array($resourceId, $dataResourceType, $owners));
            throw new Exception("Expected exactly 1 owner for $resourceId of type $dataResourceType!");
        } else {
            return $owners[0];
        }
    }
}

?>
