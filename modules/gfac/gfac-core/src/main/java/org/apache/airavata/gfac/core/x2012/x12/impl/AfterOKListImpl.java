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
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML afterOKList(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class AfterOKListImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.AfterOKList
{
    private static final long serialVersionUID = 1L;
    
    public AfterOKListImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AFTEROKLIST$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "afterOKList");
    
    
    /**
     * Gets array of all "afterOKList" elements
     */
    public java.lang.String[] getAfterOKListArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AFTEROKLIST$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "afterOKList" element
     */
    public java.lang.String getAfterOKListArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AFTEROKLIST$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "afterOKList" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetAfterOKListArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AFTEROKLIST$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "afterOKList" element
     */
    public org.apache.xmlbeans.XmlString xgetAfterOKListArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AFTEROKLIST$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "afterOKList" element
     */
    public int sizeOfAfterOKListArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AFTEROKLIST$0);
        }
    }
    
    /**
     * Sets array of all "afterOKList" element
     */
    public void setAfterOKListArray(java.lang.String[] afterOKListArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(afterOKListArray, AFTEROKLIST$0);
        }
    }
    
    /**
     * Sets ith "afterOKList" element
     */
    public void setAfterOKListArray(int i, java.lang.String afterOKList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AFTEROKLIST$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(afterOKList);
        }
    }
    
    /**
     * Sets (as xml) array of all "afterOKList" element
     */
    public void xsetAfterOKListArray(org.apache.xmlbeans.XmlString[]afterOKListArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(afterOKListArray, AFTEROKLIST$0);
        }
    }
    
    /**
     * Sets (as xml) ith "afterOKList" element
     */
    public void xsetAfterOKListArray(int i, org.apache.xmlbeans.XmlString afterOKList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AFTEROKLIST$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(afterOKList);
        }
    }
    
    /**
     * Inserts the value as the ith "afterOKList" element
     */
    public void insertAfterOKList(int i, java.lang.String afterOKList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(AFTEROKLIST$0, i);
            target.setStringValue(afterOKList);
        }
    }
    
    /**
     * Appends the value as the last "afterOKList" element
     */
    public void addAfterOKList(java.lang.String afterOKList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AFTEROKLIST$0);
            target.setStringValue(afterOKList);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "afterOKList" element
     */
    public org.apache.xmlbeans.XmlString insertNewAfterOKList(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(AFTEROKLIST$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "afterOKList" element
     */
    public org.apache.xmlbeans.XmlString addNewAfterOKList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AFTEROKLIST$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "afterOKList" element
     */
    public void removeAfterOKList(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AFTEROKLIST$0, i);
        }
    }
}
