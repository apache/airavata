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
 * XML Type:  pbsParams
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.PbsParams
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * An XML pbsParams(@http://airavata.apache.org/gfac/core/2012/12).
 *
 * This is a complex type.
 */
public class PbsParamsImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.PbsParams
{
    private static final long serialVersionUID = 1L;
    
    public PbsParamsImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName JOBID$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "jobID");
    private static final javax.xml.namespace.QName USERNAME$2 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "userName");
    private static final javax.xml.namespace.QName SHELLNAME$4 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "shellName");
    private static final javax.xml.namespace.QName QUEUENAME$6 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "queueName");
    private static final javax.xml.namespace.QName JOBNAME$8 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "jobName");
    private static final javax.xml.namespace.QName ALLENVEXPORT$10 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "allEnvExport");
    private static final javax.xml.namespace.QName MAILOPTIONS$12 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "mailOptions");
    private static final javax.xml.namespace.QName MAILADDRESS$14 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "mailAddress");
    private static final javax.xml.namespace.QName PARTITION$16 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "partition");
    private static final javax.xml.namespace.QName MAILTYPE$18 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "mailType");
    private static final javax.xml.namespace.QName ACOUNTSTRING$20 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "acountString");
    private static final javax.xml.namespace.QName MAXWALLTIME$22 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "maxWallTime");
    private static final javax.xml.namespace.QName STANDARDOUTFILE$24 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "standardOutFile");
    private static final javax.xml.namespace.QName STANDARDERRORFILE$26 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "standardErrorFile");
    private static final javax.xml.namespace.QName OUTPUTDIRECTORY$28 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "outputDirectory");
    private static final javax.xml.namespace.QName INPUTDIRECTORY$30 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "inputDirectory");
    private static final javax.xml.namespace.QName NODES$32 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "nodes");
    private static final javax.xml.namespace.QName PROCESSESPERNODE$34 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "processesPerNode");
    private static final javax.xml.namespace.QName CPUCOUNT$36 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "cpuCount");
    private static final javax.xml.namespace.QName NODELIST$38 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "nodeList");
    private static final javax.xml.namespace.QName WORKINGDIRECTORY$40 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "workingDirectory");
    private static final javax.xml.namespace.QName EXECUTABLEPATH$42 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "executablePath");
    private static final javax.xml.namespace.QName INPUTS$44 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "inputs");
    private static final javax.xml.namespace.QName EXPORTS$46 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "exports");
    private static final javax.xml.namespace.QName STATUS$48 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "status");
    private static final javax.xml.namespace.QName AFTERANY$50 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "afterAny");
    private static final javax.xml.namespace.QName AFTEROKLIST$52 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "afterOKList");
    private static final javax.xml.namespace.QName CTIME$54 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "cTime");
    private static final javax.xml.namespace.QName QTIME$56 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "qTime");
    private static final javax.xml.namespace.QName MTIME$58 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "mTime");
    private static final javax.xml.namespace.QName STIME$60 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "sTime");
    private static final javax.xml.namespace.QName COMPTIME$62 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "compTime");
    private static final javax.xml.namespace.QName OWNER$64 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "owner");
    private static final javax.xml.namespace.QName EXECUTENODE$66 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "executeNode");
    private static final javax.xml.namespace.QName ELLAPSEDTIME$68 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "ellapsedTime");
    private static final javax.xml.namespace.QName USEDCPUTIME$70 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "usedCPUTime");
    private static final javax.xml.namespace.QName USEDMEM$72 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "usedMem");
    private static final javax.xml.namespace.QName SUBMITARGS$74 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "submitArgs");
    private static final javax.xml.namespace.QName VARIABLELIST$76 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "variableList");
    private static final javax.xml.namespace.QName PREJOBCOMMANDS$78 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "preJobCommands");
    private static final javax.xml.namespace.QName MODULELOADCOMMANDS$80 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "moduleLoadCommands");
    private static final javax.xml.namespace.QName POSTJOBCOMMANDS$82 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "postJobCommands");
    private static final javax.xml.namespace.QName JOBSUBMITTERCOMMAND$84 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "jobSubmitterCommand");
    private static final javax.xml.namespace.QName CALLBACKIP$86 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "callBackIp");
    private static final javax.xml.namespace.QName CALLBACKPORT$88 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "callBackPort");
    private static final javax.xml.namespace.QName CHASSISNAME$90 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "chassisName");
    
    
    /**
     * Gets the "jobID" element
     */
    public java.lang.String getJobID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBID$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "jobID" element
     */
    public org.apache.xmlbeans.XmlString xgetJobID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBID$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "jobID" element
     */
    public boolean isSetJobID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(JOBID$0) != 0;
        }
    }
    
    /**
     * Sets the "jobID" element
     */
    public void setJobID(java.lang.String jobID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBID$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(JOBID$0);
            }
            target.setStringValue(jobID);
        }
    }
    
    /**
     * Sets (as xml) the "jobID" element
     */
    public void xsetJobID(org.apache.xmlbeans.XmlString jobID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBID$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(JOBID$0);
            }
            target.set(jobID);
        }
    }
    
    /**
     * Unsets the "jobID" element
     */
    public void unsetJobID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(JOBID$0, 0);
        }
    }
    
    /**
     * Gets the "userName" element
     */
    public java.lang.String getUserName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERNAME$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "userName" element
     */
    public org.apache.xmlbeans.XmlString xgetUserName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USERNAME$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "userName" element
     */
    public boolean isSetUserName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(USERNAME$2) != 0;
        }
    }
    
    /**
     * Sets the "userName" element
     */
    public void setUserName(java.lang.String userName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USERNAME$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USERNAME$2);
            }
            target.setStringValue(userName);
        }
    }
    
    /**
     * Sets (as xml) the "userName" element
     */
    public void xsetUserName(org.apache.xmlbeans.XmlString userName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USERNAME$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(USERNAME$2);
            }
            target.set(userName);
        }
    }
    
    /**
     * Unsets the "userName" element
     */
    public void unsetUserName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(USERNAME$2, 0);
        }
    }
    
    /**
     * Gets the "shellName" element
     */
    public java.lang.String getShellName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SHELLNAME$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "shellName" element
     */
    public org.apache.xmlbeans.XmlString xgetShellName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SHELLNAME$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "shellName" element
     */
    public boolean isSetShellName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SHELLNAME$4) != 0;
        }
    }
    
    /**
     * Sets the "shellName" element
     */
    public void setShellName(java.lang.String shellName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SHELLNAME$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SHELLNAME$4);
            }
            target.setStringValue(shellName);
        }
    }
    
    /**
     * Sets (as xml) the "shellName" element
     */
    public void xsetShellName(org.apache.xmlbeans.XmlString shellName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SHELLNAME$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SHELLNAME$4);
            }
            target.set(shellName);
        }
    }
    
    /**
     * Unsets the "shellName" element
     */
    public void unsetShellName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SHELLNAME$4, 0);
        }
    }
    
    /**
     * Gets the "queueName" element
     */
    public java.lang.String getQueueName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(QUEUENAME$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "queueName" element
     */
    public org.apache.xmlbeans.XmlString xgetQueueName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(QUEUENAME$6, 0);
            return target;
        }
    }
    
    /**
     * True if has "queueName" element
     */
    public boolean isSetQueueName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(QUEUENAME$6) != 0;
        }
    }
    
    /**
     * Sets the "queueName" element
     */
    public void setQueueName(java.lang.String queueName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(QUEUENAME$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(QUEUENAME$6);
            }
            target.setStringValue(queueName);
        }
    }
    
    /**
     * Sets (as xml) the "queueName" element
     */
    public void xsetQueueName(org.apache.xmlbeans.XmlString queueName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(QUEUENAME$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(QUEUENAME$6);
            }
            target.set(queueName);
        }
    }
    
    /**
     * Unsets the "queueName" element
     */
    public void unsetQueueName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(QUEUENAME$6, 0);
        }
    }
    
    /**
     * Gets the "jobName" element
     */
    public java.lang.String getJobName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBNAME$8, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "jobName" element
     */
    public org.apache.xmlbeans.XmlString xgetJobName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBNAME$8, 0);
            return target;
        }
    }
    
    /**
     * True if has "jobName" element
     */
    public boolean isSetJobName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(JOBNAME$8) != 0;
        }
    }
    
    /**
     * Sets the "jobName" element
     */
    public void setJobName(java.lang.String jobName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBNAME$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(JOBNAME$8);
            }
            target.setStringValue(jobName);
        }
    }
    
    /**
     * Sets (as xml) the "jobName" element
     */
    public void xsetJobName(org.apache.xmlbeans.XmlString jobName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBNAME$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(JOBNAME$8);
            }
            target.set(jobName);
        }
    }
    
    /**
     * Unsets the "jobName" element
     */
    public void unsetJobName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(JOBNAME$8, 0);
        }
    }
    
    /**
     * Gets the "allEnvExport" element
     */
    public boolean getAllEnvExport()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ALLENVEXPORT$10, 0);
            if (target == null)
            {
                return false;
            }
            return target.getBooleanValue();
        }
    }
    
    /**
     * Gets (as xml) the "allEnvExport" element
     */
    public org.apache.xmlbeans.XmlBoolean xgetAllEnvExport()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ALLENVEXPORT$10, 0);
            return target;
        }
    }
    
    /**
     * True if has "allEnvExport" element
     */
    public boolean isSetAllEnvExport()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ALLENVEXPORT$10) != 0;
        }
    }
    
    /**
     * Sets the "allEnvExport" element
     */
    public void setAllEnvExport(boolean allEnvExport)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ALLENVEXPORT$10, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ALLENVEXPORT$10);
            }
            target.setBooleanValue(allEnvExport);
        }
    }
    
    /**
     * Sets (as xml) the "allEnvExport" element
     */
    public void xsetAllEnvExport(org.apache.xmlbeans.XmlBoolean allEnvExport)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlBoolean target = null;
            target = (org.apache.xmlbeans.XmlBoolean)get_store().find_element_user(ALLENVEXPORT$10, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlBoolean)get_store().add_element_user(ALLENVEXPORT$10);
            }
            target.set(allEnvExport);
        }
    }
    
    /**
     * Unsets the "allEnvExport" element
     */
    public void unsetAllEnvExport()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ALLENVEXPORT$10, 0);
        }
    }
    
    /**
     * Gets the "mailOptions" element
     */
    public java.lang.String getMailOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILOPTIONS$12, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "mailOptions" element
     */
    public org.apache.xmlbeans.XmlString xgetMailOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILOPTIONS$12, 0);
            return target;
        }
    }
    
    /**
     * True if has "mailOptions" element
     */
    public boolean isSetMailOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MAILOPTIONS$12) != 0;
        }
    }
    
    /**
     * Sets the "mailOptions" element
     */
    public void setMailOptions(java.lang.String mailOptions)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILOPTIONS$12, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MAILOPTIONS$12);
            }
            target.setStringValue(mailOptions);
        }
    }
    
    /**
     * Sets (as xml) the "mailOptions" element
     */
    public void xsetMailOptions(org.apache.xmlbeans.XmlString mailOptions)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILOPTIONS$12, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MAILOPTIONS$12);
            }
            target.set(mailOptions);
        }
    }
    
    /**
     * Unsets the "mailOptions" element
     */
    public void unsetMailOptions()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MAILOPTIONS$12, 0);
        }
    }
    
    /**
     * Gets the "mailAddress" element
     */
    public java.lang.String getMailAddress()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILADDRESS$14, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "mailAddress" element
     */
    public org.apache.xmlbeans.XmlString xgetMailAddress()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILADDRESS$14, 0);
            return target;
        }
    }
    
    /**
     * True if has "mailAddress" element
     */
    public boolean isSetMailAddress()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MAILADDRESS$14) != 0;
        }
    }
    
    /**
     * Sets the "mailAddress" element
     */
    public void setMailAddress(java.lang.String mailAddress)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILADDRESS$14, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MAILADDRESS$14);
            }
            target.setStringValue(mailAddress);
        }
    }
    
    /**
     * Sets (as xml) the "mailAddress" element
     */
    public void xsetMailAddress(org.apache.xmlbeans.XmlString mailAddress)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILADDRESS$14, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MAILADDRESS$14);
            }
            target.set(mailAddress);
        }
    }
    
    /**
     * Unsets the "mailAddress" element
     */
    public void unsetMailAddress()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MAILADDRESS$14, 0);
        }
    }
    
    /**
     * Gets the "partition" element
     */
    public java.lang.String getPartition()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTITION$16, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "partition" element
     */
    public org.apache.xmlbeans.XmlString xgetPartition()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PARTITION$16, 0);
            return target;
        }
    }
    
    /**
     * True if has "partition" element
     */
    public boolean isSetPartition()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PARTITION$16) != 0;
        }
    }
    
    /**
     * Sets the "partition" element
     */
    public void setPartition(java.lang.String partition)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PARTITION$16, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PARTITION$16);
            }
            target.setStringValue(partition);
        }
    }
    
    /**
     * Sets (as xml) the "partition" element
     */
    public void xsetPartition(org.apache.xmlbeans.XmlString partition)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PARTITION$16, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PARTITION$16);
            }
            target.set(partition);
        }
    }
    
    /**
     * Unsets the "partition" element
     */
    public void unsetPartition()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PARTITION$16, 0);
        }
    }
    
    /**
     * Gets the "mailType" element
     */
    public java.lang.String getMailType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILTYPE$18, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "mailType" element
     */
    public org.apache.xmlbeans.XmlString xgetMailType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILTYPE$18, 0);
            return target;
        }
    }
    
    /**
     * True if has "mailType" element
     */
    public boolean isSetMailType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MAILTYPE$18) != 0;
        }
    }
    
    /**
     * Sets the "mailType" element
     */
    public void setMailType(java.lang.String mailType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAILTYPE$18, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MAILTYPE$18);
            }
            target.setStringValue(mailType);
        }
    }
    
    /**
     * Sets (as xml) the "mailType" element
     */
    public void xsetMailType(org.apache.xmlbeans.XmlString mailType)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAILTYPE$18, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MAILTYPE$18);
            }
            target.set(mailType);
        }
    }
    
    /**
     * Unsets the "mailType" element
     */
    public void unsetMailType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MAILTYPE$18, 0);
        }
    }
    
    /**
     * Gets the "acountString" element
     */
    public java.lang.String getAcountString()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ACOUNTSTRING$20, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "acountString" element
     */
    public org.apache.xmlbeans.XmlString xgetAcountString()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ACOUNTSTRING$20, 0);
            return target;
        }
    }
    
    /**
     * True if has "acountString" element
     */
    public boolean isSetAcountString()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ACOUNTSTRING$20) != 0;
        }
    }
    
    /**
     * Sets the "acountString" element
     */
    public void setAcountString(java.lang.String acountString)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ACOUNTSTRING$20, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ACOUNTSTRING$20);
            }
            target.setStringValue(acountString);
        }
    }
    
    /**
     * Sets (as xml) the "acountString" element
     */
    public void xsetAcountString(org.apache.xmlbeans.XmlString acountString)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ACOUNTSTRING$20, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ACOUNTSTRING$20);
            }
            target.set(acountString);
        }
    }
    
    /**
     * Unsets the "acountString" element
     */
    public void unsetAcountString()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ACOUNTSTRING$20, 0);
        }
    }
    
    /**
     * Gets the "maxWallTime" element
     */
    public java.lang.String getMaxWallTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAXWALLTIME$22, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "maxWallTime" element
     */
    public org.apache.xmlbeans.XmlString xgetMaxWallTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAXWALLTIME$22, 0);
            return target;
        }
    }
    
    /**
     * True if has "maxWallTime" element
     */
    public boolean isSetMaxWallTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MAXWALLTIME$22) != 0;
        }
    }
    
    /**
     * Sets the "maxWallTime" element
     */
    public void setMaxWallTime(java.lang.String maxWallTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MAXWALLTIME$22, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MAXWALLTIME$22);
            }
            target.setStringValue(maxWallTime);
        }
    }
    
    /**
     * Sets (as xml) the "maxWallTime" element
     */
    public void xsetMaxWallTime(org.apache.xmlbeans.XmlString maxWallTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MAXWALLTIME$22, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MAXWALLTIME$22);
            }
            target.set(maxWallTime);
        }
    }
    
    /**
     * Unsets the "maxWallTime" element
     */
    public void unsetMaxWallTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MAXWALLTIME$22, 0);
        }
    }
    
    /**
     * Gets the "standardOutFile" element
     */
    public java.lang.String getStandardOutFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STANDARDOUTFILE$24, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "standardOutFile" element
     */
    public org.apache.xmlbeans.XmlString xgetStandardOutFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STANDARDOUTFILE$24, 0);
            return target;
        }
    }
    
    /**
     * True if has "standardOutFile" element
     */
    public boolean isSetStandardOutFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STANDARDOUTFILE$24) != 0;
        }
    }
    
    /**
     * Sets the "standardOutFile" element
     */
    public void setStandardOutFile(java.lang.String standardOutFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STANDARDOUTFILE$24, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STANDARDOUTFILE$24);
            }
            target.setStringValue(standardOutFile);
        }
    }
    
    /**
     * Sets (as xml) the "standardOutFile" element
     */
    public void xsetStandardOutFile(org.apache.xmlbeans.XmlString standardOutFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STANDARDOUTFILE$24, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STANDARDOUTFILE$24);
            }
            target.set(standardOutFile);
        }
    }
    
    /**
     * Unsets the "standardOutFile" element
     */
    public void unsetStandardOutFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STANDARDOUTFILE$24, 0);
        }
    }
    
    /**
     * Gets the "standardErrorFile" element
     */
    public java.lang.String getStandardErrorFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STANDARDERRORFILE$26, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "standardErrorFile" element
     */
    public org.apache.xmlbeans.XmlString xgetStandardErrorFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STANDARDERRORFILE$26, 0);
            return target;
        }
    }
    
    /**
     * True if has "standardErrorFile" element
     */
    public boolean isSetStandardErrorFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STANDARDERRORFILE$26) != 0;
        }
    }
    
    /**
     * Sets the "standardErrorFile" element
     */
    public void setStandardErrorFile(java.lang.String standardErrorFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STANDARDERRORFILE$26, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STANDARDERRORFILE$26);
            }
            target.setStringValue(standardErrorFile);
        }
    }
    
    /**
     * Sets (as xml) the "standardErrorFile" element
     */
    public void xsetStandardErrorFile(org.apache.xmlbeans.XmlString standardErrorFile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STANDARDERRORFILE$26, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STANDARDERRORFILE$26);
            }
            target.set(standardErrorFile);
        }
    }
    
    /**
     * Unsets the "standardErrorFile" element
     */
    public void unsetStandardErrorFile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STANDARDERRORFILE$26, 0);
        }
    }
    
    /**
     * Gets the "outputDirectory" element
     */
    public java.lang.String getOutputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OUTPUTDIRECTORY$28, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "outputDirectory" element
     */
    public org.apache.xmlbeans.XmlString xgetOutputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OUTPUTDIRECTORY$28, 0);
            return target;
        }
    }
    
    /**
     * True if has "outputDirectory" element
     */
    public boolean isSetOutputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OUTPUTDIRECTORY$28) != 0;
        }
    }
    
    /**
     * Sets the "outputDirectory" element
     */
    public void setOutputDirectory(java.lang.String outputDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OUTPUTDIRECTORY$28, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OUTPUTDIRECTORY$28);
            }
            target.setStringValue(outputDirectory);
        }
    }
    
    /**
     * Sets (as xml) the "outputDirectory" element
     */
    public void xsetOutputDirectory(org.apache.xmlbeans.XmlString outputDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OUTPUTDIRECTORY$28, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OUTPUTDIRECTORY$28);
            }
            target.set(outputDirectory);
        }
    }
    
    /**
     * Unsets the "outputDirectory" element
     */
    public void unsetOutputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OUTPUTDIRECTORY$28, 0);
        }
    }
    
    /**
     * Gets the "inputDirectory" element
     */
    public java.lang.String getInputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INPUTDIRECTORY$30, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "inputDirectory" element
     */
    public org.apache.xmlbeans.XmlString xgetInputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INPUTDIRECTORY$30, 0);
            return target;
        }
    }
    
    /**
     * True if has "inputDirectory" element
     */
    public boolean isSetInputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INPUTDIRECTORY$30) != 0;
        }
    }
    
    /**
     * Sets the "inputDirectory" element
     */
    public void setInputDirectory(java.lang.String inputDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INPUTDIRECTORY$30, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(INPUTDIRECTORY$30);
            }
            target.setStringValue(inputDirectory);
        }
    }
    
    /**
     * Sets (as xml) the "inputDirectory" element
     */
    public void xsetInputDirectory(org.apache.xmlbeans.XmlString inputDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INPUTDIRECTORY$30, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(INPUTDIRECTORY$30);
            }
            target.set(inputDirectory);
        }
    }
    
    /**
     * Unsets the "inputDirectory" element
     */
    public void unsetInputDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INPUTDIRECTORY$30, 0);
        }
    }
    
    /**
     * Gets the "nodes" element
     */
    public int getNodes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NODES$32, 0);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "nodes" element
     */
    public org.apache.xmlbeans.XmlInt xgetNodes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(NODES$32, 0);
            return target;
        }
    }
    
    /**
     * True if has "nodes" element
     */
    public boolean isSetNodes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NODES$32) != 0;
        }
    }
    
    /**
     * Sets the "nodes" element
     */
    public void setNodes(int nodes)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NODES$32, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NODES$32);
            }
            target.setIntValue(nodes);
        }
    }
    
    /**
     * Sets (as xml) the "nodes" element
     */
    public void xsetNodes(org.apache.xmlbeans.XmlInt nodes)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(NODES$32, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_element_user(NODES$32);
            }
            target.set(nodes);
        }
    }
    
    /**
     * Unsets the "nodes" element
     */
    public void unsetNodes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NODES$32, 0);
        }
    }
    
    /**
     * Gets the "processesPerNode" element
     */
    public int getProcessesPerNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROCESSESPERNODE$34, 0);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "processesPerNode" element
     */
    public org.apache.xmlbeans.XmlInt xgetProcessesPerNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(PROCESSESPERNODE$34, 0);
            return target;
        }
    }
    
    /**
     * True if has "processesPerNode" element
     */
    public boolean isSetProcessesPerNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROCESSESPERNODE$34) != 0;
        }
    }
    
    /**
     * Sets the "processesPerNode" element
     */
    public void setProcessesPerNode(int processesPerNode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PROCESSESPERNODE$34, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PROCESSESPERNODE$34);
            }
            target.setIntValue(processesPerNode);
        }
    }
    
    /**
     * Sets (as xml) the "processesPerNode" element
     */
    public void xsetProcessesPerNode(org.apache.xmlbeans.XmlInt processesPerNode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(PROCESSESPERNODE$34, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_element_user(PROCESSESPERNODE$34);
            }
            target.set(processesPerNode);
        }
    }
    
    /**
     * Unsets the "processesPerNode" element
     */
    public void unsetProcessesPerNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROCESSESPERNODE$34, 0);
        }
    }
    
    /**
     * Gets the "cpuCount" element
     */
    public int getCpuCount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CPUCOUNT$36, 0);
            if (target == null)
            {
                return 0;
            }
            return target.getIntValue();
        }
    }
    
    /**
     * Gets (as xml) the "cpuCount" element
     */
    public org.apache.xmlbeans.XmlInt xgetCpuCount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(CPUCOUNT$36, 0);
            return target;
        }
    }
    
    /**
     * True if has "cpuCount" element
     */
    public boolean isSetCpuCount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CPUCOUNT$36) != 0;
        }
    }
    
    /**
     * Sets the "cpuCount" element
     */
    public void setCpuCount(int cpuCount)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CPUCOUNT$36, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CPUCOUNT$36);
            }
            target.setIntValue(cpuCount);
        }
    }
    
    /**
     * Sets (as xml) the "cpuCount" element
     */
    public void xsetCpuCount(org.apache.xmlbeans.XmlInt cpuCount)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlInt target = null;
            target = (org.apache.xmlbeans.XmlInt)get_store().find_element_user(CPUCOUNT$36, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlInt)get_store().add_element_user(CPUCOUNT$36);
            }
            target.set(cpuCount);
        }
    }
    
    /**
     * Unsets the "cpuCount" element
     */
    public void unsetCpuCount()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CPUCOUNT$36, 0);
        }
    }
    
    /**
     * Gets the "nodeList" element
     */
    public java.lang.String getNodeList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NODELIST$38, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "nodeList" element
     */
    public org.apache.xmlbeans.XmlString xgetNodeList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(NODELIST$38, 0);
            return target;
        }
    }
    
    /**
     * True if has "nodeList" element
     */
    public boolean isSetNodeList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(NODELIST$38) != 0;
        }
    }
    
    /**
     * Sets the "nodeList" element
     */
    public void setNodeList(java.lang.String nodeList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(NODELIST$38, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(NODELIST$38);
            }
            target.setStringValue(nodeList);
        }
    }
    
    /**
     * Sets (as xml) the "nodeList" element
     */
    public void xsetNodeList(org.apache.xmlbeans.XmlString nodeList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(NODELIST$38, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(NODELIST$38);
            }
            target.set(nodeList);
        }
    }
    
    /**
     * Unsets the "nodeList" element
     */
    public void unsetNodeList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(NODELIST$38, 0);
        }
    }
    
    /**
     * Gets the "workingDirectory" element
     */
    public java.lang.String getWorkingDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(WORKINGDIRECTORY$40, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "workingDirectory" element
     */
    public org.apache.xmlbeans.XmlString xgetWorkingDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(WORKINGDIRECTORY$40, 0);
            return target;
        }
    }
    
    /**
     * True if has "workingDirectory" element
     */
    public boolean isSetWorkingDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(WORKINGDIRECTORY$40) != 0;
        }
    }
    
    /**
     * Sets the "workingDirectory" element
     */
    public void setWorkingDirectory(java.lang.String workingDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(WORKINGDIRECTORY$40, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(WORKINGDIRECTORY$40);
            }
            target.setStringValue(workingDirectory);
        }
    }
    
    /**
     * Sets (as xml) the "workingDirectory" element
     */
    public void xsetWorkingDirectory(org.apache.xmlbeans.XmlString workingDirectory)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(WORKINGDIRECTORY$40, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(WORKINGDIRECTORY$40);
            }
            target.set(workingDirectory);
        }
    }
    
    /**
     * Unsets the "workingDirectory" element
     */
    public void unsetWorkingDirectory()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(WORKINGDIRECTORY$40, 0);
        }
    }
    
    /**
     * Gets the "executablePath" element
     */
    public java.lang.String getExecutablePath()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EXECUTABLEPATH$42, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "executablePath" element
     */
    public org.apache.xmlbeans.XmlString xgetExecutablePath()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EXECUTABLEPATH$42, 0);
            return target;
        }
    }
    
    /**
     * True if has "executablePath" element
     */
    public boolean isSetExecutablePath()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EXECUTABLEPATH$42) != 0;
        }
    }
    
    /**
     * Sets the "executablePath" element
     */
    public void setExecutablePath(java.lang.String executablePath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EXECUTABLEPATH$42, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EXECUTABLEPATH$42);
            }
            target.setStringValue(executablePath);
        }
    }
    
    /**
     * Sets (as xml) the "executablePath" element
     */
    public void xsetExecutablePath(org.apache.xmlbeans.XmlString executablePath)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EXECUTABLEPATH$42, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EXECUTABLEPATH$42);
            }
            target.set(executablePath);
        }
    }
    
    /**
     * Unsets the "executablePath" element
     */
    public void unsetExecutablePath()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EXECUTABLEPATH$42, 0);
        }
    }
    
    /**
     * Gets the "inputs" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.InputList getInputs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.InputList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.InputList)get_store().find_element_user(INPUTS$44, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "inputs" element
     */
    public void setInputs(org.apache.airavata.gfac.core.x2012.x12.InputList inputs)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.InputList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.InputList)get_store().find_element_user(INPUTS$44, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.InputList)get_store().add_element_user(INPUTS$44);
            }
            target.set(inputs);
        }
    }
    
    /**
     * Appends and returns a new empty "inputs" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.InputList addNewInputs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.InputList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.InputList)get_store().add_element_user(INPUTS$44);
            return target;
        }
    }
    
    /**
     * Gets the "exports" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties getExports()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties)get_store().find_element_user(EXPORTS$46, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "exports" element
     */
    public void setExports(org.apache.airavata.gfac.core.x2012.x12.ExportProperties exports)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties)get_store().find_element_user(EXPORTS$46, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties)get_store().add_element_user(EXPORTS$46);
            }
            target.set(exports);
        }
    }
    
    /**
     * Appends and returns a new empty "exports" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ExportProperties addNewExports()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ExportProperties target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ExportProperties)get_store().add_element_user(EXPORTS$46);
            return target;
        }
    }
    
    /**
     * Gets the "status" element
     */
    public java.lang.String getStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUS$48, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "status" element
     */
    public org.apache.xmlbeans.XmlString xgetStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUS$48, 0);
            return target;
        }
    }
    
    /**
     * True if has "status" element
     */
    public boolean isSetStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUS$48) != 0;
        }
    }
    
    /**
     * Sets the "status" element
     */
    public void setStatus(java.lang.String status)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUS$48, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUS$48);
            }
            target.setStringValue(status);
        }
    }
    
    /**
     * Sets (as xml) the "status" element
     */
    public void xsetStatus(org.apache.xmlbeans.XmlString status)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUS$48, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STATUS$48);
            }
            target.set(status);
        }
    }
    
    /**
     * Unsets the "status" element
     */
    public void unsetStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUS$48, 0);
        }
    }
    
    /**
     * Gets the "afterAny" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.AfterAnyList getAfterAny()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterAnyList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterAnyList)get_store().find_element_user(AFTERANY$50, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "afterAny" element
     */
    public boolean isSetAfterAny()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AFTERANY$50) != 0;
        }
    }
    
    /**
     * Sets the "afterAny" element
     */
    public void setAfterAny(org.apache.airavata.gfac.core.x2012.x12.AfterAnyList afterAny)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterAnyList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterAnyList)get_store().find_element_user(AFTERANY$50, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.AfterAnyList)get_store().add_element_user(AFTERANY$50);
            }
            target.set(afterAny);
        }
    }
    
    /**
     * Appends and returns a new empty "afterAny" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.AfterAnyList addNewAfterAny()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterAnyList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterAnyList)get_store().add_element_user(AFTERANY$50);
            return target;
        }
    }
    
    /**
     * Unsets the "afterAny" element
     */
    public void unsetAfterAny()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AFTERANY$50, 0);
        }
    }
    
    /**
     * Gets the "afterOKList" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.AfterOKList getAfterOKList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterOKList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterOKList)get_store().find_element_user(AFTEROKLIST$52, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "afterOKList" element
     */
    public boolean isSetAfterOKList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AFTEROKLIST$52) != 0;
        }
    }
    
    /**
     * Sets the "afterOKList" element
     */
    public void setAfterOKList(org.apache.airavata.gfac.core.x2012.x12.AfterOKList afterOKList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterOKList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterOKList)get_store().find_element_user(AFTEROKLIST$52, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.AfterOKList)get_store().add_element_user(AFTEROKLIST$52);
            }
            target.set(afterOKList);
        }
    }
    
    /**
     * Appends and returns a new empty "afterOKList" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.AfterOKList addNewAfterOKList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.AfterOKList target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.AfterOKList)get_store().add_element_user(AFTEROKLIST$52);
            return target;
        }
    }
    
    /**
     * Unsets the "afterOKList" element
     */
    public void unsetAfterOKList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AFTEROKLIST$52, 0);
        }
    }
    
    /**
     * Gets the "cTime" element
     */
    public java.lang.String getCTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CTIME$54, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "cTime" element
     */
    public org.apache.xmlbeans.XmlString xgetCTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CTIME$54, 0);
            return target;
        }
    }
    
    /**
     * True if has "cTime" element
     */
    public boolean isSetCTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CTIME$54) != 0;
        }
    }
    
    /**
     * Sets the "cTime" element
     */
    public void setCTime(java.lang.String cTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CTIME$54, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CTIME$54);
            }
            target.setStringValue(cTime);
        }
    }
    
    /**
     * Sets (as xml) the "cTime" element
     */
    public void xsetCTime(org.apache.xmlbeans.XmlString cTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CTIME$54, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CTIME$54);
            }
            target.set(cTime);
        }
    }
    
    /**
     * Unsets the "cTime" element
     */
    public void unsetCTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CTIME$54, 0);
        }
    }
    
    /**
     * Gets the "qTime" element
     */
    public java.lang.String getQTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(QTIME$56, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "qTime" element
     */
    public org.apache.xmlbeans.XmlString xgetQTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(QTIME$56, 0);
            return target;
        }
    }
    
    /**
     * True if has "qTime" element
     */
    public boolean isSetQTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(QTIME$56) != 0;
        }
    }
    
    /**
     * Sets the "qTime" element
     */
    public void setQTime(java.lang.String qTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(QTIME$56, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(QTIME$56);
            }
            target.setStringValue(qTime);
        }
    }
    
    /**
     * Sets (as xml) the "qTime" element
     */
    public void xsetQTime(org.apache.xmlbeans.XmlString qTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(QTIME$56, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(QTIME$56);
            }
            target.set(qTime);
        }
    }
    
    /**
     * Unsets the "qTime" element
     */
    public void unsetQTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(QTIME$56, 0);
        }
    }
    
    /**
     * Gets the "mTime" element
     */
    public java.lang.String getMTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MTIME$58, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "mTime" element
     */
    public org.apache.xmlbeans.XmlString xgetMTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MTIME$58, 0);
            return target;
        }
    }
    
    /**
     * True if has "mTime" element
     */
    public boolean isSetMTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MTIME$58) != 0;
        }
    }
    
    /**
     * Sets the "mTime" element
     */
    public void setMTime(java.lang.String mTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MTIME$58, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MTIME$58);
            }
            target.setStringValue(mTime);
        }
    }
    
    /**
     * Sets (as xml) the "mTime" element
     */
    public void xsetMTime(org.apache.xmlbeans.XmlString mTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MTIME$58, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MTIME$58);
            }
            target.set(mTime);
        }
    }
    
    /**
     * Unsets the "mTime" element
     */
    public void unsetMTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MTIME$58, 0);
        }
    }
    
    /**
     * Gets the "sTime" element
     */
    public java.lang.String getSTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STIME$60, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "sTime" element
     */
    public org.apache.xmlbeans.XmlString xgetSTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STIME$60, 0);
            return target;
        }
    }
    
    /**
     * True if has "sTime" element
     */
    public boolean isSetSTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STIME$60) != 0;
        }
    }
    
    /**
     * Sets the "sTime" element
     */
    public void setSTime(java.lang.String sTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STIME$60, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STIME$60);
            }
            target.setStringValue(sTime);
        }
    }
    
    /**
     * Sets (as xml) the "sTime" element
     */
    public void xsetSTime(org.apache.xmlbeans.XmlString sTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STIME$60, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STIME$60);
            }
            target.set(sTime);
        }
    }
    
    /**
     * Unsets the "sTime" element
     */
    public void unsetSTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STIME$60, 0);
        }
    }
    
    /**
     * Gets the "compTime" element
     */
    public java.lang.String getCompTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMPTIME$62, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "compTime" element
     */
    public org.apache.xmlbeans.XmlString xgetCompTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMPTIME$62, 0);
            return target;
        }
    }
    
    /**
     * True if has "compTime" element
     */
    public boolean isSetCompTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COMPTIME$62) != 0;
        }
    }
    
    /**
     * Sets the "compTime" element
     */
    public void setCompTime(java.lang.String compTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COMPTIME$62, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COMPTIME$62);
            }
            target.setStringValue(compTime);
        }
    }
    
    /**
     * Sets (as xml) the "compTime" element
     */
    public void xsetCompTime(org.apache.xmlbeans.XmlString compTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COMPTIME$62, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COMPTIME$62);
            }
            target.set(compTime);
        }
    }
    
    /**
     * Unsets the "compTime" element
     */
    public void unsetCompTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COMPTIME$62, 0);
        }
    }
    
    /**
     * Gets the "owner" element
     */
    public java.lang.String getOwner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OWNER$64, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "owner" element
     */
    public org.apache.xmlbeans.XmlString xgetOwner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OWNER$64, 0);
            return target;
        }
    }
    
    /**
     * True if has "owner" element
     */
    public boolean isSetOwner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OWNER$64) != 0;
        }
    }
    
    /**
     * Sets the "owner" element
     */
    public void setOwner(java.lang.String owner)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OWNER$64, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OWNER$64);
            }
            target.setStringValue(owner);
        }
    }
    
    /**
     * Sets (as xml) the "owner" element
     */
    public void xsetOwner(org.apache.xmlbeans.XmlString owner)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OWNER$64, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OWNER$64);
            }
            target.set(owner);
        }
    }
    
    /**
     * Unsets the "owner" element
     */
    public void unsetOwner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OWNER$64, 0);
        }
    }
    
    /**
     * Gets the "executeNode" element
     */
    public java.lang.String getExecuteNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EXECUTENODE$66, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "executeNode" element
     */
    public org.apache.xmlbeans.XmlString xgetExecuteNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EXECUTENODE$66, 0);
            return target;
        }
    }
    
    /**
     * True if has "executeNode" element
     */
    public boolean isSetExecuteNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EXECUTENODE$66) != 0;
        }
    }
    
    /**
     * Sets the "executeNode" element
     */
    public void setExecuteNode(java.lang.String executeNode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EXECUTENODE$66, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EXECUTENODE$66);
            }
            target.setStringValue(executeNode);
        }
    }
    
    /**
     * Sets (as xml) the "executeNode" element
     */
    public void xsetExecuteNode(org.apache.xmlbeans.XmlString executeNode)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EXECUTENODE$66, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EXECUTENODE$66);
            }
            target.set(executeNode);
        }
    }
    
    /**
     * Unsets the "executeNode" element
     */
    public void unsetExecuteNode()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EXECUTENODE$66, 0);
        }
    }
    
    /**
     * Gets the "ellapsedTime" element
     */
    public java.lang.String getEllapsedTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ELLAPSEDTIME$68, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "ellapsedTime" element
     */
    public org.apache.xmlbeans.XmlString xgetEllapsedTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ELLAPSEDTIME$68, 0);
            return target;
        }
    }
    
    /**
     * True if has "ellapsedTime" element
     */
    public boolean isSetEllapsedTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ELLAPSEDTIME$68) != 0;
        }
    }
    
    /**
     * Sets the "ellapsedTime" element
     */
    public void setEllapsedTime(java.lang.String ellapsedTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ELLAPSEDTIME$68, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ELLAPSEDTIME$68);
            }
            target.setStringValue(ellapsedTime);
        }
    }
    
    /**
     * Sets (as xml) the "ellapsedTime" element
     */
    public void xsetEllapsedTime(org.apache.xmlbeans.XmlString ellapsedTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ELLAPSEDTIME$68, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ELLAPSEDTIME$68);
            }
            target.set(ellapsedTime);
        }
    }
    
    /**
     * Unsets the "ellapsedTime" element
     */
    public void unsetEllapsedTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ELLAPSEDTIME$68, 0);
        }
    }
    
    /**
     * Gets the "usedCPUTime" element
     */
    public java.lang.String getUsedCPUTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USEDCPUTIME$70, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "usedCPUTime" element
     */
    public org.apache.xmlbeans.XmlString xgetUsedCPUTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USEDCPUTIME$70, 0);
            return target;
        }
    }
    
    /**
     * True if has "usedCPUTime" element
     */
    public boolean isSetUsedCPUTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(USEDCPUTIME$70) != 0;
        }
    }
    
    /**
     * Sets the "usedCPUTime" element
     */
    public void setUsedCPUTime(java.lang.String usedCPUTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USEDCPUTIME$70, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USEDCPUTIME$70);
            }
            target.setStringValue(usedCPUTime);
        }
    }
    
    /**
     * Sets (as xml) the "usedCPUTime" element
     */
    public void xsetUsedCPUTime(org.apache.xmlbeans.XmlString usedCPUTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USEDCPUTIME$70, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(USEDCPUTIME$70);
            }
            target.set(usedCPUTime);
        }
    }
    
    /**
     * Unsets the "usedCPUTime" element
     */
    public void unsetUsedCPUTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(USEDCPUTIME$70, 0);
        }
    }
    
    /**
     * Gets the "usedMem" element
     */
    public java.lang.String getUsedMem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USEDMEM$72, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "usedMem" element
     */
    public org.apache.xmlbeans.XmlString xgetUsedMem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USEDMEM$72, 0);
            return target;
        }
    }
    
    /**
     * True if has "usedMem" element
     */
    public boolean isSetUsedMem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(USEDMEM$72) != 0;
        }
    }
    
    /**
     * Sets the "usedMem" element
     */
    public void setUsedMem(java.lang.String usedMem)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(USEDMEM$72, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(USEDMEM$72);
            }
            target.setStringValue(usedMem);
        }
    }
    
    /**
     * Sets (as xml) the "usedMem" element
     */
    public void xsetUsedMem(org.apache.xmlbeans.XmlString usedMem)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(USEDMEM$72, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(USEDMEM$72);
            }
            target.set(usedMem);
        }
    }
    
    /**
     * Unsets the "usedMem" element
     */
    public void unsetUsedMem()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(USEDMEM$72, 0);
        }
    }
    
    /**
     * Gets the "submitArgs" element
     */
    public java.lang.String getSubmitArgs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SUBMITARGS$74, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "submitArgs" element
     */
    public org.apache.xmlbeans.XmlString xgetSubmitArgs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SUBMITARGS$74, 0);
            return target;
        }
    }
    
    /**
     * True if has "submitArgs" element
     */
    public boolean isSetSubmitArgs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SUBMITARGS$74) != 0;
        }
    }
    
    /**
     * Sets the "submitArgs" element
     */
    public void setSubmitArgs(java.lang.String submitArgs)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SUBMITARGS$74, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SUBMITARGS$74);
            }
            target.setStringValue(submitArgs);
        }
    }
    
    /**
     * Sets (as xml) the "submitArgs" element
     */
    public void xsetSubmitArgs(org.apache.xmlbeans.XmlString submitArgs)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SUBMITARGS$74, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SUBMITARGS$74);
            }
            target.set(submitArgs);
        }
    }
    
    /**
     * Unsets the "submitArgs" element
     */
    public void unsetSubmitArgs()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SUBMITARGS$74, 0);
        }
    }
    
    /**
     * Gets the "variableList" element
     */
    public java.lang.String getVariableList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(VARIABLELIST$76, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "variableList" element
     */
    public org.apache.xmlbeans.XmlString xgetVariableList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(VARIABLELIST$76, 0);
            return target;
        }
    }
    
    /**
     * True if has "variableList" element
     */
    public boolean isSetVariableList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(VARIABLELIST$76) != 0;
        }
    }
    
    /**
     * Sets the "variableList" element
     */
    public void setVariableList(java.lang.String variableList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(VARIABLELIST$76, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(VARIABLELIST$76);
            }
            target.setStringValue(variableList);
        }
    }
    
    /**
     * Sets (as xml) the "variableList" element
     */
    public void xsetVariableList(org.apache.xmlbeans.XmlString variableList)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(VARIABLELIST$76, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(VARIABLELIST$76);
            }
            target.set(variableList);
        }
    }
    
    /**
     * Unsets the "variableList" element
     */
    public void unsetVariableList()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(VARIABLELIST$76, 0);
        }
    }
    
    /**
     * Gets the "preJobCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PreJobCommands getPreJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PreJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PreJobCommands)get_store().find_element_user(PREJOBCOMMANDS$78, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "preJobCommands" element
     */
    public boolean isSetPreJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PREJOBCOMMANDS$78) != 0;
        }
    }
    
    /**
     * Sets the "preJobCommands" element
     */
    public void setPreJobCommands(org.apache.airavata.gfac.core.x2012.x12.PreJobCommands preJobCommands)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PreJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PreJobCommands)get_store().find_element_user(PREJOBCOMMANDS$78, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.PreJobCommands)get_store().add_element_user(PREJOBCOMMANDS$78);
            }
            target.set(preJobCommands);
        }
    }
    
    /**
     * Appends and returns a new empty "preJobCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PreJobCommands addNewPreJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PreJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PreJobCommands)get_store().add_element_user(PREJOBCOMMANDS$78);
            return target;
        }
    }
    
    /**
     * Unsets the "preJobCommands" element
     */
    public void unsetPreJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PREJOBCOMMANDS$78, 0);
        }
    }
    
    /**
     * Gets the "moduleLoadCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands getModuleLoadCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands)get_store().find_element_user(MODULELOADCOMMANDS$80, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "moduleLoadCommands" element
     */
    public boolean isSetModuleLoadCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MODULELOADCOMMANDS$80) != 0;
        }
    }
    
    /**
     * Sets the "moduleLoadCommands" element
     */
    public void setModuleLoadCommands(org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands moduleLoadCommands)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands)get_store().find_element_user(MODULELOADCOMMANDS$80, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands)get_store().add_element_user(MODULELOADCOMMANDS$80);
            }
            target.set(moduleLoadCommands);
        }
    }
    
    /**
     * Appends and returns a new empty "moduleLoadCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands addNewModuleLoadCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.ModuleLoadCommands)get_store().add_element_user(MODULELOADCOMMANDS$80);
            return target;
        }
    }
    
    /**
     * Unsets the "moduleLoadCommands" element
     */
    public void unsetModuleLoadCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MODULELOADCOMMANDS$80, 0);
        }
    }
    
    /**
     * Gets the "postJobCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PostJobCommands getPostJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PostJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PostJobCommands)get_store().find_element_user(POSTJOBCOMMANDS$82, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "postJobCommands" element
     */
    public boolean isSetPostJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(POSTJOBCOMMANDS$82) != 0;
        }
    }
    
    /**
     * Sets the "postJobCommands" element
     */
    public void setPostJobCommands(org.apache.airavata.gfac.core.x2012.x12.PostJobCommands postJobCommands)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PostJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PostJobCommands)get_store().find_element_user(POSTJOBCOMMANDS$82, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.PostJobCommands)get_store().add_element_user(POSTJOBCOMMANDS$82);
            }
            target.set(postJobCommands);
        }
    }
    
    /**
     * Appends and returns a new empty "postJobCommands" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PostJobCommands addNewPostJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PostJobCommands target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PostJobCommands)get_store().add_element_user(POSTJOBCOMMANDS$82);
            return target;
        }
    }
    
    /**
     * Unsets the "postJobCommands" element
     */
    public void unsetPostJobCommands()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(POSTJOBCOMMANDS$82, 0);
        }
    }
    
    /**
     * Gets the "jobSubmitterCommand" element
     */
    public java.lang.String getJobSubmitterCommand()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBSUBMITTERCOMMAND$84, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "jobSubmitterCommand" element
     */
    public org.apache.xmlbeans.XmlString xgetJobSubmitterCommand()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBSUBMITTERCOMMAND$84, 0);
            return target;
        }
    }
    
    /**
     * True if has "jobSubmitterCommand" element
     */
    public boolean isSetJobSubmitterCommand()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(JOBSUBMITTERCOMMAND$84) != 0;
        }
    }
    
    /**
     * Sets the "jobSubmitterCommand" element
     */
    public void setJobSubmitterCommand(java.lang.String jobSubmitterCommand)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(JOBSUBMITTERCOMMAND$84, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(JOBSUBMITTERCOMMAND$84);
            }
            target.setStringValue(jobSubmitterCommand);
        }
    }
    
    /**
     * Sets (as xml) the "jobSubmitterCommand" element
     */
    public void xsetJobSubmitterCommand(org.apache.xmlbeans.XmlString jobSubmitterCommand)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(JOBSUBMITTERCOMMAND$84, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(JOBSUBMITTERCOMMAND$84);
            }
            target.set(jobSubmitterCommand);
        }
    }
    
    /**
     * Unsets the "jobSubmitterCommand" element
     */
    public void unsetJobSubmitterCommand()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(JOBSUBMITTERCOMMAND$84, 0);
        }
    }
    
    /**
     * Gets the "callBackIp" element
     */
    public java.lang.String getCallBackIp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CALLBACKIP$86, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "callBackIp" element
     */
    public org.apache.xmlbeans.XmlString xgetCallBackIp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CALLBACKIP$86, 0);
            return target;
        }
    }
    
    /**
     * True if has "callBackIp" element
     */
    public boolean isSetCallBackIp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CALLBACKIP$86) != 0;
        }
    }
    
    /**
     * Sets the "callBackIp" element
     */
    public void setCallBackIp(java.lang.String callBackIp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CALLBACKIP$86, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CALLBACKIP$86);
            }
            target.setStringValue(callBackIp);
        }
    }
    
    /**
     * Sets (as xml) the "callBackIp" element
     */
    public void xsetCallBackIp(org.apache.xmlbeans.XmlString callBackIp)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CALLBACKIP$86, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CALLBACKIP$86);
            }
            target.set(callBackIp);
        }
    }
    
    /**
     * Unsets the "callBackIp" element
     */
    public void unsetCallBackIp()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CALLBACKIP$86, 0);
        }
    }
    
    /**
     * Gets the "callBackPort" element
     */
    public java.lang.String getCallBackPort()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CALLBACKPORT$88, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "callBackPort" element
     */
    public org.apache.xmlbeans.XmlString xgetCallBackPort()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CALLBACKPORT$88, 0);
            return target;
        }
    }
    
    /**
     * True if has "callBackPort" element
     */
    public boolean isSetCallBackPort()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CALLBACKPORT$88) != 0;
        }
    }
    
    /**
     * Sets the "callBackPort" element
     */
    public void setCallBackPort(java.lang.String callBackPort)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CALLBACKPORT$88, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CALLBACKPORT$88);
            }
            target.setStringValue(callBackPort);
        }
    }
    
    /**
     * Sets (as xml) the "callBackPort" element
     */
    public void xsetCallBackPort(org.apache.xmlbeans.XmlString callBackPort)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CALLBACKPORT$88, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CALLBACKPORT$88);
            }
            target.set(callBackPort);
        }
    }
    
    /**
     * Unsets the "callBackPort" element
     */
    public void unsetCallBackPort()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CALLBACKPORT$88, 0);
        }
    }
    
    /**
     * Gets the "chassisName" element
     */
    public java.lang.String getChassisName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CHASSISNAME$90, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "chassisName" element
     */
    public org.apache.xmlbeans.XmlString xgetChassisName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CHASSISNAME$90, 0);
            return target;
        }
    }
    
    /**
     * True if has "chassisName" element
     */
    public boolean isSetChassisName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CHASSISNAME$90) != 0;
        }
    }
    
    /**
     * Sets the "chassisName" element
     */
    public void setChassisName(java.lang.String chassisName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CHASSISNAME$90, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CHASSISNAME$90);
            }
            target.setStringValue(chassisName);
        }
    }
    
    /**
     * Sets (as xml) the "chassisName" element
     */
    public void xsetChassisName(org.apache.xmlbeans.XmlString chassisName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CHASSISNAME$90, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CHASSISNAME$90);
            }
            target.set(chassisName);
        }
    }
    
    /**
     * Unsets the "chassisName" element
     */
    public void unsetChassisName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CHASSISNAME$90, 0);
        }
    }
}
