<?php

namespace Keycloak\Facades;

use Illuminate\Support\Facades\Facade;

class Keycloak extends Facade {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    protected static function getFacadeAccessor() { return 'keycloak'; }

}
