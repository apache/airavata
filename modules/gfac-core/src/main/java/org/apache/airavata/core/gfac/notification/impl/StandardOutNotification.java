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
import org.apache.airavata.core.gfac.notification.GFacNotifiable;

/**
 * Print out all notification to System.out
 */
public class StandardOutNotification implements GFacNotifiable {

    public void startSchedule(InvocationContext context) {
        printOut(context);
    }

    public void finishSchedule(InvocationContext context) {
        printOut(context);
    }

    public void input(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void output(InvocationContext context, String... data) {
    	 printOut(context, data);
    }

    public void startExecution(InvocationContext context) {
        printOut(context);
    }

    public void applicationInfo(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void finishExecution(InvocationContext context) {
        printOut(context);
    }

    public void statusChanged(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void executionFail(InvocationContext context, Exception e, String... data) {
        printOut(context, data);
    }

    public void debug(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void info(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void warning(InvocationContext context, String... data) {
        printOut(context, data);
    }

    public void exception(InvocationContext context, String... data) {
        printOut(context, data);
    }

    private void printOut(InvocationContext context, String... data) {
        System.out.println("Notifier: " + this.getClass().toString());
        if (data != null) {
            System.out.println("-----DATA-----");
            for (int i = 0; i < data.length; i++) {
                System.out.println(data[i]);
            }
            System.out.println("-----END DATA-----");
        }
    }

}
