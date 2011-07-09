/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.utils.Utils;

import xsul.MLogger;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;
import xsul.http_server.HttpServerResponse;
import xsul.http_server.ServerSocketFactory;
import xsul.message_router.MessageContext;
import xsul.processor.DynamicInfosetProcessorException;
import xsul.xservo_soap_http.HttpBasedServices;

public class XregistryHttpbasedServices extends HttpBasedServices {
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    private static Pattern restReqPattern = Pattern.compile(".*xregistry/(.*?)/(.*?)");
    
    private XregistryImpl xregistryImpl;
 
    public XregistryHttpbasedServices(HttpMiniServer server, XregistryImpl xregistryPortType)
            throws DynamicInfosetProcessorException {
        super(server);
        this.xregistryImpl = xregistryPortType;
    }

    public XregistryHttpbasedServices(int tcpPort, XregistryImpl xregistryPortType)
            throws DynamicInfosetProcessorException {
        super(tcpPort);
        this.xregistryImpl = xregistryPortType;
    }

    public XregistryHttpbasedServices(ServerSocketFactory serverSocketFactory,
            XregistryImpl xregistryPortType) throws DynamicInfosetProcessorException {
        super(serverSocketFactory);
        this.xregistryImpl = xregistryPortType;
    }

    @Override
    public void service(HttpServerRequest req, HttpServerResponse res) throws HttpServerException {
        try {
            String method = req.getMethod();
            String path = req.getPath();
            if (method.equals("GET") && path.startsWith("/xregistry/")) {
                String[] parameters = parseURL(path);
                String docType =  parameters[0];
                String documentName = parameters[1];

                String result = null;
                OutputStream out = res.getOutputStream();
                
                if (docType.equals("servicedesc")) {
                    result = xregistryImpl.getServiceDesc(findUserDN(), documentName);
                } else if (docType.equals("appdesc")) {
                    String[] params = documentName.split("#");
                    result = xregistryImpl.getAppDesc(findUserDN(), params[0], parameters[1]);
                } else if (docType.equals("servicedesc")) {
                } else if (docType.equals("hostdesc")) {
                    result = xregistryImpl.getHostDesc(findUserDN(), documentName);
                } else if (docType.equals("cwsdl")) {
                    result = xregistryImpl.getConcreateWsdl(findUserDN(), documentName);
                } else if (docType.equals("awsdl")) {
                    result = xregistryImpl.getAbstractWsdl(findUserDN(), documentName);
                } else if (docType.equals("doc")) {
                    result = xregistryImpl.getDocument(findUserDN(), QName.valueOf(documentName));
                }
                res.setContentType("text/xml");
                out.write(result.getBytes());
            }else{
                super.service(req, res);
            }
        } catch (XregistryException e) {
            throw new HttpServerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new HttpServerException(e.getMessage(), e);
        }
    }

    
    public static String[] parseURL(String requestUrl) throws XregistryException{
        try {
            Matcher matcher = restReqPattern.matcher(requestUrl);
            if(matcher.matches()){
                return new String[]{matcher.group(1),URLDecoder.decode(matcher.group(2),"UTF-8")};
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            throw new XregistryException(e);
        }
    }
    
//    private String[] parsePath(String path) {
//        ArrayList<String> params = new ArrayList<String>(5);
//        StringBuffer buffer = new StringBuffer();
//        boolean skip = false;
//        for(char c:path.toCharArray()){
//            switch(c){
//                case '/':
//                    if(!skip){
//                        params.add(buffer.toString());
//                        buffer.setLength(0);
//                    }else{
//                        buffer.append(c);
//                    }
//                    break;
//                case '(': skip = true;buffer.append('{');break; 
//                case ')': skip = false;buffer.append('}');break;
//                default:
//                    buffer.append(c);
//            }
//        }
//        return params.toArray(new String[]{});
//    }

    private String findUserDN() {
        // /C=US/O=National Center for Supercomputing Applications/CN=LEAD
        // Community User

        // return "/C=US/O=National Center for Supercomputing
        // Applications/CN=Hemapani Srinath Perera";
        String userDN = null;
        MessageContext mc = xsul.xhandler_context.ServerContextAccessHandler.getContext();
        if (mc != null) {
            userDN = mc.getIncomingUserDn();
        }

        if (userDN == null) {
            userDN = XregistryConstants.ANONYMOUS_USER;
        }
        userDN = Utils.canonicalizeDN(userDN);
        log.info("Invoker = " + userDN);
        return userDN;
    }
}
