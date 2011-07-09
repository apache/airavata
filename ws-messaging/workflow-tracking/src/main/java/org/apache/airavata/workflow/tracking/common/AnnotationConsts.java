/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.workflow.tracking.common;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.xml.namespace.QName;

public enum AnnotationConsts {

    // localName, simpleType, multiValued
    ExperimentID("experimentID", true, false), // experiment id used in mylead
    ServiceLocation("ServiceLocation", true, false), // location (EPR?) of the service
    AbstractServiceID("abstractServiceID", true, false), // abstract service QName
    AbstractWorkflowID("abstractWorkflowID", true, false), // abstract workfow QName
    DataProductType("dataProductType", true, false), // namelist file, etc.
    TypedSOAPRequest("typedSOAPRequest", false, false), // SOAP request with leadType fields set
    TypedSOAPResponse("typedSOAPResponse", false, false), // SOAP request with leadType fields set
    UserDN("userDN", true, false), // User DN of person invoking the service
    ParamNameInSOAP("paramName", true, false), // element name of the (data) parameter in the SOAP Message
    ServiceReplicaID("Service_Replica_ID", true, false);

    public QName getQName() {
        return qname;
    }

    public boolean isSimpleType() {
        return isSimpleType;
    }

    public boolean isMultiValued() {
        return isMultiValued;
    }

    private static final String WFT_NS = "http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking";
    private QName qname;
    private boolean isSimpleType, isMultiValued;

    private AnnotationConsts(String name, boolean isSimpleType_, boolean isMultiValued_) {
        this(WFT_NS, name, isSimpleType_, isMultiValued_);
    }

    private AnnotationConsts(String ns, String name, boolean isSimpleType_, boolean isMultiValued_) {
        qname = new QName(ns, name);
        isSimpleType = isSimpleType_;
        isMultiValued = isMultiValued_;
    }

    private static List<QName> qNameList = null;

    public static List<QName> getQNameList() {
        if (qNameList != null)
            return new ArrayList<QName>(qNameList);
        final EnumSet<AnnotationConsts> allAnnos = EnumSet.allOf(AnnotationConsts.class);
        List<QName> qNameList = new ArrayList<QName>();
        for (AnnotationConsts anno : allAnnos) {
            qNameList.add(anno.getQName());
        }
        return qNameList;
    }
}
