<?php
namespace Wsis\Stubs;
use SoapClient;
class CaptchaInfoBean {
  public $imagePath; // string
  public $secretKey; // string
  public $userAnswer; // string
}

class UserInformationRecoveryServiceIdentityMgtServiceException {
  public $IdentityMgtServiceException; // IdentityMgtServiceException
}

class updatePassword {
  public $username; // string
  public $confirmationCode; // string
  public $newPassword; // string
}

class updatePasswordResponse {
  public $return; // VerificationBean
}

class verifyConfirmationCode {
  public $username; // string
  public $code; // string
  public $captcha; // CaptchaInfoBean
}

class verifyConfirmationCodeResponse {
  public $return; // VerificationBean
}

class getUserChallengeQuestion {
  public $userName; // string
  public $confirmation; // string
  public $questionId; // string
}

class getUserChallengeQuestionResponse {
  public $return; // UserChallengesDTO
}

class getUserChallengeQuestionIds {
  public $username; // string
  public $confirmation; // string
}

class getUserChallengeQuestionIdsResponse {
  public $return; // ChallengeQuestionIdsDTO
}

class getAllChallengeQuestions {
}

class getAllChallengeQuestionsResponse {
  public $return; // ChallengeQuestionDTO
}

class verifyUserChallengeAnswer {
  public $userName; // string
  public $confirmation; // string
  public $questionId; // string
  public $answer; // string
}

class verifyUserChallengeAnswerResponse {
  public $return; // VerificationBean
}

class verifyUser {
  public $username; // string
  public $captcha; // CaptchaInfoBean
}

class verifyUserResponse {
  public $return; // VerificationBean
}

class sendRecoveryNotification {
  public $username; // string
  public $key; // string
  public $notificationType; // string
}

class sendRecoveryNotificationResponse {
  public $return; // VerificationBean
}

class getCaptcha {
}

class getCaptchaResponse {
  public $return; // CaptchaInfoBean
}

class UserInformationRecoveryServiceIdentityException {
  public $IdentityException; // IdentityException
}

class getUserIdentitySupportedClaims {
  public $dialect; // string
}

class getUserIdentitySupportedClaimsResponse {
  public $return; // UserIdentityClaimDTO
}

class verifyAccount {
  public $claims; // UserIdentityClaimDTO
  public $captcha; // CaptchaInfoBean
  public $tenantDomain; // string
}

class verifyAccountResponse {
  public $return; // VerificationBean
}

class registerUser {
  public $userName; // string
  public $password; // string
  public $claims; // UserIdentityClaimDTO
  public $profileName; // string
  public $tenantDomain; // string
}

class registerUserResponse {
  public $return; // VerificationBean
}

class confirmUserSelfRegistration {
  public $username; // string
  public $code; // string
  public $captcha; // CaptchaInfoBean
  public $tenantDomain; // string
}

class confirmUserSelfRegistrationResponse {
  public $return; // VerificationBean
}

class NotificationDataDTO {
  public $domainName; // string
  public $firstName; // string
  public $notification; // string
  public $notificationAddress; // string
  public $notificationCode; // string
  public $notificationSent; // boolean
  public $notificationSubject; // string
  public $notificationType; // string
  public $userId; // string
}

class UserChallengesDTO {
  public $answer; // string
  public $error; // string
  public $id; // string
  public $key; // string
  public $order; // int
  public $primary; // boolean
  public $question; // string
  public $verfied; // boolean
}

class ChallengeQuestionIdsDTO {
  public $error; // string
  public $ids; // string
  public $key; // string
}

class ChallengeQuestionDTO {
  public $order; // int
  public $promoteQuestion; // boolean
  public $question; // string
  public $questionSetId; // string
}

class UserIdentityClaimDTO {
  public $claimUri; // string
  public $claimValue; // string
}

class VerificationBean {
  public $error; // string
  public $key; // string
  public $notificationData; // NotificationDataDTO
  public $redirectPath; // string
  public $userId; // string
  public $verified; // boolean
}

class IdentityException {
}

class IdentityMgtServiceException {
}


/**
 * UserInformationRecoveryStub class
 * 
 *  
 * 
 * @author    {author}
 * @copyright {copyright}
 * @package   {package}
 */
class UserInformationRecoveryStub extends SoapClient {

  private static $classmap = array(
                                    'CaptchaInfoBean' => 'CaptchaInfoBean',
                                    'UserInformationRecoveryServiceIdentityMgtServiceException' => 'UserInformationRecoveryServiceIdentityMgtServiceException',
                                    'updatePassword' => 'updatePassword',
                                    'updatePasswordResponse' => 'updatePasswordResponse',
                                    'verifyConfirmationCode' => 'verifyConfirmationCode',
                                    'verifyConfirmationCodeResponse' => 'verifyConfirmationCodeResponse',
                                    'getUserChallengeQuestion' => 'getUserChallengeQuestion',
                                    'getUserChallengeQuestionResponse' => 'getUserChallengeQuestionResponse',
                                    'getUserChallengeQuestionIds' => 'getUserChallengeQuestionIds',
                                    'getUserChallengeQuestionIdsResponse' => 'getUserChallengeQuestionIdsResponse',
                                    'getAllChallengeQuestions' => 'getAllChallengeQuestions',
                                    'getAllChallengeQuestionsResponse' => 'getAllChallengeQuestionsResponse',
                                    'verifyUserChallengeAnswer' => 'verifyUserChallengeAnswer',
                                    'verifyUserChallengeAnswerResponse' => 'verifyUserChallengeAnswerResponse',
                                    'verifyUser' => 'verifyUser',
                                    'verifyUserResponse' => 'verifyUserResponse',
                                    'sendRecoveryNotification' => 'sendRecoveryNotification',
                                    'sendRecoveryNotificationResponse' => 'sendRecoveryNotificationResponse',
                                    'getCaptcha' => 'getCaptcha',
                                    'getCaptchaResponse' => 'getCaptchaResponse',
                                    'UserInformationRecoveryServiceIdentityException' => 'UserInformationRecoveryServiceIdentityException',
                                    'getUserIdentitySupportedClaims' => 'getUserIdentitySupportedClaims',
                                    'getUserIdentitySupportedClaimsResponse' => 'getUserIdentitySupportedClaimsResponse',
                                    'verifyAccount' => 'verifyAccount',
                                    'verifyAccountResponse' => 'verifyAccountResponse',
                                    'registerUser' => 'registerUser',
                                    'registerUserResponse' => 'registerUserResponse',
                                    'confirmUserSelfRegistration' => 'confirmUserSelfRegistration',
                                    'confirmUserSelfRegistrationResponse' => 'confirmUserSelfRegistrationResponse',
                                    'NotificationDataDTO' => 'NotificationDataDTO',
                                    'UserChallengesDTO' => 'UserChallengesDTO',
                                    'ChallengeQuestionIdsDTO' => 'ChallengeQuestionIdsDTO',
                                    'ChallengeQuestionDTO' => 'ChallengeQuestionDTO',
                                    'UserIdentityClaimDTO' => 'UserIdentityClaimDTO',
                                    'VerificationBean' => 'VerificationBean',
                                    'IdentityException' => 'IdentityException',
                                    'IdentityMgtServiceException' => 'IdentityMgtServiceException',
                                   );

  public function UserInformationRecoveryService($wsdl = "UserInformationRecoveryService.xml", $options = array()) {
    foreach(self::$classmap as $key => $value) {
      if(!isset($options['classmap'][$key])) {
        $options['classmap'][$key] = $value;
      }
    }
    parent::__construct($wsdl, $options);
  }

  /**
   *  
   *
   * @param getAllChallengeQuestions $parameters
   * @return getAllChallengeQuestionsResponse
   */
  public function getAllChallengeQuestions(getAllChallengeQuestions $parameters) {
    return $this->__soapCall('getAllChallengeQuestions', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param sendRecoveryNotification $parameters
   * @return sendRecoveryNotificationResponse
   */
  public function sendRecoveryNotification(sendRecoveryNotification $parameters) {
    return $this->__soapCall('sendRecoveryNotification', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getCaptcha $parameters
   * @return getCaptchaResponse
   */
  public function getCaptcha(getCaptcha $parameters) {
    return $this->__soapCall('getCaptcha', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param verifyConfirmationCode $parameters
   * @return verifyConfirmationCodeResponse
   */
  public function verifyConfirmationCode(verifyConfirmationCode $parameters) {
    return $this->__soapCall('verifyConfirmationCode', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getUserChallengeQuestionIds $parameters
   * @return getUserChallengeQuestionIdsResponse
   */
  public function getUserChallengeQuestionIds(getUserChallengeQuestionIds $parameters) {
    return $this->__soapCall('getUserChallengeQuestionIds', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getUserChallengeQuestion $parameters
   * @return getUserChallengeQuestionResponse
   */
  public function getUserChallengeQuestion(getUserChallengeQuestion $parameters) {
    return $this->__soapCall('getUserChallengeQuestion', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param confirmUserSelfRegistration $parameters
   * @return confirmUserSelfRegistrationResponse
   */
  public function confirmUserSelfRegistration(confirmUserSelfRegistration $parameters) {
    return $this->__soapCall('confirmUserSelfRegistration', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param verifyUserChallengeAnswer $parameters
   * @return verifyUserChallengeAnswerResponse
   */
  public function verifyUserChallengeAnswer(verifyUserChallengeAnswer $parameters) {
    return $this->__soapCall('verifyUserChallengeAnswer', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param updatePassword $parameters
   * @return updatePasswordResponse
   */
  public function updatePassword(updatePassword $parameters) {
    return $this->__soapCall('updatePassword', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getUserIdentitySupportedClaims $parameters
   * @return getUserIdentitySupportedClaimsResponse
   */
  public function getUserIdentitySupportedClaims(getUserIdentitySupportedClaims $parameters) {
    return $this->__soapCall('getUserIdentitySupportedClaims', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param registerUser $parameters
   * @return registerUserResponse
   */
  public function registerUser(registerUser $parameters) {
    return $this->__soapCall('registerUser', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param verifyUser $parameters
   * @return verifyUserResponse
   */
  public function verifyUser(verifyUser $parameters) {
    return $this->__soapCall('verifyUser', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param verifyAccount $parameters
   * @return verifyAccountResponse
   */
  public function verifyAccount(verifyAccount $parameters) {
    return $this->__soapCall('verifyAccount', array($parameters),       array(
            'uri' => 'http://services.mgt.identity.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

}

?>
