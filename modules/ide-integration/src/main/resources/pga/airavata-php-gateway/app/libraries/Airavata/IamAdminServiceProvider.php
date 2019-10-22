<?php namespace Airavata;

use Airavata\Service\Iam\Admin\Services\CPI\IamAdminServicesClient;
use Illuminate\Routing\UrlGenerator;
use Illuminate\Support\ServiceProvider;
use Illuminate\Support\Facades\Config;
use Airavata\API\AiravataClient;
use Thrift\Transport\TSocket;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Protocol\TMultiplexedProtocol;
use Illuminate\Routing\Redirector;

class IamAdminServiceProvider extends ServiceProvider {

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
        $this->package('airavata/iam_admin_services');
    }

	/**
	 * Register the service provider.
	 *
	 * @return void
	 */
	public function register()
	{
        //registering service provider
        $this->app['iam_admin_services'] = $this->app->share(function($app)
        {
            try{
                $transport = new TSocket(
                    Config::get('pga_config.airavata')['airavata-profile-service-server'],
                    Config::get('pga_config.airavata')['airavata-profile-service-port']
                );
                $transport->setRecvTimeout( Config::get('pga_config.airavata')['airavata-timeout']);
                $transport->setSendTimeout( Config::get('pga_config.airavata')['airavata-timeout']);

                $protocol = new TBinaryProtocol($transport);
                $protocol = new TMultiplexedProtocol($protocol, "IamAdminServices");
                $transport->open();

                $client = new IamAdminServicesClient($protocol);

            }catch (\Exception $ex){
                throw new \Exception("Unable to instantiate Airavata IamAdminServices Client", 0,  $ex);
            }

            if( is_object( $client))
                return $client;
            else
                throw new \Exception("Unable to instantiate Airavata IamAdminServices Client");
        });

        //registering alis
        $this->app->booting(function()
        {
            $loader = \Illuminate\Foundation\AliasLoader::getInstance();
            $loader->alias('IamAdminServices', 'Airavata\Facades\IamAdminServices');
        });
	}

	/**
	 * Get the services provided by the provider.
	 *
	 * @return array
	 */
	public function provides()
	{
		return array('iam_admin_services');
	}

}
