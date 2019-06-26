<?php

namespace Wsis\Facades;

use Illuminate\Support\Facades\Facade;

class Wsis extends Facade {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    protected static function getFacadeAccessor() { return 'wsis'; }

}