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

package org.apache.airavata.commons;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;

public class LeadCrosscutParametersUtil {
    public LeadCrosscutParametersUtil(XmlObject xmlObjectToWrap) throws XMLStreamException {
        this(XBeansUtil.xmlObjectToOMElement(xmlObjectToWrap));
    }

    public LeadCrosscutParametersUtil(OMElement elementToWrap) {
        this.target = elementToWrap;
        if (elementToWrap == null) {
            logger.error("illegal argument null found");
            throw new IllegalArgumentException("null");
        } else
            return;
    }

    public LeadCrosscutParametersUtil(QName topElQName) {
        this.target = factory.createOMElement(new QName(topElQName.getNamespaceURI(), topElQName.getLocalPart()));
        this.target.declareNamespace(TYPE_NS);
    }

    public LeadCrosscutParametersUtil() {
        this(new QName("xml-fragment"));
    }

    public LeadCrosscutParametersUtil(Properties props) {
        this();
        for (Iterator<OMElement> it = target.getChildElements(); it.hasNext();) {
            OMElement child = it.next();
            child.detach();
        }

        String key;
        String value;
        for (Iterator i = props.keySet().iterator(); i.hasNext(); setString(key, value)) {
            key = (String) i.next();
            value = props.getProperty(key);
        }
        setFromProperties(props);
    }

    public void setFromProperties(Properties props) {
        for (Iterator it = target.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            child.detach();
        }
        String key;
        String value;
        for (Iterator i = props.keySet().iterator(); i.hasNext(); setString(key, value)) {
            key = (String) i.next();
            value = props.getProperty(key);
        }

    }

    public void setString(String name, String value) {
        Iterator<OMElement> el = target.getChildrenWithLocalName(name);
        if (value == null) {
            throw new IllegalArgumentException();
        } else {
            while (el.hasNext())
                el.next().setText(value);
            return;
        }
    }

    public OMNamespace parameterNs() {
        return TYPE_NS;
    }

    public Float getCenterLatitude() {
        return getFloat("ctrlat");
    }

    public void setCenterLatitude(float value) {
        setFloat("ctrlat", value);
    }

    public Float getCenterLongitude() {
        return getFloat("ctrlon");
    }

    public void setCenterLongitude(float value) {
        setFloat("ctrlon", value);
    }

    public Float getForecastTimeInHours() {
        return getFloat("fcst_time");
    }

    public void setForecastTimeInHours(float value) {
        setFloat("fcst_time", value);
    }

    public String getForecastStartDate() {
        return target.getFirstChildWithName(new QName(null, "start_date")).getText();
    }

    public void setForecastStartDate(String startDate) {
        setString("start_date", startDate);
    }

    public Integer getForecastStartHour() {
        return getInt("start_hour");
    }

    public void setForecastStartHour(int startHour) {
        setInt("start_hour", startHour);
    }

    public Float getWestBc() {
        return getFloat("westbc");
    }

    public float requireWestBc() {
        return requireFloat("westbc");
    }

    public void setWestBc(float value) {
        setFloat("westbc", value);
    }

    public Float getEastBc() {
        return getFloat("eastbc");
    }

    public float requireEastBc() {
        return requireFloat("eastbc");
    }

    public void setEastBc(float value) {
        setFloat("eastbc", value);
    }

    public Float getSouthBc() {
        return getFloat("southbc");
    }

    public float requireSouthBc() {
        return requireFloat("southbc");
    }

    public void setSouthBc(float value) {
        setFloat("southbc", value);
    }

    public Float getNorthBc() {
        return getFloat("northbc");
    }

    public float requireNorthBc() {
        return requireFloat("northbc");
    }

    public void setNorthBc(float value) {
        setFloat("northbc", value);
    }

    public Integer getDx() {
        return getInt("dx");
    }

    public void setDx(int value) {
        setInt("dx", value);
    }

    public Integer getDy() {
        return getInt("dy");
    }

    public void setDy(int value) {
        setInt("dy", value);
    }

    public Integer getDz() {
        return getInt("dz");
    }

    public void setDz(int value) {
        setInt("dz", value);
    }

    public Integer getNx() {
        return getInt("nx");
    }

    public void setNx(int value) {
        setInt("nx", value);
    }

    public Integer getNy() {
        return getInt("ny");
    }

    public void setNy(int value) {
        setInt("ny", value);
    }

    public Integer getNz() {
        return getInt("nz");
    }

    public void setNz(int value) {
        setInt("nz", value);
    }

    public String getString(String name) {
        OMElement el = target.getFirstChildWithName(new QName(null, name));
        if (el == null)
            return null;
        else
            return el.getText();
    }

    public Integer getInt(String name) {
        String s = getString(name);
        if (s == null)
            return null;
        else
            return new Integer(s);
    }

    public Float getFloat(String name) {
        String s = getString(name);
        if (s == null)
            return null;
        else
            return new Float(s);
    }

    public void setFloat(String name, float value) {
        setString(name, Float.toString(value));
    }

    public void setInt(String name, int value) {
        setString(name, Integer.toString(value));
    }

    public float requireFloat(String name) {
        String s = getString(name);
        if (s == null)
            throw new IllegalArgumentException((new StringBuilder()).append("missing parameter ").append(name)
                    .toString());
        else
            return Float.parseFloat(s);
    }

    private static final org.apache.log4j.Logger logger = Logger.getLogger(LeadCrosscutParametersUtil.class);
    public static final String FORECAST_TIME = "fcst_time";
    public static final String START_DATE = "start_date";
    public static final String START_HOUR = "start_hour";
    public static final String CENTER_LONGITUDE = "ctrlon";
    public static final String CENTER_LATITUDE = "ctrlat";
    public static final String DX = "dx";
    public static final String DY = "dy";
    public static final String DZ = "dz";
    public static final String NX = "nx";
    public static final String NY = "ny";
    public static final String NZ = "nz";
    public static final String WEST_BC = "westbc";
    public static final String EAST_BC = "eastbc";
    public static final String SOUTH_BC = "southbc";
    public static final String NORTH_BC = "northbc";
    private static final QName COMPLEX_TYPE;
    public static final OMNamespace TYPE_NS;
    private OMElement target;
    private static OMFactory factory = OMAbstractFactory.getOMFactory();

    static {

        COMPLEX_TYPE = new QName("http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/",
                "LeadCrosscutParameters");
        TYPE_NS = factory.createOMNamespace(COMPLEX_TYPE.getNamespaceURI(), "lcp");
    }
}
