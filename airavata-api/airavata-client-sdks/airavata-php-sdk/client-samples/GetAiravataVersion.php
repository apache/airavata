<?php

//use Airavata\Client;

ini_set("include_path", ini_get("include_path") . PATH_SEPARATOR . "../lib" . PATH_SEPARATOR .
    "../lib/Thrift" . PATH_SEPARATOR . "../lib/Airavata/" . PATH_SEPARATOR);
require_once 'autoload.php';

require_once 'AiravataClientFactory.php';

use Airavata\Client\AiravataClientFactory;

$airavataClientFactory = new AiravataClientFactory(array('airavataServerHost' => "gw111.iu.xsede.org", 'airavataServerPort' => "8930"));

$airavata = $airavataClientFactory->getAiravataClient();

echo "Airavata Server Version is: " . $airavata->GetAPIVersion();