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

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;

/**
 * This class represents a topic or subject that can be notified from {@link GFacNotifier} to {@link GFacNotifiable}.
 * 
 * TODO: Think about a better way
 */
public interface Subject {
    /*
     * 
     */
    void startSchedule(InvocationContext context);

    void finishSchedule(InvocationContext context);

    /*
     * 
     */
    void input(InvocationContext context, String... data);

    void output(InvocationContext context, String... data);

    /*
     * 
     */
    void startExecution(InvocationContext context);

    void applicationInfo(InvocationContext context, String... data);

    void finishExecution(InvocationContext context);

    void statusChanged(InvocationContext context, String... data);

    void executionFail(InvocationContext context, Exception e, String... data);

    /*
     * Interface for developer to use
     */
    void debug(InvocationContext context, String... data);

    void info(InvocationContext context, String... data);

    void warning(InvocationContext context, String... data);

    void exception(InvocationContext context, String... data);
}
