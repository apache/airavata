/**
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
 */
/*
 * XML Type:  afterOKList
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.AfterOKList
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12;


/**
 * An XML afterOKList(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public interface AfterOKList extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AfterOKList.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sCF8C40CE6FDA0A41BEE004F5930560FF").resolveHandle("afteroklistc764type");
    
    /**
     * Gets array of all "afterOKList" elements
     */
    java.lang.String[] getAfterOKListArray();
    
    /**
     * Gets ith "afterOKList" element
     */
    java.lang.String getAfterOKListArray(int i);
    
    /**
     * Gets (as xml) array of all "afterOKList" elements
     */
    org.apache.xmlbeans.XmlString[] xgetAfterOKListArray();
    
    /**
     * Gets (as xml) ith "afterOKList" element
     */
    org.apache.xmlbeans.XmlString xgetAfterOKListArray(int i);
    
    /**
     * Returns number of "afterOKList" element
     */
    int sizeOfAfterOKListArray();
    
    /**
     * Sets array of all "afterOKList" element
     */
    void setAfterOKListArray(java.lang.String[] afterOKListArray);
    
    /**
     * Sets ith "afterOKList" element
     */
    void setAfterOKListArray(int i, java.lang.String afterOKList);
    
    /**
     * Sets (as xml) array of all "afterOKList" element
     */
    void xsetAfterOKListArray(org.apache.xmlbeans.XmlString[] afterOKListArray);
    
    /**
     * Sets (as xml) ith "afterOKList" element
     */
    void xsetAfterOKListArray(int i, org.apache.xmlbeans.XmlString afterOKList);
    
    /**
     * Inserts the value as the ith "afterOKList" element
     */
    void insertAfterOKList(int i, java.lang.String afterOKList);
    
    /**
     * Appends the value as the last "afterOKList" element
     */
    void addAfterOKList(java.lang.String afterOKList);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "afterOKList" element
     */
    org.apache.xmlbeans.XmlString insertNewAfterOKList(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "afterOKList" element
     */
    org.apache.xmlbeans.XmlString addNewAfterOKList();
    
    /**
     * Removes the ith "afterOKList" element
     */
    void removeAfterOKList(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList newInstance() {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.airavata.gfac.core.x2012.x12.AfterOKList parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (org.apache.airavata.gfac.core.x2012.x12.AfterOKList) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
