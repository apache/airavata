<?php

$GLOBALS['THRIFT_ROOT'] = 'lib/Thrift/';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TBufferedTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TApplicationException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TProtocolException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TTransportException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Base/TBase.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TMessageType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Factory/TStringFuncFactory.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/TStringFunc.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/Core.php';

$GLOBALS['AIRAVATA_ROOT'] = 'lib/Airavata/';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Airavata.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/AppCatalog/AppInterface/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'Model/Workspace/Experiment/Types.php';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Error/Types.php';

require_once 'lib/AiravataClientFactory.php';

use Airavata\API\Error\AiravataClientException;
use Airavata\API\Error\AiravataSystemException;
use Airavata\API\Error\InvalidRequestException;
use Airavata\Client\AiravataClientFactory;
use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TBufferedTransport;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;
use Airavata\Model\Workspace\Project;
use Airavata\Model\Workspace\Experiment\Experiment;
use Airavata\Model\Workspace\Experiment\UserConfigurationData;
use Airavata\Model\Workspace\Experiment\ComputationalResourceScheduling;
use Airavata\API\Error\ExperimentNotFoundException;
use Airavata\Model\Workspace\Experiment\ExperimentState;
use Thrift\Exception\TTransportException;
use Thrift\Exception\TException;
use Airavata\Model\AppCatalog\AppInterface\InputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\OutputDataObjectType;
use Airavata\Model\AppCatalog\AppInterface\DataType;



function createProject($projectName){
    $airavataconfig = parse_ini_file("conf/airavata-client-properties.ini");
    $transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
    $transport->setRecvTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
    $transport->setSendTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
    $gatewayId = $airavataconfig['AIRAVATA_GATEWAY'];
    $owner = $airavataconfig['AIRAVATA_LOGIN'];
    $protocol = new TBinaryProtocol($transport);
    try
    {
        $transport->open();
        $airavataclient = new AiravataClient($protocol);
        $project = new Project();
        $project->owner = $owner;
        $project->name = $projectName;
        $projId = $airavataclient->createProject($gatewayId, $project);
        $outputData = array();

        if ($projId)
        {
            $outputData["ProjectId"] = $projId;
            // return json_encode($output);
        }
        else
        {
            $outputData["error"] = 'Failed to create project.';
        }

        $transport->close();
    }
    catch (InvalidRequestException $ire)
    {
        $outputData["error"] = 'InvalidRequestException: ' . $ire->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ase->getMessage();
    }
    catch(TException $tx)
    {
        $outputData["error"] = 'There is some connection problem, please check if airavata is runnig properly and try again later';
    }
    catch (\Exception $e)
    {
        $outputData["error"] = 'Exception: ' . $e->getMessage();
    }
    return json_encode($outputData);
}

function createExperiment($expName, $projId, $appId, $inp){
    $airavataconfig = parse_ini_file("conf/airavata-client-properties.ini");
    $transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
    $transport->setSendTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
    $protocol = new TBinaryProtocol($transport);
    try
    {
        $transport->open();
        $airavataclient = new AiravataClient($protocol);
        /* Experiment input and output data. */
        $input = new InputDataObjectType();
        $input->name = "input";
        $input->value = $inp;
        $input->type = DataType::STRING;
        $exInputs = array($input);
        $output = new OutputDataObjectType();
        $output->name = "output";
        $output->value = "";
        $output->type = DataType::STDOUT;
        $err = new OutputDataObjectType();
        $err->name = "output_err";
        $err->value = "";
        $err->type = DataType::STDERR;
        $exOutputs = array($output,$err);

        /* Create Experiment: needs to update using unique project ID. */
        $user = $airavataconfig['AIRAVATA_LOGIN'];
        $host = $airavataconfig['AIRAVATA_SERVER'];
        $hostname = $airavataconfig['AIRAVATA_SERVER_ALIAS'];
        $gatewayId = $airavataconfig['AIRAVATA_GATEWAY'];
        $proAccount = $airavataconfig['AIRAVATA_PROJECT_ACCOUNT'];
        $exp_name = $expName;
        $proj = $projId;

        $experiment = new Experiment();
        $experiment->projectID = $proj;
        $experiment->userName = $user;
        $experiment->name = $exp_name;
        $experiment->applicationId = $appId;
        $experiment->experimentInputs = $exInputs;
        $experiment->experimentOutputs = $exOutputs;
        $computeResources = $airavataclient->getAvailableAppInterfaceComputeResources($appId);
        if(isset($computeResources) && !empty($computeResources)){
            foreach ($computeResources as $key => $value) {
                if($value == $host || $value == $hostname){
                    $cmRST = new ComputationalResourceScheduling();
                    $cmRST->resourceHostId = $key;
                    $cmRST->computationalProjectAccount = $proAccount;
                    $cmRST->nodeCount = 1;
                    $cmRST->numberOfThreads = 1;
                    $cmRST->queueName = "normal";
                    $cmRST->totalCPUCount = 1;
                    $cmRST->wallTimeLimit = 30;
                    $cmRST->jobStartTime = 0;
                    $cmRST->totalPhysicalMemory = 1;
                    $userConfigurationData = new UserConfigurationData();
                    $userConfigurationData->airavataAutoSchedule = 0;
                    $userConfigurationData->overrideManualScheduledParams = 0;
                    $userConfigurationData->computationalResourceScheduling = $cmRST;
                    $experiment->userConfigurationData = $userConfigurationData;
                }
            }
        }
        $outputData = array();
        $expId = $airavataclient->createExperiment($gatewayId,$experiment);
        $transport->close();

        if ($expId)
        {
            $outputData["ExperimentId"] = $expId;
        }
        else
        {
            $outputData["error"] = 'Experiment Not Created';
        }
    }
    catch (InvalidRequestException $ire)
    {
        $outputData["error"] = 'InvalidRequestException: ' . $ire->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ase->getMessage();
    }
    catch(TException $tx)
    {
        $outputData["error"] = 'There is some connection problem, please check if airavata is runnig properly and try again later';
    }
    catch (\Exception $e)
    {
        $outputData["error"] = 'Exception: ' . $e->getMessage();
    }
    return json_encode($outputData);
}

function launchExperiment( $expId){
    $airavataconfig = parse_ini_file("conf/airavata-client-properties.ini");
    $token = $airavataconfig['AIRAVATA_CREDENTIAL_STORE_TOKEN'];
    $transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
    $transport->setSendTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
    $protocol = new TBinaryProtocol($transport);
    try
    {
        $transport->open();
        $airavataclient = new AiravataClient($protocol);
        $airavataclient->launchExperiment($expId, $token);
        $transport->close();
        $outputData = array();
        $outputData["isExperimentLaunched"] = true;
    }
    catch (InvalidRequestException $ire)
    {
        $outputData["error"] = 'InvalidRequestException: ' . $ire->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ase->getMessage();
    }
    catch(TException $tx)
    {
        $outputData["error"] = 'There is some connection problem, please check if airavata is runnig properly and try again later';
    }
    catch (\Exception $e)
    {
        $outputData["error"] = 'Exception: ' . $e->getMessage();
    }
    return json_encode($outputData);
}

function getOutput( $expId)
{
    $airavataconfig = parse_ini_file("conf/airavata-client-properties.ini");
    $transport = new TSocket($airavataconfig['AIRAVATA_SERVER'], $airavataconfig['AIRAVATA_PORT']);
    $transport->setSendTimeout($airavataconfig['AIRAVATA_TIMEOUT']);
    $protocol = new TBinaryProtocol($transport);
    $errors = array(
     'CANCELED' => "Experiment Cancelled",
     'SUSPENDED' => "Experiment Suspended",
     'FAILED' => "Experiment Failed"
        );
    try
    {
        $airavataclient = new AiravataClient($protocol);
        $transport->open();
        while(($status=get_experiment_status($airavataclient, $expId))!="COMPLETED"){
            if(isset($errors[$status])){
              $transport->close();
              return "{\"error\":\"".$errors[$status]."\"}";
              exit();
            }
            sleep(1);
        }
        $outputs =  $airavataclient->getExperimentOutputs($expId);
        $transport->close();
        $outputData = array();
        if(!empty($outputs[0]->value)){
            $outputData["output"] = $outputs[0]->value;
        }else {
            $outputData["error"] = $outputs[1]->value;
        }

    }
    catch (InvalidRequestException $ire)
    {
        $outputData["error"] = 'InvalidRequestException: ' . $ire->getMessage();
    }
    catch (AiravataClientException $ace)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ace->getMessage();
    }
    catch (AiravataSystemException $ase)
    {
        $outputData["error"] = 'Airavata System Exception: ' . $ase->getMessage();
    }
    catch(TException $tx)
    {
        $outputData["error"] = 'There is some connection problem, please check if airavata is runnig properly and try again later';
    }
    catch (ExperimentNotFoundException $enf)
    {
        $outputData["error"] = 'ExperimentNotFoundException: ' . $enf->getMessage();
    }
    catch (TTransportException $tte)
    {
        $outputData["error"] = 'TTransportException: ' . $tte->getMessage();
    }
    catch (\Exception $e)
    {
        $outputData["error"] = 'Exception: ' . $e->getMessage();
    }
    return json_encode($outputData);


}

function get_experiment_status($client, $expId)
{
        try
        {
            $outputData = array();
            $experimentStatus = $client->getExperimentStatus($expId);
            return ExperimentState::$__names[$experimentStatus->experimentState];
            exit();
        }
        catch (InvalidRequestException $ire)
        {
            $outputData["error"] = 'InvalidRequestException: ' . $ire->getMessage();
        }
        catch (AiravataClientException $ace)
        {
            $outputData["error"] = 'Airavata System Exception: ' . $ace->getMessage();
        }
        catch (AiravataSystemException $ase)
        {
            $outputData["error"] = 'Airavata System Exception: ' . $ase->getMessage();
        }
        catch(TException $tx)
        {
            $outputData["error"] = 'There is some connection problem, please check if airavata is runnig properly and try again later';
        }
        catch (ExperimentNotFoundException $enf)
        {
            $outputData["error"] = 'ExperimentNotFoundException: ' . $enf->getMessage();
        }
        catch (TTransportException $tte)
        {
            $outputData["error"] = 'TTransportException: ' . $tte->getMessage();
        }
        catch (\Exception $e)
        {
            $outputData["error"] = 'Exception: ' . $e->getMessage();
        }
        return json_encode($outputData);

}

?>
