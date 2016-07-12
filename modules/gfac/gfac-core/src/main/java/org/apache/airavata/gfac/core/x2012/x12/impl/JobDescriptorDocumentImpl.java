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
 * An XML document type.
 * Localname: JobDescriptor
 * Namespace: http://airavata.apache.org/gfac/core/2012/12
 * Java type: org.apache.airavata.gfac.core.x2012.x12.JobDescriptorDocument
 *
 * Automatically generated - do not modify.
 */
package org.apache.airavata.gfac.core.x2012.x12.impl;
/**
 * A document containing one JobDescriptor(@http://airavata.apache.org/gfac/core/2012/12) element.
 *
 * This is a complex type.
 */
public class JobDescriptorDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.apache.airavata.gfac.core.x2012.x12.JobDescriptorDocument
{
    private static final long serialVersionUID = 1L;
    
    public JobDescriptorDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName JOBDESCRIPTOR$0 = 
        new javax.xml.namespace.QName("http://airavata.apache.org/gfac/core/2012/12", "JobDescriptor");
    
    
    /**
     * Gets the "JobDescriptor" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PbsParams getJobDescriptor()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PbsParams target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PbsParams)get_store().find_element_user(JOBDESCRIPTOR$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "JobDescriptor" element
     */
    public void setJobDescriptor(org.apache.airavata.gfac.core.x2012.x12.PbsParams jobDescriptor)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PbsParams target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PbsParams)get_store().find_element_user(JOBDESCRIPTOR$0, 0);
            if (target == null)
            {
                target = (org.apache.airavata.gfac.core.x2012.x12.PbsParams)get_store().add_element_user(JOBDESCRIPTOR$0);
            }
            target.set(jobDescriptor);
        }
    }
    
    /**
     * Appends and returns a new empty "JobDescriptor" element
     */
    public org.apache.airavata.gfac.core.x2012.x12.PbsParams addNewJobDescriptor()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.airavata.gfac.core.x2012.x12.PbsParams target = null;
            target = (org.apache.airavata.gfac.core.x2012.x12.PbsParams)get_store().add_element_user(JOBDESCRIPTOR$0);
            return target;
        }
    }
}
