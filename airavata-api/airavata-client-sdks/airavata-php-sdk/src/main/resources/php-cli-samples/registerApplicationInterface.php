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

use Airavata\Model\AppCatalog\AppInterface\ApplicationInterfaceDescription;

try
{
    if ($argc != 4)
    {
        echo 'php registerApplicationInterface.php <appName> <appDescription> <appModuleId>';
    }
    else
    {
        $appName = $argv[1];
        $appDescription = $argv[2];
        $appModuleId = $argv[3];

        $appInterface = new ApplicationInterfaceDescription();
        $appInterface->applicationName = $appName;
        $appInterface->applicationDesription = $appDescription;

        //Add the module provided in argument. If you would like to add multiple modules, you may modify this script or
        // use the updateAppInterface.php script
        $appModuleList = array($appModuleId);
        $appInterface->applicationModules=$appModuleList;
        var_dump($appModuleList);

        $appInterfaceId = $airavataclient->registerApplicationInterface($appInterface);

        if ($appInterfaceId)
        {
            var_dump($appInterface);
            echo "\n Application Interface $appInterfaceId is registered! \n    ";
        }
        else
        {
            echo "\n Failed to register application interface. \n";
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

