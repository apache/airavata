<?php

class AccountController extends BaseController
{
    const PASSWORD_VALIDATION = "required|min:6|max:48|regex:/^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@!$#*&]).*$/";
    const PASSWORD_VALIDATION_MESSAGE = "Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$&*";

    public function __construct()
    {
        Session::put("nav-active", "user-dashboard");
    }

    public function createAccountView()
    {
        $auth_options = Config::get('pga_config.wsis')['auth-options'];
        $auth_password_option = CommonUtilities::getAuthPasswordOption();
        if ($auth_password_option == null) {
            return Redirect::to("login");
        }
        $auth_code_options = CommonUtilities::getAuthCodeOptions();
        return View::make('account/create', array(
            "auth_password_option" => $auth_password_option,
            "auth_code_options" => $auth_code_options));
    }

    public function createAccountSubmit()
    {
        $rules = array(
            "username" => "required|min:6|regex:/^[a-z0-9_-]+$/",
            "password" => self::PASSWORD_VALIDATION,
            "confirm_password" => "required|same:password",
            "email" => "required|email",
            "confirm_email" => "required|same:email",
        );

        $messages = array(
            'username.regex' => "Username can only contain lowercase letters, numbers, underscores and hyphens.",
            'password.regex' => self::PASSWORD_VALIDATION_MESSAGE,
        );

        $validator = Validator::make(Input::all(), $rules, $messages);
        if ($validator->fails()) {
            return Redirect::to("create")
                ->withInput(Input::except('password', 'confirm_password', 'email', 'confirm_email'))
                ->withErrors($validator);
        }

        $first_name = $_POST['first_name'];
        $last_name = $_POST['last_name'];
        $username = $_POST['username'];
        $password = $_POST['password'];
        $email = $_POST['email'];

        if (Keycloak::usernameExists($username)) {
            return Redirect::to("create")
                ->withInput(Input::except('password', 'confirm_password'))
                ->with("username_exists", true);
        } else {

            IamAdminServicesUtilities::registerUser($username, $email, $first_name, $last_name, $password);

            // add user to initial role
            IamAdminServicesUtilities::addInitialRoleToUser($username);
            // Send account confirmation email
            EmailUtilities::sendVerifyEmailAccount($username, $first_name, $last_name, $email);

            CommonUtilities::print_success_message('Account confirmation request was sent to your email account');
            return View::make('home');
        }
    }

    public function loginView()
    {
        // If user already logged in, redirect to home
        if(CommonUtilities::id_in_session()){
            return Redirect::to("home");
        }

        // Only support for one password option
        $auth_password_option = CommonUtilities::getAuthPasswordOption();
        // Support for many external identity providers (authorization code auth flow)
        $auth_code_options = CommonUtilities::getAuthCodeOptions();
        $has_auth_code_and_password_options = $auth_password_option != null && count($auth_code_options) > 0;

        // If no username/password option and only one external identity
        // provider, just redirect immediately
        if ($auth_password_option == null && count($auth_code_options) == 1) {
            return Redirect::away($auth_code_options[0]["auth_url"]);
        } else {
            return View::make('account/login', array(
                "auth_password_option" => $auth_password_option,
                "auth_code_options" => $auth_code_options,
                "has_auth_code_and_password_options" => $has_auth_code_and_password_options,
            ));
        }
    }

    public function loginDesktopView()
    {
        // Only support for one password option
        $auth_password_option = CommonUtilities::getAuthPasswordOption();
        // Support for many external identity providers (authorization code auth flow)
        $auth_code_options = CommonUtilities::getAuthCodeOptions();
        $has_auth_code_and_password_options = $auth_password_option != null && count($auth_code_options) > 0;

        // If no username/password option and only one external identity
        // provider, just redirect immediately
        if ($auth_password_option == null && count($auth_code_options) == 1) {
            return Redirect::away($auth_code_options[0]["auth_url"]);
        } else {
            return View::make('account/login-desktop', array(
                "auth_password_option" => $auth_password_option,
                "auth_code_options" => $auth_code_options,
                "has_auth_code_and_password_options" => $has_auth_code_and_password_options,
            ));
        }
    }

    public function loginSubmit()
    {
        if (CommonUtilities::form_submitted()) {
            $username = strtolower(Input::get("username"));

            $password = $_POST['password'];
            $response = Keycloak::authenticate($username, $password);
            if(!isset($response->access_token)){
                if (Keycloak::isUpdatePasswordRequired($username)) {
                    return Redirect::to("login" . '?status=failed')->with("update-password-required", true);
                } else {
                    return Redirect::to("login". '?status=failed')->with("invalid-credentials", true);
                }
            }

            $accessToken = $response->access_token;
            $refreshToken = $response->refresh_token;
            $expirationTime = time() + $response->expires_in - 300; // 5 minutes safe margin

            $userProfile = Keycloak::getUserProfileFromOAuthToken($accessToken);
            Session::put("iam-user-profile", $userProfile);
            $username = $userProfile['username'];
            $userRoles = $userProfile['roles'];
            $userEmail = $userProfile["email"];
            $firstName = $userProfile["firstname"];
            $lastName = $userProfile["lastname"];

            $authzToken = new Airavata\Model\Security\AuthzToken();
            $authzToken->accessToken = $accessToken;
            $authzToken->claimsMap['gatewayID'] = Config::get('pga_config.airavata')['gateway-id'];
            $authzToken->claimsMap['userName'] = $username;

            Session::put('authz-token',$authzToken);
            Session::put('oauth-refresh-code',$refreshToken);
            Session::put('oauth-expiration-time',$expirationTime);

            Session::put("roles", $userRoles);
            if (in_array(Config::get('pga_config.wsis')['admin-role-name'], $userRoles)) {
                Session::put("admin", true);
            }
            if (in_array(Config::get('pga_config.wsis')['read-only-admin-role-name'], $userRoles)) {
                Session::put("authorized-user", true);
                Session::put("admin-read-only", true);
            }
            if (in_array(Config::get('pga_config.wsis')['user-role-name'], $userRoles)) {
                Session::put("authorized-user", true);
            }
            //gateway-provider-code
            if (in_array("gateway-provider", $userRoles)) {
                Session::put("gateway-provider", true);
            }
            //only for super admin
            if(  Config::get('pga_config.portal')['super-admin-portal'] == true && Session::has("admin")){
                Session::put("super-admin", true);
            }
            CommonUtilities::store_id_in_session($username);
            Session::put("gateway_id", Config::get('pga_config.airavata')['gateway-id']);

            if(Session::has("admin") || Session::has("admin-read-only") || Session::has("authorized-user") || Session::has("gateway-provider")){
                return $this->initializeWithAiravata($username, $userEmail, $firstName, $lastName, $accessToken,
                    $refreshToken, $expirationTime);
            }

            return Redirect::to("account/dashboard" . "?status=less_privileged&code=".$accessToken . "&username=".$username
                . "&refresh_code=" . $refreshToken . "&valid_time=" . $expirationTime);
        }

    }

    public function apiLoginSubmit() {
        $username = strtolower(Input::get("username"));
        $password = Input::get("password");
        $response = Keycloak::authenticate($username, $password);
        return Response::json($response);
    }

    public function oauthCallback()
    {
        if (!isset($_GET["code"])) {
            return Redirect::to('home');
        }

        $code = $_GET["code"];
        $response = Keycloak::getOAuthToken($code);
        if(!isset($response->access_token)){
            return Redirect::to('home');
        }

        $accessToken = $response->access_token;
        $refreshToken = $response->refresh_token;
        $expirationTime = time() + $response->expires_in - 300; //5 minutes safe margin

        $userProfile = Keycloak::getUserProfileFromOAuthToken($accessToken);
        Log::debug("userProfile", array($userProfile));
        Session::put("iam-user-profile", $userProfile);
        $username = $userProfile['username'];
        $userRoles = $userProfile['roles'];
        $userEmail = $userProfile['email'];
        $firstName = $userProfile['firstname'];
        $lastName = $userProfile['lastname'];

        # As a workaround to figuring out if the user is logging in for the first
        # time, if the user has no roles, assume they are logging in for the first
        # time and add them to the initial role
        if (!$this->hasAnyRoles($userRoles)){
            IamAdminServicesUtilities::addInitialRoleToUser($username);
            # Reload the roles
            $userProfile = Keycloak::getUserProfileFromOAuthToken($accessToken);
            $userRoles = $userProfile['roles'];
            # Notify admin
            $this->sendAccountCreationNotification2Admin($username);
        }

        $authzToken = new Airavata\Model\Security\AuthzToken();
        $authzToken->accessToken = $accessToken;
        $authzToken->claimsMap = array('userName'=>$username, 'gatewayID'=> Config::get('pga_config.airavata')['gateway-id']);
        Session::put('authz-token',$authzToken);
        Session::put('oauth-refresh-code',$refreshToken);
        Session::put('oauth-expiration-time',$expirationTime);

        Session::put("roles", $userRoles);
        if (in_array(Config::get('pga_config.wsis')['admin-role-name'], $userRoles)) {
            Session::put("admin", true);
        }
        if (in_array(Config::get('pga_config.wsis')['read-only-admin-role-name'], $userRoles)) {
            Session::put("admin-read-only", true);
        }
        if (in_array(Config::get('pga_config.wsis')['user-role-name'], $userRoles)) {
            Session::put("authorized-user", true);
        }
        //gateway-provider-code
        if (in_array("gateway-provider", $userRoles)) {
            Session::put("gateway-provider", true);
        }
        //only for super admin
        if(  Config::get('pga_config.portal')['super-admin-portal'] == true && Session::has("admin")){
            Session::put("super-admin", true);
        }

        CommonUtilities::store_id_in_session($username);
        Session::put("gateway_id", Config::get('pga_config.airavata')['gateway-id']);

        if(Session::has("admin") || Session::has("admin-read-only") || Session::has("authorized-user") || Session::has("gateway-provider")){
            return $this->initializeWithAiravata($username, $userEmail, $firstName, $lastName, $accessToken, $refreshToken, $expirationTime);
        }

        return Redirect::to("account/dashboard" . "?status=less_privileged&code=".$accessToken . "&username=".$username
            . "&refresh_code=" . $refreshToken . "&valid_time=" . $expirationTime);

    }

    private function hasAnyRoles($roles) {
        return in_array("gateway-provider", $roles)
            or in_array("user-pending", $roles)
            or in_array(Config::get('pga_config.wsis')['admin-role-name'], $roles)
            or in_array(Config::get('pga_config.wsis')['read-only-admin-role-name'], $roles)
            or in_array(Config::get('pga_config.wsis')['user-role-name'], $roles)
            or in_array(Config::get('pga_config.wsis')['initial-role-name'], $roles);
    }
    private function initializeWithAiravata($username, $userEmail, $firstName, $lastName, $accessToken, $refreshToken, $validTime){

        // Log the user out if Airavata is down. If a new user we want to make
        // sure we create the default project and setup experiment storage
        // before they do anything else.
        if (!CommonUtilities::isAiravataUp()) {
            Session::flush();
            return Redirect::to("home")->with("airavata-down", true);
        }

        // Create basic user profile if it doesn't exist
        if (!UserProfileUtilities::does_user_profile_exist($username)) {
            UserProfileUtilities::create_basic_user_profile($username, $userEmail, $firstName, $lastName);
        }
        $userProfile = UserProfileUtilities::get_user_profile($username);
        Session::put('user-profile', $userProfile);

        //creating a default project for user
        $projects = ProjectUtilities::get_all_user_projects(Config::get('pga_config.airavata')['gateway-id'], $username);
        if($projects == null || count($projects) == 0){
            //creating a default project for user
            ProjectUtilities::create_default_project($username);
        }

        $dirPath = Config::get('pga_config.airavata')['experiment-data-absolute-path'] . "/" . Session::get('username');
        if(!file_exists($dirPath)){
            $old_umask = umask(0);
            mkdir($dirPath, 0777, true);
            umask($old_umask);
        }

        // Must create the UserResourceProfile before we can add auto provisioned accounts to it
        $user_resource_profile = URPUtilities::get_or_create_user_resource_profile();
        $auto_provisioned_accounts = URPUtilities::setup_auto_provisioned_accounts();
        // Log::debug("auto_provisioned_accounts", array($auto_provisioned_accounts));

        if(Session::has("admin") || Session::has("admin-read-only") || Session::has("gateway-provider")){
            $response = Redirect::to("admin/dashboard". "?status=ok&code=".$accessToken . "&username=".$username
                . "&refresh_code=" . $refreshToken . "&valid_time=" . $validTime);
            if (!empty($auto_provisioned_accounts)) {
                $response = $response->with("auto_provisioned_accounts", $auto_provisioned_accounts);
            }
            return $response;
        }else{
            $response = Redirect::to("account/dashboard". "?status=ok&code=".$accessToken ."&username=".$username
                . "&refresh_code=" . $refreshToken . "&valid_time=" . $validTime);
            if (!empty($auto_provisioned_accounts)) {
                $response = $response->with("auto_provisioned_accounts", $auto_provisioned_accounts);
            }
            return $response;
        }
    }

    public function forgotPassword()
    {
//        $capatcha = WSIS::getCapatcha()->return;
        return View::make("account/forgot-password");
    }

    public function forgotPasswordSubmit()
    {
        $username = strtolower(Input::get("username"));
        if(empty($username)){
            CommonUtilities::print_error_message("Please provide a valid username");
            return View::make("account/forgot-password");
        }else{
            try{
                $user_profile = Keycloak::getUserProfile($username);
                EmailUtilities::sendPasswordResetEmail($username, $user_profile["firstname"], $user_profile["lastname"], $user_profile["email"]);
                return Redirect::to("forgot-password")->with("forgot-password-success", "Password reset notification was sent to your email account");
            }catch (Exception $ex){
                Log::error($ex);
                return Redirect::to("forgot-password")->with("forgot-password-error", "Password reset operation failed");
            }
        }
    }

    public function dashboard(){

        // dashboard requires that the user is logged in, if not redirect to login
        if (!CommonUtilities::id_in_session()) {
            return Redirect::to("login");
        }

        $userRoles = Session::get("roles");
        if (Session::has("user-profile")) {
            $userEmail = Session::get("user-profile")->emails[0];
        } else {
            $userEmail = Session::get("iam-user-profile")["email"];
        }

        return View::make("account/dashboard");
    }

    public function resetPassword()
    {
        $code = Input::get("code", Input::old("code"));
        $username = Input::get("username", Input::old("username"));
        if(empty($username) || empty($code)){
            return Redirect::to("forgot-password")->with("password-reset-error", "Reset password link failed. Please request to reset user password again.");
        }else{
            return View::make("account/reset-password", array("code" => $code, "username"=>$username, "password_regex_tooltip"=>self::PASSWORD_VALIDATION_MESSAGE));
        }
    }

    public function confirmAccountCreation()
    {
        $code = Input::get("code");
        $username = Input::get("username");
        if(empty($username) || empty($code)){
            return View::make("home");
        }else{
            try{
                $enabled = IamAdminServicesUtilities::isUserEnabled($username);
                if ($enabled) {
                    return Redirect::to("login")->with("account-created-success", "Your account has already been successfully created. Please log in now.");
                } else {
                    $verified = EmailUtilities::verifyEmailVerification($username, $code);
                    if (!$verified){
                        $user_profile = Keycloak::getUserProfile($username);
                        EmailUtilities::sendVerifyEmailAccount($username,
                            $user_profile["firstname"], $user_profile["lastname"], $user_profile["email"]);
                        CommonUtilities::print_error_message("Account confirmation "
                            . "failed! We're sending another confirmation email. "
                            . "Please click the link in the confirmation email that "
                            . "you should be receiving soon.");
                        return View::make("home");
                    }
                    $result = IamAdminServicesUtilities::enableUser($username);
                    if($result){
                        $this->sendAccountCreationNotification2Admin($username);
                        return Redirect::to("login")->with("account-created-success", "Your account has been successfully created. Please log in now.");
                    }else{
                        CommonUtilities::print_error_message("Account confirmation failed! Please contact the Gateway Admin");
                        return View::make("home");
                    }
                }
            }catch (Exception $e){
                CommonUtilities::print_error_message("Account confirmation failed! Please contact the Gateway Admin");
                return View::make("home");
            }
        }
    }

    private function sendAccountCreationNotification2Admin($username){

        $mail = new PHPMailer;

        $mail->isSMTP();
        // Note: setting SMTPDebug will cause output to be dumped into the
        // response, so only enable for testing purposes
        // $mail->SMTPDebug = 3;
        $mail->Host = Config::get('pga_config.portal')['portal-smtp-server-host'];

        $mail->SMTPAuth = true;

        $mail->Username = Config::get('pga_config.portal')['portal-email-username'];
        $mail->Password = Config::get('pga_config.portal')['portal-email-password'];

        $mail->SMTPSecure = "tls";
        $mail->Port = intval(Config::get('pga_config.portal')['portal-smtp-server-port']);

        $mail->From = Config::get('pga_config.portal')['portal-email-username'];
        $gatewayURL = $_SERVER['SERVER_NAME'];
        $portalTitle = Config::get('pga_config.portal')['portal-title'];
        $mail->FromName = "$portalTitle ($gatewayURL)";

        $recipients = Config::get('pga_config.portal')['admin-emails'];
        foreach($recipients as $recipient){
            $mail->addAddress($recipient);
        }

        $mail->isHTML(true);

        $mail->Subject = "New User Account Was Created Successfully";
        $userProfile = Keycloak::getUserProfile($username);
        $wsisConfig = Config::get('pga_config.wsis');
        $tenant = $wsisConfig['tenant-domain'];

        $str = "Gateway Portal: " . $_SERVER['SERVER_NAME'] ."<br/>";
        $str = $str . "Tenant: " . $tenant . "<br/>";
        $str = $str . "Username: " . $username ."<br/>";
        $str = $str . "Name: " . $userProfile["firstname"] . " " . $userProfile["lastname"] . "<br/>";
        $str = $str . "Email: " . $userProfile["email"];

        $mail->Body = $str;
        $mail->send();
    }

    public function resetPasswordSubmit()
    {
        $rules = array(
            "new_password" => self::PASSWORD_VALIDATION,
            "confirm_new_password" => "required|same:new_password",
        );
        $messages = array(
            'new_password.regex' => self::PASSWORD_VALIDATION_MESSAGE,
        );

        $validator = Validator::make(Input::all(), $rules, $messages);
        if ($validator->fails()) {
            Log::debug("validation failed", array($validator->messages()));
            return Redirect::to("reset-password")
                ->withInput(Input::except('new_password', 'confirm_new_password'))
                ->withErrors($validator);
        }

        $code =  $_POST['code'];
        $username =  $_POST['username'];
        $new_password =  $_POST['new_password'];

        try{
            $verified = EmailUtilities::verifyPasswordResetCode($username, $code);
            if (!$verified){
                return Redirect::to("forgot-password")->with("password-reset-error", "Resetting user password operation failed. Please request to reset user password again.");
            }

            $result = IamAdminServicesUtilities::resetUserPassword($username, $new_password);
            if($result){
                return Redirect::to("login")->with("password-reset-success", "User password was reset successfully");
            }else{
                Log::error("Failed to reset password for user $username");
                return Redirect::to("reset-password")
                    ->withInput(Input::except('new_password', 'confirm_new_password'))
                    ->with("password-reset-error", "Resetting user password operation failed");
            }
        }catch (Exception $e){
            Log::error("Failed to reset password for user $username");
            Log::error($e);
            return Redirect::to("reset-password")
                ->withInput(Input::except('new_password', 'confirm_new_password'))
                ->with("password-reset-error", "Resetting user password operation failed");
        }
    }

    public function getRefreshedTokenForDesktop(){
        $refreshToken = Input::get('refresh_code');
        $response = Keycloak::getRefreshedOAuthToken($refreshToken);
        if(isset($response->access_token)){
            $accessToken = $response->access_token;
            $refreshToken = $response->refresh_token;
            $expirationTime = $response->expires_in; // 5 minutes safe margin
            echo json_encode(array('status'=>'ok', 'code'=>$accessToken, 'refresh_code'=>$refreshToken, 'valid_time'=>$expirationTime));
        }else{
            echo json_encode(array('status'=>'failed'));
        }
    }

    public function logout()
    {
       Session::flush();
       return Redirect::away(Keycloak::getOAuthLogoutUrl(URL::to("/")));
    }

    public function allocationRequestView(){
        return View::make("account/request-allocation");
    }

    public function allocationRequestSubmit(){
        return 'result';
    }

    public function noticeSeenAck(){
        Session::put( "notice-count", Input::get("notice-count"));
        Session::put("notice-seen", true);
    }
}
