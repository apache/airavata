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
 * XML Type:  inputList
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.InputList
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML inputList(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class InputListImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.InputList
{
    private static final long serialVersionUID = 1L;
    
    public InputListImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INPUT$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "input");
    
    
    /**
     * Gets array of all "input" elements
     */
    public java.lang.String[] getInputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(INPUT$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "input" element
     */
    public java.lang.String getInputArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "input" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetInputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(INPUT$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "input" element
     */
    public org.apache.xmlbeans.XmlString xgetInputArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "input" element
     */
    public int sizeOfInputArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INPUT$0);
        }
    }
    
    /**
     * Sets array of all "input" element
     */
    public void setInputArray(java.lang.String[] inputArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(inputArray, INPUT$0);
        }
    }
    
    /**
     * Sets ith "input" element
     */
    public void setInputArray(int i, java.lang.String input)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(input);
        }
    }
    
    /**
     * Sets (as xml) array of all "input" element
     */
    public void xsetInputArray(org.apache.xmlbeans.XmlString[]inputArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(inputArray, INPUT$0);
        }
    }
    
    /**
     * Sets (as xml) ith "input" element
     */
    public void xsetInputArray(int i, org.apache.xmlbeans.XmlString input)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INPUT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(input);
        }
    }
    
    /**
     * Inserts the value as the ith "input" element
     */
    public void insertInput(int i, java.lang.String input)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(INPUT$0, i);
            target.setStringValue(input);
        }
    }
    
    /**
     * Appends the value as the last "input" element
     */
    public void addInput(java.lang.String input)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(INPUT$0);
            target.setStringValue(input);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "input" element
     */
    public org.apache.xmlbeans.XmlString insertNewInput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(INPUT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "input" element
     */
    public org.apache.xmlbeans.XmlString addNewInput()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(INPUT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "input" element
     */
    public void removeInput(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INPUT$0, i);
        }
    }
}
