<?php
/**
 * Bundle all thrift and Airavata stubs into a include file. This is simple but not so elegant way.
 *  Contributions welcome to improve writing PHP Client Samples.
 *
 */
include 'getAiravataClient.php';
global $airavataclient;
global $transport;

Airavata\Client\Samples\sampleDisabled();

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Thrift\Exception\TTransportException;

use Airavata\Model\AppCatalog\AppDeployment\ApplicationModule;

try {

    if (count($argv) != 3) {
        exit("\n Incorrect Arguments \n. Usage: deleteGatewayComputeResourcePreference.php <gateway id> <compute resource id>. \n");
    } else {

        $gatewayId = $argv[1];
        $computeResourceId = $argv[2];

        $success = $airavataclient->deleteGatewayComputeResourcePreference($gatewayId, $computeResourceId);

        if ($success) {
            echo "Gateway Profile for $gatewayId for resource $computeResourceId is successfully deleted";
        } else {
            echo "\n Failed to delete gateway profile $computeResourceId \n";
        }
    }
} catch (InvalidRequestException $ire) {
    print 'InvalidRequestException: ' . $ire->getMessage() . "\n";
} catch (AiravataClientException $ace) {
    print 'Airavata System Exception: ' . $ace->getMessage() . "\n";
} catch (AiravataSystemException $ase) {
    print 'Airavata System Exception: ' . $ase->getMessage() . "\n";
} catch (TTransportException $tte) {
    echo 'TTransportException!<br><br>' . $tte->getMessage();
} catch (\Exception $e) {
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

