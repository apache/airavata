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

package org.apache.airavata.core.gfac.notification;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;

public interface NotificationService {
    /*
     * 
     */
    void startSchedule(Object notifer, InvocationContext context, Scheduler scheduler);
    void finishSchedule(Object notifer, InvocationContext context, Scheduler scheduler, Provider provider);
    
    /*
     * 
     */
    void input(Object notifier, InvocationContext context, String... data);
    void output(Object notifier, InvocationContext context, String... data);
    
    /*
     * 
     */
    void startExecution(Object notifer, InvocationContext context);
    void applicationInfo(Object notifier, InvocationContext context, String... data);
    void finishExecution(Object notifer, InvocationContext context);
    void statusChanged(Object notifer, InvocationContext context, String... data);
    void executionFail(Object notifer, InvocationContext context, Exception e, String... data);    

    /*
     * Interface for developer to use
     */
    void debug(Object notifer, InvocationContext context, String... data);
    void info(Object notifer, InvocationContext context, String... data);
    void warning(Object notifer, InvocationContext context, String... data);
    void exception(Object notifer, InvocationContext context, String... data);        
}
