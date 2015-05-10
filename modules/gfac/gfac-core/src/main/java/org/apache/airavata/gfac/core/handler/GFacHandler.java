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

package org.apache.airavata.gfac.core.handler;

import org.apache.airavata.gfac.core.context.JobExecutionContext;

import java.util.Map;
import java.util.Properties;

/**
 * This is the handler interface in gfac, most of the tasks
 * which can be decoupled from each other can be implemented
 * as a Handler. After implementing these can be registered in
 * gfac-config.xml and those will get invoked by the framework.
 */
public interface GFacHandler {
    /**
     * This can be used to initialize the Handler, This will get
     * invoked after creating the handler instance using the default constructor
     *
     * @param properties
     * @throws GFacHandlerException
     */
    public void initProperties(Properties properties) throws GFacHandlerException;

    /**
     * This is the method which will invoked by the frameworkd to exectute the
     * Handler functionality.
     * @param jobExecutionContext
     * @throws GFacHandlerException
     */
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException;


    /**
     * This method can be used to implement recovering part of the stateful handler
     * If you do not want to recover an already ran handler you leave this recover method empty.
     * @param jobExecutionContext
     */
    public void recover(JobExecutionContext jobExecutionContext)throws GFacHandlerException;
}
