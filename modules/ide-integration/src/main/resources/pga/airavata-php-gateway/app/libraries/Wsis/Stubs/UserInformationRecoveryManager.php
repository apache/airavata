<?php
namespace Wsis\Stubs;

use Illuminate\Support\Facades\Config;
use Wsis\Stubs\UserInformationRecoveryStub;

/**
 * UserInformationRecoveryManager class
 * 
 * This class provide an easy to use interface for
 * WSO2 IS 5.0.0 TenantMgtAdmin service.
 */
class UserInformationRecoveryManager {
    /**
     * @var UserInformationRecoveryStub $serviceStub
     * @access private
     */
    private $serviceStub;

    public function __construct($server_url, $options) {
        $this->serviceStub = new UserInformationRecoveryStub(
                $server_url . "services/UserInformationRecoveryService?wsdl", $options
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

    /**
     * Method to validate username and get key which is to be used for the next call
     * @param $username
     */
    public function validateUsername($userAnswer, $imagePath, $secretKey, $username){
        $verifyUser = new verifyUser();
        $verifyUser->username = $username;
        $captcha = new CaptchaInfoBean();
        $captcha->userAnswer = $userAnswer;
        $captcha->imagePath = $imagePath;
        $captcha->secretKey = $secretKey;
        $verifyUser->captcha = $captcha;
        $result = $this->serviceStub->verifyUser($verifyUser);
        if($result->return->verified){
            return $result->return->key;
        }
    }

    /**
     * Method to send password reset notification
     * @param $username
     * @param $key
     * @return mixed
     */
    public function sendPasswordResetNotification($username, $key){
        $recoveryNotification = new sendRecoveryNotification();
        $recoveryNotification->username = $username;
        $recoveryNotification->key = $key;
        $result = $this->serviceStub->sendRecoveryNotification($recoveryNotification);
        return $result->return->verified;
    }

    /**
     * Method to validate confirmation code
     * @param $username
     * @param $confirmation
     */
    public function validateConfirmationCode($username, $confirmation){
        $verifyConfirmationCode = new verifyConfirmationCode();
        $verifyConfirmationCode->username = $username;
        $verifyConfirmationCode->code = $confirmation;
        $result = $this->serviceStub->verifyConfirmationCode($verifyConfirmationCode);
        if($result->return->verified){
            return $result->return->key;
        }
    }

    /**
     * Method to reset user password
     * @param $username
     * @param $newPassword
     * @param $key
     * @return mixed
     */
    public function resetPassword($username, $newPassword, $key){
        $updatePassword = new updatePassword();
        $updatePassword->username = $username;
        $updatePassword->confirmationCode = $key;
        $updatePassword->newPassword = $newPassword;
        $result = $this->serviceStub->updatePassword($updatePassword);
        return $result->return->verified;
    }


    /**
     * Function to create a user account. This user account is not activate unless activated by the user
     * via email
     * @param $username
     * @param $password
     * @param $email
     * @param $firstName
     * @param $lastName
     * @param $tenantDomain
     * @return mixed
     */
    public function registerAccount($username, $password, $email,$firstName, $lastName, $organization, $address, $country, $telephone, $mobile, $im, $url, $tenantDomain){

        $registerUser =  new registerUser();
        $registerUser->userName  = $username;
        $registerUser->password = $password;
        $registerUser->profileName = "default";
        $registerUser->tenantDomain = $tenantDomain;

        $fieldValues = array();
        $usernameDTO = new UserIdentityClaimDTO();
        $usernameDTO->claimUri = "http://wso2.org/claims/username";
        $usernameDTO->claimValue = $username;
        array_push($fieldValues, $usernameDTO);

        $emailDTO = new UserIdentityClaimDTO;
        $emailDTO->claimUri = "http://wso2.org/claims/emailaddress";
        $emailDTO->claimValue = $email;
        array_push($fieldValues, $emailDTO);

        $firstNameDTO = new UserIdentityClaimDTO();
        $firstNameDTO->claimUri = "http://wso2.org/claims/givenname";
        $firstNameDTO->claimValue = $firstName;
        array_push($fieldValues, $firstNameDTO);

        $lastNameDTO = new UserIdentityClaimDTO();
        $lastNameDTO->claimUri = "http://wso2.org/claims/lastname";
        $lastNameDTO->claimValue = $lastName;
        array_push($fieldValues, $lastNameDTO);
        $registerUser->claims = $fieldValues;

        $lastNameDTO = new UserIdentityClaimDTO();
        $lastNameDTO->claimUri = "http://wso2.org/claims/lastname";
        $lastNameDTO->claimValue = $lastName;
        array_push($fieldValues, $lastNameDTO);
        $registerUser->claims = $fieldValues;

        //Todo Add other information too
        if(!empty($organization)){
            $organizationDTO = new UserIdentityClaimDTO();
            $organizationDTO->claimUri = "http://wso2.org/claims/organization";
            $organizationDTO->claimValue = $organization;
            array_push($fieldValues, $organizationDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($address)){
            $addressDTO = new UserIdentityClaimDTO();
            $addressDTO->claimUri = "http://wso2.org/claims/streetaddress";
            $addressDTO->claimValue = $address;
            array_push($fieldValues, $addressDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($country)){
            $countryDTO = new UserIdentityClaimDTO();
            $countryDTO->claimUri = "http://wso2.org/claims/country";
            $countryDTO->claimValue = $country;
            array_push($fieldValues, $countryDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($telephone)){
            $telephoneDTO = new UserIdentityClaimDTO();
            $telephoneDTO->claimUri = "http://wso2.org/claims/telephone";
            $telephoneDTO->claimValue = $telephone;
            array_push($fieldValues, $telephoneDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($mobile)){
            $mobileDTO = new UserIdentityClaimDTO();
            $mobileDTO->claimUri = "http://wso2.org/claims/mobile";
            $mobileDTO->claimValue = $mobile;
            array_push($fieldValues, $mobileDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($im)){
            $imDTO = new UserIdentityClaimDTO();
            $imDTO->claimUri = "http://wso2.org/claims/im";
            $imDTO->claimValue = $im;
            array_push($fieldValues, $imDTO);
            $registerUser->claims = $fieldValues;
        }

        if(!empty($url)){
            $urlDTO = new UserIdentityClaimDTO();
            $urlDTO->claimUri = "http://wso2.org/claims/url";
            $urlDTO->claimValue = $url;
            array_push($fieldValues, $urlDTO);
            $registerUser->claims = $fieldValues;
        }

        $result = $this->serviceStub->registerUser($registerUser);
        return $result->return->verified;
    }


    /**
     * Function to confirm user registration
     * @param $userName
     * @param $tenantDomain
     */
    public function confirmUserRegistration($userName, $code, $tenantDomain){
        $confirmUserSelfRegistration = new confirmUserSelfRegistration();
        $confirmUserSelfRegistration->username = $userName;
        $confirmUserSelfRegistration->code = $code;
        $confirmUserSelfRegistration->tenantDomain = $tenantDomain;
        $confirmUserSelfRegistration->captcha = new CaptchaInfoBean();
//        $confirmUserSelfRegistration->captcha->userAnswer = $userAnswer;
//        $confirmUserSelfRegistration->captcha->imagePath = $imagePath;
//        $confirmUserSelfRegistration->captcha->secretKey = $secret;
        $result = $this->serviceStub->confirmUserSelfRegistration($confirmUserSelfRegistration);
        return $result->return;
    }

    /**
     * Public function to get Capatcha
     * @return getCaptchaResponse
     */
    public function getCapatcha(){
        return $this->serviceStub->getCaptcha(new getCaptcha());
    }
}
