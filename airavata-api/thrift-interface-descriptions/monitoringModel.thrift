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

namespace java org.apache.airavata.model.monitoring
namespace php Airavata.Model.Monitoring

/**
 *  Experiment Monitoring Data is a structure contains fine grained experiment status information.
*/

/** 
* To communicate date in ISO 8601 format like YYYY-MM-DD hh:mm:ss or YYYY-MM-DD
*/ 
typedef string TimeStamp

/**
* Enumeration for the job monitoring status. 
*/
enum STATUS {
  CREATED,
  SUBMITTED,
  AUTHENTICATE,
  CONFIGURING_WORKSPACE,
  INPUT_TRANSFER,
  OUTPUT_TRANSFER,
  QUEUED,
  PENDING,
  ACTIVE,
  COMPLETE,
  CANCELED,
  FINISHED,
  FAILED,
  UNKNOWN  
}
/**
* Application runtime details 
*/
struct ApplicationDetails {
 	1: optional string name,
 	2: optional string instanceid,
 	3: optional STATUS status,
 	4: optional string currentAction,
 	5: optional string errormessage
}
 
 /**
 * A structure holding the experiment monitoring request.
 *
 * expeimentID: 
 * 	Unique id returned by airavata on experiment creation.  
 *	
 * userName:
 * 	User of an experiment. This can be used to get all experiment triggered by user
 * 
 * filter:
 * 	This is to filter the output results. Filter can be failed, canceled, running, aborted and all.
 * 
 * To get data for certain date range, provide a date range for the experiment run
 * startDateTime: 
 * 	Start criteria for a date range 
 * endDateTime:
 *  End criteria for a date range
 *
 */
struct MonitoringRequest {
 	1: optional string experimentID,
  	2: optional string userName,
  	3: optional string filter = "all",
  	4: optional i32 startDateTime,
  	5: optional i32 endDateTime,
  	6: optional bool applicationdetails = false
}

 /**
 * A structure holding the experiment monitoring response.
 *
 * expeimentID: 
 * 	Unique id returned by airavata on experiment creation.  
 *	
 * userName:
 * 	User of an experiment. 
 * 
 * experimentStatus:
 * 	Experiment status for a experiment run.
 * 
 * startTime: 
 * 	Experiment start datetime in ISO 8601 format.
 *
 * endTime:
 *  Experiment end datetime in ISO 8601 format.
 * 
 * errormessage: 
 * 	If the experiment status is failed then this will return the reason of error. 
 *
 * appdetails
 *  Application details are specific to applications executed in this experiment.
 */

struct MonitoringResponse {
  1: required string experimentID,
  2: required string userName,
  3: required STATUS experimentStatus,
  4: optional TimeStamp startTime,
  5: optional TimeStamp endTime,
  6: optional string errormessage,
  7: optional list <ApplicationDetails> appdetails
}