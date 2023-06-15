<?php

class GatewayprofileController extends BaseController {

	public function __construct()
	{
		$this->beforeFilter('verifyadmin');
		Session::put("nav-active", "gateway-profile");
	}

	public function createView()
	{
		return View::make("gateway/create");
	}

	public function createSubmit()
	{
		$gatewayProfileId = CRUtilities::create_or_update_gateway_profile( Input::all() );
		//TODO:: Maybe this is a better way. Things to ponder upon.
		//return Redirect::to("gp/browse")->with("gpId", $gatewayProfileId);
		return Redirect::to("gp/browse")->with("message","Gateway has been created. You can set preferences now.");
	}

	public function editGP()
	{
		$gatewayProfileId = CRUtilities::create_or_update_gateway_profile( Input::all(), true );
		return Redirect::to("gp/browse")->with("message","Gateway has been created. You can set preferences now.");
	}

	public function browseView()
	{
		//var_dump( $crObjects[0]); exit;
		return View::make("gateway/browse", array(	"gatewayProfiles" => CRUtilities::getAllGatewayProfilesData(),
													"computeResources" => CRUtilities::getAllCRObjects(),
													"crData" => CRUtilities::getEditCRData()
 												));
	}

	public function modifyCRP()
	{
		if( CRUtilities::add_or_update_CRP( Input::all()) )
		{            
			if( Request::ajax()){
				return 1;
			}
			else
				return Redirect::to("admin/dashboard/gateway")->with("message","Compute Resource Preference has been set.");
		}
	}

	public function modifySRP()
	{
		if( SRUtilities::add_or_update_SRP( Input::all()) )
		{
			if( Request::ajax()){
				return 1;
			}
			else
				return Redirect::to("admin/dashboard/gateway")->with("message","Storage Resource Preference has been set.");
		}
	}

	public function modifyIDP()
	{
		if( AdminUtilities::add_or_update_IDP( Input::all()) )
		{
			if( Request::ajax()){
				return 1;
			}
			else
				return Redirect::to("admin/dashboard/gateway")->with("message","Identity Server Preference has been set.");
		}
	}

	public function delete()
	{
		$error = false;
		if( Input::has("del-gpId")) // if Gateway has to be deleted
		{
			if( CRUtilities::deleteGP( Input::get("del-gpId")) )
				return Redirect::to("admin/dashboard/gateway")->with("message","Gateway Profile has been deleted.");
			else
				$error = true;
		}
		else if( Input::has("rem-crId")) // if Compute Resource has to be removed from Gateway
		{
			if(CRUtilities::deleteCR( Input::all()) )
				return Redirect::to("admin/dashboard/gateway")->with("message", "The selected Compute Resource has been successfully removed");
			else
				$error = true;
		}
		else if( Input::has("rem-srId")) // if Compute Resource has to be removed from Gateway
		{
			if(CRUtilities::deleteSR( Input::all()) )
				return Redirect::to("admin/dashboard/gateway")->with("message", "The selected Compute Resource has been successfully removed");
			else
				$error = true;
		}
		else
			$error = true;


		if( $error)
		{
			return Redirect::to("admin/dashboard/gateway")->with("message","An error has occurred. Please try again later or report a bug using the link in the Help menu");
		}
	}

	public function cstChange(){
		$inputs = Input::all();
		
		if( CRUtilities::updateGatewayProfile( $inputs) )
		{
            return "Credential Store Token has been updated";     
        }
        else
            return "An error has occurred. Please try again later or report a bug using the link in the Help menu";
	}
}

?>