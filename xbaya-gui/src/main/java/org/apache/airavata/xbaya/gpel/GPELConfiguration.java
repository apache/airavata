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

package org.apache.airavata.xbaya.gpel;

import java.net.URI;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.event.Event.Type;
import org.apache.airavata.xbaya.event.EventProducer;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;

/**
 * The GPEL Configuration class holds the configuration to access to a GPEL Engine.
 */
@Deprecated
public class GPELConfiguration extends EventProducer {

    private final static Event event = new Event(Type.GPEL_CONFIGURATION_CHANGED);

    private URI uri;

    private String user;

    private String password;

    /**
     * Constructs a GPELConfiguration.
     */
    public GPELConfiguration() {
        // Nothing
    }

    /**
     * @param uri
     *            The URI of a GPEL Engine.
     * @param user
     *            The username to access to the engine.
     * @param password
     *            The password to access to the engine.
     */
    public GPELConfiguration(URI uri, String user, String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    /**
     * @param uri
     * @param user
     * @param password
     * @throws org.apache.airavata.xbaya.workflow.WorkflowEngineException
     *             When the connection to the GPEL engine fails.
     * @throws XBayaException
     *             Should not happen.
     */
    public void set(URI uri, String user, String password) throws WorkflowEngineException, XBayaException {
        this.uri = uri;
        this.user = user;
        this.password = password;
        sendEvent(event);
    }

    /**
     * @return The URL of the GPEL Engine.
     */
    public URI getURL() {
        return this.uri;
    }

    /**
     * @return the username.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @return The password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Checks if the configuration is complete to use.
     * 
     * @return true if the configuration is complete; false otherwise.
     */
    public boolean isValid() {
        if (this.uri == null) {
            return false;
        }
        if (this.user == null || this.user.equals("")) {
            return false;
        }
        if (this.password == null || this.password.equals("")) {
            return false;
        }
        return true;
    }
}