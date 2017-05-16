/**
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
 */
///
// Copyright (c) 2016. Highfive Technologies, Inc.
///
package org.apache.airavata.common.logging;
import org.slf4j.MDC;

import java.util.Map;

public class MDCUtil {
    public static Runnable wrapWithMDC(Runnable r) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> oldMdc = MDC.getCopyOfContextMap();

            if (mdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(mdc);
            }
            try {
                r.run();
            } finally {
                if (oldMdc == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(oldMdc);
                }
            }

        };
    }
}
