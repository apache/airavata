/*
 * Copyright (c) 2004-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: IOUtil.java,v 1.4 2008/04/01 21:44:27 echintha Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class IOUtil {

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeToFile(String content, String path) throws IOException {
        logger.entering(new Object[] { path, content });

        FileWriter fw = new FileWriter(path);
        writeToWriter(content, fw);

        logger.exiting();
    }

    /**
     * @param content
     * @param file
     * @throws IOException
     */
    public static void writeToFile(String content, File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        writeToWriter(content, fw);
    }

    /**
     * @param inputStream
     * @param file
     * @throws IOException
     */
    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.close();
    }

    /**
     * Writes a specified String to a specified Writer.
     * 
     * @param content
     *            The content to write
     * @param writer
     *            The specified Writer
     * 
     * @throws IOException
     */
    public static void writeToWriter(String content, Writer writer) throws IOException {
        writer.write(content);
        writer.close();
    }

    /**
     * Returns the content of a specified file as a String.
     * 
     * @param path
     * @return the content of a specified file as a String
     * @throws IOException
     */
    public static String readFileToString(String path) throws IOException {
        FileReader read = new FileReader(path);
        return readToString(read);
    }

    /**
     * Returns the content of a specified file as a String.
     * 
     * @param file
     * @return the content of a specified file as a String
     * @throws IOException
     */
    public static String readFileToString(File file) throws IOException {
        FileReader reader = new FileReader(file);
        return readToString(reader);
    }

    /**
     * Returns a String read from a specified InputStream.
     * 
     * @param stream
     *            The specified InputStream
     * @return The String read from the specified InputStream
     * @throws IOException
     */
    public static String readToString(InputStream stream) throws IOException {
        return readToString(new InputStreamReader(stream));
    }

    /**
     * Returns a String read from a specified Reader.
     * 
     * @param reader
     *            The specified Reader
     * @return The String read from the specified Reader
     * @throws IOException
     */
    public static String readToString(Reader reader) throws IOException {
        char[] cbuf = new char[1024];
        StringBuilder sbuf = new StringBuilder();
        int len;
        while ((len = reader.read(cbuf)) != -1) {
            sbuf.append(cbuf, 0, len);
        }
        return sbuf.toString();
    }

    /**
     * @param file
     * @return The byte array
     * @throws IOException
     */
    public static byte[] readToByteArray(File file) throws IOException {
        return readToByteArray(new FileInputStream(file));
    }

    /**
     * @param inputStream
     * @return The byte array.
     * @throws IOException
     */
    public static byte[] readToByteArray(InputStream inputStream) throws IOException {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            byteArrayStream.write(buf, 0, len);
        }
        return byteArrayStream.toByteArray();
    }

    /**
     * @param path
     * @return <code>true</code> if and only if the file or directory is successfully deleted; <code>false</code>
     *         otherwise
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    /**
     * Gets the extension of a specified file.
     * 
     * @param file
     *            the specified file.
     * @return the extension of the file in lower case if there is an extension; null otherwise
     */
    public static String getExtension(File file) {
        String ext = null;
        String name = file.getName();

        int index = name.lastIndexOf('.');
        if (index > 0 && index < name.length() - 1) {
            ext = name.substring(index + 1).toLowerCase();
        }
        return ext;
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2004-2007 The Trustees of Indiana University. All rights reserved.
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
