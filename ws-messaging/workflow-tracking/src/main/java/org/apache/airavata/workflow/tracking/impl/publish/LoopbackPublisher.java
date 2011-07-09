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

package org.apache.airavata.workflow.tracking.impl.publish;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.common.ConstructorConsts;
import org.apache.airavata.workflow.tracking.common.ConstructorProps;
import org.apache.airavata.workflow.tracking.types.BaseIDType;
import org.apache.airavata.workflow.tracking.types.BaseNotificationType;
import org.apache.airavata.workflow.tracking.types.LogInfoDocument;
import org.apache.airavata.workflow.tracking.types.PublishURLDocument;
import org.apache.airavata.workflow.tracking.util.MessageUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * acts as a workflow tracking NotificationPublisher that calls a workflow tracking callback listener without having to
 * pass though a real notification broker. Default listener prints to stdout
 */
public class LoopbackPublisher extends AbstractPublisher implements NotificationPublisher {

    private Callback listener;
    private String topic;
    private static int globalCount = 0;

    public LoopbackPublisher(Callback listener_, String topic_) {
        super(10, false); // capacity, async
        topic = topic_;
        listener = listener_;
        if (listener == null) {
            listener = new Callback() {
                int count = 0;

                public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {

                    System.out
                            .printf("----\nReceived Message [L:%d/G:%d] on topic [%s] of type [%s] with payload:\n[%s]\n====\n",
                                    count, globalCount, topic, notificationType, messageObj);
                    count++;
                    globalCount++;
                }
            };
        }
    }

    public LoopbackPublisher(final PrintStream out_, String topic_) {
        this(new Callback() {
            int count = 0;

            public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {

                out_.printf(
                        "----\nReceived Message [L:%d/G:%d] on topic [%s] of type [%s] with payload:\n[%s]\n====\n",
                        count, globalCount, topic, notificationType, messageObj);
                count++;
                globalCount++;
            }
        }, topic_);
    }

    public LoopbackPublisher(String topic_) {
        this(System.out, topic_);
    }

    public LoopbackPublisher(ConstructorProps props) {
        this((Callback) props.get(ConstructorConsts.CALLBACK_LISTENER), (String) props.get(ConstructorConsts.TOPIC));
    }

    /**
     * Method publishSync
     * 
     * @param message
     *            a String message that should be a valid XML String (serialized XML document)
     * 
     */
    public void publishSync(String message) {

        try {
            XmlObject xmlMessage = XmlObject.Factory.parse(message);
            NotificationType type = MessageUtil.getType(xmlMessage);
            listener.deliverMessage(topic, type, xmlMessage);
        } catch (XmlException e) {
            System.err.println("Error parsing workflow tracking message : [" + message + "]\n" + "as an XML Object");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        LoopbackPublisher publisher = new LoopbackPublisher(new Callback() {
            int count = 0;

            public void deliverMessage(String topic, NotificationType notificationType, XmlObject messageObj) {

                System.out.printf("----\nReceived Message [%d] on topic [%s] of type [%s] with payload:\n[%s]\n====\n",
                        count++, topic, notificationType, messageObj);
            }
        }, "testTopic");

        // create & publish log message
        {
            LogInfoDocument logMsg = LogInfoDocument.Factory.newInstance();
            BaseNotificationType log = logMsg.addNewLogInfo();
            // add timestamp
            final Calendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            log.setTimestamp(cal);
            // add notification source
            BaseIDType baseID = BaseIDType.Factory.newInstance();
            baseID.setServiceID("http://tempuri.org/test_service");
            log.addNewNotificationSource().set(baseID);
            // add description
            log.setDescription("A test message");

            // publish message as XML Object
            publisher.publish(logMsg);
        }
        // create & publish publishURl message
        {
            // create publish URL message
            PublishURLDocument pubMsg = PublishURLDocument.Factory.newInstance();
            PublishURLDocument.PublishURL pub = pubMsg.addNewPublishURL();
            // add timestamp
            final Calendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            pub.setTimestamp(cal);
            // add notification source
            BaseIDType baseID = BaseIDType.Factory.newInstance();
            baseID.setServiceID("http://tempuri.org/test_service");
            pub.addNewNotificationSource().set(baseID);
            pub.setTitle("Some URL's Title");
            pub.setLocation("http://tempuri.org/published_url");

            // publish message as XML string
            publisher.publish(pubMsg.xmlText());
        }
    }

}
