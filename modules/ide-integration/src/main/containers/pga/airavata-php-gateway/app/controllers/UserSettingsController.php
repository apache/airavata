<?php

class UserSettingsController extends BaseController
{
    public function __construct()
    {
        Session::put("nav-active", "user-menu");
    }

    public function getUserSettings() {
        return View::make("account/settings");
    }

    public function getCredentialStore() {

        $userResourceProfile = URPUtilities::get_or_create_user_resource_profile();
        $userCredentialSummaries = URPUtilities::get_all_ssh_pub_keys_summary_for_user();
        $defaultCredentialToken = $userResourceProfile->credentialStoreToken;
        foreach ($userCredentialSummaries as $credentialSummary) {
            $credentialSummary->canDelete = $this->canDeleteCredential($credentialSummary->token, $userResourceProfile);
        }

        return View::make("account/credential-store", array(
            "userResourceProfile" => $userResourceProfile,
            "credentialSummaries" => $userCredentialSummaries,
            "defaultCredentialToken" => $defaultCredentialToken
        ));
    }

    // Don't allow deleting credential if default credential or in use by a
    // userComputeResourcePreference or a userStoragePreference
    private function canDeleteCredential($token, $userResourceProfile) {
        if ($token == $userResourceProfile->credentialStoreToken) {
            return false;
        } else {
            foreach ($userResourceProfile->userComputeResourcePreferences as $userCompResPref) {
                if ($userCompResPref->resourceSpecificCredentialStoreToken == $token) {
                    return false;
                }
            }
            foreach ($userResourceProfile->userStoragePreferences as $userStoragePreference) {
                if ($userStoragePreference->resourceSpecificCredentialStoreToken == $token) {
                    return false;
                }
            }
        }
        return true;
    }

    public function setDefaultCredential() {

        $defaultToken = Input::get("defaultToken");
        $userResourceProfile = URPUtilities::get_user_resource_profile();
        $userResourceProfile->credentialStoreToken = $defaultToken;
        URPUtilities::update_user_resource_profile($userResourceProfile);

        $credentialSummaries = URPUtilities::get_all_ssh_pub_keys_summary_for_user();
        $description = $credentialSummaries[$defaultToken]->description;

        return Redirect::to("account/credential-store")->with("message", "SSH Key '$description' is now the default");
    }

    public function addCredential() {

        $rules = array(
            "credential-description" => "required",
        );

        $messages = array(
            "credential-description.required" => "A description is required for a new SSH key",
        );

        $validator = Validator::make(Input::all(), $rules, $messages);
        if ($validator->fails()) {
            return Redirect::to("account/credential-store")
                ->withErrors($validator);
        }

        $description = Input::get("credential-description");

        if (AdminUtilities::create_ssh_token_for_user($description)) {
            return Redirect::to("account/credential-store")->with("message", "SSH Key '$description' was added");
        }
    }

    public function deleteCredential() {

        $userResourceProfile = URPUtilities::get_user_resource_profile();
        $credentialStoreToken = Input::get("credentialStoreToken");
        if ($credentialStoreToken == $userResourceProfile->credentialStoreToken) {
            return Redirect::to("account/credential-store")->with("error-message", "You are not allowed to delete the default SSH key.");
        }

        $credentialSummaries = URPUtilities::get_all_ssh_pub_keys_summary_for_user();
        $description = $credentialSummaries[$credentialStoreToken]->description;

        if (AdminUtilities::remove_ssh_token($credentialStoreToken)) {
            return Redirect::to("account/credential-store")->with("message", "SSH Key '$description' was deleted");
        }
    }

    public function getComputeResources(){

        $userResourceProfile = URPUtilities::get_or_create_user_resource_profile();
        $gatewayResourceProfile = CRUtilities::getGatewayResourceProfile();
        $computeResourcePreferences = $gatewayResourceProfile->computeResourcePreferences;
        $computeResourcePreferencesById = array();
        foreach ($computeResourcePreferences as $computeResourcePreference) {
            $computeResourcePreferencesById[$computeResourcePreference->computeResourceId] = $computeResourcePreference;
        }

        $allCRs = CRUtilities::getAllCRObjects();
        foreach( $allCRs as $index => $crObject)
        {
            $allCRsById[$crObject->computeResourceId] = $crObject;
        }
        // Add crDetails to each UserComputeResourcePreference
        foreach ($userResourceProfile->userComputeResourcePreferences as $index => $userCompResPref) {
            $userCompResPref->crDetails = $allCRsById[$userCompResPref->computeResourceId];
            // Disallow editing a UserComputeResourcePreference that was automatically setup by an sshAccountProvisioner
            $userCompResPref->editable = true;
            if (array_key_exists($userCompResPref->computeResourceId, $computeResourcePreferencesById)) {
                $computeResourcePreference = $computeResourcePreferencesById[$userCompResPref->computeResourceId];
                $userCompResPref->editable = $computeResourcePreference->sshAccountProvisioner == null;
            }
            // To figure out the unselectedCRs, remove this compute resource from allCRsById
            unset($allCRsById[$userCompResPref->computeResourceId]);
        }
        $unselectedCRs = array_values($allCRsById);

        $credentialSummaries = URPUtilities::get_all_ssh_pub_keys_summary_for_user();
        $defaultCredentialSummary = $credentialSummaries[$userResourceProfile->credentialStoreToken];

        return View::make("account/user-compute-resources", array(
            "userResourceProfile" => $userResourceProfile,
            "computeResources" => $allCRs,
            "unselectedCRs" => $unselectedCRs,
            "credentialSummaries" => $credentialSummaries,
            "defaultCredentialSummary" => $defaultCredentialSummary
        ));
    }

    public function addUserComputeResourcePreference() {

        if( URPUtilities::add_or_update_user_CRP( Input::all()) )
        {
            return Redirect::to("account/user-compute-resources")->with("message","Compute Resource Account Settings have been saved.");
        }
    }

    public function updateUserComputeResourcePreference() {

        if( URPUtilities::add_or_update_user_CRP( Input::all(), true ) )
        {
            return Redirect::to("account/user-compute-resources")->with("message","Compute Resource Account Settings have been updated.");
        }
    }

    public function deleteUserComputeResourcePreference() {
        $computeResourceId = Input::get("rem-user-crId");
        $result = URPUtilities::delete_user_CRP( $computeResourceId );
        if( $result )
        {
            return Redirect::to("account/user-compute-resources")->with("message","Compute Resource Account Settings have been deleted.");
        }
    }

    public function getStorageResources(){

        $userResourceProfile = URPUtilities::get_or_create_user_resource_profile();

        $allSRs = SRUtilities::getAllSRObjects();
        foreach( $allSRs as $index => $srObject )
        {
            $allSRsById[$srObject->storageResourceId] = $srObject;
        }
        // Add srDetails to each UserStoragePreference
        foreach ($userResourceProfile->userStoragePreferences as $index => $userStoragePreference) {
            $userStoragePreference->srDetails = $allSRsById[$userStoragePreference->storageResourceId];
            // To figure out the unselectedSRs, remove this storage resource from allSRsById
            unset($allSRsById[$userStoragePreference->storageResourceId]);
        }
        $unselectedSRs = array_values($allSRsById);

        $credentialSummaries = URPUtilities::get_all_ssh_pub_keys_summary_for_user();
        $defaultCredentialSummary = $credentialSummaries[$userResourceProfile->credentialStoreToken];

        return View::make("account/user-storage-resources", array(
            "userResourceProfile" => $userResourceProfile,
            "storageResources" => $allSRs,
            "unselectedSRs" => $unselectedSRs,
            "credentialSummaries" => $credentialSummaries,
            "defaultCredentialSummary" => $defaultCredentialSummary
        ));
    }

    public function addUserStorageResourcePreference() {

        if( URPUtilities::add_or_update_user_SRP( Input::all()) )
        {
            return Redirect::to("account/user-storage-resources")->with("message","Storage Resource Account Settings have been saved.");
        }
    }

    public function updateUserStorageResourcePreference() {

        if( URPUtilities::add_or_update_user_SRP( Input::all(), true ) )
        {
            return Redirect::to("account/user-storage-resources")->with("message","Storage Resource Account Settings have been updated.");
        }
    }

    public function deleteUserStorageResourcePreference() {
        $storageResourceId = Input::get("rem-user-srId");
        $result = URPUtilities::delete_user_SRP( $storageResourceId );
        if( $result )
        {
            return Redirect::to("account/user-storage-resources")->with("message","Storage Resource Account Settings have been deleted.");
        }
    }

    public function getUserProfile() {

        $userProfile = UserProfileUtilities::get_user_profile(Session::get("username"));
        return View::make("account/user-profile", array(
            "userProfile" => $userProfile
        ));
    }

    public function updateUserProfile() {

        $username = Session::get('username');
        $userProfile = UserProfileUtilities::get_user_profile($username);

        // Copy data from form to $userProfile object and update
        $userProfile->firstName = Input::get("firstName");
        $userProfile->lastName = Input::get("lastName");
        $userProfile->homeOrganization = Input::get("homeOrganization");
        try {
            UserProfileUtilities::update_user_profile($userProfile);
            // Now update the UserProfile in the Session
            $userProfile = UserProfileUtilities::get_user_profile($username);
            Session::put("user-profile", $userProfile);
            return Redirect::to("account/user-profile")->with("message", "Your profile has been updated.");
        } catch (Exception $e) {
            return View::make("account/user-profile", array(
                "userProfile" => $userProfile,
                "errorMessage" => "An error occurred while trying to update your profile: " . $e->getMessage()
            ));
        }

    }

    public function showUpdateEmailView() {
        try {
            $userProfile = UserProfileUtilities::get_user_profile(Session::get("username"));
        } catch (Exception $e) {
            Log::error("Failed to retrieve user profile. Error: " . $e->getMessage());
            return View::make("account/user-profile-update-email", array(
                "email" => null
            ));
        }
        return View::make("account/user-profile-update-email", array(
            "email" => $userProfile->emails[0]
        ));
    }

    public function submitUpdateEmail() {

        try {
            $username = Session::get("username");
            $newEmail = Input::get("newEmail");
            $user_profile = UserProfileUtilities::get_user_profile($username);
            EmailUtilities::sendVerifyUpdatedEmailAccount($username, $user_profile->firstName, $user_profile->lastName, $newEmail);
            Session::put("UserSettingsController_newEmail", $newEmail);
            return Redirect::to("account/user-profile")->with("message",
                "Confirmation email has been sent to " . htmlspecialchars($newEmail)
                . ". Please click on the confirmation link in the email once you receive it.");
        } catch (Exception $e) {
            return View::make("account/user-profile-update-email", array(
                "email" => Input::get("newEmail"),
                "errorMessage" => "An error occurred while trying to submit updated email address: " . $e->getMessage()
            ));
        }
    }

    public function confirmUpdateEmail() {

        try {
            $username = Input::get("username");
            $code = Input::get("code");

            $verified = EmailUtilities::verifyUpdatedEmailAccount($username, $code);
            if ($verified) {
                $newEmail = Session::get("UserSettingsController_newEmail");
                if (empty($newEmail)) {
                    throw new Exception("New email not found in session");
                }
                $user_profile = UserProfileUtilities::get_user_profile($username);
                $user_profile->emails = array($newEmail);
                $result = UserProfileUtilities::update_user_profile($user_profile);
                if ($result) {
                    return Redirect::to("account/user-profile")->with(
                        "message", "Email address updated successfully");
                } else {
                    return Redirect::to("account/user-profile-update-email")->with(
                        "errorMessage", "Failed to update email address, please try again.");
                }
            } else {
                return Redirect::to("account/user-profile-update-email")->with(
                    "errorMessage", "Failed to update email address, please try again. Reason: confirmation link was not verified successfully.");
            }
        } catch (Exception $e) {
            Log::error("Failed to update email address", array(Input::all()));
            Log::error($e);
            return Redirect::to("account/user-profile-update-email")->with(
                "errorMessage", "Failed to update email address, please try again. Reason: " . $e->getMessage());
        }
    }
}
