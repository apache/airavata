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
 * This class represents a topic or subject that can be notified from
 * {@link GFacNotifier} to {@link GFacNotifiable}.
 * 
 * TODO: Think about a better way
 */
public interface Subject {
    /*
     * 
     */
    void startSchedule(Object notifier, InvocationContext context);

    void finishSchedule(Object notifier, InvocationContext context);

    /*
     * 
     */
    void input(Object notifier, InvocationContext context, String... data);

    void output(Object notifier, InvocationContext context, String... data);

    /*
     * 
     */
    void startExecution(Object notifier, InvocationContext context);

    void applicationInfo(Object notifier, InvocationContext context, String... data);

    void finishExecution(Object notifier, InvocationContext context);

    void statusChanged(Object notifier, InvocationContext context, String... data);

    void executionFail(Object notifier, InvocationContext context, Exception e, String... data);

    /*
     * Interface for developer to use
     */
    void debug(Object notifier, InvocationContext context, String... data);

    void info(Object notifier, InvocationContext context, String... data);

    void warning(Object notifier, InvocationContext context, String... data);

    void exception(Object notifier, InvocationContext context, String... data);
}
