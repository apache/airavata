<?php namespace Keycloak;

use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Config;

class KeycloakServiceProvider extends ServiceProvider {

    /**
    * Indicates if loading of the provider is deferred.
    *
    * @var bool
    */
    protected $defer = false;

    /**
    * Bootstrap the application events.
    *
    * @return void
    */
    public function boot()
    {
        $this->package('keycloak/keycloak');
    }

    /**
    * Register the service provider.
    *
    * @return void
    */
    public function register()
    {
        //registering service provider
        $this->app['keycloak'] = $this->app->share(function($app)
        {
            $identityServerConfig = Config::get('pga_config.wsis');
            $airavataConfig = Config::get('pga_config.airavata');
            return new Keycloak(
                $identityServerConfig['tenant-domain'],
                // TODO: we can derive this from tenant-domain and service-url
                $identityServerConfig['openid-connect-discovery-url'],
                $identityServerConfig['oauth-client-key'],
                $identityServerConfig['oauth-client-secret'],
                $identityServerConfig['oauth-callback-url'],
                $identityServerConfig['cafile-path'],
                $identityServerConfig['verify-peer'],
                $identityServerConfig['service-url'],
                $identityServerConfig['admin-username'],
                $identityServerConfig['admin-password'],
                $airavataConfig['gateway-id']
            );
        });

        //registering alis
        $this->app->booting(function()
        {
            $loader = \Illuminate\Foundation\AliasLoader::getInstance();
            $loader->alias('Keycloak', 'Keycloak\Facades\Keycloak');
        });
    }

    /**
    * Get the services provided by the provider.
    *
    * @return array
    */
    public function provides()
    {
        return array('keycloak');
    }

}
