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

package org.apache.airavata.wsmg.samples.wse;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class MsgUtil {

	static final String TAG_MSG = "msg";
	static final String TAG_SEQ = "seq";
	static final String TAG_SRC = "src";
	static final String TAG_UUID = "uuid";

	public static OMElement createMsg(long seq, String src, String uuid) {

		// "<msg><seq>%d</seq><src>%s</src><uuid>%s</uuid></msg>"

		OMFactory factory = OMAbstractFactory.getOMFactory();

		OMElement omMsg = factory.createOMElement(TAG_MSG, null);

		OMElement omSeq = factory.createOMElement(TAG_SEQ, null, omMsg);
		omSeq.setText("" + seq);

		OMElement omSrc = factory.createOMElement(TAG_SRC, null, omMsg);
		omSrc.setText(src);

		OMElement omUUID = factory.createOMElement(TAG_UUID, null, omMsg);
		omUUID.setText(uuid);

		return omMsg;
	}

	public static String getSeq(OMElement msg) {

		return msg.getFirstElement().getText();

	}

	public static void print(String msg, OMElement ele){
		
		try{
			System.out.println(msg + "-" + ele.toStringWithConsume());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
}
