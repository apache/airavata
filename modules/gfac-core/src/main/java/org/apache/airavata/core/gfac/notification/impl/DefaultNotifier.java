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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default notifier which uses {@link ArrayList} to store {@link Notifiable}
 * objects. Notification method is done by a single thread. It ignore all errors
 * from {@link Notifiable} object.
 */
public class DefaultNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotifier.class);

    private List<Notifiable> notifiableObjects = new ArrayList<Notifiable>();

    public void addNotifiable(Notifiable notif) {
        notifiableObjects.add(notif);
    }

    public Notifiable[] getNotifiable() {
        return (Notifiable[]) notifiableObjects.toArray();
    }

    public void startSchedule(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.startSchedule(notifier, context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void finishSchedule(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.finishSchedule(notifier, context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void input(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.info(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void output(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.output(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void startExecution(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.startExecution(notifier, context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void applicationInfo(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.applicationInfo(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void finishExecution(Object notifier, InvocationContext context) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.finishExecution(notifier, context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void statusChanged(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.statusChanged(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void executionFail(Object notifier, InvocationContext context, Exception e, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.executionFail(notifier, context, e, data);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void debug(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.debug(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void info(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.info(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void warning(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.warning(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void exception(Object notifier, InvocationContext context, String... data) {
        for (Notifiable notif : notifiableObjects) {
            try {
                notif.exception(notifier, context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
