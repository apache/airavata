<?php
use Airavata\Model\Workspace\Notification;
use Airavata\Model\Workspace\NotificationPriority;

class CommonUtilities
{

    /**
     * Print success message
     * @param $message
     */
    public static function print_success_message($message)
    {
        echo '<div class="alert alert-success">' . $message . '</div>';
    }

    /**
     * Print warning message
     * @param $message
     */
    public static function print_warning_message($message)
    {
        echo '<div class="alert alert-warning">' . $message . '</div>';
    }

    /**
     * Print error message
     * @param $message
     */
    public static function print_error_message($message)
    {
        echo '<div class="alert alert-danger">' . $message . '</div>';
    }

    /**
     * Print info message
     * @param $message
     */
    public static function print_info_message($message)
    {
        echo '<div class="alert alert-info">' . $message . '</div>';
    }

    /**
     * Redirect to the given url
     * @param $url
     */
    public static function redirect($url)
    {
        echo '<meta http-equiv="Refresh" content="0; URL=' . $url . '">';
    }

    /**
     * Return true if the form has been submitted
     * @return bool
     */
    public static function form_submitted()
    {
        return isset($_POST['Submit']);
    }

    /**
     * Store username in session variables
     * @param $username
     */
    public static function store_id_in_session($username)
    {
        Session::put('username', $username);
        Session::put('loggedin', true);
    }

    /**
     * Return true if the username stored in the session
     * @return bool
     */
    public static function id_in_session()
    {
        if (Session::has("username") && Session::has('loggedin'))
            return true;
        else
            return false;
    }

    /**
     * Verify if the user is already logged in. If not, redirect to the home page.
     */
    public static function verify_login()
    {
        if (CommonUtilities::id_in_session()) {
            return true;
        } else {
            CommonUtilities::print_error_message('User is not logged in!');
            return false;
        }
    }

    /**
     * Create navigation bar
     * Used for all pages
     */
    public static function create_nav_bar()
    {
        $menus = array();
        if  ( Session::has('loggedin') && 
            ( Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only')) &&
              !Session::has("gateway-provider")
            ) {
            $menus = array
            (
                'Project' => array
                (
                    array('label' => 'Create', 'url' => URL::to('/') . '/project/create', "nav-active" => "project"),
                    array('label' => 'Browse', 'url' => URL::to('/') . '/project/browse', "nav-active" => "project")
                ),
                'Experiment' => array
                (
                    array('label' => 'Create', 'url' => URL::to('/') . '/experiment/create', "nav-active" => "experiment"),
                    array('label' => 'Browse', 'url' => URL::to('/') . '/experiment/browse', "nav-active" => "experiment")
                )
            );

            if( isset( Config::get('pga_config.portal')['jira-help']))
            {
                $menus['Help'] = array();
                if( Config::get('pga_config.portal')['jira-help']['report-issue-script'] != '' 
                    && Config::get('pga_config.portal')['jira-help']['report-issue-collector-id'] != '')
                {
                    $menus['Help'][] = array('label' => 'Report Issue', 'url' => '#', "nav-active", "");
                }  
    //                array('label' => 'Forgot Password?', 'url' => URL::to('/') . '/forgot-password', "nav-active" => "")
                if( Config::get('pga_config.portal')['jira-help']['request-feature-script'] != '' 
                    && Config::get('pga_config.portal')['jira-help']['request-feature-collector-id'] != '')
                {
                    $menus['Help'][] = array('label' => 'Request Feature', 'url' => '#', "nav-active", "");
                }

                if( count( $menus['Help'] ) == 0 )
                    unset( $menus['Help']);
            }
        }

        $navbar = '<nav class="navbar navbar-inverse navbar-static-top" role="navigation">
            <div class="container-fluid">
                <!-- Brand and toggle get grouped for better mobile display -->
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-2">
                       <span class="sr-only">Toggle navigation</span>
                       <span class="icon-bar"></span>
                       <span class="icon-bar"></span>
                       <span class="icon-bar"></span>
                    </button>
                    <!--
                    <a class="navbar-brand" href="' . URL::to('home') . '" title="PHP Gateway with Airavata">PGA</a>
                    -->
                </div>

                <!-- Collect the nav links, forms, and other content for toggling -->
                <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-2">
                    <ul class="nav navbar-nav">';
            
            $navbar .= '<li>
                        <a href="' . URL::to("/") . '">
                            <span class="brand-logo"></span>
                        </a>
                        </li>';


        foreach ($menus as $label => $options) {
            Session::has('loggedin') ? $disabled = '' : $disabled = ' class="disabled"';

            $active = "";
            if (Session::has("nav-active") && isset($options[0]['nav-active'])) {
                if ($options[0]['nav-active'] == Session::get("nav-active"))
                    $active = " active ";
            }
            $navbar .= '<li class="dropdown ' . $active . '">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">' . $label . '<span class="caret"></span></a>
                <ul class="dropdown-menu" role="menu">';

            if (Session::has('loggedin')) {
                foreach ($options as $option) {
                    $id = strtolower(str_replace(' ', '-', $option['label']));

                    $navbar .= '<li' . $disabled . '><a href="' . $option['url'] . '" id=' . $id . '>' . $option['label'] . '</a></li>';
                }
            }

            $navbar .= '</ul></li>';
        }

        $active = "";
        if(Session::has('loggedin') && 
            (Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only')) &&
            !Session::has("gateway-provider")
        ){
            if( Session::get("nav-active") == "storage")
                $active = "active";
            $navbar .= '<li class="' . $active . '"><a href="' . URL::to("/") . '/files/browse"><span class="glyphicon glyphicon-folder-close"></span> Storage</a></li>';
        }
        $navbar .= '</ul>

        <ul class="nav navbar-nav navbar-right">';

        // right-aligned content

        if (Session::has('loggedin')) {
            $active = "";
            if (Session::has("nav-active")) {
                if ("user-console" == Session::get("nav-active") || "user-dashboard" == Session::get("nav-active"))
                    $active = " active ";
            }

            if( Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only') || Session::has('gateway-provider')){
                //notification bell
                $notices = array();
                if (CommonUtilities::isAiravataUp()) {
                    $notices = CommonUtilities::get_all_notices();
                }
                $navbar .= CommonUtilities::get_notices_ui( $notices);
            }

            if ( (Session::has("admin") || Session::has("admin-read-only")) && !Session::has("gateway-provider") )
                $navbar .= '<li class="' . $active . '"><a href="' . URL::to("/") . '/admin/dashboard"><span class="glyphicon glyphicon-user"></span>Admin Dashboard</a></li>';
            else if ( Session::has("gateway-provider"))
                $navbar .= '<li class="' . $active . '"><a href="' . URL::to("/") . '/admin/dashboard"><span class="glyphicon glyphicon-user"></span>Dashboard</a></li>';
            else
                $navbar .= '<li class="' . $active . '"><a href="' . URL::to("/") . '/account/dashboard"><span class="glyphicon glyphicon-user"></span>Dashboard</a></li>';

            $navbar .= '<li class="dropdown' . (Session::get("nav-active") == 'user-menu' ? ' active' : '') . '">

                <a href="#" class="dropdown-toggle" data-toggle="dropdown">' . Session::get("username") . ' <span class="caret"></span></a>';
            $navbar .= '<ul class="dropdown-menu" role="menu">';

            if ( Session::has("existing-gateway-provider")) {
                $requestedGateways = Session::get("requestedGateways");
                foreach( $requestedGateways as $gatewayId => $gateway){
                    if( $gateway["approvalStatus"] == "Approved"){
                        $navbar .= '<li><a href="' . URL::to('/') . '/admin/dashboard?gatewayId=' . $gateway["gatewayInfo"]->gatewayId . '">Manage ' . $gateway["gatewayInfo"]->gatewayName . '</a></li>';
                    }
                }
            }

            if( Session::has('authorized-user') || Session::has('admin') || Session::has('admin-read-only') || Session::has('gateway-provider') ){
                $navbar .= '<li><a href="' . URL::to('/') . '/account/settings"><span class="glyphicon glyphicon-cog"></span> User settings</a></li>';
            }
            $navbar .= '<li><a href="' . URL::to('/') . '/logout"><span class="glyphicon glyphicon-log-out"></span> Log out</a></li>';
            $navbar .= '</ul></li>';
        } else {

            if( CommonUtilities::hasAuthPasswordOption() ){
                $navbar .= '<li><a href="' . URL::to('/') . '/create"><span class="glyphicon glyphicon-user"></span> Create account</a></li>';
            }
            $navbar .= '<li><a href="' . URL::to('/') . '/login"><span class="glyphicon glyphicon-log-in"></span> Log in</a></li>';
        }

        $navbar .= '</ul></div></div></nav>';

        // Check if theme user has created links in their theme to the login and create account page.
        if( !Session::has('loggedin') &&
                isset( Config::get('pga_config.portal')['theme-based-login-links-configured']))
        {
            if( Config::get('pga_config.portal')['theme-based-login-links-configured'] ){
                $navbar = "";
            }
        }

        echo $navbar;
    }

    public static function get_notices_ui( $notices){
        $notifVisibility = "";

        $publishedNoticesCount = 0;
        $currentTime = floatval( time()*1000);
        $noticesUI = "";
        foreach( $notices as $notice){
            $endTime = $notice->expirationTime;
            if( $endTime == null)
                $endTime = $currentTime;
            if( $currentTime >= $notice->publishedTime && $currentTime <= $endTime)
            {
                $publishedNoticesCount++;
                $textColor = "text-info";
                if( $notice->priority == NotificationPriority::LOW)
                    $textColor = "text-primary";
                elseif( $notice->priority ==NotificationPriority::NORMAL)
                    $textColor = "text-warning";
                elseif( $notice->priority == NotificationPriority::HIGH)
                    $textColor = "text-danger";


                $noticesUI .= '
                <div class="notification">
                    <div class="notification-title ' . $textColor . '">' . $notice->title . '</div>
                    <div class="notification-description"><strong>' . $notice->notificationMessage . '</strong></div>
                    <div class="notification-ago time" unix-time="' . $notice->publishedTime/1000 . '">' . date("m/d/Y h:i:s A T", $notice->publishedTime/1000) . '</div>
                    <div class="notification-icon"></div>
                </div> <!-- / .notification -->
                ';
            }
        }

        $countOfNotices = $publishedNoticesCount;
        $newNotices = 0;
        if( Session::has("notice-count")){
            $newNotices = $countOfNotices - Session::get("notice-count");
        }
        else
            $newNotices = $countOfNotices;

        if( $newNotices <=0)
            $notifVisibility = "hide";

        $noticesUI = '<li clas="dropdown" style="color:#fff; relative">' .
                        '<a href="#" class="dropdown-toggle notif-link" data-toggle="dropdown">' .
                        '<span class="glyphicon glyphicon-bell notif-bell"></span>' .
                        '<span class="notif-num ' . $notifVisibility . '" data-total-notices="' . $countOfNotices . '">' . $newNotices . '</span>'.
                        '<div class="dropdown-menu widget-notifications no-padding" style="width: 300px"><div class="slimScrollDiv"><div class="notifications-list" id="main-navbar-notifications">'

                    . $noticesUI;

        
        $noticesUI .= '
        </div><div class="slimScrollBar" style=""></div>

        <div class="slimScrollRail" style=""></div></div> <!-- / .notifications-list -->';
        // NOTE: There is a legacy issue where gateway-provider users were also
        // given the admin role.  To allow gateway-provider users to see
        // notifications but not be able to manage them we have to check that
        // the user doesn't have the gateway-provider role.
        // if ( Session::has("admin") && !Session::has("gateway-provider"))
        if ( Session::has("admin") && !Session::has("gateway-provider"))
        {        
            $noticesUI .= '<a href="' . URL::to('/') . '/admin/dashboard/notices" class="notifications-link">Manage Notifications</a>';
        }
        else
            $noticesUI .= '<a href="#" class="notifications-link"></a>';
        
        $noticesUI .= '</div>'.'</li>';

        return $noticesUI;
    }   

    /**
     * Add attributes to the HTTP header.
     */
    public static function create_http_header()
    {
        header('Cache-Control: no-store, no-cache, must-revalidate');
        header('Cache-Control: post-check=0, pre-check=0', false);
        header('Pragma: no-cache');
    }

    /**
     * Open the XML file containing the community token
     * @param $tokenFilePath
     * @throws Exception
     */
    public static function open_tokens_file($tokenFilePath)
    {
        if (file_exists($tokenFilePath)) {
            $tokenFile = simplexml_load_file($tokenFilePath);
        } else {
            throw new Exception('Error: Cannot connect to tokens database!');
        }


        if (!$tokenFile) {
            throw new Exception('Error: Cannot open tokens database!');
        }
    }

    /**
     * Get All Notifications for a gateway
     * @param 
     * 
     */
    public static function get_all_notices(){
        return Airavata::getAllNotifications( Session::get('authz-token'), Session::get("gateway_id"));
    }

    public static function get_notice_priorities(){
        return NotificationPriority::$__names;
    }

    public static function arrSortObjsByKey($key, $order = 'DESC') {
        return function($a, $b) use ($key, $order) {
            // Swap order if necessary
            if ($order == 'DESC') {
                list($a, $b) = array($b, $a);
            } 
            // Check data type
            if (is_numeric($a->$key)) {
                return $a->$key - $b->$key; // compare numeric
            } else {
                return strnatcasecmp($a->$key, $b->$key); // compare string
            }
        };
    }

    /**
     * Convert from UTC time to local time. Units are seconds since Unix Epoch.
     */
    public static function convertUTCToLocal($utcTime) {

        $timeDifference = Session::get("user_timezone");
        $addOrSubtract = "-";
        if( $timeDifference < 0)
            $addOrSubtract = "+";

        return strtotime( $addOrSubtract . " " . abs($timeDifference) . " hours", $utcTime);
    }

    /**
     * Convert from local time to UTC time. Units are seconds since Unix Epoch.
     */
    public static function convertLocalToUTC($localTime) {

        $timeDifference = Session::get("user_timezone");
        $addOrSubtract = "-";
        if( $timeDifference > 0)
            $addOrSubtract = "+";

        return strtotime( $addOrSubtract . " " . abs($timeDifference) . " hours", $localTime);
    }

    public static function isAiravataUp() {
        // Cache whether Airavata is up in the REQUEST scope
        if (array_key_exists("isAiravataUp", $_REQUEST)) {
            return $_REQUEST["isAiravataUp"];
        }
        $_REQUEST["isAiravataUp"] = CommonUtilities::checkIfAiravataIsUp();
        return $_REQUEST["isAiravataUp"];
    }

    private static function checkIfAiravataIsUp() {

        try {
            $version = Airavata::getAPIVersion(Session::get('authz-token'));
            return true;
        } catch (Exception $e) {
            Log::error("Airavata is down!", array("exception" => $e));
            return false;
        }
    }

    public static function getInitialRoleName() {
        return Config::get('pga_config.wsis.initial-role-name', 'user-pending');
    }

    /**
     * Filter given list of role names to only include Airavata roles.
     */
    public static function filterAiravataRoles($roles) {
        return array_filter($roles, function($role) {
            return $role == Config::get('pga_config.wsis.admin-role-name')
                || $role == Config::get('pga_config.wsis.read-only-admin-role-name')
                || $role == Config::get('pga_config.wsis.user-role-name')
                || $role == Config::get('pga_config.wsis.initial-role-name')
                || $role == 'user-pending';
        });
    }

    public static function hasAuthPasswordOption() {
        return CommonUtilities::getAuthPasswordOption() != null;
    }

    public static function getAuthPasswordOption() {

        $auth_options = Config::get('pga_config.wsis')['auth-options'];
        $auth_password_option = null;
        foreach ($auth_options as $key => $auth_option) {
            if ($auth_option["oauth-grant-type"] == "password") {
                $auth_password_option = $auth_option;
                break;
            }
        }
        return $auth_password_option;
    }

    public static function getAuthCodeOptions() {

        $auth_options = Config::get('pga_config.wsis')['auth-options'];
        // Support for many external identity providers (authorization code auth flow)
        $auth_code_options = array();
        foreach ($auth_options as $auth_option) {
            if ($auth_option["oauth-grant-type"] == "password") {
                continue;
            } else if ($auth_option["oauth-grant-type"] == "authorization_code") {
                $extra_params = isset($auth_option["oauth-authorize-url-extra-params"]) ? $auth_option["oauth-authorize-url-extra-params"] : null;
                $auth_url = Keycloak::getOAuthRequestCodeUrl($extra_params);
                $auth_option["auth_url"] = $auth_url;
                $auth_code_options[] = $auth_option;
            } else {
                throw new Exception("Unrecognized oauth-grant-type: " . $auth_option["oauth-grant-type"]);
            }
        }
        return $auth_code_options;
    }

}

