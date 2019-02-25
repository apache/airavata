<?php

class AdminController extends BaseController {

	public function __construct()
	{
		$this->beforeFilter('verifyadmin');
		Session::put("nav-active", "user-console");
	}

	public function dashboard(){
        $userInfo = array();
        $data = array();
        $userRoles = Session::get("roles");
        $username = Session::get("username");
        if (Session::has("user-profile")) {
            $userEmail = Session::get("user-profile")->emails[0];
        } else {
            $userEmail = Session::get("iam-user-profile")["email"];
        }
        Session::forget("new-gateway-provider");

        //check for gateway provider users
        if( in_array( "gateway-provider", $userRoles) ) {
            $gatewayOfUser = "";
            Session::put("super-admin", true);
            $gatewaysInfo = AdminUtilities::get_gateways_for_requester( $username );
            Log::info("Gateways: ", $gatewaysInfo);
            Log::info("Username: ", [Session::get("username")]);
            //var_dump( $gatewaysInfo); exit;
            $requestedGateways = array();
            $gatewayApprovalStatuses = AdminUtilities::get_gateway_approval_statuses();

            foreach ($gatewaysInfo as $index => $gateway) {
                $gatewayOfUser = $gateway->gatewayId;
                Session::forget("super-admin");
                Session::put("new-gateway-provider", true);
                Session::put("existing-gateway-provider", true);

                $requestedGateways[ $gateway->airavataInternalGatewayId]["gatewayInfo"] = $gateway;
                $requestedGateways[ $gateway->airavataInternalGatewayId]["approvalStatus"] = $gatewayApprovalStatuses[ $gateway->gatewayApprovalStatus];
                //seeing if admin wants to start managing one of the gateways
                if( Input::has("gatewayId")){
                    if( Input::get("gatewayId") == $gateway->gatewayId)
                    {
                        Session::put("gateway_id", $gateway->gatewayId);
                    }
                }
            }
            $data["requestedGateways"] = $requestedGateways;
            $data["gatewayApprovalStatuses"] = $gatewayApprovalStatuses;
            //to make it accessible to navbar
            Session::put("requestedGateways", $requestedGateways);

            if ($gatewayOfUser == "") {
                $userInfo["username"] = $username;
                $userInfo["email"] = $userEmail;
    			$data["userInfo"] = $userInfo;
    			$data["gatewaysInfo"] = $gatewaysInfo;
                Session::put("new-gateway-provider", true);
            }
        }

		return View::make("account/dashboard", $data);
	}

	public function addAdminSubmit(){
		WSIS::update_user_roles( Input::get("username"), array( "new"=>array("admin"), "deleted"=>array() ) );

   		return View::make("account/admin-dashboard")->with("message", "User has been added to Admin.");
	}

	public function usersView(){
		if( Input::has("role"))
		{
			Session::flash("warning-message", "Please note: the following list "
			. "may not be complete. Only the most recent 100 users have been "
			. "searched for role " . htmlspecialchars(Input::get("role")) . ".");
			$users = IamAdminServicesUtilities::getUsersWithRole(Input::get("role"));
		}
		else
			$users =  Keycloak::listUsers();

		$roles = Keycloak::getAllRoles();
		sort($roles);
		Session::put("admin-nav", "manage-users");
		return View::make("admin/manage-users", array("users" => $users, "roles" => $roles));
	}

	public function getUserCountInRole(){
			$users = IamAdminServicesUtilities::getUsersWithRole(Input::get("role"));
			return count( $users);
	}

	public function searchUsersView(){
		if(Input::has("search_val"))
		{
			$users =  Keycloak::searchUsers(Input::get("search_val"));
		}
		else
			$users = Keycloak::listUsers();

		if(!isset($users) || empty($users)){
			$users = array();
		}
		$roles = Keycloak::getAllRoles();
		sort($roles);
		Session::put("admin-nav", "manage-users");
		return View::make("admin/manage-users", array("users" => $users, "roles" => $roles));
	}

	private function cmp($a, $b)
	{
		return strcmp($b->requestCreationTime, $a->requestCreationTime);
	}

    public function gatewayView(){
    	//only for super admin
		//Session::put("super-admin", true);
		
		$gatewaysInfo = CRUtilities::getAllGatewayProfilesData();
		$gateways = $gatewaysInfo["gateways"];
		usort($gateways, array($this, "cmp"));
		$tokens = AdminUtilities::get_all_ssh_tokens_with_description();
		$pwdTokens = AdminUtilities::get_all_pwd_tokens();
		$srData = SRUtilities::getEditSRData();
		$crData = CRUtilities::getEditCRData();

		$unselectedCRs = array();
		$unselectedSRs = array();

		foreach( (array)$gatewaysInfo["allCRs"] as $crId => $cr){
			if( ! in_array($cr->computeResourceId, $gatewaysInfo["selectedCRs"]) )
			$unselectedCRs[] = $cr;
		}

		foreach( (array)$gatewaysInfo["allSRs"] as $index => $sr){
			if( ! in_array($sr->storageResourceId, $gatewaysInfo["selectedSRs"]) )
			$unselectedSRs[] = $sr;
		}

		//$dsData = CRUtilities::getAllDataStoragePreferences( $gateways);
		$gatewayData = array( 
								"gateways" => $gateways, 
								"computeResources" => $gatewaysInfo["allCRs"],
								"crData" => $crData,
								"storageResources" => $gatewaysInfo["allSRs"],
								"srData" => $srData,
								"tokens" => $tokens,
								"pwdTokens" => $pwdTokens,
								"unselectedCRs" => $unselectedCRs,
								"unselectedSRs" => $unselectedSRs,
								"gatewayApprovalStatuses" => AdminUtilities::get_gateway_approval_statuses()
							);
		$view = "admin/manage-gateway";

        Session::put("admin-nav", "gateway-prefs");
		return View::make( $view, $gatewayData);
    }

	public function addGatewayAdminSubmit(){
		//check if username exists
		if(Keycloak::usernameExists( Input::get("username")) )
		{
            Keycloak::updateUserRoles(Input::get("username"), array( "new"=>array( Config::get('wsis::admin-role-name')), "deleted"=>array() ) );
			return Redirect::to("admin/dashboard/users?role=" . Config::get('wsis::admin-role-name'))->with("Gateway Admin has been added.");
		}
		else
		{
			echo ("username doesn't exist."); exit;
		}
	}

	public function updateGateway(){

        $rules = array(
            "password" => "min:6|max:48|regex:/^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@!$#*&]).*$/",
            "confirm_password" => "same:password",
            "email" => "email",
        );
        $messages = array(
            'password.regex' => 'Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$&*',
        );
        $checkValidation = array();
        $checkValidation["password"] = Input::get("gatewayAdminPassword");
        $checkValidation["confirm_password"] = Input::get("gatewayAdminPasswordConfirm");
        $checkValidation["email"] = Input::get("gatewayAdminEmail");

        $validator = Validator::make( $checkValidation, $rules, $messages);
        if ($validator->fails()) {
            if(Request::ajax()){
                return json_encode(array("errors" => true, "validationMessages" => $validator->messages()));
            } else {
                Session::put("message", "An error has occurred while updating the Gateway: " + $validator->messages());
                return Redirect::back();
            }
        }

	    $gateway = TenantProfileService::getGateway( Session::get('authz-token'), Input::get("internal_gateway_id"));
		$returnVal = AdminUtilities::update_gateway( Input::get("internal_gateway_id"), Input::except("oauthClientId","oauthClientSecret"));
		if( Request::ajax()){
			if( $returnVal == 1) {
                $email = Config::get('pga_config.portal')['admin-emails'];
                EmailUtilities::gatewayUpdateMailToProvider($gateway->emailAddress, Input::get("gateway_id"));
                EmailUtilities::gatewayUpdateMailToAdmin($email, Input::get("gateway_id"));
                if (isset($gatewayData["createTenant"]))
                    Session::put("successMessages", "Tenant has been created successfully!");
                else
                    Session::put("successMessages", "Gateway has been updated successfully!");
                return json_encode(array("errors" => false, "gateway" => AdminUtilities::get_gateway(Input::get("internal_gateway_id"))));
            }
			else {
                return json_encode(array("errors" => true)); // anything other than positive update result
            }
		}
		else{
			if( $returnVal) {
                $email = Config::get('pga_config.portal')['admin-emails'];
                EmailUtilities::gatewayUpdateMailToProvider($gateway->emailAddress, Input::get("gateway_id"));
                EmailUtilities::gatewayUpdateMailToAdmin($email, Input::get("gateway_id"));
                Session::put("message", "Gateway has been updated successfully!");
            }
			else {
                Session::put("message", "An error has occurred while updating your Gateway. Please make sure you've entered all the details correctly. Try again or contact the Admin to report the issue.");
            }

			return Redirect::back();

		}
		//return 1;
	}

	public function rolesView(){
		$roles = Keycloak::getAllRoles();
		sort($roles);
		Session::put("admin-nav", "manage-roles");
		return View::make("admin/manage-roles", array("roles" => $roles));
	}

	public function experimentsView(){
        Session::put("admin-nav", "exp-statistics");

        $applications = AppUtilities::get_all_applications();
        uksort($applications, 'strcasecmp');
        $hostnames = CRUtilities::getAllCRObjects(true);
        uksort($hostnames, 'strcasecmp');
        return View::make("admin/manage-experiments", array("applications" => $applications, "hostnames" => $hostnames));
	}

	public function resourcesView(){
		$data = CRUtilities::getBrowseCRData(false);
		$allCRs = $data["crObjects"];
		return View::make("admin/manage-resources", array("resources" => $allCRs) );
	}

	public function addRole(){
		WSIS::addRole( Input::get("role") );
		return Redirect::to("admin/dashboard/roles")->with( "message", "Role has been added.");
	}

    public function addRolesToUser(){
        $currentRoles = Keycloak::getUserRoles(Input::get("username"));
		if(!is_array($currentRoles))
			$currentRoles = array($currentRoles);
        $roles["new"] = array_diff(Input::all()["roles"], $currentRoles);
        $roles["deleted"] = array_diff($currentRoles, Input::all()["roles"]);

        $index = array_search('Internal/everyone',$roles["new"]);
        if($index !== FALSE){
            unset($roles["new"][$index]);
        }

        $index = array_search('Internal/everyone',$roles["deleted"]);
        if($index !== FALSE){
            unset($roles["deleted"][$index]);
        }

        $username = Input::all()["username"];
        Keycloak::updateUserRoles($username, $roles);
        $newCurrentRoles = Keycloak::getUserRoles($username);
        if(in_array(Config::get("pga_config.wsis")["admin-role-name"], $newCurrentRoles) || in_array(Config::get("pga_config.wsis")["read-only-admin-role-name"], $newCurrentRoles)
                || in_array(Config::get("pga_config.wsis")["user-role-name"], $newCurrentRoles)){
            $userProfile = Keycloak::getUserProfile($username);
            $recipients = array($userProfile["email"]);
            $this->sendAccessGrantedEmailToTheUser(Input::get("username"), $recipients);

            // remove the initial role when the initial role isn't a privileged
            // role and the admin has now assigned the user to a privileged
            // role, unless the admin is trying to add the user back to the
            // initial role
            if (!$this->isInitialRoleOneOfPrivilegedRoles()) {

                $initialRoleName = CommonUtilities::getInitialRoleName();
                if(in_array($initialRoleName, $newCurrentRoles) && !in_array($initialRoleName, $roles["new"])) {
                    $userRoles["new"] = array();
                    $userRoles["deleted"] = $initialRoleName;
                    Keycloak::updateUserRoles( $username, $userRoles);
                } else if(in_array($initialRoleName, $newCurrentRoles) && in_array($initialRoleName, $roles["new"])) {
                    // When initial role added remove all roles except for initial role and Internal/everyone
                    $userRoles["new"] = array();
                    $userRoles["deleted"] = array_diff($newCurrentRoles, array($initialRoleName, "Internal/everyone"));
                    Keycloak::updateUserRoles( $username, $userRoles);
                }
            }
        }
        return Redirect::to("admin/dashboard/roles")->with( "message", "Roles has been added.");
    }

    /*
     * Return true if the initial-role-name is one of the four privileged
     * roles. This is used to figure out whether the initial-role-name is a
     * 'user-pending' kind of role (returns false), or whether the initial role
     * is a privileged role (returns true) and no admin intervention is
     * necessary.
     */
    private function isInitialRoleOneOfPrivilegedRoles() {

        $initialRoleName = CommonUtilities::getInitialRoleName();
        $adminRoleName = Config::get("pga_config.wsis")["admin-role-name"];
        $adminReadOnlyRoleName = Config::get("pga_config.wsis")["read-only-admin-role-name"];
        $userRoleName = Config::get("pga_config.wsis")["user-role-name"];
        $gatewayProviderRoleName = "gateway-provider";
        return in_array($initialRoleName, array($adminRoleName, $adminReadOnlyRoleName, $userRoleName, $gatewayProviderRoleName));
    }

    public function removeRoleFromUser(){
        $roles["deleted"] = array(Input::all()["roleName"]);
        $roles["new"] = array();
        $username = Input::all()["username"];
        Keycloak::updateUserRoles($username, $roles);
        return Redirect::to("admin/dashboard/roles")->with( "message", "Role has been deleted.");
    }

	public function getRoles(){
		$roles = Keycloak::getUserRoles(Input::get("username"));
		sort($roles);
		return json_encode((array)$roles);
	}

	public function deleteRole(){
		WSIS::deleteRole( Input::get("role") );
		return Redirect::to("admin/dashboard/roles")->with( "message", "Role has been deleted.");

	}

	public function credentialStoreView(){
        Session::put("admin-nav", "credential-store");
        $tokens = AdminUtilities::get_all_ssh_tokens_with_description();
		$pwdTokens = AdminUtilities::get_all_pwd_tokens();
        //var_dump( $tokens); exit;
		return View::make("admin/manage-credentials", array("tokens" => $tokens , "pwdTokens" => $pwdTokens) );
	}

	private function sendAccessGrantedEmailToTheUser($username, $recipients){

		$mail = new PHPMailer;

		$mail->isSMTP();
		$mail->SMTPDebug = 3;
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

		foreach($recipients as $recipient){
			$mail->addAddress($recipient);
		}

		$mail->isHTML(true);

		$mail->Subject = "Your user account (".$username.") privileges changed!";
		$userProfile = Keycloak::getUserProfile($username);
		$wsisConfig = Config::get('pga_config.wsis');
		$tenant = $wsisConfig['tenant-domain'];

		$str = "Please re-login into the portal to use new privileges" ."<br/><br/>";
		$str = $str . "Gateway Portal: " . $_SERVER['SERVER_NAME'] ."<br/>";
		$str = $str . "Tenant: " . $tenant . "<br/>";
		$str = $str . "Username: " . $username ."<br/>";
		$str = $str . "Name: " . $userProfile["firstname"] . " " . $userProfile["lastname"] . "<br/>";
		$str = $str . "Email: " . $userProfile["email"] ;

		$mail->Body = $str;
		$mail->send();
	}

    public function experimentStatistics()
    {
        if (Request::ajax()) {
            $inputs = Input::all();
            $username = Input::get('username');
            $appname = Input::get('appname');
            $hostname = Input::get('hostname');
            $expStatistics = AdminUtilities::get_experiment_execution_statistics(strtotime($inputs['fromTime']) * 1000
                , strtotime($inputs['toTime']) * 1000, $username, $appname, $hostname);
            return View::make("admin/experiment-statistics", array("expStatistics" => $expStatistics,
                      "username" => $username, "appname" => $appname, "hostname" => $hostname));
        }
    }

    public function getExperimentsOfTimeRange()
    {
        if (Request::ajax()) {
            $inputs = Input::all();
            $expContainer = AdminUtilities::get_experiments_of_time_range($inputs);
            $expStates = ExperimentUtilities::getExpStates();
            return View::make("partials/experiment-container", 
            	array(	"expContainer" => $expContainer,
                		"expStates" => $expStates,
                		"dashboard" => true
                	));
        }
    }

	public function createSSH(){
        $description = Input::get("description");
        $newToken = AdminUtilities::create_ssh_token_for_gateway($description);
        return Redirect::to("admin/dashboard/credential-store")->with("message", "SSH Key was successfully created");
	}

	public function createPWD(){
		AdminUtilities::create_pwd_token(Input::all());
		return Redirect::to("admin/dashboard/credential-store")->with("message", "Password Credential was successfully created");

	}

	public function removeSSH(){
		$removeToken = Input::get("token");
		if( AdminUtilities::remove_ssh_token( $removeToken) )
            return Redirect::to("admin/dashboard/credential-store")->with("message", "SSH Key was successfully deleted");
		else
            return Redirect::to("admin/dashboard/credential-store")->with("error-message", "Unable to delete SSH Key");

	}

	public function removePWD(){
		$removeToken = Input::get("token");
		if( AdminUtilities::remove_pwd_token( $removeToken) )
			return Redirect::to("admin/dashboard/credential-store")->with("message", "Password Credential was successfully deleted");
		else
			return Redirect::to("admin/dashboard/credential-store")->with("error-message", "Unable to delete Password Credential"); 

	}


	/* ---- Super Admin Functions ------- */

    public function createGateway(){

        return View::make("admin/create-gateway");

    }

	public function addGateway(){
		$inputs = Input::all();

		$rules = array(
            "password" => "required|min:6|max:48|regex:/^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@!$#*&]).*$/",
            "confirm_password" => "required|same:password",
            "email" => "required|email",
        );

        $messages = array(
            'password.regex' => 'Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$&*',
        );

        $checkValidation = array();
        $checkValidation["password"] = $inputs["admin-password"];
        $checkValidation["confirm_password"] = $inputs["admin-password-confirm"];
        $checkValidation["email"] = $inputs["admin-email"];

        $validator = Validator::make( $checkValidation, $rules, $messages);
        if ($validator->fails()) {
            Session::put("validationMessages", [$validator->messages()] );
            return Redirect::back()
                ->withErrors($validator);
        }
        else{
            $username = Session::get("username");
            $returnVal = AdminUtilities::add_gateway(Input::all());

            if ($returnVal == 1){
                $email = Config::get('pga_config.portal')['admin-emails'];
                $user_profile = Keycloak::getUserProfile($username);
                EmailUtilities::gatewayRequestMail($user_profile["firstname"], $user_profile["lastname"], $email, Input::get("gateway-name"));
                Session::put("message", "Gateway " . $inputs["gateway-name"] . " has been added.");
            }
            else{
                Session::put("errorMessages", "Error: A Gateway already exists with the same GatewayId, Name or URL! Please make a new request.");
            }

			return Redirect::back();
		}
	}

	public function checkRequest(){
	    $inputs = Input::all();

        $rules = array(
            "email" => "required|email",
        );

        $messages = array(
            'email.format' => 'Please enter a valid Email ID',
        );

        $checkValidation = array();
        $checkValidation["email"] = $inputs["email-address"];

        $validator = Validator::make( $checkValidation, $rules, $messages);
        if ($validator->fails()) {
            Session::put("validationMessages", [$validator->messages()] );
            return Redirect::to("admin/dashboard")
                ->withInput()
                ->withErrors($validator);
        }
        else{
            $username = Session::get("username");
            $returnVal = AdminUtilities::check_request(Input::all());

            if ($returnVal == 1) {
                $email = Config::get('pga_config.portal')['admin-emails'];
                $user_profile = Keycloak::getUserProfile($username);
                EmailUtilities::gatewayRequestMail($user_profile["firstname"], $user_profile["lastname"], $email, $inputs["gateway-name"]);
                Session::put("message", "Your Gateway request for " . $inputs["gateway-name"] . " has been created. Your new Gateway request is yet to be approved. You will be notified of the approval status via email notification.");
            }
            else{
                $error = "A Gateway already exists with the same GatewayId, Name or URL! Please make a new request.";
                return Redirect::to("admin/dashboard")
                    ->withInput()
                    ->withErrors($error);
            }
            return Redirect::to("admin/dashboard");
        }
    }

	public function requestGateway(){
		$inputs = Input::all();
		
		$rules = array(
            "username" => "required",
            "password" => "required|min:6|max:48|regex:/^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@!$#*&]).*$/",
            "confirm_password" => "required|same:password",
            "email" => "required|email",
        );

        $messages = array(
            'password.regex' => 'Password needs to contain at least (a) One lower case letter (b) One Upper case letter and (c) One number (d) One of the following special characters - !@#$&*',
        );

        $checkValidation = array();
        $checkValidation["username"] = $inputs["admin-username"];
        $checkValidation["password"] = $inputs["admin-password"];
        $checkValidation["confirm_password"] = $inputs["admin-password-confirm"];
        $checkValidation["email"] = $inputs["email-address"];

        $validator = Validator::make( $checkValidation, $rules, $messages);
        if ($validator->fails()) {
            Session::put("validationMessages", $validator->messages() );
            return Redirect::to("admin/dashboard")
            	->withInput(Input::except('password', 'password_confirm'))
            	->withErrors($validator);
        }
        else{
	        $gateway = AdminUtilities::request_gateway(Input::all());
			Session::put("message", "Your request for Gateway " . $inputs["gateway-name"] . " has been created.");
			
            return Redirect::to("admin/dashboard");
		}
	}

    public function enableComputeResource(){
        $resourceId = Input::get("resourceId");
        $computeResource = CRUtilities::get_compute_resource($resourceId);
        $computeResource->enabled = true;
        CRUtilities::register_or_update_compute_resource($computeResource, true);
    }

    public function disableComputeResource(){
        $resourceId = Input::get("resourceId");
        $computeResource = CRUtilities::get_compute_resource($resourceId);
        $computeResource->enabled = false;
        CRUtilities::register_or_update_compute_resource($computeResource, true);
    }

    public function enableStorageResource(){
        $resourceId = Input::get("resourceId");
        $storageResource = SRUtilities::get_storage_resource($resourceId);
        $storageResource->enabled = true;
        SRUtilities::register_or_update_storage_resource($storageResource, true);
    }

    public function disableStorageResource(){
        $resourceId = Input::get("resourceId");
        $storageResource = SRUtilities::get_storage_resource($resourceId);
        $storageResource->enabled = false;
        SRUtilities::register_or_update_storage_resource($storageResource, true);
    }

    public function viewAllocationRequests(){
    	return 'result';
    }

	public function noticesView(){
        Session::put("admin-nav", "notices");
        $notices = array();
        $noticePriorities = CommonUtilities::get_notice_priorities();
        $notices = CommonUtilities::get_all_notices();
		return View::make("admin/manage-notices", array("notices" => $notices, "priorities" => $noticePriorities));
	}

	public function addNotice(){
		$inputs = Input::all();
		$newNotice = AdminUtilities::add_or_update_notice( $inputs);
			return json_encode( $newNotice);
	}

	public function updateNotice(){
		$inputs = Input::all();
		return AdminUtilities::add_or_update_notice( $inputs, true);
	}

	public function deleteNotice(){
		$inputs = Input::all();
		if( AdminUtilities::delete_notice( $inputs["notificationId"]))
		{
			//ajax
			return 1;
		}
		else
			return 0;
	}

}
