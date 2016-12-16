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
