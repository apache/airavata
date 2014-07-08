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

use Airavata\Model\AppCatalog\ComputeResource\ComputeResourceDescription;

try
{
    if ($argc != 3)
    {
        echo 'php registerComputeResource.php <hostName> <resourceDescription>';
    }
    else
    {
        $hostName = $argv[1];
        $resourceDescription = $argv[2];

        $computeResource = new ComputeResourceDescription();
        $computeResource->hostName = $hostName;
        $computeResource->resourceDescription = $resourceDescription;




        $computeResourceId = $airavataclient->registerComputeResource($computeResource);

        if ($computeResourceId)
        {
            var_dump($computeResource);
            echo "\n Compute Resource $computeResourceId is registered! \n    ";
        }
        else
        {
            echo "\n Failed to register compute resource description. \n";
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
catch (\Exception $e)
{
    echo 'Exception!<br><br>' . $e->getMessage();
}

$transport->close();

?>

