<?php

namespace Airavata\Client;

$GLOBALS['THRIFT_ROOT'] = 'Thrift/';
//require_once $GLOBALS['THRIFT_ROOT'] . 'Thrift.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TTransport.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Transport/TSocket.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Protocol/TBinaryProtocol.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Exception/TTransportException.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Type/TMessageType.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'Factory/TStringFuncFactory.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/TStringFunc.php';
require_once $GLOBALS['THRIFT_ROOT'] . 'StringFunc/Core.php';

$GLOBALS['AIRAVATA_ROOT'] = 'Airavata/';
require_once $GLOBALS['AIRAVATA_ROOT'] . 'API/Airavata.php';

use Thrift\Protocol\TBinaryProtocol;
use Thrift\Transport\TSocket;
use Airavata\API\AiravataClient;

class AiravataClientFactory
{

    private $airavataServerHost;
    private $airavataServerPort;

    public function __construct($options)
    {
        $this->airavataServerHost = isset($options['airavataServerHost']) ? $options['airavataServerHost'] : null;
        $this->airavataServerPort = isset($options['airavataServerPort']) ? $options['airavataServerPort'] : null;
    }

    public function getAiravataClient()
    {
        $transport = new TSocket($this->airavataServerHost, $this->airavataServerPort);
        $protocol = new TBinaryProtocol($transport);
	$transport->open();
        return new AiravataClient($protocol);
    }
}
