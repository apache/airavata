/**
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
 */
package org.apache.airavata.common.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.airavata.common.exception.AiravataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens URLs with the OS-specific browser.
 */
public class BrowserLauncher {

    private static final String ERROR_MESSAGE = "Error while attempting to launch web browser";

    private static Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);

    /**
     * Opens a specified URL with the browser.
     * 
     * @param url
     *            The specified URL.
     * @throws AiravataException
     */
    public static void openURL(URL url) throws AiravataException {
        openURL(url.toString());
    }

    /**
     * Opens a specified URL with the browser.
     * 
     * @param url
     *            The specified URL.
     * @throws AiravataException
     */
    public static void openURL(String url) throws AiravataException {
        logger.debug("Enter:" + url);
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
                    throw new AiravataException("Could not find web browser.");
                } else {
                    Runtime.getRuntime().exec(new String[] { browser, url });
                }
            }
        } catch (ClassNotFoundException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (NoSuchMethodException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (IllegalAccessException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (InvocationTargetException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (IOException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (InterruptedException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        } catch (RuntimeException e) {
            throw new AiravataException(ERROR_MESSAGE, e);
        }
    }
}