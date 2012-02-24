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

package org.apache.airavata.client.airavata;

import java.net.URL;

public class AiravataClientConfiguration {
	private URL gfacURL;
	private URL xregistryURL;
	private String myproxyHost="myproxy.teragrid.org";
	private URL messageboxURL;
	private URL messagebrokerURL;
	private String myproxyUsername="ogce";
	private String myproxyPassword="testpassword";
	private URL xbayaServiceURL;
	private URL jcrURL;
	private String jcrUsername="admin";
	private String jcrPassword="admin";
	private String echoMessage="Hello World";
	
	public URL getGfacURL() {
		return gfacURL;
	}
	public void setGfacURL(URL gfacURL) {
		this.gfacURL = gfacURL;
	}
	public URL getXregistryURL() {
		return xregistryURL;
	}
	public void setXregistryURL(URL xregistryURL) {
		this.xregistryURL = xregistryURL;
	}
	public String getMyproxyHost() {
		return myproxyHost;
	}
	public void setMyproxyHost(String myproxyHost) {
		this.myproxyHost = myproxyHost;
	}
	public URL getMessageboxURL() {
		return messageboxURL;
	}
	public void setMessageboxURL(URL messageboxURL) {
		this.messageboxURL = messageboxURL;
	}
	public URL getMessagebrokerURL() {
		return messagebrokerURL;
	}
	public void setMessagebrokerURL(URL messagebrokerURL) {
		this.messagebrokerURL = messagebrokerURL;
	}
	public String getMyproxyUsername() {
		return myproxyUsername;
	}
	public void setMyproxyUsername(String myproxyUsername) {
		this.myproxyUsername = myproxyUsername;
	}
	public String getMyproxyPassword() {
		return myproxyPassword;
	}
	public void setMyproxyPassword(String myproxyPassword) {
		this.myproxyPassword = myproxyPassword;
	}
	public URL getXbayaServiceURL() {
		return xbayaServiceURL;
	}
	public void setXbayaServiceURL(URL xbayaServiceURL) {
		this.xbayaServiceURL = xbayaServiceURL;
	}
	public URL getJcrURL() {
		return jcrURL;
	}
	public void setJcrURL(URL jcrURL) {
		this.jcrURL = jcrURL;
	}
	public String getJcrUsername() {
		return jcrUsername;
	}
	public void setJcrUsername(String jcrUsername) {
		this.jcrUsername = jcrUsername;
	}
	public String getJcrPassword() {
		return jcrPassword;
	}
	public void setJcrPassword(String jcrPassword) {
		this.jcrPassword = jcrPassword;
	}
	public String getEchoMessage() {
		return echoMessage;
	}
	public void setEchoMessage(String echoMessage) {
		this.echoMessage = echoMessage;
	}
	
}
