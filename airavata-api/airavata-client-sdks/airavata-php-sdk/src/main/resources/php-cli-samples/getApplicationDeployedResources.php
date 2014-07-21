<?php

/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Airavata\Model\Workspace\Experiment\ExperimentState;
use Thrift\Exception\TTransportException;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

if ($argc != 2) {
    exit("Usage: php getApplicationDeployedResources.php <appModuleID> \n");
}

$appModuleId = $argv[1];

$deployedresources = get_application_deployed_resources($appModuleId);

if (empty($deployedresources)) {
    echo "deployment returned an empty list \n";
} else {
    foreach ($deployedresources as $resource) {
        echo "$resource->type: $resource->value      <br><br>";
    }
}

var_dump($deployedresources);


$transport->close();

/**
 * Get the list of deployed hosts with the given ID
 * @param $appModuleId
 * @return null
 */
function get_application_deployed_resources($appModuleId)
{
    global $airavataclient;

    try {
        return $airavataclient->getAppModuleDeployedResources($appModuleId);
    } catch (InvalidRequestException $ire) {
        echo 'InvalidRequestException!<br><br>' . $ire->getMessage();
    } catch (ExperimentNotFoundException $enf) {
        echo 'ExperimentNotFoundException!<br><br>' . $enf->getMessage();
    } catch (AiravataClientException $ace) {
        echo 'AiravataClientException!<br><br>' . $ace->getMessage();
    } catch (AiravataSystemException $ase) {
        echo 'AiravataSystemException!<br><br>' . $ase->getMessage();
    } catch (TTransportException $tte) {
        echo 'TTransportException!<br><br>' . $tte->getMessage();
    } catch (\Exception $e) {
        echo 'Exception!<br><br>' . $e->getMessage();
    }

}

?>

