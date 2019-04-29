/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/*
* This file describes the definitions of the Error Messages that can occur
*  when invoking Apache Airavata Services through the API. In addition Thrift provides
*  built in funcationality to raise TApplicationException for all internal server errors.
*/

include "../data-models/experiment-catalog-models/experiment_model.thrift"

namespace java org.apache.airavata.model.error
namespace php Airavata.API.Error
namespace cpp apache.airavata.api.error
namespace perl AiravataAPIError
namespace py airavata.api.error
namespace js AiravataAPIError

/**
 * A list of Airavata API Error Message Types
 *
 *  UNKNOWN: No information available about the error
 *   
 *  PERMISSION_DENIED: Not permitted to perform action
 * 
 *  INTERNAL_ERROR: Unexpected problem with the service
 * 
 *  AUTHENTICATION_FAILURE: The client failed to authenticate.
 *
 *  INVALID_AUTHORIZATION: Security Token and/or Username and/or password is incorrect
 *   
 *  AUTHORIZATION_EXPIRED: Authentication token expired
 *  
 *  UNKNOWN_GATEWAY_ID: The gateway is not registered with Airavata.
 * 
 *  UNSUPPORTED_OPERATION: Operation denied because it is currently unsupported.
 */

enum AiravataErrorType {
  UNKNOWN,
  PERMISSION_DENIED,
  INTERNAL_ERROR,
  AUTHENTICATION_FAILURE,
  INVALID_AUTHORIZATION,
  AUTHORIZATION_EXPIRED,
  UNKNOWN_GATEWAY_ID,
  UNSUPPORTED_OPERATION
}

/**
 * This exception is thrown when a client asks to perform an operation on an experiment that does not exist.
 *
 * identifier:  A description of the experiment that was not found on the server.
 *
 * key:  The value passed from the client in the identifier, which was not found.
 */
exception ExperimentNotFoundException {
  1: required string message
  /**
  * 1:  optional  string identifier,
  * 2:  optional  string key
  **/
}

exception ProjectNotFoundException {
  1: required string message
}

/** 
* This exception is thrown for invalid requests that occur from any reasons like required input parameters are missing, 
*  or a parameter is malformed.
* 
*  message: contains the associated error message.
*/
exception InvalidRequestException {
    1: required string message
}


/** 
*  This exception is thrown when RPC timeout gets exceeded. 
*/
exception TimedOutException {
}

/** 
* This exception is thrown for invalid sshKeyAuthentication requests.
* 
*  message: contains the cause of the authorization failure.
*/
exception AuthenticationException {
    1: required string message
}

/** 
* This exception is thrown for invalid authorization requests such user does not have acces to an aplication or resource.
*
*  message: contains the authorization failure message
*/
exception AuthorizationException {
    1: required string message
}

/**
* This exception is thrown when you try to save a duplicate entity that already exists
*   in the database.
*
*   message: contains the associated error message
**/
exception DuplicateEntryException {
    1: required string message
}

/**
 * This exception is thrown by Airavata Services when a call fails as a result of
 * a problem that a client may be able to resolve.  For example, if the user
 * attempts to execute an application on a resource gateway does not have access to.
 *
 * This exception would not be used for internal system errors that do not
 * reflect user actions, but rather reflect a problem within the service that
 * the client cannot resolve.
 *
 * airavataErrorType:  The message type indicating the error that occurred.
 *   must be one of the values of AiravataErrorType.
 *
 * parameter:  If the error applied to a particular input parameter, this will
 *   indicate which parameter.
 */
exception AiravataClientException {
  1:  required  AiravataErrorType airavataErrorType,
  2:  optional  string parameter
}

struct ValidatorResult {
    1: required bool result,
    2: optional string errorDetails
}

struct ValidationResults {
    1: required bool validationState,
    2: required list<ValidatorResult> validationResultList
}

exception LaunchValidationException {
  1: required ValidationResults validationResult;
  2: optional string errorMessage;
}

/**
 * This exception is thrown by Airavata Services when a call fails as a result of
 * a problem in the service that could not be changed through client's action.
 *
 * airavataErrorType:  The message type indicating the error that occurred.
 *   must be one of the values of AiravataErrorType.
 *
 * message:  This may contain additional information about the error
 *
 */
exception AiravataSystemException {
  1:  required  AiravataErrorType airavataErrorType,
  2:  optional  string message,
}


