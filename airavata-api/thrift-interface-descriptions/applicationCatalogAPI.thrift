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
 * Application Programming Interface definition for Apache Airavata Services.
 *   this parent thrift file is contains all service interfaces. The data models are 
 *   described in respective thrift files.
*/

include "airavataErrors.thrift"
include "airavataDataModel.thrift"
include "applicationCatalogDataModel.thrift"

namespace java org.apache.airavata.api.appcatalog
namespace php Airavata.API.AppCatalog
namespace cpp airavata.api.appcatalog
namespace perl AiravataAPI.AppCatalog
namespace py airavata.api.appcatalog
namespace js AiravataAPI.AppCatalog

/*
 * Airavata Interface Versions depend upon this Thrift Interface File. When Making changes, please edit the
 *  Version Constants according to Semantic Versioning Specification (SemVer) http://semver.org.
 *
 * Note: The Airavata API version may be different from the Airavata software release versions.
 *
 * The Airavata API version is composed as a dot delimited string with major, minor, and patch level components.
 *
 *  - Major: Incremented for backward incompatible changes. An example would be changes to interfaces.
 *  - Minor: Incremented for backward compatible changes. An example would be the addition of a new optional methods.
 *  - Patch: Incremented for bug fixes. The patch level should be increased for every edit that doesn't result
 *              in a change to major/minor version numbers.
*/
const string AIRAVATA_API_VERSION = "0.12.0"

service ApplicationCatalogAPI {

/*
 * Apache Airavata API Service Methods. For data structures associated in the signatures, please see included thrift files
*/

  /** Query Airavata to fetch the API version */
  string GetAPIVersion(),
  
  /**
   * Manage Computer Resources
  */
  void addComputeResourceDescription(1: required applicationCatalogDataModel.ComputeResourceDescription computeResourceDescription)
    throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  void addSSHJobSubmissionProtocol(1: required string computeResourceId, 2: required applicationCatalogDataModel.SSHJobSubmission jobSubmission)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  void addGSISSHJobSubmissionProtocol(1: required string computeResourceId, 2: required applicationCatalogDataModel.GSISSHJobSubmission jobSubmission)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  void addGlobusJobSubmissionProtocol(1: required string computeResourceId, 2: required applicationCatalogDataModel.GlobusJobSubmission jobSubmission)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)     
            
  void addSCPDataMovementProtocol(1: required string computeResourceId, 2: required applicationCatalogDataModel.SCPDataMovement dataMovement)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase) 
            
  list<string> listComputeResourceDescriptions()
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)

  applicationCatalogDataModel.ComputeResourceDescription getComputeResourceDescription(1: required string computeResourceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)

  applicationCatalogDataModel.SSHJobSubmission getSSHJobSubmissionProtocol(1: required string sshJobSubmissionProtocolResourceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  applicationCatalogDataModel.GSISSHJobSubmission getGSISSHJobSubmissionProtocol(1: required string gsisshJobSubmissionProtocolResourceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  applicationCatalogDataModel.GlobusJobSubmission getGlobusJobSubmissionProtocol(1: required string globusJobSubmissionProtocolResourceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)     
            
  applicationCatalogDataModel.SCPDataMovement getSCPDataMovementProtocol(1: required string scpDataMovementResourceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase) 

  bool isComputeResourceDescriptionRegistered(1: required string hostName)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)

  applicationCatalogDataModel.ComputeResourceDescription getComputeResourceDescriptionFromHostName(1: required string hostName)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
                       
  /**
   * Manage Application Interfaces
  */
  void addApplicationInterface(1: required applicationCatalogDataModel.ApplicationInterface applicationInterface)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)
            
  list<string> listApplicationInterfaceIds()
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)  

  applicationCatalogDataModel.ApplicationInterface getApplicationInterface(1: required string applicationInterfaceId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)  

  /**
   * Manage application deployments
  */
  void addApplicationDeployment(1: required string applicationInterfaceId, 2: required applicationCatalogDataModel.ApplicationDeployment applicationDeployment)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)   
            
  list<string> listApplicationDeploymentIds()
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)  

  applicationCatalogDataModel.ApplicationDeployment getApplicationDeployment(1: required string applicationDeploymentId)
  	throws (1: airavataErrors.InvalidRequestException ire,
            2: airavataErrors.AiravataClientException ace,
            3: airavataErrors.AiravataSystemException ase)  
}
