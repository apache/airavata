<?php

namespace Airavata\Facades;

use Illuminate\Support\Facades\Facade;

class TenantProfileServices extends Facade {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    protected static function getFacadeAccessor() { return 'tenant_profile_services'; }

}