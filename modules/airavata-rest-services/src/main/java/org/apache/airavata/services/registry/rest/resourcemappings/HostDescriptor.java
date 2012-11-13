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

package org.apache.airavata.services.registry.rest.resourcemappings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "host")
@XmlAccessorType(XmlAccessType.FIELD)
public class HostDescriptor {

    private String hostname;
    private String hostAddress;

    private List<String> hostType = new ArrayList<String>();
    private List<String> gridFTPEndPoint = new ArrayList<String>();
    private List<String> globusGateKeeperEndPoint = new ArrayList<String>();
    private List<String> imageID = new ArrayList<String>();
    private List<String> instanceID = new ArrayList<String>();

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public List<String> getHostType() {
        return hostType;
    }

    public void setHostType(List<String> hostType) {
        this.hostType = hostType;
    }

    public List<String> getGridFTPEndPoint() {
        return gridFTPEndPoint;
    }

    public void setGridFTPEndPoint(List<String> gridFTPEndPoint) {
        this.gridFTPEndPoint = gridFTPEndPoint;
    }

    public List<String> getGlobusGateKeeperEndPoint() {
        return globusGateKeeperEndPoint;
    }

    public void setGlobusGateKeeperEndPoint(List<String> globusGateKeeperEndPoint) {
        this.globusGateKeeperEndPoint = globusGateKeeperEndPoint;
    }

    public List<String> getImageID() {
        return imageID;
    }

    public void setImageID(List<String> imageID) {
        this.imageID = imageID;
    }

    public List<String> getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(List<String> instanceID) {
        this.instanceID = instanceID;
    }

}
