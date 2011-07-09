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

package wsmg.samples.msgBox;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;

import wsmg.samples.util.ConfigKeys;
import edu.indiana.extreme.www.xgws.msgbox.client.MsgBoxClient;
import edu.indiana.extreme.www.xgws.msgbox.util.MsgBoxUtils;

public class SimpleMsgBoxExec {

	private static Properties getDefaults() {
		Properties defaults = new Properties();
		defaults.setProperty(ConfigKeys.MSGBOX_SERVICE_URL,
				"http://localhost:8080/axis2/services/MsgBoxService");
		return defaults;
	}

	public static void main(String[] args) throws IOException {

		Properties configurations = new Properties(getDefaults());
		URL configURL = ClassLoader
				.getSystemResource(ConfigKeys.CONFIG_FILE_NAME);

		if (configURL != null) {
			configurations.load(configURL.openStream());

		} else {
			System.out
					.println("unable to load configurations defaults will be used");
		}

		String msgBoxId = UUID.randomUUID().toString();
		MsgBoxClient client = new MsgBoxClient();

		EndpointReference msgBoxEpr = client.createMessageBox(configurations
				.getProperty(ConfigKeys.MSGBOX_SERVICE_URL), 500L);

		try {
			client.storeMessage(msgBoxEpr, 500L, MsgBoxUtils
					.reader2OMElement(new StringReader(
							"<test>A simple test message</test>")));
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<OMElement> iterator = client.takeMessagesFromMsgBox(msgBoxEpr,
				500L);
		int i = 0;
		if (iterator != null)
			while (iterator.hasNext()) {
				i++;
				System.out.println("Retrieved message :" + i);
				try {
					System.out.println(iterator.next().toStringWithConsume());
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			}

		System.out.println("Delete message box response :  "
				+ client.deleteMsgBox(msgBoxEpr, 500L));
	}

}
