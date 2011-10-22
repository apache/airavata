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

package org.apache.airavata.wsmg.commons.util;

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.dispatchers.AddressingBasedDispatcher;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;

public class Axis2Utils {

    public static void overrideAddressingPhaseHander(ConfigurationContext configContext,
            AddressingBasedDispatcher dispatcher) {

        List<Phase> inflowPhases = configContext.getAxisConfiguration().getPhasesInfo().getINPhases();
        boolean foundFlag = false;

        for (Phase p : inflowPhases) {

            if (p.getName().equalsIgnoreCase("Addressing")) {

                List<Handler> handlers = p.getHandlers();

                for (Iterator<Handler> ite = handlers.iterator(); ite.hasNext();) {
                    Handler h = ite.next();
                    if (h.getClass().isAssignableFrom(dispatcher.getClass())) {
                        p.removeHandler(h.getHandlerDesc());
                        break;
                    }
                }

                p.addHandler(dispatcher, 0);
                foundFlag = true;
                break;
            }

        }

        if (!foundFlag) {
            throw new RuntimeException("unable to find addressing phase - inside inflow phases");
        }
    }
}
