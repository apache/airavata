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
 * XML Type:  afterAnyList
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.AfterAnyList
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML afterAnyList(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class AfterAnyListImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.AfterAnyList
{
    private static final long serialVersionUID = 1L;
    
    public AfterAnyListImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AFTERANY$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "afterAny");
    
    
    /**
     * Gets array of all "afterAny" elements
     */
    public java.lang.String[] getAfterAnyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AFTERANY$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "afterAny" element
     */
    public java.lang.String getAfterAnyArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AFTERANY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "afterAny" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetAfterAnyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AFTERANY$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "afterAny" element
     */
    public org.apache.xmlbeans.XmlString xgetAfterAnyArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AFTERANY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "afterAny" element
     */
    public int sizeOfAfterAnyArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AFTERANY$0);
        }
    }
    
    /**
     * Sets array of all "afterAny" element
     */
    public void setAfterAnyArray(java.lang.String[] afterAnyArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(afterAnyArray, AFTERANY$0);
        }
    }
    
    /**
     * Sets ith "afterAny" element
     */
    public void setAfterAnyArray(int i, java.lang.String afterAny)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AFTERANY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(afterAny);
        }
    }
    
    /**
     * Sets (as xml) array of all "afterAny" element
     */
    public void xsetAfterAnyArray(org.apache.xmlbeans.XmlString[]afterAnyArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(afterAnyArray, AFTERANY$0);
        }
    }
    
    /**
     * Sets (as xml) ith "afterAny" element
     */
    public void xsetAfterAnyArray(int i, org.apache.xmlbeans.XmlString afterAny)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AFTERANY$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(afterAny);
        }
    }
    
    /**
     * Inserts the value as the ith "afterAny" element
     */
    public void insertAfterAny(int i, java.lang.String afterAny)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(AFTERANY$0, i);
            target.setStringValue(afterAny);
        }
    }
    
    /**
     * Appends the value as the last "afterAny" element
     */
    public void addAfterAny(java.lang.String afterAny)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AFTERANY$0);
            target.setStringValue(afterAny);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "afterAny" element
     */
    public org.apache.xmlbeans.XmlString insertNewAfterAny(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(AFTERANY$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "afterAny" element
     */
    public org.apache.xmlbeans.XmlString addNewAfterAny()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AFTERANY$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "afterAny" element
     */
    public void removeAfterAny(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AFTERANY$0, i);
        }
    }
}
