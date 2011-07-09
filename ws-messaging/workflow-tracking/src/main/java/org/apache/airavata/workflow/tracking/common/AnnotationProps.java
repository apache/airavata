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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;

/**
 * Use set(list) or setXml(list) to set multi valued annotations. Use add() or addXml() to add to multi valued
 * annotations. Use set(string) or setXml(xmlobj) to set single valued annotations.
 * 
 */
public class AnnotationProps {

    private Map<AnnotationConsts, Object> localMap;

    private AnnotationProps() {
        localMap = new HashMap<AnnotationConsts, Object>();
    }

    public static AnnotationProps newProps() {
        return new AnnotationProps();
    }

    public static AnnotationProps newProps(AnnotationConsts key, String value) {
        return newProps().set(key, value);
    }

    public static AnnotationProps newProps(AnnotationConsts key, XmlObject value) {
        return newProps().setXml(key, value);
    }

    /**
     * Use for single valued string annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a String
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps set(AnnotationConsts key, String value) {
        if (!key.isSimpleType())
            throw new RuntimeException("Expect XML Object value for annotation, not String [" + key + ", " + value
                    + "]");
        if (key.isMultiValued())
            throw new RuntimeException("Expect list XML Object value for annotation, not single [" + key + ", " + value
                    + "]");

        localMap.put(key, value);
        return this;
    }

    /**
     * Use for single valued XmlObj annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a XmlObject
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps setXml(AnnotationConsts key, XmlObject value) {
        if (key.isSimpleType())
            throw new RuntimeException("Expect string value for annotation, not Xml Object [" + key + ", " + value
                    + "]");
        if (key.isMultiValued())
            throw new RuntimeException("Expect list XML Object value for annotation, not single [" + key + ", " + value
                    + "]");

        localMap.put(key, value);
        return this;
    }

    /**
     * Use for multivalued string annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a List
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps set(AnnotationConsts key, List<String> value) {
        if (!key.isSimpleType())
            throw new RuntimeException("Expect XML Object value for annotation, not String [" + key + ", " + value
                    + "]");
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation, not List [" + key + ", " + value
                    + "]");

        localMap.put(key, value);
        return this;
    }

    /**
     * Use for multivalued XmlObj annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a List
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps setXml(AnnotationConsts key, List<XmlObject> value) {
        if (key.isSimpleType())
            throw new RuntimeException("Expect string value for annotation, not Xml Object [" + key + ", " + value
                    + "]");
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation, not List [" + key + ", " + value
                    + "]");

        localMap.put(key, value);
        return this;
    }

    /**
     * Use to add to existing multivalued string annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a String
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps add(AnnotationConsts key, String value) {
        if (!key.isSimpleType())
            throw new RuntimeException("Expect XML Object value for annotation, not String [" + key + ", " + value
                    + "]");
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation. use set, not add [" + key + ", "
                    + value + "]");

        List<String> val = (List<String>) localMap.get(key);
        if (val == null) {
            val = new ArrayList<String>();
            localMap.put(key, val);
        }
        val.add(value);
        return this;
    }

    /**
     * Use to add to existing multivalued XmlObj annotation
     * 
     * @param key
     *            an AnnotationConsts
     * @param value
     *            a XmlObject
     * 
     * @return an AnnotationProps
     * 
     */
    public AnnotationProps addXml(AnnotationConsts key, XmlObject value) {
        if (key.isSimpleType())
            throw new RuntimeException("Expect string value for annotation, not Xml Object [" + key + ", " + value
                    + "]");
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation, use set, not add [" + key + ", "
                    + value + "]");

        List<XmlObject> val = (List<XmlObject>) localMap.get(key);
        if (val == null) {
            val = new ArrayList<XmlObject>();
            localMap.put(key, val);
        }
        val.add(value);
        return this;
    }

    public String get(AnnotationConsts key) {
        if (!key.isSimpleType())
            throw new RuntimeException("Expect XML Object value for annotation, not String" + " for anno: " + key);
        if (key.isMultiValued())
            throw new RuntimeException("Expect list XML Object value for annotation, not single" + " for anno: " + key);

        return (String) localMap.get(key);
    }

    public XmlObject getXml(AnnotationConsts key) {
        if (key.isSimpleType())
            throw new RuntimeException("Expect string value for annotation, not Xml Object" + " for anno: " + key);
        if (!key.isMultiValued())
            throw new RuntimeException("Expect list XML Object value for annotation, not single" + " for anno: " + key);

        return (XmlObject) localMap.get(key);
    }

    public List<String> getAll(AnnotationConsts key) {
        if (!key.isSimpleType())
            throw new RuntimeException("Expect XML Object value for annotation, not String" + " for anno: " + key);
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation, not List" + " for anno: " + key);

        return (List<String>) localMap.get(key);
    }

    public List<XmlObject> getAllXml(AnnotationConsts key) {
        if (key.isSimpleType())
            throw new RuntimeException("Expect string value for annotation, not Xml Object" + " for anno: " + key);
        if (!key.isMultiValued())
            throw new RuntimeException("Expect single XML Object value for annotation, not List" + " for anno: " + key);

        return (List<XmlObject>) localMap.get(key);
    }

    public Set<AnnotationConsts> getKeys() {
        return localMap.keySet();
    }

    public int size() {
        return localMap.size();
    }

    @Override
    public String toString() {
        final StringBuffer anno = new StringBuffer();
        for (AnnotationConsts key : localMap.keySet()) {
            if (key.isSimpleType()) {
                if (key.isMultiValued()) {
                    // List<String>
                    List<String> values = (List<String>) localMap.get(key);
                    for (String val : values) {
                        addStartTag(anno, key);
                        anno.append(val);
                        addEndTag(anno, key);
                    }
                } else {
                    // String
                    String val = (String) localMap.get(key);
                    addStartTag(anno, key);
                    anno.append(val);
                    addEndTag(anno, key);
                }
            } else {
                if (key.isMultiValued()) {
                    // List<XmlObject>
                    List<XmlObject> values = (List<XmlObject>) localMap.get(key);
                    for (XmlObject val : values) {
                        addStartTag(anno, key);
                        anno.append(val.xmlText());
                        addEndTag(anno, key);
                    }
                } else {
                    // XmlObject
                    XmlObject val = (XmlObject) localMap.get(key);
                    addStartTag(anno, key);
                    anno.append(val.xmlText());
                    addEndTag(anno, key);
                }
            }
        }
        return anno.toString();
    }

    private void addStartTag(StringBuffer anno, AnnotationConsts key) {
        QName qname = key.getQName();
        anno.append('<');
        anno.append(qname.getLocalPart());
        if (qname.getNamespaceURI() != null) {
            anno.append(" xmlns='");
            anno.append(qname.getNamespaceURI());
            anno.append('\'');
        }
        anno.append('>');
    }

    private void addEndTag(StringBuffer anno, AnnotationConsts key) {
        anno.append("</");
        anno.append(key.getQName().getLocalPart());
        anno.append('>');
    }

}
