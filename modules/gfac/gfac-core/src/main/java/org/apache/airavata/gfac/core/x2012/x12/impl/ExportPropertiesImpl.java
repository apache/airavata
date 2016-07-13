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
 * XML Type:  exportProperties
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.ExportProperties
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML exportProperties(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class ExportPropertiesImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.ExportProperties
{
    private static final long serialVersionUID = 1L;
    
    public ExportPropertiesImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName NAME$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "name");
    
    
    /**
     * Gets array of all "name" elements
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name[] getNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(NAME$0, targetList);
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name[] result = new org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "name" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name getNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name)get_store().find_element_user(NAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "name" element
     */
    public int sizeOfNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NAME$0);
        }
    }
    
    /**
     * Sets array of all "name" element
     */
    public void setNameArray(org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name[] nameArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(nameArray, NAME$0);
        }
    }
    
    /**
     * Sets ith "name" element
     */
    public void setNameArray(int i, org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name name)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name)get_store().find_element_user(NAME$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(name);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "name" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name insertNewName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name)get_store().insert_element_user(NAME$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "name" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name addNewName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name)get_store().add_element_user(NAME$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "name" element
     */
    public void removeName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NAME$0, i);
        }
    }
    /**
     * An XML name(@http://airavata.apache.org/gfac/core/2012/12).
     *
     * This is an atomic type that is a restriction of org.apache.airavata.gfac.core.x2012.x12.ExportProperties$Name.
     */
    public static class NameImpl extends org.apache.xmlbeans.impl.values.JavaStringHolderEx implements org.apache.airavata.gfac.core.x2012.x12.ExportProperties.Name
    {
        private static final long serialVersionUID = 1L;
        
        public NameImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType, true);
        }
        
        protected NameImpl(org.apache.xmlbeans.SchemaType sType, boolean b)
        {
            super(sType, b);
        }
        
        private static final javax.xml.namespace.QName VALUE$0 = 
            new javax.xml.namespace.QName("", "value");
        
        
        /**
         * Gets the "value" attribute
         */
        public java.lang.String getValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$0);
                if (target == null)
                {
                    return null;
                }
                return target.getStringValue();
            }
        }
        
        /**
         * Gets (as xml) the "value" attribute
         */
        public org.apache.xmlbeans.XmlString xgetValue()
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$0);
                return target;
            }
        }
        
        /**
         * Sets the "value" attribute
         */
        public void setValue(java.lang.String value)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.SimpleValue target = null;
                target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VALUE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VALUE$0);
                }
                target.setStringValue(value);
            }
        }
        
        /**
         * Sets (as xml) the "value" attribute
         */
        public void xsetValue(org.apache.xmlbeans.XmlString value)
        {
            synchronized (monitor())
            {
                check_orphaned();
                org.apache.xmlbeans.XmlString target = null;
                target = (org.apache.xmlbeans.XmlString)get_store().find_attribute_user(VALUE$0);
                if (target == null)
                {
                    target = (org.apache.xmlbeans.XmlString)get_store().add_attribute_user(VALUE$0);
                }
                target.set(value);
            }
        }
    }
}
