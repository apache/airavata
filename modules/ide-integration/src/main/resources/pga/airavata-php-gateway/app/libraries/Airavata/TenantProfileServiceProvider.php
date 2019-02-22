<?php namespace Airavata;

use Airavata\Service\Profile\Tenant\CPI\TenantProfileServiceClient;
use Illuminate\Routing\UrlGenerator;
use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Config;
use Thrift\Transport\TSocket;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Protocol\TMultiplexedProtocol;
use Illuminate\Routing\Redirector;

class TenantProfileServiceProvider extends ServiceProvider {

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
        $this->package('airavata/tenant_profile_services');
    }

	/**
	 * Register the service provider.
	 *
	 * @return void
	 */
	public function register()
	{
        //registering service provider
        $this->app['tenant_profile_services'] = $this->app->share(function($app)
        {
            try{
                $transport = new TSocket(
                    Config::get('pga_config.airavata')['airavata-profile-service-server'],
                    Config::get('pga_config.airavata')['airavata-profile-service-port']
                );
                $transport->setRecvTimeout( Config::get('pga_config.airavata')['airavata-timeout']);
                $transport->setSendTimeout( Config::get('pga_config.airavata')['airavata-timeout']);

                $protocol = new TBinaryProtocol($transport);
                $protocol = new TMultiplexedProtocol($protocol, "TenantProfileService");
                $transport->open();

                $client = new TenantProfileServiceClient($protocol);

            }catch (\Exception $ex){
                throw new \Exception("Unable to instantiate Airavata TenantProfileService Client", 0,  $ex);
            }

            if( is_object( $client))
                return $client;
            else
                throw new \Exception("Unable to instantiate Airavata TenantProfileService Client");
        });

        //registering alis
        $this->app->booting(function()
        {
            $loader = \Illuminate\Foundation\AliasLoader::getInstance();
            $loader->alias('TenantProfileService', 'Airavata\Facades\TenantProfileServices');
        });
	}

	/**
	 * Get the services provided by the provider.
	 *
	 * @return array
	 */
	public function provides()
	{
		return array('tenant_profile_services');
	}

}
