<?php
return array(
    /**
     * *****************************************************************
     *  WSO2 Identity Server Related Configurations
     * *****************************************************************
     */

    'wsis' => [

        /**
         * Admin Role Name
         */
        'admin-role-name' => 'Internal/everyone',

        /**
         * Read only Admin Role Name
         */
        'read-only-admin-role-name' => 'Internal/everyone',

        /**
         * Gateway user role
         */
        'user-role-name' => 'Internal/everyone',

        /**
         * Initial user role. This is the initial user role assigned to a new
         * user. Set this to one of the three roles above to automatically
         * grant new users that role, or set to some other role ('user-pending')
         * to require admin approval before users have access.
         */
        'initial-role-name' => 'user-pending',

        /**
         * Tenant Domain
         */
        'tenant-domain' => 'default',

        /**
         * Tenant admin's username
         */
        'admin-username' => 'default-admin',

        /**
         * Tenant admin's password
         */
        'admin-password' => 'ade4#21242ftfd',

        /**
         * OAuth client key
         */
        'oauth-client-key' => 'pga',

        /**
         * OAuth client secret
         */
        'oauth-client-secret' => 'upCMVu2RZcAXUqpr9V7phAbz6hhF9cbl',

        /**
         * Authentication options
         */
        'auth-options' => [
            // Example of password based login
            [
                'oauth-grant-type' => 'password',
                'name' => 'Airavata PHP Gateway',
            ],
            // Example of external identity provider login
            [
                'oauth-grant-type' => 'authorization_code',
                'name' => 'CILogon',
                // Optional
                // Note: kc_idp_hint is used to skip Keycloak login screen and redirect immediately to this identity provider
                // http://www.keycloak.org/docs/2.5/server_admin/topics/identity-broker/suggested.html
                'oauth-authorize-url-extra-params' => 'kc_idp_hint=oidc',
                // Optional
                'logo' => '/assets/path_to_image.png'
            ],
        ],

        /**
         * OAuth Grant Type (password or authorization_code)
         */
        'oauth-grant-type' => 'authorization_code',

        /**
         * OAuth call back url (only if the grant type is authorization_code)
         */
        'oauth-callback-url' => 'http://localhost:8000/callback-url',

        /**
         * For OIDC servers that support the discovery protocol.
         */
        'openid-connect-discovery-url' => 'http://airavata.host:18080/realms/default/.well-known/openid-configuration',

        /**
         * Identity server url
         */
        'service-url' => 'http://airavata.host:18080',

        /**
         * Enable HTTPS server verification
         */
        'verify-peer' => false,

        /**
         * Path to the server certificate file
         */
        'cafile-path' => app_path() . '/resources/security/incommon_rsa_server_ca.pem',
    ],


    /**
     * *****************************************************************
     *  Airavata Client Configurations
     * *****************************************************************
     */
    'airavata' => [

        /**
         * Airavata API server location. Use tls:// as the protocol to
         * connect TLS enabled Airavata
         */
        'airavata-server' => 'airavata.host',

        /**
         * Airavata API server port
         */
        'airavata-port' => '8930',

        /**
         * Airavata Profile Service server location. Use tls:// as the protocol to
         * connect over TLS
         */
        'airavata-profile-service-server' => 'airavata.host',

        /**
         * Airavata Profile Service port
         */
        'airavata-profile-service-port' => '8962',

        /**
         * Airavata API server thrift communication timeout
         */
        'airavata-timeout' => '1000000',

        /**
         * PGA Gateway ID
         */
        'gateway-id' => 'default',

        /**
         * absolute path of the data dir
         */
        'experiment-data-absolute-path' => '/var/www/experimentData',

        /**
         * Advanced experiments options
         */
        'advanced-experiment-options' => '',

        /**
         * Default queue name
         */
        'queue-name' => 'long',

        /**
         * Default node count
         */
        'node-count' => '1',

        /**
         * Default total core count
         */
        'total-cpu-count' => '16',

        /**
         * Default wall time limit
         */
        'wall-time-limit' => '30',

        /**
         * Max node count
         */
        'max-node-count' => '4',

        /**
         * Max total core count
         */
        'max-total-cpu-count' => '96',

        /**
         * Max wall time limit
         */
        'max-wall-time-limit' => '120',

        /**
         * Enable app-catalog cache
         */
        'enable-app-catalog-cache' => true,

        /**
         * Life time of app catalog data cache in minutes
         */
        'app-catalog-cache-duration' => 5,
 
         /**
         * Gateway data store resource id
         */
         'gateway-data-store-resource-id' => '',

         /**
          * Data Sharing enabled
          */
          'data-sharing-enabled' => false,

        /**
         * Group Resource Profile ID to use when submitting experiments
         */
        'group-resource-profile-id' => '',
    ],

    /**
     * *****************************************************************
     *  Portal Related Configurations
     * *****************************************************************
     */
    'portal' => [
        /**
         * Whether this portal is the SciGaP admin portal
         */
        'super-admin-portal' => true,

        /**
         * Set the name of theme in use here
         */
        'theme' => 'base',

        /**
         * Portal title
         */
        'portal-title' => 'Airavata PHP Gateway',

        /**
         * Email address of the portal admin. Portal admin well get email notifications for events
         * such as new user creation
         */
        'admin-emails' => ['xxx@xxx.com','yyy@yyy.com'],

        /**
         * Email account that the portal should login to send emails
         */
        'portal-email-username' => 'xyz@abc.com',

        /**
         * Password for the portal's email account
         */
        'portal-email-password' => 'xxxxxxxxxxx',

        /**
         * SMTP server on which the portal should connect
         */
        'portal-smtp-server-host' => 'smtp.gmail.com',

        /**
         * SMTP server port on which the portal should connect
         */
        'portal-smtp-server-port' => '587',

        /**
         * Email verification code valid time interval in minutes
         */
         'email-verify-code-valid-time' => 360,

        /**
         * Set this to true if theme has set links to login
         */
        'theme-based-login-links-configured' => false,

        /**
         * Set the URL of the new Django portal for re-routing
         */
        'airavata-django-url' => "http://airavata.host:8009/",

        /**
         * Set to true to enable reroute to the new Django portal
         */
        'reroute-to-django' => true,

        /**
         * Set JIRA Issue Collector scripts here.
         */
        'jira-help' => 
        [
            /**
             * Report Issue Script issued for your app by Atlassian JIRA
             */
            'report-issue-script' => '',
            /**
             * Collector id at the end of the above script
             */
            'report-issue-collector-id' => '',
            /**
             * Create Report Script issued for your app by Atlassian JIRA
             */
            'request-feature-script' => '',
            /**
             * Collector id at the end of the above script
             */
            'request-feature-collector-id' => ''
        ],

        /**
         * Set Google Analytics Id here. ID format that generates from  
         * creating tracker object should be 
         *
         * UA-XXXXX-Y 
         *
         * for it to be working correctly. Currently it is only set for 
         * sending pageviews.
         */
        'google-analytics-id' => ''
    ]
);
