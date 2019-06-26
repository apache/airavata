<?php

class ApplicationController extends BaseController {

	public function __construct()
	{
		$this->beforeFilter('verifyadmin');
		Session::put("nav-active", "app-catalog");
	}

	public function showAppModuleView()
	{
		$data = array();
		$data["modules"] = AppUtilities::getAllModules();
        Session::put("admin-nav", "app-module");
		return View::make('application/module', $data);
	}

	public function modifyAppModuleSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		$update = false;
		if( Input::has("appModuleId") )
			$update = true;
			
		if( AppUtilities::create_or_update_appModule( Input::all(), $update ) )
		{
			if( $update)
				$message = "Module has been updated successfully!";
			else
				$message = "Module has been created successfully!";
		}
		else
			$message = "An error has occurred. Please report the issue.";


		return Redirect::to("app/module")->with("message", $message);
	}

	public function deleteAppModule()
	{
        
        $this->beforeFilter('verifyeditadmin');
        $data = AppUtilities::getAppInterfaceData();
        $connections = AppUtilities::checkAppModuleConnections( Input::get("appModuleId"));
        if( count( $connections["appInterfaces"]) > 0 || count( $connections["appDeployments"]) > 0)
        {	
        	$message = "App Module you are trying to delete is connected to - <br/>";
        	foreach( $connections["appInterfaces"] as $index => $interface)
        	{
        		$message .= "Interface " . ($index + 1) . " : " . $interface . "<br/>";
        	}
        	foreach( $connections["appDeployments"] as $index => $deployment)
        	{
        		$message .= "Deployment " . ($index + 1) . " : " . $deployment . "<br/>";
        	}
        	$message .= "<br/>Please disconnect it from all the above interfaces and deployments to be able to delete it.";
        	return Redirect::to("app/module")->with("errorMessage", $message);
        }
        else{
			if( AppUtilities::deleteAppModule( Input::get("appModuleId") ) )
				$message = "Module has been deleted successfully!";
			else
				$message = "An error has occurred. Please report the issue.";
		}

		return Redirect::to("app/module")->with("message", $message);
	}

	public function showAppInterfaceView()
	{
		$data = AppUtilities::getAppInterfaceData();
        Session::put("admin-nav", "app-interface");
		return View::make("application/interface", $data);
	}

	public function createAppInterfaceSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		$appInterfaceValues = Input::all();
		//var_dump( $appInterfaceValues); exit;
		AppUtilities::create_or_update_appInterface( $appInterfaceValues);

		return Redirect::to( "app/interface")->with("message","Application Interface <strong>" . $appInterfaceValues["applicationName"] . "</strong> has been created.");
	}

	public function cloneAppInterfaceSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		$appInterfaceValues = Input::all();

		AppUtilities::clone_appInterface( $appInterfaceValues);

		return Redirect::to( "app/interface")->with("message","Clone Application Interface <strong>" . $appInterfaceValues["applicationName"] . "</strong> has been created.");
	}

	public function editAppInterfaceSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		if( Input::has("app-interface-id"))
		{
			$update = true;
			$appInterfaceValues = Input::all();
			//var_dump( $appInterfaceValues); exit;
			AppUtilities::create_or_update_appInterface( $appInterfaceValues, $update);
			$message = "Application Interface has been updated!";
		}
		else
		{
			$message = "An error has occurred. Please report the issue.";
		}
		return Redirect::to( "app/interface")->with("message", $message);

	}

	public function deleteAppInterface()
	{
        $this->beforeFilter('verifyeditadmin');
		if( AppUtilities::deleteAppInterface( Input::get("appInterfaceId") ) )
			$message = "Interface has been deleted successfully!";
		else
			$message = "An error has occurred. Please report the issue.";

		return Redirect::to("app/interface")->with("message", $message);

	}

	public function showAppDeploymentView()
	{
		$data = AppUtilities::getAppDeploymentData();
		//var_dump( $data); exit;
        Session::put("admin-nav", "app-deployment");
		return View::make("application/deployment", $data);
	}

	public function createAppDeploymentSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		$appDeploymentValues = Input::all();
		AppUtilities::create_or_update_appDeployment( $appDeploymentValues );
		return Redirect::to("app/deployment")->with("message", "App Deployment was created successfully!");
	}

	public function editAppDeploymentSubmit()
	{
        $this->beforeFilter('verifyeditadmin');
		if( Input::has("app-deployment-id"))
		{
			$update = true;
			$appDeploymentValues = Input::all();
			AppUtilities::create_or_update_appDeployment( $appDeploymentValues, $update);
			$message = "Application Deployment has been updated!";
		}
		else
		{
			$message = "An error has occurred. Please report the issue.";
		}
		return Redirect::to( "app/deployment")->with("message", $message);
	}

	public function deleteAppDeployment()
	{
        $this->beforeFilter('verifyeditadmin');
		if( AppUtilities::deleteAppDeployment( Input::get("appDeploymentId") ) )
			$message = "Deployment has been deleted successfully!";
		else
			$message = "An error has occurred. Please report the issue.";

		return Redirect::to("app/deployment")->with("message", $message);

	}

}

?>