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
import org.apache.airavata.core.gfac.notification.GFacNotifiable;
import org.apache.airavata.core.gfac.notification.GFacNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default notifier which uses {@link ArrayList} to store
 * {@link org.apache.airavata.core.gfac.notification.GFacNotifiable} objects. Notification method is done by a single
 * thread. It ignore all errors from {@link org.apache.airavata.core.gfac.notification.GFacNotifiable} object.
 */
public class DefaultNotifier implements GFacNotifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotifier.class);

    private List<GFacNotifiable> notifiableObjects = new ArrayList<GFacNotifiable>();

    public void addNotifiable(GFacNotifiable notif) {
        notifiableObjects.add(notif);
    }

    public GFacNotifiable[] getNotifiable() {
        return notifiableObjects.toArray(new GFacNotifiable[] {});
    }

    public void startSchedule(InvocationContext context) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.startSchedule(context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void finishSchedule(InvocationContext context) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.finishSchedule(context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void input(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.info(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void output(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.output(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void startExecution(InvocationContext context) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.startExecution(context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void applicationInfo(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.applicationInfo(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void finishExecution(InvocationContext context) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.finishExecution(context);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void statusChanged(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.statusChanged(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void executionFail(InvocationContext context, Exception e, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.executionFail(context, e, data);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    public void debug(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.debug(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void info(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.info(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void warning(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.warning(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void exception(InvocationContext context, String... data) {
        for (GFacNotifiable notif : notifiableObjects) {
            try {
                notif.exception(context, data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
