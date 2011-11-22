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

package org.apache.airavata.wsmg.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class CommonRoutines {

    private static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private static XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    // Format date to string like "2004-06-26T21:07:00.000-08:00"
    public static String getXsdDateTime(Date time) {
        Date now = time;
        DateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        TimeZone timeZone = TimeZone.getDefault();
        ISO8601Local.setTimeZone(timeZone);
        int offset = timeZone.getOffset(now.getTime());
        String sign = "+";
        if (offset < 0) {
            offset = -offset;
            sign = "-";
        }
        int hours = offset / 3600000;
        int minutes = (offset - hours * 3600000) / 60000;
        if (offset != hours * 3600000 + minutes * 60000) {
            // E.g. TZ=Asia/Riyadh87
            throw new RuntimeException("TimeZone offset (" + sign + offset + " ms) is not an exact number of minutes");
        }
        DecimalFormat twoDigits = new DecimalFormat("00");
        String ISO8601Now = ISO8601Local.format(now) + sign + twoDigits.format(hours) + ":" + twoDigits.format(minutes);
        return ISO8601Now;
    }

    public static SOAPEnvelope reader2SOAPEnvelope(Reader reader) throws XMLStreamException {

        XMLStreamReader inflow = getXMLInputFactory().createXMLStreamReader(reader);

        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow);
        SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
        return omEnvelope;
    }

    private static XMLInputFactory getXMLInputFactory() {
        return xmlInputFactory;
    }

    private static XMLOutputFactory getXMLOutputFactory() {
        return xmlOutputFactory;
    }

    public static OMElement reader2OMElement(Reader reader) throws XMLStreamException {

        XMLStreamReader inflow = getXMLInputFactory().createXMLStreamReader(reader);

        StAXOMBuilder builder = new StAXOMBuilder(inflow);
        OMElement omElement = builder.getDocumentElement();
        return omElement;
    }

    public static String omToString(OMElement element) {

        StringWriter writer = new StringWriter();

        String ret = null;
        try {
            XMLStreamWriter streamWriter = getXMLOutputFactory().createXMLStreamWriter(writer);

            element.serializeAndConsume(streamWriter);
            streamWriter.flush();
            ret = writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("unable to serialize the OMElement", e);
        }
        return ret;
    }

    public static boolean isAvailable(URI address) {
        // Create a socket with a timeout
        try {
            // exclude message box URL from availability check.
            // if(addressString.indexOf("MsgBox")>0) return true;
            // How to parse the address to port
            InetAddress addr = InetAddress.getByName(address.getHost());
            int port = address.getPort();
            if (port == -1) { // URI has no port, invalid URI. Here I choose not
                // to try port 80.
                return false;
            }
            SocketAddress sockaddr = new InetSocketAddress(addr, port);

            // Create an unbound socket
            Socket sock = new Socket();

            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 1000; // 2 seconds
            sock.connect(sockaddr, timeoutMs);
            sock.close();
            // System.out.println("Still
            // availabe:"+address.getHost()+":"+address.getPort());
        } catch (UnknownHostException e) {
            // e.printStackTrace();
            return false;
        } catch (SocketTimeoutException e) {
            // e.printStackTrace();
            return false;
        } catch (IOException e) {
            // e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String readFromStream(InputStream in) throws IOException {
        StringBuffer wsdlStr = new StringBuffer();

        int read;

        byte[] buf = new byte[1024];
        while ((read = in.read(buf)) > 0) {
            wsdlStr.append(new String(buf, 0, read));
        }
        in.close();
        return wsdlStr.toString();
    }

    public static Options getOptions(String soapAction, long timeout, EndpointReference destination) {
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setAction(soapAction);

        opts.setTimeOutInMilliSeconds(timeout);
        opts.setMessageId(UUIDGenerator.getUUID());
        opts.setTo(destination);

        return opts;
    }

    public static void setHeaders(String soapAction, String destination, ServiceClient client,
            OMElement... customHeaders) throws AxisFault {

        SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

        SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", NameSpaceConstants.WSA_NS);
        msgId.setText(UUIDGenerator.getUUID());

        SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", NameSpaceConstants.WSA_NS);
        to.setText(destination);

        SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", NameSpaceConstants.WSA_NS);
        action.setText(soapAction);

        client.addHeader(action);
        client.addHeader(msgId);
        client.addHeader(to);

        for (OMElement h : customHeaders) {
            try {
                client.addHeader(org.apache.axiom.om.util.ElementHelper.toSOAPHeaderBlock(h, soapfactory));
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }

    }
}
