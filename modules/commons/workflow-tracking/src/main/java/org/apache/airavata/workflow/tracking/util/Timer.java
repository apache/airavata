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

package org.apache.airavata.workflow.tracking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * calculates simple timing information for instrumenting code
 */
public class Timer {

    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    private long start, end, total = 0, deltaStart, delta = 0;
    private final String msg;
    private boolean ended = false, started = false, paused = false, terminated = false;

    Timer(String msg_) {
        msg = msg_;
        start = System.currentTimeMillis();
    }

    public static Timer init(String msg_) {
        return new Timer(msg_);
    }

    public static Timer initAndStart(String msg_) {

        Timer tp = new Timer(msg_);
        tp.start();
        return tp;
    }

    public void start() {

        assert started == false && terminated == false;
        started = true;
        ended = false;
        start = System.currentTimeMillis();
    }

    public boolean startOrContinue() {

        if (started)
            return false; // continued...not started fresh
        start();
        return true; // started fresh
    }

    public long end(String msg_) {

        assert started == true && terminated == false;
        end = System.currentTimeMillis();
        total += (end - start - delta);
        logger.debug("\n!T!ENDOne \t" + msg_ + " \t" + (end - start - delta) + "\t millis of \t" + total);
        ended = true;
        return end - start;
    }

    public long end() {
        return end(msg);
    }

    public long endAll() {

        logger.debug("\n!T!ENDAll \t" + msg + " \t" + total);
        terminated = true;
        return total;
    }

    public void pause() {

        assert started == true && terminated == false;
        deltaStart = System.currentTimeMillis();
        paused = true;
    }

    public void resume() {

        assert paused == true && terminated == false;
        paused = false;
        delta = System.currentTimeMillis() - deltaStart;
    }
};
