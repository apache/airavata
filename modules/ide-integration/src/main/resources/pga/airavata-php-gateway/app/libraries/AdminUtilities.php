<?php

use Airavata\Model\Workspace\Gateway;
use Airavata\Model\Workspace\GatewayApprovalStatus;
use Airavata\Model\Workspace\Notification;
use Airavata\Model\Workspace\NotificationPriority;
use Airavata\Model\Credential\Store\SummaryType;
use Illuminate\Support\Facades\Log;

class AdminUtilities
{

    /**
     * To create a new gateway
     * @param $input
     * @return string
     */
    public static function add_gateway($inputs)
    {
        $gateway = new Gateway();
        $id = preg_replace('~^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$~', '', $inputs["gateway-name"]);
        $id = strtolower(preg_replace('~[^a-zA-Z0-9]+~', '-', $id));
        $gateway->gatewayId = $id;
        $gateway->gatewayName = $inputs["gateway-name"];
        $gateway->emailAddress = $inputs["email-address"];
        $gateway->gatewayURL = $inputs["gateway-url"];
        $gateway->gatewayAdminFirstName = $inputs["admin-firstname"];
        $gateway->gatewayAdminEmail = $inputs["admin-email"];
        $gateway->gatewayAdminLastName = $inputs["admin-lastname"];
        $gateway->identityServerUserName = $inputs["admin-username"];
        $token = AdminUtilities::create_pwd_token([
            "username" => $inputs["admin-username"],
            "password" => $inputs["admin-password"],
            "description" => "Admin user password for Gateway " . $id
        ]);
        $gateway->identityServerPasswordToken  = $token;
        $gateway->reviewProposalDescription = $inputs["project-details"];
        $gateway->gatewayPublicAbstract = $inputs["public-project-description"];
        $gateway->requesterUsername = Session::get('username');
        $gateway->gatewayApprovalStatus = GatewayApprovalStatus::APPROVED;

        try {
            TenantProfileService::addGateway(Session::get('authz-token'), $gateway);
            return 1;
        }
        catch (Exception $ex) {
            return -1;
        }
    }

    public static function get_gateway( $gateway_id)
    {
        return TenantProfileService::getGateway( Session::get("authz-token"), $gateway_id);
    }

    public static function get_gateways_for_requester( $username )
    {
        return TenantProfileService::getAllGatewaysForUser( Session::get("authz-token"), $username );
    }

    public static function check_request( $inputs)
    {
        $gateway = new Gateway( $inputs);
        $id = preg_replace('~^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$~', '', $inputs["gateway-name"]);
        $id = strtolower(preg_replace('~[^a-zA-Z0-9]+~', '-', $id));
        $gateway->gatewayId = $id;
        $gateway->gatewayApprovalStatus = GatewayApprovalStatus::REQUESTED;
        //$gateway->domain = 'airavata.' . $inputs["gateway-acronym"];
        $gateway->gatewayName = $inputs["gateway-name"];
        $gateway->emailAddress = $inputs["email-address"];
        $gateway->gatewayPublicAbstract = $inputs["public-project-description"];
        $gateway->requesterUsername = Session::get('username');

        try {
            TenantProfileService::addGateway(Session::get('authz-token'), $gateway);
            return 1;
        }
        catch (Exception $ex) {
            return -1;
        }
    }

    public static function request_gateway( $inputs)
    {
        $gateway = new Gateway( $inputs);
        $id = preg_replace('~^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$~', '', $inputs["gateway-name"]);
        $id = strtolower(preg_replace('~[^a-zA-Z0-9]+~', '-', $id));
        $gateway->gatewayId = $id;
        $gateway->gatewayApprovalStatus = GatewayApprovalStatus::APPROVED;
        //$gateway->domain = 'airavata.' . $inputs["gateway-acronym"];
        $gateway->gatewayName = $inputs["gateway-name"];
        $gateway->emailAddress = $inputs["email-address"];
        //$gateway->gatewayAcronym = $inputs["gateway-acronym"];
        $gateway->gatewayURL = $inputs["gateway-url"];
        $gateway->gatewayAdminFirstName = $inputs["admin-firstname"];
        $gateway->gatewayAdminLastName = $inputs["admin-lastname"];
        $gateway->identityServerUserName = $inputs["admin-username"];
        $token = AdminUtilities::create_pwd_token([
            "username" => $inputs["admin-username"],
            "password" => $inputs["admin-password"],
            "description" => "Admin user password for Gateway " . $id
        ]);
        $gateway->identityServerPasswordToken  = $token;
        $gateway->reviewProposalDescription = $inputs["project-details"];
        $gateway->gatewayPublicAbstract = $inputs["public-project-description"];
        $gateway->requesterUsername = Session::get('username');

        return TenantProfileService::addGateway(Session::get('authz-token'), $gateway);
    }

    public static function get_gateway_approval_statuses()
    {
        $gatewayApprovalStatusObject = new GatewayApprovalStatus();
        return $gatewayApprovalStatusObject::$__names;
    }

    public static function update_form( $gatewayId, $gatewayData){

        if( isset( $gatewayData["updateRequest"])) {
            return TenantProfileService::getGateway( Session::get('authz-token'), $gatewayId);
        }

    }

    public static function user_update_gateway( $gatewayId, $gatewayData){
        $gateway = TenantProfileService::getGateway( Session::get('authz-token'), $gatewayId);
        $gateway->gatewayApprovalStatus = GatewayApprovalStatus::APPROVED;
        $gateway->emailAddress = $gatewayData["email-address"];
        $gateway->gatewayURL = $gatewayData["gateway-url"];
        $gateway->reviewProposalDescription = $gatewayData["project-details"];
        $gateway->gatewayPublicAbstract = $gatewayData["public-project-description"];
        if( TenantProfileService::updateGateway( Session::get('authz-token'), $gateway) ){
            return 1;
        }
        else{
            //Need to find a better way for this.
            // retun echo "Tenant Name is already in use";
            return -1;
        }
    }

    public static function update_gateway( $gatewayId, $gatewayData){

        $gateway = TenantProfileService::getGateway( Session::get('authz-token'), $gatewayId);
        if( isset( $gatewayData["cancelRequest"]))
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::CANCELLED;
        else{
            $gateway->gatewayName = $gatewayData["gatewayName"];
            $gateway->gatewayURL = $gatewayData["gatewayURL"];
            $gateway->declinedReason = $gatewayData["declinedReason"];
        }
        if( isset($gatewayData["createTenant"])) {
            $gatewayData["declinedReason"] = " ";
            if ($gateway->identityServerPasswordToken == null)
            {
                Session::flash("errorMessages", "Error: Please provide an admin password.");
                return -1;
            }
            foreach ($gatewayData as $key => $data) {
                // Don't check these fields, see related check above
                // where we make sure that password is provided
                if ($key == "gatewayAdminPassword" || $key == "gatewayAdminPasswordConfirm") {
                    continue;
                }
                if ($data == null) {
                    return -1;
                }
            }
            $gateway = IamAdminServices::setUpGateway( Session::get('authz-token'), $gateway);
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::CREATED;
        }
        elseif( isset( $gatewayData["approveRequest"])){
            $gateway->emailAddress = $gatewayData["emailAddress"];
            $gateway->gatewayURL = $gatewayData["gatewayURL"];
            $gateway->gatewayAdminFirstName = $gatewayData["gatewayAdminFirstName"];
            $gateway->gatewayAdminLastName = $gatewayData["gatewayAdminLastName"];
            $gateway->gatewayAdminEmail = $gatewayData["gatewayAdminEmail"];
            $gateway->identityServerUserName = $gatewayData["identityServerUserName"];
            if (!empty($gatewayData["gatewayAdminPassword"])) {
                $token = AdminUtilities::create_pwd_token([
                    "username" => $gatewayData["identityServerUserName"],
                    "password" => $gatewayData["gatewayAdminPassword"],
                    "description" => "Admin user password for Gateway " . $gateway->gatewayName
                ]);
                $gateway->identityServerPasswordToken = $token;
            }
            $gateway->reviewProposalDescription = $gatewayData["reviewProposalDescription"];
            $gateway->gatewayPublicAbstract = $gatewayData["gatewayPublicAbstract"];
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::APPROVED;
        }
        elseif( isset( $gatewayData["denyRequest"])){
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::DENIED;
        }
        elseif( isset( $gatewayData["updateGateway"])){
            $gateway->emailAddress = $gatewayData["emailAddress"];
            $gateway->gatewayURL = $gatewayData["gatewayURL"];
            $gateway->gatewayAdminFirstName = $gatewayData["gatewayAdminFirstName"];
            $gateway->gatewayAdminLastName = $gatewayData["gatewayAdminLastName"];
            $gateway->gatewayAdminEmail = $gatewayData["gatewayAdminEmail"];
            $gateway->identityServerUserName = $gatewayData["identityServerUserName"];
            if (!empty($gatewayData["gatewayAdminPassword"])) {
                $token = AdminUtilities::create_pwd_token([
                    "username" => $gatewayData["identityServerUserName"],
                    "password" => $gatewayData["gatewayAdminPassword"],
                    "description" => "Admin user password for Gateway " . $gateway->gatewayName
                ]);
                $gateway->identityServerPasswordToken = $token;
            }
            $gateway->reviewProposalDescription = $gatewayData["reviewProposalDescription"];
            $gateway->gatewayPublicAbstract = $gatewayData["gatewayPublicAbstract"];
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::APPROVED;
        }
        elseif( isset( $gatewayData["deployGateway"])){
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::DEPLOYED;
        }
        elseif( isset( $gatewayData["deactivateGateway"])){
            $gateway->gatewayApprovalStatus = GatewayApprovalStatus::DEACTIVATED;
        }

        if( TenantProfileService::updateGateway( Session::get('authz-token'), $gateway) ){
            return 1;
        }
        else{
            //Need to find a better way for this.
           // retun echo "Tenant Name is already in use";
            return -1;
        }
    }

    public static function add_tenant( $gateway){
        $tenants = WSIS::getTenants();
        $tenantExists = false;
        foreach( $tenants as $tenant){
            if( $tenant->tenantDomain == $gateway->domain){
                $tenantExists = true;
            }
        }
        if( !$tenantExists){
            $gatewayDomain = $gateway->domain;

            //finally create tenant
            return WSIS::createTenant(1, $gateway->identityServerUserName, $gateway->identityServerPasswordToken, $gateway->emailAddress, $gateway->gatewayAdminFirstName, $gateway->gatewayAdminLastName, $gatewayDomain);
        }
        else
            return false;
    }

    /**
     * Method to get experiment execution statistics object
     * @param $fromTime
     * @param $toTime
     * @return \Airavata\Model\Experiment\ExperimentStatistics
     */
    public static function get_experiment_execution_statistics($fromTime, $toTime, $username, $appname, $hostname)
    {
        if (trim($username) == '') {
            $username = null;
        }
        if (trim($appname) == '') {
            $appname = null;
        }
        if (trim($hostname) == '') {
            $hostname = null;
        }
        return Airavata::getExperimentStatistics(Session::get('authz-token'),
            Config::get('pga_config.airavata')['gateway-id'], $fromTime, $toTime, $username, $appname, $hostname);
    }

    /**
     * Method to get experiments of a particular time range
     * @param $inputs
     * @return array
     */
    public static function get_experiments_of_time_range($inputs)
    {
        $experimentStatistics = AdminUtilities::get_experiment_execution_statistics(
            strtotime($inputs["from-date"]) * 1000,
            strtotime($inputs["to-date"]) * 1000,
            $inputs['username'],
            $inputs['appname'],
            $inputs['hostname']
        );
        $experiments = array();
        if ($inputs["status-type"] == "ALL") {
            $experiments = $experimentStatistics->allExperiments;
        }else if ($inputs["status-type"] == "COMPLETED") {
            $experiments = $experimentStatistics->completedExperiments;
        }else if ($inputs["status-type"] == "CREATED") {
            $experiments = $experimentStatistics->createdExperiments;
        }else if ($inputs["status-type"] == "RUNNING") {
            $experiments = $experimentStatistics->runningExperiments;
        } elseif ($inputs["status-type"] == "FAILED") {
            $experiments = $experimentStatistics->failedExperiments;
        } else if ($inputs["status-type"] == "CANCELED") {
            $experiments = $experimentStatistics->cancelledExperiments;
        }

        $expContainer = array();
        $expNum = 0;
        foreach ($experiments as $experiment) {
            //var_dump( $experiment); exit;
            $expValue = ExperimentUtilities::get_experiment_values($experiment, true);
            $expContainer[$expNum]['experiment'] = $experiment;
            $expValue["editable"] = false;
            $expContainer[$expNum]['expValue'] = $expValue;
            $expNum++;
        }

        return $expContainer;
    }

    public static function create_ssh_token_for_gateway($description) {
        $token = Airavata::generateAndRegisterSSHKeys(Session::get('authz-token'), $description);
    }

    public static function create_ssh_token_for_user($description) {
        return Airavata::generateAndRegisterSSHKeys(Session::get('authz-token'), $description);
    }

    public static function create_pwd_token($inputs){
        $username = $inputs['username'];
        $password = $inputs['password'];
        $description = $inputs['description'];
        return $newToken = Airavata::registerPwdCredential( Session::get('authz-token'),
            $username, $password, $description);

    }

    public static function get_all_ssh_tokens_with_description(){
        return Airavata::getAllCredentialSummaries( Session::get('authz-token'), "SSH");
    }

    public static function get_all_pwd_tokens(){
        $credential_summaries = Airavata::getAllCredentialSummaries(Session::get('authz-token'), SummaryType::PASSWD);
        $result = [];
        foreach ($credential_summaries as $credential_summary) {
            $result[$credential_summary->token] = $credential_summary->description;
        }
        return $result;
    }

    public static function remove_ssh_token( $token){
        return Airavata::deleteSSHPubKey( Session::get('authz-token'), $token);
    }

    public static function remove_pwd_token( $token){
        return Airavata::deletePWDCredential( Session::get('authz-token'), $token);
    }

    public static function add_or_update_notice( $notifData, $update = false){
        $notification = new Notification();
        $notification->gatewayId = Session::get("gateway_id");
        $notification->title = $notifData["title"];
        $notification->notificationMessage = $notifData["notificationMessage"];
        $notification->publishedTime = strtotime( $notifData["publishedTime"])* 1000;
        $notification->expirationTime = strtotime( $notifData["expirationTime"]) * 1000;
        $notification->priority = intval($notifData["priority"]);

        if( $update){
            $notification->notificationId =  $notifData["notificationId"];
            if( Airavata::updateNotification( Session::get("authz-token"), $notification) )
            {
                return json_encode( Airavata::getNotification(  Session::get('authz-token'),
                                                                Session::get("gateway_id"),
                                                                $notifData["notificationId"] ));
            }
            else
                0;
        }
        else
            return Airavata::getNotification(
                    Session::get('authz-token'),
                    Session::get("gateway_id"),
                    Airavata::createNotification( Session::get("authz-token"), $notification) );
    }

    public static function delete_notice( $notificationId){
        return Airavata::deleteNotification( Session::get('authz-token'), Session::get("gateway_id"), $notificationId);
    }

    public static function add_or_update_IDP($inputs)
    {
        $gatewayId = $inputs['gatewayId'];
        $identityServerTenant = $inputs['identityServerTenant'];
        $identityServerPwdCredToken = $inputs['identityServerPwdCredToken'];

        $gp = Airavata::getGatewayResourceProfile(Session::get('authz-token'), $gatewayId);
        if(!empty($identityServerTenant)){
            $gp->identityServerTenant = $identityServerTenant;
        }else{
            $gp->identityServerTenant = "";
        }

        if(!empty($identityServerPwdCredToken) and $identityServerPwdCredToken != 'DO-NOT-SET'){
            $gp->identityServerPwdCredToken = $identityServerPwdCredToken;
        }else{
            $gp->identityServerPwdCredToken = null;
        }


        Airavata::updateGatewayResourceProfile(Session::get('authz-token'), $gatewayId, $gp);

        return true;
    }
}
