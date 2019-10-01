<?php namespace Wsis;

use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Config;

class WsisServiceProvider extends ServiceProvider {

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
        $this->package('wsis/wsis');
    }

	/**
	 * Register the service provider.
	 *
	 * @return void
	 */
	public function register()
	{
        //registering service provider
        $this->app['wsis'] = $this->app->share(function($app)
        {
            $wsisConfig = Config::get('pga_config.wsis');
            if( $wsisConfig['tenant-domain'] == "")
                $adminUsername = $wsisConfig['admin-username'];
            else
                $adminUsername = $wsisConfig['admin-username'] . "@" . $wsisConfig['tenant-domain'];
            return new Wsis(
                $adminUsername,
                $wsisConfig['admin-password'],
                $wsisConfig['server'],
                $wsisConfig['service-url'],
                $wsisConfig['cafile-path'],
                $wsisConfig['verify-peer'],
                $wsisConfig['allow-self-signed-cert']
            );
        });

        //registering alis
        $this->app->booting(function()
        {
            $loader = \Illuminate\Foundation\AliasLoader::getInstance();
            $loader->alias('WSIS', 'Wsis\Facades\Wsis');
        });
	}

	/**
	 * Get the services provided by the provider.
	 *
	 * @return array
	 */
	public function provides()
	{
		return array('wsis');
	}

}
