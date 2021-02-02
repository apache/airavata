<?php
namespace Wsis\Stubs;
use SoapClient;

class TransactionConfiguration {
  public $transactionManager; // anyType
  public $transactionTimeout; // int
  public $userTransaction; // anyType
}

class AbstractPolicyOperator {
  public $empty; // boolean
  public $firstPolicyComponent; // PolicyComponent
  public $policyComponents; // anyType
}

class PolicyComponent {
  public $type; // short
}

class All {
  public $assertions; // anyType
  public $type; // short
}

class Policy {
  public $alternatives; // Iterator
  public $attributes; // Map
  public $id; // string
  public $name; // string
  public $type; // short
}

class Enum {
  public $value; // string
}

class ThreadFactory {
}

class XmlSchemaObject {
  public $lineNumber; // int
  public $linePosition; // int
  public $metaInfoMap; // Map
  public $sourceURI; // string
}

class XmlSchemaAnnotated {
  public $annotation; // XmlSchemaAnnotation
  public $id; // string
  public $unhandledAttributes; // Attr
}

class XmlSchemaAnnotation {
  public $items; // XmlSchemaObjectCollection
}

class XmlSchemaObjectCollection {
  public $count; // int
  public $iterator; // Iterator
}

class XmlSchemaParticle {
  public $maxOccurs; // long
  public $minOccurs; // long
}

class XmlSchemaElement {
  public $QName; // anyType
  public $abstract; // boolean
  public $block; // XmlSchemaDerivationMethod
  public $blockResolved; // XmlSchemaDerivationMethod
  public $constraints; // XmlSchemaObjectCollection
  public $defaultValue; // string
  public $elementType; // anyType
  public $final; // XmlSchemaDerivationMethod
  public $fixedValue; // string
  public $form; // XmlSchemaForm
  public $name; // string
  public $nillable; // boolean
  public $refName; // anyType
  public $schemaType; // XmlSchemaType
  public $schemaTypeName; // anyType
  public $substitutionGroup; // anyType
  public $type; // XmlSchemaType
}

class XmlSchemaDerivationMethod {
  public $values; // string
}

class XmlSchemaForm {
  public $values; // string
}

class XmlSchemaType {
  public $QName; // anyType
  public $baseSchemaType; // anyType
  public $baseSchemaTypeName; // anyType
  public $dataType; // XmlSchemaDatatype
  public $deriveBy; // XmlSchemaDerivationMethod
  public $final; // XmlSchemaDerivationMethod
  public $finalResolved; // XmlSchemaDerivationMethod
  public $mixed; // boolean
  public $name; // string
}

class XmlSchemaDatatype {
}

class TargetResolver {
}

class LifecycleManager {
}

class SOAPEnvelope {
  public $SOAPBodyFirstElementLocalName; // string
  public $SOAPBodyFirstElementNS; // OMNamespace
  public $body; // SOAPBody
  public $header; // SOAPHeader
  public $version; // SOAPVersion
}

class SOAPBody {
  public $fault; // SOAPFault
  public $firstElementLocalName; // string
  public $firstElementNS; // OMNamespace
}

class SOAPFault {
  public $code; // SOAPFaultCode
  public $detail; // SOAPFaultDetail
  public $exception; // anyType
  public $node; // SOAPFaultNode
  public $reason; // SOAPFaultReason
  public $role; // SOAPFaultRole
}

class SOAPFaultCode {
}

class SOAPFaultDetail {
  public $allDetailEntries; // Iterator
}

class SOAPFaultNode {
  public $nodeValue; // string
}

class SOAPFaultReason {
  public $allSoapTexts; // anyType
  public $firstSOAPText; // SOAPFaultText
}

class SOAPFaultText {
  public $lang; // string
}

class SOAPFaultRole {
  public $roleValue; // string
}

class SOAPHeader {
}

class SOAPVersion {
  public $encodingURI; // string
  public $envelopeURI; // string
  public $faultCodeQName; // anyType
  public $faultDetailQName; // anyType
  public $faultReasonQName; // anyType
  public $faultRoleQName; // anyType
  public $mustUnderstandFaultCode; // anyType
  public $nextRoleURI; // string
  public $receiverFaultCode; // anyType
  public $roleAttributeQName; // anyType
  public $senderFaultCode; // anyType
}

class Attachments {
  public $SOAPPartContentID; // string
  public $SOAPPartContentType; // string
  public $SOAPPartInputStream; // InputStream
  public $allContentIDs; // string
  public $attachmentSpecType; // string
  public $contentIDList; // anyType
  public $contentIDSet; // Set
  public $contentLength; // long
  public $incomingAttachmentStreams; // IncomingAttachmentStreams
  public $incomingAttachmentsAsSingleStream; // InputStream
  public $lifecycleManager; // LifecycleManager
  public $map; // Map
}

class IncomingAttachmentStreams {
  public $nextStream; // IncomingAttachmentInputStream
  public $readyToGetNextStream; // boolean
}

class IncomingAttachmentInputStream {
  public $contentId; // string
  public $contentLocation; // string
  public $contentType; // string
  public $headers; // Map
}

class AuthenticationException {
  public $uiErrorCode; // string
}

class TransportListener {
}

class TransportSender {
}

class Attr {
  public $id; // boolean
  public $name; // string
  public $ownerElement; // Element
  public $schemaTypeInfo; // TypeInfo
  public $specified; // boolean
  public $value; // string
}

class Element {
  public $schemaTypeInfo; // TypeInfo
  public $tagName; // string
}

class TypeInfo {
  public $typeName; // string
  public $typeNamespace; // string
}

class Options {
  public $action; // string
  public $callTransportCleanup; // boolean
  public $exceptionToBeThrownOnSOAPFault; // boolean
  public $faultTo; // EndpointReference
  public $from; // EndpointReference
  public $listener; // TransportListener
  public $logCorrelationIDString; // string
  public $manageSession; // boolean
  public $messageId; // string
  public $parent; // Options
  public $password; // string
  public $properties; // string
  public $relatesTo; // RelatesTo
  public $relationships; // RelatesTo
  public $replyTo; // EndpointReference
  public $soapVersionURI; // string
  public $timeOutInMilliSeconds; // long
  public $to; // EndpointReference
  public $transportIn; // TransportInDescription
  public $transportInProtocol; // string
  public $transportOut; // TransportOutDescription
  public $useSeparateListener; // boolean
  public $userName; // string
}

class Set {
  public $empty; // boolean
}

class Map {
  public $empty; // boolean
}

class Iterator {
}

class LinkedList {
  public $first; // anyType
  public $last; // anyType
}

class LinkedHashMap {
}

class InputStream {
}

class NodeManager {
  public $configurationContext; // ConfigurationContext
}

class AxisMessage {
  public $axisOperation; // AxisOperation
  public $direction; // string
  public $effectivePolicy; // Policy
  public $elementQName; // anyType
  public $extensibilityAttributes; // anyType
  public $key; // anyType
  public $messageFlow; // anyType
  public $messagePartName; // string
  public $modulerefs; // string
  public $name; // string
  public $partName; // string
  public $policyUpdated; // boolean
  public $schemaElement; // XmlSchemaElement
  public $soapHeaders; // anyType
  public $wrapped; // boolean
}

class AxisOperation {
  public $WSAMappingList; // anyType
  public $axisService; // AxisService
  public $axisSpecificMEPConstant; // int
  public $controlOperation; // boolean
  public $faultAction; // string
  public $faultActionNames; // string
  public $faultMessages; // anyType
  public $inputAction; // string
  public $key; // anyType
  public $messageExchangePattern; // string
  public $messageReceiver; // MessageReceiver
  public $messages; // Iterator
  public $moduleRefs; // anyType
  public $name; // anyType
  public $outputAction; // string
  public $phasesInFaultFlow; // anyType
  public $phasesOutFaultFlow; // anyType
  public $phasesOutFlow; // anyType
  public $remainingPhasesInFlow; // anyType
  public $soapAction; // string
  public $style; // string
  public $wsamappingList; // string
}

class AxisService {
  public $EPRs; // string
  public $WSAddressingFlag; // string
  public $active; // boolean
  public $axisServiceGroup; // AxisServiceGroup
  public $bindingName; // string
  public $classLoader; // anyType
  public $clientSide; // boolean
  public $controlOperations; // anyType
  public $customSchemaNamePrefix; // string
  public $customSchemaNameSuffix; // string
  public $customWsdl; // boolean
  public $elementFormDefault; // boolean
  public $enableAllTransports; // boolean
  public $endpointName; // string
  public $endpointURL; // string
  public $endpoints; // Map
  public $excludeInfo; // ExcludeInfo
  public $exposedTransports; // string
  public $fileName; // URL
  public $importedNamespaces; // anyType
  public $key; // anyType
  public $lastUpdate; // long
  public $lastupdate; // long
  public $messageElementQNameToOperationMap; // anyType
  public $modifyUserWSDLPortAddress; // boolean
  public $modules; // anyType
  public $name; // string
  public $nameSpacesMap; // Map
  public $namespaceMap; // Map
  public $objectSupplier; // ObjectSupplier
  public $operations; // Iterator
  public $operationsNameList; // anyType
  public $p2nMap; // Map
  public $parent; // AxisServiceGroup
  public $portTypeName; // string
  public $publishedOperations; // anyType
  public $schemaLocationsAdjusted; // boolean
  public $schemaMappingTable; // Map
  public $schemaTargetNamespace; // string
  public $schemaTargetNamespacePrefix; // string
  public $schematargetNamespace; // string
  public $schematargetNamespacePrefix; // string
  public $scope; // string
  public $serviceDescription; // string
  public $serviceLifeCycle; // ServiceLifeCycle
  public $setEndpointsToAllUsedBindings; // boolean
  public $soapNsUri; // string
  public $targetNamespace; // string
  public $targetNamespacePrefix; // string
  public $typeTable; // TypeTable
  public $useDefaultChains; // boolean
  public $useUserWSDL; // boolean
  public $wsdlFound; // boolean
}

class AxisServiceGroup {
  public $axisDescription; // AxisConfiguration
  public $foundWebResources; // boolean
  public $key; // anyType
  public $moduleRefs; // anyType
  public $serviceGroupClassLoader; // anyType
  public $serviceGroupName; // string
  public $services; // Iterator
}

class TransportInDescription {
  public $faultFlow; // Flow
  public $faultPhase; // Phase
  public $inFlow; // Flow
  public $inPhase; // Phase
  public $name; // string
  public $parameters; // anyType
  public $receiver; // TransportListener
}

class Flow {
  public $handlerCount; // int
}

class HandlerDescription {
  public $className; // string
  public $handler; // Handler
  public $name; // string
  public $parameters; // anyType
  public $parent; // ParameterInclude
  public $rules; // PhaseRule
}

class ParameterInclude {
  public $parameters; // anyType
}

class PhaseRule {
  public $after; // string
  public $before; // string
  public $phaseFirst; // boolean
  public $phaseLast; // boolean
  public $phaseName; // string
}

class TransportOutDescription {
  public $faultFlow; // Flow
  public $faultPhase; // Phase
  public $name; // string
  public $outFlow; // Flow
  public $outPhase; // Phase
  public $parameters; // anyType
  public $sender; // TransportSender
}

class OMNamespace {
  public $name; // string
  public $namespaceURI; // string
  public $prefix; // string
}

class loginWithRememberMeCookie {
  public $cookie; // string
}

class loginWithRememberMeCookieResponse {
  public $return; // boolean
}

class AuthenticationAdminAuthenticationException {
  public $AuthenticationException; // AuthenticationException
}

class logout {
}

class login {
  public $username; // string
  public $password; // string
  public $remoteAddress; // string
}

class loginResponse {
  public $return; // boolean
}

class loginWithRememberMeOption {
  public $username; // string
  public $password; // string
  public $remoteAddress; // string
}

class loginWithRememberMeOptionResponse {
  public $return; // RememberMeData
}

class getAuthenticatorName {
}

class getAuthenticatorNameResponse {
  public $return; // string
}

class authenticateWithRememberMe {
  public $messageContext; // MessageContext
}

class authenticateWithRememberMeResponse {
  public $return; // boolean
}

class isDisabled {
}

class isDisabledResponse {
  public $return; // boolean
}

class getPriority {
}

class getPriorityResponse {
  public $return; // int
}

class MessageContext {
  public $FLOW; // int
  public $SOAP11; // boolean
  public $WSAAction; // string
  public $WSAMessageId; // string
  public $attachmentMap; // Attachments
  public $axisMessage; // AxisMessage
  public $axisOperation; // AxisOperation
  public $axisService; // AxisService
  public $axisServiceGroup; // AxisServiceGroup
  public $configurationContext; // ConfigurationContext
  public $currentHandlerIndex; // int
  public $currentPhaseIndex; // int
  public $doingMTOM; // boolean
  public $doingREST; // boolean
  public $doingSwA; // boolean
  public $effectivePolicy; // Policy
  public $envelope; // SOAPEnvelope
  public $executedPhases; // Iterator
  public $executedPhasesExplicit; // LinkedList
  public $executionChain; // anyType
  public $failureReason; // anyType
  public $fault; // boolean
  public $faultTo; // EndpointReference
  public $from; // EndpointReference
  public $headerPresent; // boolean
  public $inboundContentLength; // long
  public $incomingTransportName; // string
  public $isSOAP11Explicit; // boolean
  public $logCorrelationID; // string
  public $logIDString; // string
  public $messageID; // string
  public $newThreadRequired; // boolean
  public $operationContext; // OperationContext
  public $options; // Options
  public $optionsExplicit; // Options
  public $outputWritten; // boolean
  public $paused; // boolean
  public $processingFault; // boolean
  public $properties; // Map
  public $relatesTo; // RelatesTo
  public $relationships; // RelatesTo
  public $replyTo; // EndpointReference
  public $responseWritten; // boolean
  public $rootContext; // ConfigurationContext
  public $selfManagedDataMapExplicit; // LinkedHashMap
  public $serverSide; // boolean
  public $serviceContext; // ServiceContext
  public $serviceContextID; // string
  public $serviceGroupContext; // ServiceGroupContext
  public $serviceGroupContextId; // string
  public $sessionContext; // SessionContext
  public $soapAction; // string
  public $to; // EndpointReference
  public $transportIn; // TransportInDescription
  public $transportOut; // TransportOutDescription
}

class ConfigurationContext {
  public $anyOperationContextRegistered; // boolean
  public $axisConfiguration; // AxisConfiguration
  public $contextRoot; // string
  public $listenerManager; // ListenerManager
  public $rootContext; // ConfigurationContext
  public $serviceContextPath; // string
  public $serviceGroupContextIDs; // string
  public $serviceGroupContextTimeoutInterval; // long
  public $serviceGroupContextTimoutInterval; // long
  public $serviceGroupContexts; // anyType
  public $servicePath; // string
  public $threadPool; // ThreadFactory
  public $transportManager; // ListenerManager
}

class OperationContext {
  public $axisOperation; // AxisOperation
  public $complete; // boolean
  public $configurationContext; // ConfigurationContext
  public $key; // string
  public $logCorrelationIDString; // string
  public $messageContexts; // string
  public $operationName; // string
  public $rootContext; // ConfigurationContext
  public $serviceContext; // ServiceContext
  public $serviceGroupName; // string
  public $serviceName; // string
}

class ServiceContext {
  public $axisService; // AxisService
  public $cachingOperationContext; // boolean
  public $configurationContext; // ConfigurationContext
  public $groupName; // string
  public $lastOperationContext; // OperationContext
  public $logCorrelationIDString; // string
  public $myEPR; // EndpointReference
  public $name; // string
  public $rootContext; // ConfigurationContext
  public $serviceGroupContext; // ServiceGroupContext
  public $targetEPR; // EndpointReference
}

class ServiceGroupContext {
  public $description; // AxisServiceGroup
  public $id; // string
  public $rootContext; // ConfigurationContext
  public $serviceContexts; // Iterator
}

class SessionContext {
  public $cookieID; // string
  public $lastTouchedTime; // long
  public $rootContext; // ConfigurationContext
  public $serviceGroupContext; // Iterator
}

class PhasesInfo {
  public $INPhases; // anyType
  public $IN_FaultPhases; // anyType
  public $OUTPhases; // anyType
  public $OUT_FaultPhases; // anyType
  public $globalInFaultPhases; // anyType
  public $globalInflow; // anyType
  public $globalOutPhaseList; // anyType
  public $operationInFaultPhases; // anyType
  public $operationInPhases; // anyType
  public $operationOutFaultPhases; // anyType
  public $operationOutPhases; // anyType
  public $operationPhases; // AxisOperation
  public $outFaultPhaseList; // anyType
}

class ExcludeInfo {
}

class URL {
  public $authority; // string
  public $content; // anyType
  public $defaultPort; // int
  public $file; // string
  public $host; // string
  public $path; // string
  public $port; // int
  public $protocol; // string
  public $query; // string
  public $ref; // string
  public $userInfo; // string
}

class EndpointReference {
  public $WSAddressingAnonymous; // boolean
  public $address; // string
  public $addressAttributes; // anyType
  public $allReferenceParameters; // Map
  public $attributes; // anyType
  public $extensibleElements; // anyType
  public $logCorrelationIDString; // string
  public $metaData; // anyType
  public $metadataAttributes; // anyType
  public $name; // string
  public $referenceParameters; // anyType
}

class RelatesTo {
  public $extensibilityAttributes; // anyType
  public $relationshipType; // string
  public $value; // string
}

class SecretResolver {
  public $initialized; // boolean
}

class AxisConfiguration {
  public $childFirstClassLoading; // boolean
  public $clusteringAgent; // ClusteringAgent
  public $configurator; // AxisConfigurator
  public $faultyModules; // string
  public $faultyServices; // string
  public $faultyServicesDuetoModules; // Map
  public $globalModules; // anyType
  public $globalOutPhase; // anyType
  public $inFaultFlowPhases; // anyType
  public $inFaultPhases; // anyType
  public $inFlowPhases; // anyType
  public $inPhasesUptoAndIncludingPostDispatch; // anyType
  public $key; // anyType
  public $localPolicyAssertions; // anyType
  public $moduleClassLoader; // anyType
  public $modules; // anyType
  public $observersList; // anyType
  public $outFaultFlowPhases; // anyType
  public $outFaultPhases; // anyType
  public $outFlowPhases; // anyType
  public $phasesInfo; // PhasesInfo
  public $repository; // URL
  public $secretResolver; // SecretResolver
  public $serviceClassLoader; // anyType
  public $serviceGroups; // Iterator
  public $services; // anyType
  public $start; // boolean
  public $systemClassLoader; // anyType
  public $targetResolverChain; // TargetResolver
  public $transactionConfig; // TransactionConfiguration
  public $transactionConfiguration; // TransactionConfiguration
  public $transportsIn; // string
  public $transportsOut; // string
}

class ListenerManager {
  public $configctx; // ConfigurationContext
  public $shutdownHookRequired; // boolean
  public $stopped; // boolean
}

class AxisConfigurator {
  public $axisConfiguration; // AxisConfiguration
}

class ObjectSupplier {
}

class ServiceLifeCycle {
}

class MessageReceiver {
}

class Phase {
  public $handlerCount; // int
  public $handlerDesc; // HandlerDescription
  public $handlers; // anyType
  public $name; // string
  public $phaseFirst; // Handler
  public $phaseLast; // Handler
  public $phaseName; // string
}

class Handler {
  public $handlerDesc; // HandlerDescription
  public $name; // string
}

class StateManager {
  public $configurationContext; // ConfigurationContext
  public $replicationExcludePatterns; // Map
}

class RememberMeData {
  public $authenticated; // boolean
  public $maxAge; // int
  public $value; // string
}

class ClusteringAgent {
  public $aliveMemberCount; // int
  public $configurationContext; // ConfigurationContext
  public $coordinator; // boolean
  public $domains; // Set
  public $members; // anyType
  public $nodeManager; // NodeManager
  public $stateManager; // StateManager
}

class TypeTable {
  public $complexSchemaMap; // Map
}


/**
 * AuthenticationAdmin class
 * 
 *  
 * 
 * @author    {author}
 * @copyright {copyright}
 * @package   {package}
 */

67377932bbc662c02ae7e30210680aa82d38fcf5

class AuthenticationAdminStub extends SoapClient {

  private static $classmap = array(
                                    'TransactionConfiguration' => 'TransactionConfiguration',
                                    'AbstractPolicyOperator' => 'AbstractPolicyOperator',
                                    'PolicyComponent' => 'PolicyComponent',
                                    'All' => 'All',
                                    'Policy' => 'Policy',
                                    'Enum' => 'Enum',
                                    'ThreadFactory' => 'ThreadFactory',
                                    'XmlSchemaObject' => 'XmlSchemaObject',
                                    'XmlSchemaAnnotated' => 'XmlSchemaAnnotated',
                                    'XmlSchemaAnnotation' => 'XmlSchemaAnnotation',
                                    'XmlSchemaObjectCollection' => 'XmlSchemaObjectCollection',
                                    'XmlSchemaParticle' => 'XmlSchemaParticle',
                                    'XmlSchemaElement' => 'XmlSchemaElement',
                                    'XmlSchemaDerivationMethod' => 'XmlSchemaDerivationMethod',
                                    'XmlSchemaForm' => 'XmlSchemaForm',
                                    'XmlSchemaType' => 'XmlSchemaType',
                                    'XmlSchemaDatatype' => 'XmlSchemaDatatype',
                                    'TargetResolver' => 'TargetResolver',
                                    'LifecycleManager' => 'LifecycleManager',
                                    'SOAPEnvelope' => 'SOAPEnvelope',
                                    'SOAPBody' => 'SOAPBody',
                                    'SOAPFault' => 'SOAPFault',
                                    'SOAPFaultCode' => 'SOAPFaultCode',
                                    'SOAPFaultDetail' => 'SOAPFaultDetail',
                                    'SOAPFaultNode' => 'SOAPFaultNode',
                                    'SOAPFaultReason' => 'SOAPFaultReason',
                                    'SOAPFaultText' => 'SOAPFaultText',
                                    'SOAPFaultRole' => 'SOAPFaultRole',
                                    'SOAPHeader' => 'SOAPHeader',
                                    'SOAPVersion' => 'SOAPVersion',
                                    'Attachments' => 'Attachments',
                                    'IncomingAttachmentStreams' => 'IncomingAttachmentStreams',
                                    'IncomingAttachmentInputStream' => 'IncomingAttachmentInputStream',
                                    'AuthenticationException' => 'AuthenticationException',
                                    'TransportListener' => 'TransportListener',
                                    'TransportSender' => 'TransportSender',
                                    'Attr' => 'Attr',
                                    'Element' => 'Element',
                                    'TypeInfo' => 'TypeInfo',
                                    'Options' => 'Options',
                                    'Set' => 'Set',
                                    'Map' => 'Map',
                                    'Iterator' => 'Iterator',
                                    'LinkedList' => 'LinkedList',
                                    'LinkedHashMap' => 'LinkedHashMap',
                                    'InputStream' => 'InputStream',
                                    'NodeManager' => 'NodeManager',
                                    'AxisMessage' => 'AxisMessage',
                                    'AxisOperation' => 'AxisOperation',
                                    'AxisService' => 'AxisService',
                                    'AxisServiceGroup' => 'AxisServiceGroup',
                                    'TransportInDescription' => 'TransportInDescription',
                                    'Flow' => 'Flow',
                                    'HandlerDescription' => 'HandlerDescription',
                                    'ParameterInclude' => 'ParameterInclude',
                                    'PhaseRule' => 'PhaseRule',
                                    'TransportOutDescription' => 'TransportOutDescription',
                                    'OMNamespace' => 'OMNamespace',
                                    'loginWithRememberMeCookie' => 'loginWithRememberMeCookie',
                                    'loginWithRememberMeCookieResponse' => 'loginWithRememberMeCookieResponse',
                                    'AuthenticationAdminAuthenticationException' => 'AuthenticationAdminAuthenticationException',
                                    'logout' => 'logout',
                                    'login' => 'login',
                                    'loginResponse' => 'loginResponse',
                                    'loginWithRememberMeOption' => 'loginWithRememberMeOption',
                                    'loginWithRememberMeOptionResponse' => 'loginWithRememberMeOptionResponse',
                                    'getAuthenticatorName' => 'getAuthenticatorName',
                                    'getAuthenticatorNameResponse' => 'getAuthenticatorNameResponse',
                                    'authenticateWithRememberMe' => 'authenticateWithRememberMe',
                                    'authenticateWithRememberMeResponse' => 'authenticateWithRememberMeResponse',
                                    'isDisabled' => 'isDisabled',
                                    'isDisabledResponse' => 'isDisabledResponse',
                                    'getPriority' => 'getPriority',
                                    'getPriorityResponse' => 'getPriorityResponse',
                                    'MessageContext' => 'MessageContext',
                                    'ConfigurationContext' => 'ConfigurationContext',
                                    'OperationContext' => 'OperationContext',
                                    'ServiceContext' => 'ServiceContext',
                                    'ServiceGroupContext' => 'ServiceGroupContext',
                                    'SessionContext' => 'SessionContext',
                                    'PhasesInfo' => 'PhasesInfo',
                                    'ExcludeInfo' => 'ExcludeInfo',
                                    'URL' => 'URL',
                                    'EndpointReference' => 'EndpointReference',
                                    'RelatesTo' => 'RelatesTo',
                                    'SecretResolver' => 'SecretResolver',
                                    'AxisConfiguration' => 'AxisConfiguration',
                                    'ListenerManager' => 'ListenerManager',
                                    'AxisConfigurator' => 'AxisConfigurator',
                                    'ObjectSupplier' => 'ObjectSupplier',
                                    'ServiceLifeCycle' => 'ServiceLifeCycle',
                                    'MessageReceiver' => 'MessageReceiver',
                                    'Phase' => 'Phase',
                                    'Handler' => 'Handler',
                                    'StateManager' => 'StateManager',
                                    'RememberMeData' => 'RememberMeData',
                                    'ClusteringAgent' => 'ClusteringAgent',
                                    'TypeTable' => 'TypeTable',
                                   );

  public function AuthenticationAdminStub($wsdl = "AuthenticationAdmin.xml", $options = array()) {
    foreach(self::$classmap as $key => $value) {
      if(!isset($options['classmap'][$key])) {
        $options['classmap'][$key] = $value;
      }
    }
    parent::__construct($wsdl, $options);
  }

  /**
   *  
   *
   * @param logout $parameters
   * @return void
   */
  public function logout(logout $parameters) {
    return $this->__soapCall('logout', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param loginWithRememberMeCookie $parameters
   * @return loginWithRememberMeCookieResponse
   */
  public function loginWithRememberMeCookie(loginWithRememberMeCookie $parameters) {
    return $this->__soapCall('loginWithRememberMeCookie', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param authenticateWithRememberMe $parameters
   * @return authenticateWithRememberMeResponse
   */
  public function authenticateWithRememberMe(authenticateWithRememberMe $parameters) {
    return $this->__soapCall('authenticateWithRememberMe', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getAuthenticatorName $parameters
   * @return getAuthenticatorNameResponse
   */
  public function getAuthenticatorName(getAuthenticatorName $parameters) {
    return $this->__soapCall('getAuthenticatorName', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param login $parameters
   * @return loginResponse
   */
  public function login(login $parameters) {
    return $this->__soapCall('login', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param loginWithRememberMeOption $parameters
   * @return loginWithRememberMeOptionResponse
   */
  public function loginWithRememberMeOption(loginWithRememberMeOption $parameters) {
    return $this->__soapCall('loginWithRememberMeOption', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param isDisabled $parameters
   * @return isDisabledResponse
   */
  public function isDisabled(isDisabled $parameters) {
    return $this->__soapCall('isDisabled', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }

  /**
   *  
   *
   * @param getPriority $parameters
   * @return getPriorityResponse
   */
  public function getPriority(getPriority $parameters) {
    return $this->__soapCall('getPriority', array($parameters),       array(
            'uri' => 'http://authentication.services.core.carbon.wso2.org',
            'soapaction' => ''
           )
      );
  }
}

?>
