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

package org.apache.airavata.core.gfac.notification.impl;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;

public class DummyNotification implements NotificationService {

    public void startSchedule(Object notifier, InvocationContext context, Scheduler scheduler) {
    }

    public void finishSchedule(Object notifier, InvocationContext context, Scheduler scheduler, Provider provider) {
    }

    public void input(Object notifier, InvocationContext context, String... data) {
    }

    public void output(Object notifier, InvocationContext context, String... data) {
    }

    public void startExecution(Object notifier, InvocationContext context) {
    }

    public void applicationInfo(Object notifier, InvocationContext context, String... data) {
    }

    public void finishExecution(Object notifier, InvocationContext context) {
    }

    public void statusChanged(Object notifier, InvocationContext context, String... data) {
    }

    public void executionFail(Object notifier, InvocationContext context, Exception e, String... data) {
    }

    public void debug(Object notifier, InvocationContext context, String... data) {
    }

    public void info(Object notifier, InvocationContext context, String... data) {
    }

    public void warning(Object notifier, InvocationContext context, String... data) {
    }

    public void exception(Object notifier, InvocationContext context, String... data) {
    }

}
