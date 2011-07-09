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

package org.apache.airavata.wsmg.messenger.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

interface TaskCompletionCallback {
    void onCompletion(ThreadCrew.WSMGRunnable r);
}

interface RunnableEx {
    public void run();

}

public class ThreadCrew implements TaskCompletionCallback {

    int nextIndex = 0;

    Logger log = Logger.getLogger(ThreadCrew.class);

    List<WSMGRunnable> wsmgRunnables = new ArrayList<WSMGRunnable>();

    ExecutorService service;

    public void onCompletion(WSMGRunnable r) {

        synchronized (wsmgRunnables) {
            log.error("error occued ...");
            List<RunnableEx> jobList = r.getJobList();
            wsmgRunnables.remove(r);
            WSMGRunnable newR = new WSMGRunnable(this, jobList);
            wsmgRunnables.add(newR);
            service.submit(newR);
        }

    }

    public ThreadCrew(int poolSize) {

        log.debug("pool size=" + poolSize);

        service = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < poolSize; i++) {

            WSMGRunnable r = new WSMGRunnable(this);
            wsmgRunnables.add(r);
            service.submit(r);
        }

    }

    public void submitTask(RunnableEx task) {

        WSMGRunnable r = null;

        synchronized (wsmgRunnables) {

            nextIndex++;
            if (nextIndex >= wsmgRunnables.size())
                nextIndex = 0;

            r = wsmgRunnables.get(nextIndex);

        }

        r.submit(task);
    }

    public void stop() {

        for (WSMGRunnable r : wsmgRunnables) {
            r.shutdown();
        }

        service.shutdown();
    }

    class WSMGRunnable implements Runnable {

        List<RunnableEx> jobs = new ArrayList<RunnableEx>();
        TaskCompletionCallback completionCallback;
        boolean runFlag = true;

        public WSMGRunnable(TaskCompletionCallback c) {

            this(c, new ArrayList<RunnableEx>());
            System.out.println("org.apache.airavata.wsmg created new");
        }

        public WSMGRunnable(TaskCompletionCallback c, List<RunnableEx> jl) {
            completionCallback = c;
            jobs = jl;
        }

        public void shutdown() {
            runFlag = false;
        }

        public void run() {

            try {

                int i = 0;

                while (runFlag) {

                    if (jobs.isEmpty()) {
                        waitForJobs();
                        continue;
                    }

                    RunnableEx runnable = jobs.get(i);
                    runnable.run();

                    i++;
                    if (i >= jobs.size()) {
                        i = 0;
                    }

                }

            } finally {

                completionCallback.onCompletion(this);
            }

        }

        public void submit(RunnableEx w) {

            synchronized (jobs) {
                jobs.add(w);
            }

        }

        public List<RunnableEx> getJobList() {
            return jobs;
        }

        final int SLEEP_TIME_SECONDS = 1;

        private void waitForJobs() {
            try {

                TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
                if (log.isDebugEnabled())
                    log.debug("finished - waiting for messages");
            } catch (InterruptedException e) {
                log.error("interrupted while waiting for messages", e);
            }
        }
    }

}
