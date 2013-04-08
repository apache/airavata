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

package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.gfac.provider.GFacProviderException;
import org.xmlpull.v1.builder.XmlElement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Represents a DataID (A schema with real names), currently it only sends a one
 * location value.
 */
public class DataIDType {
    public static final String LOCATION_ATTRIBUTE = "location";

    private URI dataID;

    private ArrayList<URI> dataLocations = new ArrayList<URI>();

    public URI getRealLocation() {
        if (dataLocations.size() > 0) {
            return dataLocations.get(0);
        } else {
            return null;
        }
    }

    public DataIDType(XmlElement ele) throws GFacProviderException {
        try {
            String value = ele.requiredTextContent();
            if (value != null) {
                this.dataID = new URI(value);
            } else {
                throw new GFacProviderException(
                        "Illegal InputMessage, No value content found for the parameter "
                                + ele.getName() + "/value. Invalid Local Argument");
            }
            String location = ele.getAttributeValue(null, DataIDType.LOCATION_ATTRIBUTE);
            if (location != null) {
                addDataLocation(new URI(location));
            }
        } catch (URISyntaxException e) {
            throw new GFacProviderException("Invalid Local Argument", e);
        }
    }

    public DataIDType(URI dataID) {
        super();
        this.dataID = dataID;
    }

    public void addDataLocation(URI dataLocation) {
        dataLocations.add(dataLocation);
    }

    public ArrayList<URI> getDataLocations() {
        return dataLocations;
    }

    public URI getDataID() {
        return dataID;
    }

    public void fillData(XmlElement ele) {
        ele.addChild(dataID.toString());
        URI location = getRealLocation();
        if (location != null) {
            ele.addAttribute(DataIDType.LOCATION_ATTRIBUTE, location.toString());
        }
    }

}

