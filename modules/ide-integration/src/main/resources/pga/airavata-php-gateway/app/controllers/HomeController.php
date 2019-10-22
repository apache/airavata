<?php

class HomeController extends BaseController {

	/*
	|--------------------------------------------------------------------------
	| Default Home Controller
	|--------------------------------------------------------------------------
	|
	| You may wish to use controllers instead of, or in addition to, Closure
	| based routes. That's great! Here is an example controller method to
	| get you started. To route to this controller, just add the route:
	|
	|	Route::get('/', 'HomeController@showWelcome');
	|
	*/

	public function getIndex()
	{
		Session::put("nav-active", "home");
		// If not logged in and theme has a landing page, display the landing page
		if (!CommonUtilities::id_in_session()){
			try {
				$theme = Theme::uses( Session::get("theme") );
				// FIXME: can't figure out how to pass variables to the landingpage template
				return View::make($theme->scope('landingpage')->location());
			}catch (Exception $ex){
				Log::debug("Theme has no landingpage view, will render standard home page", array($ex->getMessage()));
			}
		}
		return View::make('home');
	}

}

?>
