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
 * XML Type:  moduleLoadCommands
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML moduleLoadCommands(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class ModuleLoadCommandsImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands
{
    private static final long serialVersionUID = 1L;
    
    public ModuleLoadCommandsImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COMMAND$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "command");
    
    
    /**
     * Gets array of all "command" elements
     */
    public java.lang.String[] getCommandArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COMMAND$0, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "command" element
     */
    public java.lang.String getCommandArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMMAND$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "command" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetCommandArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COMMAND$0, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "command" element
     */
    public org.apache.xmlbeans.XmlString xgetCommandArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMMAND$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return (org.apache.xmlbeans.XmlString)target;
        }
    }
    
    /**
     * Returns number of "command" element
     */
    public int sizeOfCommandArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COMMAND$0);
        }
    }
    
    /**
     * Sets array of all "command" element
     */
    public void setCommandArray(java.lang.String[] commandArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(commandArray, COMMAND$0);
        }
    }
    
    /**
     * Sets ith "command" element
     */
    public void setCommandArray(int i, java.lang.String command)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMMAND$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(command);
        }
    }
    
    /**
     * Sets (as xml) array of all "command" element
     */
    public void xsetCommandArray(org.apache.xmlbeans.XmlString[]commandArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(commandArray, COMMAND$0);
        }
    }
    
    /**
     * Sets (as xml) ith "command" element
     */
    public void xsetCommandArray(int i, org.apache.xmlbeans.XmlString command)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMMAND$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(command);
        }
    }
    
    /**
     * Inserts the value as the ith "command" element
     */
    public void insertCommand(int i, java.lang.String command)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(COMMAND$0, i);
            target.setStringValue(command);
        }
    }
    
    /**
     * Appends the value as the last "command" element
     */
    public void addCommand(java.lang.String command)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COMMAND$0);
            target.setStringValue(command);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "command" element
     */
    public org.apache.xmlbeans.XmlString insertNewCommand(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(COMMAND$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "command" element
     */
    public org.apache.xmlbeans.XmlString addNewCommand()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COMMAND$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "command" element
     */
    public void removeCommand(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COMMAND$0, i);
        }
    }
}
