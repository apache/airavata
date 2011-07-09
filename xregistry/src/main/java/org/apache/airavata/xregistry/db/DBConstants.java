/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.db;

public abstract class DBConstants {

    /****************************************************************
     *               Summary Table Information                      *
     ****************************************************************/
    public static final String T_SUMMARY_NAME = "summary";
    public static final String T_SUMMARY_WORKFLOW_ID = "workflowid";
    public static final String T_SUMMARY_TEMPLATE_ID = "templateid";
    public static final String T_SUMMARY_STATUS = "status";
    public static final String T_SUMMARY_END_TIME = "endTime";
    public static final String T_SUMMARY_START_TIME = "startTime";

    /****************************************************************
     *               Faults Table Information                      *
     ****************************************************************/
    public static final String T_FAULTS_NAME = "faults";
    public static final String T_FAULTS_ID = "id";
    public static final String T_FAULTS_XML = "xml";
    public static final String T_FAULTS_WORKFLOW_ID = "workflowid";

}

 

