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
use Airavata\API\Error\InvalidRequestException;
use Thrift\Exception\TTransportException;
use Airavata\Model\AppCatalog\AppInterface\DataType;

try
{

    if ($argc < 2)
    {
        echo 'php getGatewayComputeResourcePreference.php <gateway id> <compute resource id>';
    }
    else {

        $gatewayId = $argv[1];
        $computeResourceId = $argv[2];

        $computeResourcePreferences = $airavataclient->getGatewayComputeResourcePreference($gatewayId, $computeResourceId);

        if ($computeResourcePreferences) {
                var_dump($computeResourcePreferences);
        } else {
            echo "\n Failed to fetch compute resource preference Inputs. \n";
        }
}
}
catch (InvalidRequestException $ire)
{
    print 'InvalidRequestException: ' . $ire->getMessage()."\n";
}
catch (AiravataClientException $ace)
{
    print 'Airavata System Exception: ' . $ace->getMessage()."\n";
}
catch (AiravataSystemException $ase)
{
    print 'Airavata System Exception: ' . $ase->getMessage()."\n";
}
catch (TTransportException $tte)
{
    echo 'TTransportException!<br><br>' . $tte->getMessage();
}
catch (\Exception $e)
{
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

