<?php

namespace Airavata\Facades;

use Illuminate\Support\Facades\Facade;

class UserProfileServices extends Facade {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    protected static function getFacadeAccessor() { return 'user_profile_services'; }

}