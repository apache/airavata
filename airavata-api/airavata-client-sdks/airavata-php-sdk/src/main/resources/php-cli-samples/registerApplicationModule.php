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

try
{
    if ($argc != 4)
    {
        echo 'php registerApplicationModule.php <appModuleName> <appModuleVersion> <appModuleDescription>';
    }
    else
    {
        $appModuleName = $argv[1];
        $appModuleVersion = $argv[2];
        $appModuleDescription = $argv[3];

        $appModule = new ApplicationModule();
        $appModule->appModuleName = $appModuleName;
        $appModule->appModuleVersion = $appModuleVersion;
        $appModule->appModuleDescription = $appModuleDescription;

        $appModuleId = $airavataclient->registerApplicationModule($appModule);

        if ($appModuleId)
        {
            var_dump($appModule);
            echo "Application Module $appModuleId is registered! \n    ";
        }
        else
        {
            echo "Failed to register application module. \n";
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

