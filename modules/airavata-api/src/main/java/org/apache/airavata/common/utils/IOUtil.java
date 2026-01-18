/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I/O utility class using modern Java NIO.
 */
public class IOUtil {

    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

    /**
     * Writes content to a file at the specified path.
     */
    public static void writeToFile(String content, String path) throws IOException {
        logger.debug("Writing to path: {}", path);
        Files.writeString(Path.of(path), content);
    }

    /**
     * Writes content to the specified file.
     */
    public static void writeToFile(String content, File file) throws IOException {
        Files.writeString(file.toPath(), content);
    }

    /**
     * Writes input stream to file.
     */
    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        try (inputStream;
                var outputStream = Files.newOutputStream(file.toPath())) {
            inputStream.transferTo(outputStream);
        }
    }

    /**
     * Writes content to writer and closes it.
     */
    public static void writeToWriter(String content, Writer writer) throws IOException {
        try (writer) {
            writer.write(content);
        }
    }

    /**
     * Reads file content as string.
     */
    public static String readFileToString(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    /**
     * Reads file content as string.
     */
    public static String readFileToString(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * Reads input stream to string.
     */
    public static String readToString(InputStream stream) throws IOException {
        try (stream) {
            return new String(stream.readAllBytes());
        }
    }

    /**
     * Reads reader content to string.
     */
    public static String readToString(Reader reader) throws IOException {
        try (reader) {
            var sb = new StringBuilder();
            var cbuf = new char[8192];
            int len;
            while ((len = reader.read(cbuf)) != -1) {
                sb.append(cbuf, 0, len);
            }
            return sb.toString();
        }
    }

    /**
     * Reads file to byte array.
     */
    public static byte[] readToByteArray(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Reads input stream to byte array.
     */
    public static byte[] readToByteArray(InputStream inputStream) throws IOException {
        try (inputStream) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Deletes directory recursively.
     */
    public static boolean deleteDirectory(File path) {
        if (!path.exists()) {
            return true;
        }
        try (Stream<Path> walk = Files.walk(path.toPath())) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (IOException e) {
            logger.warn("Failed to delete directory: {}", path, e);
            return false;
        }
    }

    /**
     * Gets file extension in lowercase.
     */
    public static String getExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return (index > 0 && index < name.length() - 1)
                ? name.substring(index + 1).toLowerCase()
                : null;
    }
}
