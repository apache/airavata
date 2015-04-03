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
package org.apache.airavata.gfac.monitor.impl.mail.parser;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBSEmailParser implements EmailParser {

    private static final String STATUS = "status";
    private static final String JOBID = "jobId";
    private static final String REGEX = "[a-zA-Z: ]*(?<" + JOBID + ">[a-zA-Z0-9-\\.]*)\\s+.*\\s+.*\\s+(?<" + STATUS + ">[a-zA-Z\\ ]*)";

    @Override
    public void parseEmail(Message message) throws MessagingException {
        try {
            String content = ((String) message.getContent());
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String statusLine = matcher.group(STATUS);
                String jobId = matcher.group(JOBID);
                switch (statusLine) {
                    case "Begun execution":
                        System.out.println("Begun execution  -> " + jobId);
                        break;
                    case "Execution terminated":
                        System.out.println("Execution terminated -> " + jobId);
                        break;
                    default:
                        System.out.println("Un-handle status change -> " + jobId);
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/*    -----------------------
    This is the message envelope
    ---------------------------
    FROM: pbsconsult@sdsc.edu
    TO: shameera@scigap.org
    SUBJECT: PBS JOB 2556782.trestles-fe1.local
    ----------------------------
    CONTENT-TYPE: TEXT/PLAIN
    This is plain text
    ---------------------------
    PBS Job Id: 2556782.trestles-fe1.local
    Job Name:   A1182004055
    Exec host:  trestles-1-12/0+trestles-1-12/1+trestles-1-12/2+trestles-1-12/3
    Begun execution
    */
/*
    -----------------------
    This is the message envelope
    ---------------------------
    FROM: pbsconsult@sdsc.edu
    TO: shameera@scigap.org
    SUBJECT: PBS JOB 2556782.trestles-fe1.local
    ----------------------------
    CONTENT-TYPE: TEXT/PLAIN
    This is plain text
    ---------------------------
    PBS Job Id: 2556782.trestles-fe1.local
    Job Name:   A1182004055
    Exec host:  trestles-1-12/0+trestles-1-12/1+trestles-1-12/2+trestles-1-12/3
    Execution terminated
    Exit_status=0
    resources_used.cput=00:14:31
    resources_used.mem=124712kb
    resources_used.vmem=3504116kb
    resources_used.walltime=00:04:10
    Error_Path: trestles-login2.sdsc.edu:/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs/MonitorTest_9169517d-e2d9-4ff5-bed5-dee6eb3eebb2/Amber_Sander.stderr
    Output_Path: trestles-login2.sdsc.edu:/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs/MonitorTest_9169517d-e2d9-4ff5-bed5-dee6eb3eebb2/Amber_Sander.stdout
    */


}
