<?php
namespace Wsis\Stubs;

use Wsis\Stubs\UserProfileManagerStub;

/**
 * UserProfileManager class
 *
 * This class provide an easy to use interface for
 * WSO2 IS 5.0.0 UserProfileMgt service.
 */
class UserProfileManager {
    /**
     * @var UserProfileManagerStub $serviceStub
     * @access private
     */
    private $serviceStub;

    public function __construct($server_url, $options) {
        $this->serviceStub = new UserProfileManagerStub(
            $server_url . "services/UserProfileMgtService?wsdl", $options
        );
    }

    /**
     * Function to get the soap client
     *
     * @return SoapClient
     */
    public function getSoapClient(){
        return $this->serviceStub;
    }

    public function updateUserProfile($username, $email, $firstName, $lastName) {
        $setUserProfile = new setUserProfile();
        $setUserProfile->username = $username;

        $profile = new UserProfileDTO();
        $fieldValues = array();

        $usernameDTO = new UserFieldDTO();
        $usernameDTO->claimUri = "http://wso2.org/claims/username";
        $usernameDTO->fieldValue = $username;
        array_push($fieldValues, $usernameDTO);

        $emailDTO = new UserFieldDTO();
        $emailDTO->claimUri = "http://wso2.org/claims/emailaddress";
        $emailDTO->fieldValue = $email;
        array_push($fieldValues, $emailDTO);

        $firstNameDTO = new UserFieldDTO();
        $firstNameDTO->claimUri = "http://wso2.org/claims/givenname";
        $firstNameDTO->fieldValue = $firstName;
        array_push($fieldValues, $firstNameDTO);

        $lastNameDTO = new UserFieldDTO();
        $lastNameDTO->claimUri = "http://wso2.org/claims/lastname";
        $lastNameDTO->fieldValue = $lastName;
        array_push($fieldValues, $lastNameDTO);

        $profile->fieldValues = $fieldValues;
        $profile->profileName = "default";
        $setUserProfile->profile = $profile;

        $this->serviceStub->setUserProfile($setUserProfile);
    }

    public function getUserProfile($username) {
        $getUserProfile = new getUserProfile();
        $getUserProfile->username = $username;
        $getUserProfile->profileName = "default";

        $userProfile = $this->serviceStub->getUserProfile($getUserProfile);
        $result  = array();
        foreach($userProfile->return->fieldValues as $fieldValue){
            if($fieldValue->claimUri == "http://wso2.org/claims/emailaddress"){
                $result["email"] = $fieldValue->fieldValue;
            }else if($fieldValue->claimUri == "http://wso2.org/claims/givenname"){
                $result["firstname"] = $fieldValue->fieldValue;
            }else if($fieldValue->claimUri == "http://wso2.org/claims/lastname"){
                $result["lastname"] = $fieldValue->fieldValue;
            }
        }
        return $result;
    }
}
?>
