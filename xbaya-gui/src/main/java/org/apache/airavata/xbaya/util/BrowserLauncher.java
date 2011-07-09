/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: BrowserLauncher.java,v 1.5 2008/04/01 21:44:27 echintha Exp $
 */
package org.apache.airavata.xbaya.util;

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

// This class is based on the following.
//
// Bare Bones Browser Launch
// Version 1.1
// July 8, 2005
// Supports: Mac OS X, GNU/Linux, Unix, Windows XP
// Example Usage:
// String url = "http://www.centerkey.com/";
// BareBonesBrowserLaunch.openURL(url);
// Public Domain Software -- Free to Use as You Like

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.airavata.xbaya.XBayaException;

import xsul5.MLogger;

/**
 * Opens URLs with the OS-specific browser.
 */
public class BrowserLauncher {

    private static final String ERROR_MESSAGE = "Error while attempting to launch web browser";

    private static MLogger logger = MLogger.getLogger();

    /**
     * Opens a specified URL with the browser.
     * 
     * @param url
     *            The specified URL.
     * @throws XBayaException
     */
    public static void openURL(URL url) throws XBayaException {
        openURL(url.toString());
    }

    /**
     * Opens a specified URL with the browser.
     * 
     * @param url
     *            The specified URL.
     * @throws XBayaException
     */
    public static void openURL(String url) throws XBayaException {
        logger.entering(url);
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
                Method openURL = macUtils.getDeclaredMethod("openURL", new Class[] { String.class });
                openURL.invoke(null, new Object[] { url });
            } else if (osName.startsWith("Windows"))
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else { // assume Unix or Linux
                String[] browsers = { "firefox", "mozilla", "netscape", "opera", "konqueror" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                        browser = browsers[count];
                if (browser == null) {
                    throw new XBayaException("Could not find web browser.");
                } else {
                    Runtime.getRuntime().exec(new String[] { browser, url });
                }
            }
        } catch (ClassNotFoundException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (NoSuchMethodException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (IllegalAccessException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (InvocationTargetException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (IOException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (InterruptedException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        } catch (RuntimeException e) {
            throw new XBayaException(ERROR_MESSAGE, e);
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
