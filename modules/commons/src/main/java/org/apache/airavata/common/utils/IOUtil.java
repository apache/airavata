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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtil {

    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    /**
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeToFile(String content, String path) throws IOException {
        logger.debug("Path:" + path + " Content:" + content);

        FileWriter fw = new FileWriter(path);
        writeToWriter(content, fw);
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