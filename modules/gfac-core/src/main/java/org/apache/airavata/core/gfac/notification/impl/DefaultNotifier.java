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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.notification.Notifiable;
import org.apache.airavata.core.gfac.notification.Notifier;


public class DefaultNotifier implements Notifier {
    
    private List<Notifiable> notifiableObjects = new ArrayList<Notifiable>();
    
    public void addNotifiable(Notifiable notif){
        notifiableObjects.add(notif);
    }
    
    public Notifiable[] getNotifiable(){
        return (Notifiable[]) notifiableObjects.toArray();
    }

    public void startSchedule(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            notif.startSchedule(notifier, context);
        }        
    }

    public void finishSchedule(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            notif.finishSchedule(notifier, context);
        }        
    }

    public void input(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.info(notifier, context, data);
        }        
    }

    public void output(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.output(notifier, context, data);
        }
    }

    public void startExecution(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            notif.startExecution(notifier, context);
        }
    }

    public void applicationInfo(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.applicationInfo(notifier, context, data);
        }
    }

    public void finishExecution(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            notif.finishExecution(notifier, context);
        }        
    }

    public void statusChanged(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.statusChanged(notifier, context, data);
        }        
    }

    public void executionFail(Object notifier, InvocationContext context, Exception e, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.executionFail(notifier, context, e, data);
        }
    }

    public void debug(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.debug(notifier, context, data);
        }
    }

    public void info(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.info(notifier, context, data);
        }
        
    }

    public void warning(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.warning(notifier, context, data);
        }
        
    }

    public void exception(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            notif.exception(notifier, context, data);
        }        
    }        
}
