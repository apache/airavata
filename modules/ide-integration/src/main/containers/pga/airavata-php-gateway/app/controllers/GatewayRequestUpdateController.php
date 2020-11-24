<?php

class GatewayRequestUpdateController extends BaseController
{

    public function updateGateway(){

        $gateway = AdminUtilities::update_form( Input::get("gateway-id"), Input::all() );

        $gatewayData = array ("airavataInternalGatewayId" => $gateway->airavataInternalGatewayId,
                              "gatewayId" => $gateway->gatewayId,
                              "gatewayName" => $gateway->gatewayName,
                              "emailAddress" => $gateway->emailAddress,
                              "publicProjectDescription" => $gateway->gatewayPublicAbstract,
                              "gatewayURL" => $gateway->gatewayURL,
                              "adminFirstName" => $gateway->gatewayAdminFirstName,
                              "adminLastName" => $gateway->gatewayAdminLastName,
                              "adminUsername" => $gateway->identityServerUserName,
                              "adminEmail" => $gateway->gatewayAdminEmail,
                              "projectDetails" => $gateway->reviewProposalDescription);

        return View::make("account/update")->with(array('gatewayData'=>$gatewayData));

    }

    public function updateDetails(){

        $inputs = Input::all();
        $rules = array(
            "email" => "required|email",
        );

        $messages = array();

        $checkValidation = array();
        $checkValidation["email"] = $inputs["email-address"];

        $validator = Validator::make( $checkValidation, $rules, $messages);
        if ($validator->fails()) {
            Session::put("validationMessages", $validator->messages() );
            return Redirect::back()->withErrors($validator);
        }
        else {
            $returnVal = AdminUtilities::user_update_gateway(Input::get("internal-gateway-id"), Input::all());

            if ($returnVal == 1) {
                $email = Config::get('pga_config.portal')['admin-emails'];
                EmailUtilities::gatewayUpdateMailToProvider(Input::get("email-address"), Input::get("gateway-id"));
                EmailUtilities::gatewayUpdateMailToAdmin($email, Input::get("gateway-id"));
                Session::put("message", "Your Gateway has been updated");
            } else
                Session::put("errorMessages", "Error: Please try again or contact admin to report the issue.");
        }

        return Redirect::to("admin/dashboard");

    }

}