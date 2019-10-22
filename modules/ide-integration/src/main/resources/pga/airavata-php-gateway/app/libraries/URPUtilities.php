<?php

use Airavata\API\Error\AiravataSystemException;
use Airavata\Model\AppCatalog\UserResourceProfile\UserResourceProfile;
use Airavata\Model\AppCatalog\UserResourceProfile\UserComputeResourcePreference;
use Airavata\Model\AppCatalog\UserResourceProfile\UserStoragePreference;
use Airavata\Model\Credential\Store\SummaryType;

class URPUtilities
{

    public static function get_or_create_user_resource_profile()
    {
        if (!URPUtilities::is_user_resource_profile_exists())
        {
            $userResourceProfile = URPUtilities::create_user_resource_profile();
        }
        else
        {
            $userResourceProfile = URPUtilities::get_user_resource_profile();
        }
        return $userResourceProfile;
    }

    public static function get_user_resource_profile()
    {
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        return Airavata::getUserResourceProfile(Session::get('authz-token'), $userId, $gatewayId);
    }

    public static function is_user_resource_profile_exists()
    {
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        return Airavata::isUserResourceProfileExists(Session::get('authz-token'), $userId, $gatewayId);
    }

    public static function create_user_resource_profile()
    {

        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        $credentialStoreToken = AdminUtilities::create_ssh_token_for_user("Default SSH Key");
        $userResourceProfileData = new UserResourceProfile(array(
                "userId" => $userId,
                "gatewayID" => $gatewayId,
                "credentialStoreToken" => $credentialStoreToken
            )
        );
        Airavata::registerUserResourceProfile(Session::get('authz-token'), $userResourceProfileData);

        return Airavata::getUserResourceProfile(Session::get('authz-token'), $userId, $gatewayId);
    }

    public static function update_user_resource_profile($userResourceProfile)
    {

        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        Airavata::updateUserResourceProfile(Session::get('authz-token'), $userId, $gatewayId, $userResourceProfile);
    }

    public static function get_all_ssh_pub_keys_summary_for_user()
    {

        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');

        $all_ssh_pub_key_summaries = Airavata::getAllCredentialSummaries(Session::get('authz-token'), SummaryType::SSH);
        foreach ($all_ssh_pub_key_summaries as $ssh_pub_key_summary) {
            # strip whitespace from public key: there can't be trailing
            # whitespace in a public key entry in the authorized_keys file
            $ssh_pub_key_summary->publicKey = trim($ssh_pub_key_summary->publicKey);
        }
        return URPUtilities::create_credential_summary_map($all_ssh_pub_key_summaries);
    }

    // Create array of CredentialSummary objects where the token is the key
    private static function create_credential_summary_map($credentialSummaries) {

        $credentialSummaryMap = array();
        foreach ($credentialSummaries as $csIndex => $credentialSummary) {
            $credentialSummaryMap[$credentialSummary->token] = $credentialSummary;
        }
        return $credentialSummaryMap;
    }

    public static function add_or_update_user_CRP($inputs, $update = false)
    {
        $inputs = Input::all();
        if( $inputs["reservationStartTime"] != "")
            $inputs["reservationStartTime"] = CommonUtilities::convertLocalToUTC(strtotime($inputs["reservationStartTime"])) * 1000;
        if( $inputs["reservationEndTime"] != "")
            $inputs["reservationEndTime"] = CommonUtilities::convertLocalToUTC(strtotime($inputs["reservationEndTime"])) * 1000;

        $userComputeResourcePreference = new UserComputeResourcePreference($inputs);
        // FIXME: for now assume that if a user is adding or updating a UserComputeResourcePreference then they have also validated that it works. It would be better to confirm that in Airavata.
        $userComputeResourcePreference->validated = true;
        // Log::debug("add_or_update_user_CRP: ", array($userComputeResourcePreference));
        $userId = Session::get('username');
        if ($update)
        {
            return Airavata::updateUserComputeResourcePreference(Session::get('authz-token'), $userId, $inputs["gatewayId"], $inputs["computeResourceId"], $userComputeResourcePreference);
        } else
        {
            return Airavata::addUserComputeResourcePreference(Session::get('authz-token'), $userId, $inputs["gatewayId"], $inputs["computeResourceId"], $userComputeResourcePreference);
        }
    }

    public static function delete_user_CRP($computeResourceId)
    {
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        $result = Airavata::deleteUserComputeResourcePreference(Session::get('authz-token'), $userId, $gatewayId, $computeResourceId);
        // Log::debug("deleteUserComputeResourcePreference($userId, $gatewayId, $computeResourceId) => $result");
        return $result;
    }

    /*
     * Get all user's *validated* compute resource preferences, keyed by compute resource id.
     */
    public static function get_all_validated_user_compute_resource_prefs()
    {

        return array_filter(URPUtilities::get_all_user_compute_resource_prefs(), function($userComputeResourcePreference) {
            return $userComputeResourcePreference->validated;
        });
    }

    private static function get_all_user_compute_resource_prefs()
    {

        $userComputeResourcePreferencesById = array();
        if (URPUtilities::is_user_resource_profile_exists())
        {
            $userResourceProfile = URPUtilities::get_user_resource_profile();
            $userComputeResourcePreferences = $userResourceProfile->userComputeResourcePreferences;
            // Put $userComputeResourcePreferences in a map keyed by computeResourceId
            foreach( $userComputeResourcePreferences as $userComputeResourcePreference )
            {
                $userComputeResourcePreferencesById[$userComputeResourcePreference->computeResourceId] = $userComputeResourcePreference;
            }
        }
        return $userComputeResourcePreferencesById;
    }

    public static function add_or_update_user_SRP($inputs, $update = false)
    {
        $inputs = Input::all();

        $userStoragePreference = new UserStoragePreference($inputs);
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        $storageResourceId = $inputs["storageResourceId"];
        if ($update)
        {
            return Airavata::updateUserStoragePreference(Session::get('authz-token'), $userId, $inputs["gatewayId"], $inputs["storageResourceId"], $userStoragePreference);
        } else
        {
            // Log::debug("addUserStoragePreference($userId, $gatewayId, $storageResourceId)", array($userStoragePreference));
            $result = Airavata::addUserStoragePreference(Session::get('authz-token'), $userId, $gatewayId, $storageResourceId, $userStoragePreference);
            return $result;
        }
    }

    public static function delete_user_SRP($storageResourceId)
    {
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        $result = Airavata::deleteUserStoragePreference(Session::get('authz-token'), $userId, $gatewayId, $storageResourceId);
        // Log::debug("deleteUserStoragePreference($userId, $gatewayId, $storageResourceId) => $result");
        return $result;
    }

    // Only used for testing
    public static function delete_user_resource_profile()
    {
        $userId = Session::get('username');
        $gatewayId = Session::get('gateway_id');
        Airavata::deleteUserResourceProfile(Session::get('authz-token'), $userId, $gatewayId);
    }

    /**
     * Returns an array with compute resource ids as the key and each entry is a 
     * map with the following fields:
     * * hostname: hostname of compute resource
     * * userComputeResourcePreference: if UserComputeResourcePreference exists for compute resource or was able to be created
     * * accountIsMissing: (boolean) true if account doesn't exist on cluster and needs to be created manually (or by some other process)
     * * additionalInfo: Additional info field from ComputeResourcePreference
     * * errorMessage: Error message associated with trying to setup account
     */
    public static function setup_auto_provisioned_accounts()
    {
        $results = array();
        $gatewayResourceProfile = CRUtilities::getGatewayResourceProfile();
        $computeResourcePreferences = $gatewayResourceProfile->computeResourcePreferences;
        $userComputeResourcePreferences = URPUtilities::get_all_user_compute_resource_prefs();
        $sshAccountProvisioners = URPUtilities::get_ssh_account_provisioners();
        $userId = Session::get("username");
        $gatewayId = Session::get("gateway_id");
        foreach( $computeResourcePreferences as $computeResourcePreference)
        {
            if( !empty($computeResourcePreference->sshAccountProvisioner))
            {
                $sshAccountProvisioner = $sshAccountProvisioners[$computeResourcePreference->sshAccountProvisioner];
                $computeResourceId = $computeResourcePreference->computeResourceId;
                $computeResource = CRUtilities::get_compute_resource($computeResourceId);
                $hostname = $computeResource->hostName;
                $userComputeResourcePreference = null;
                $errorMessage = null;
                $accountIsMissing = false;
                try {
                    if( array_key_exists($computeResourceId, $userComputeResourcePreferences)) {
                        $userComputeResourcePreference = $userComputeResourcePreferences[$computeResourceId];
                        // If a $userComputeResourcePreference exists but isn't
                        // validated some error must have occurred the last time
                        // it was attempted to be setup. We'll try to set it up again.
                        // Also, the setup may be incomplete in which case we
                        // should also try to setup it up again.
                        if (!$userComputeResourcePreference->validated || !URPUtilities::is_ssh_account_setup_complete($computeResourceId, $userComputeResourcePreference)) {
                            $userComputeResourcePreference = URPUtilities::setup_ssh_account($gatewayId, $userId, $computeResourceId, $hostname, $userComputeResourcePreference);
                        }
                    } else if ($sshAccountProvisioner->canCreateAccount) {
                        $userComputeResourcePreference = URPUtilities::setup_ssh_account($gatewayId, $userId, $computeResourceId, $hostname);
                    } else if (Airavata::doesUserHaveSSHAccount(Session::get('authz-token'), $computeResourceId, $userId)) {
                        $userComputeResourcePreference = URPUtilities::setup_ssh_account($gatewayId, $userId, $computeResourceId, $hostname);
                    } else {
                        $accountIsMissing = true;
                    }
                } catch (Exception $ex) {
                    Log::error("Failed to setup SSH Account for " . $userId . " on " . $hostname);
                    Log::error($ex);
                    $errorMessage = $ex->getMessage();
                }
                $results[] = array(
                    "hostname" => $hostname,
                    "userComputeResourcePreference" => $userComputeResourcePreference,
                    "accountIsMissing" => $accountIsMissing,
                    "additionalInfo" => $computeResourcePreference->sshAccountProvisionerAdditionalInfo,
                    "errorMessage" => $errorMessage
                );
            }
        }

        return $results;
    }

    private static function get_ssh_account_provisioners()
    {
        $sshAccountProvisionersByName = array();
        $sshAccountProvisioners = Airavata::getSSHAccountProvisioners(Session::get('authz-token'));
        foreach ($sshAccountProvisioners as $sshAccountProvisioner) {
            $sshAccountProvisionersByName[$sshAccountProvisioner->name] = $sshAccountProvisioner;
        }
        return $sshAccountProvisionersByName;
    }

    private static function is_ssh_account_setup_complete($computeResourceId, $userComputeResourcePreference)
    {
        return Airavata::isSSHSetupCompleteForUserComputeResourcePreference(
            Session::get('authz-token'),
            $computeResourceId,
            $userComputeResourcePreference->resourceSpecificCredentialStoreToken
        );
    }

    private static function setup_ssh_account($gatewayId, $userId, $computeResourceId, $hostname, $userComputeResourcePreference=null)
    {
        if (empty($userComputeResourcePreference)) {
            Log::debug("userComputeResourcePreference is empty", array($userComputeResourcePreference));
            // Initially create a UserComputeResourcePreference record to store
            // the key. This will be marked validated=false initially until it
            // is successfully setup. This way in case an error occurs we have a
            // record of the generated SSH key to use and can try again later.
            $userComputeResourcePreference = new UserComputeResourcePreference();
            $userComputeResourcePreference->computeResourceId = $computeResourceId;
            $credentialStoreToken = AdminUtilities::create_ssh_token_for_user("SSH Key for " . $hostname);
            $userComputeResourcePreference->resourceSpecificCredentialStoreToken = $credentialStoreToken;
            Airavata::addUserComputeResourcePreference(Session::get('authz-token'), $userId, $gatewayId, $computeResourceId, $userComputeResourcePreference);
        }
        $updatedUserCompResourcePref = Airavata::setupUserComputeResourcePreferencesForSSH(Session::get('authz-token'), $computeResourceId, $userId, $userComputeResourcePreference->resourceSpecificCredentialStoreToken);
        $updatedUserCompResourcePref->resourceSpecificCredentialStoreToken = $userComputeResourcePreference->resourceSpecificCredentialStoreToken;
        Airavata::updateUserComputeResourcePreference(Session::get('authz-token'), $userId, $gatewayId, $computeResourceId, $updatedUserCompResourcePref);
        return $updatedUserCompResourcePref;
    }
}

?>
