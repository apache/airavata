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

use Airavata\Model\AppCatalog\AppDeployment\ApplicationModule;
use Airavata\Model\AppCatalog\AppDeployment\ApplicationDeploymentDescription;

try
{
    if ($argc != 4)
    {
        echo 'php registerAppDeployment.php <appModuleId> <computeHostId> <executablePath>';
    }
    else
    {
        $appModuleId = $argv[1];
        $computeHostId = $argv[2];
        $executablePath = $argv[3];

        $appDeployment = new ApplicationDeploymentDescription();
        $appDeployment->appModuleId = $appModuleId;
        $appDeployment->computeHostId = $computeHostId;
        $appDeployment->executablePath = $executablePath;



        $appDeploymentId = $airavataclient->registerApplicationDeployment($appDeployment);

        if ($appDeploymentId)
        {
            var_dump($appDeployment);
            echo "Application Deployment $appDeploymentId is registered! \n    ";
        }
        else
        {
            echo "Failed to register application deployment. \n";
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

