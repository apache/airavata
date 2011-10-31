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

package org.apache.airavata.commons.gfac.wsdl;

import java.util.Vector;

import javax.xml.namespace.QName;

public class WSDLMessageBean {
    private QName portType;
    private String methodName;
    private String namespace = null;

    private String inElementName;
    private Vector inMsgParamNames;
    private Vector inMsgParamTypes;

    private String outElementName;
    private Vector outMsgParamNames;
    private Vector outMsgParamTypes;

    /**
     * Sets OutMsgParamTypes
     * 
     * @param OutMsgParamTypes
     *            a Vector
     */
    public void setOutMsgParamTypes(Vector outMsgParamTypes) {
        this.outMsgParamTypes = outMsgParamTypes;
    }

    /**
     * Returns OutMsgParamTypes
     * 
     * @return a Vector
     */
    public Vector getOutMsgParamTypes() {
        return outMsgParamTypes;
    }

    /**
     * Sets Namespace
     * 
     * @param Namespace
     *            a String
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns Namespace
     * 
     * @return a String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets InMsgParamTypes
     * 
     * @param InMsgParamTypes
     *            a Vector
     */
    public void setInMsgParamTypes(Vector inMsgParamTypes) {
        this.inMsgParamTypes = inMsgParamTypes;
    }

    /**
     * Returns InMsgParamTypes
     * 
     * @return a Vector
     */
    public Vector getInMsgParamTypes() {
        return inMsgParamTypes;
    }

    /**
     * Sets PortType
     * 
     * @param PortType
     *            a QName
     */
    public void setPortType(QName portType) {
        this.portType = portType;
    }

    /**
     * Returns PortType
     * 
     * @return a QName
     */
    public QName getPortType() {
        return portType;
    }

    /**
     * Sets OutputMessageParamNames
     * 
     * @param OutputMessageParamNamesa
     *            Vector
     */
    public void setOutMsgParamNames(Vector outputMessageParamNames) {
        this.outMsgParamNames = outputMessageParamNames;
    }

    /**
     * Returns OutputMessageParamNames
     * 
     * @return a Vector
     */
    public Vector getOutMsgParamNames() {
        return outMsgParamNames;
    }

    /**
     * Sets InputMessagePartNames
     * 
     * @param InputMessagePartNamesa
     *            Vector
     */
    public void setInMsgParamNames(Vector inputMessagePartNames) {
        this.inMsgParamNames = inputMessagePartNames;
    }

    /**
     * Returns InputMessagePartNames
     * 
     * @return a Vector
     */
    public Vector getInMsgParamNames() {
        return inMsgParamNames;
    }

    /**
     * Sets MethodName
     * 
     * @param MethodName
     *            a String
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns MethodName
     * 
     * @return a String
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets OutputMessageName
     * 
     * @param OutputMessageName
     *            a String
     */
    public void setOutElementName(String outputMessageName) {
        this.outElementName = outputMessageName;
    }

    /**
     * Returns OutputMessageName
     * 
     * @return a String
     */
    public String getOutElementName() {
        return outElementName;
    }

    /**
     * Sets InputMessageName
     * 
     * @param InputMessageName
     *            a String
     */
    public void setInElementName(String inputMessageName) {
        this.inElementName = inputMessageName;
    }

    /**
     * Returns InputMessageName
     * 
     * @return a String
     */
    public String getInElementName() {
        return inElementName;
    }
}