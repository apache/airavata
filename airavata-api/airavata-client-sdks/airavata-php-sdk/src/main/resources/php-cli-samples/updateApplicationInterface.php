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

use Airavata\Model\AppCatalog\AppInterface\ApplicationInterfaceDescription;
use Airavata\Model\AppCatalog\AppInterface\InputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\OutputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\DataType;

try
{
    if ($argc != 2)
    {
        echo 'php updateApplicationInterface.php <appInterfaceId>';
    }
    else
    {
        $appInterfaceId = $argv[1];
        //$appDescription = $argv[2];
        //$appModuleId = $argv[3];


        $appInputs = new InputDataObjectType();
        $appInputs->name = "Input_to_Echo";
        $appInputs->userFriendlyDescription = "A string to test echo application";
        $appInputs->type = DataType::STRING;

        $appOutputs = new OutputDataObjectType();
        $appOutputs->name = "Echoed_Output";
        $appOutputs->type = DataType::STRING;

        $appInterface = $airavataclient->getApplicationInterface($appInterfaceId);
        $appInterface->applicationInputs = array($appInputs);
        $appInterface->applicationOutputs = array($appOutputs);
        var_dump($appInterface);

        $status = $airavataclient->updateApplicationInterface($appInterfaceId, $appInterface);

        if ($status)
        {
            echo "\n Application Interface $appInterfaceId is updated! \n    ";
        }
        else
        {
            echo "\n Failed to update application interface. \n";
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

