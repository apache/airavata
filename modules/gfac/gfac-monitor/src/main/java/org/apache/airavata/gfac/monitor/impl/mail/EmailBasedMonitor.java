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
package org.apache.airavata.gfac.monitor.impl.mail;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.monitor.core.Monitor;
import org.apache.airavata.gfac.monitor.impl.mail.parser.EmailParser;
import org.apache.airavata.gfac.monitor.impl.mail.parser.PBSEmailParser;
import org.apache.airavata.gfac.monitor.impl.mail.parser.SLURMEmailParser;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.util.Properties;

public class EmailBasedMonitor implements Monitor {

    private static final String PBS_CONSULT_SDSC_EDU = "pbsconsult@sdsc.edu";
    private static final String SLURM_BATCH_STAMPEDE = "slurm@batch1.stampede.tacc.utexas.edu";

    private Session session ;
    private Store store;
    private Folder emailFolder;

    public void monitorEmails(String host, String username, String password ) throws MessagingException,
            InterruptedException, AiravataException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        session = Session.getDefaultInstance(properties);
        store = session.getStore("imaps");
        store.connect(host, username, password);
        while (!ServerSettings.isStopAllThreads()) {
            Thread.sleep(2000);
            emailFolder = store.getFolder("TEST");
            emailFolder.open(Folder.READ_WRITE);
            Message[] searchMessages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : searchMessages) {
                parse(message);
            }
            emailFolder.setFlags(searchMessages, new Flags(Flags.Flag.SEEN), true);
            emailFolder.close(false);
        }
        store.close();
    }

    private void parse(Message message) throws MessagingException, AiravataException {
        Address fromAddress = message.getFrom()[0];
        EmailParser emailParser;
        String addressStr = fromAddress.toString();
        switch (addressStr) {
            case PBS_CONSULT_SDSC_EDU:
                emailParser = new PBSEmailParser();
                break;
            case SLURM_BATCH_STAMPEDE:
                emailParser = new SLURMEmailParser();
                break;
            default:
                throw new AiravataException("Un-handle address type for email monitoring -->  " + addressStr);
        }
        emailParser.parseEmail(message);
    }

}
