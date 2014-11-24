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

package org.apache.airavata.distribution.xbaya.jnlp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will go through lib directory and creates the jnlp configuration file.
 */
public class Main {

    private static final String CONFIGURATION_ELEMENT = "DEPENDENT_JARS";

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err
                    .println("[ERROR] JNLP creator must be given with lib directory of Xbaya and JNLP template location.");
            System.exit(-1);
        }

        String libDirectory = args[0];
        String jnlpTemplateFile = args[1];

        System.out.println("[INFO] The lib directory is " + libDirectory);
        System.out.println("[INFO] The jnlp file is " + jnlpTemplateFile);

        File libDirectoryFile = new File(libDirectory);

        if (!libDirectoryFile.exists()) {
            System.err.println("[ERROR] Invalid lib directory given - " + libDirectory + ". Cannot add dependent jars");
            System.exit(-1);
        }

        File jnlpFile = new File(jnlpTemplateFile);
        if (!jnlpFile.canRead()) {
            System.err.println("[ERROR] Unable to read given jnlp file - " + jnlpTemplateFile + ".");
            System.exit(-1);

        }

        StringBuilder stringBuilder = new StringBuilder();

        // Read all dependencies
        for (File file : libDirectoryFile.listFiles(new JarFileFilter())) {
            String line = "<jar href=\"lib/" + file.getName() + "\"/>";
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        // System.out.println(stringBuilder.toString());
        modifyConfigurations(jnlpFile, stringBuilder);

    }

    private static void modifyConfigurations(File jnlpFile, StringBuilder dependencies) {

        List<String> lines = new ArrayList<String>();

        // first, read the file and store the changes
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(jnlpFile));
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] Error occurred while reading the file. " + e.getMessage());
        }

        String line = null;
        if (in != null) {
            try {
                line = in.readLine();
            } catch (IOException e) {
                System.err.println("[ERROR] Error occurred while reading the file. " + e.getMessage());
                try {
                    in.close();
                } catch (IOException e1) {
                    System.err.println("[ERROR] Error occurred while closing the file. " + e.getMessage());
                }
            }
        }

        try {
            while (line != null) {

                if (line.trim().startsWith(CONFIGURATION_ELEMENT)) {
                    line = line.replaceAll(CONFIGURATION_ELEMENT, dependencies.toString());
                }
                lines.add(line);
                line = in.readLine();

            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error occurred while reading the file. " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Error occurred while closing the file. " + e.getMessage());
            }
        }

        // now, write the file again with the changes
        PrintWriter out = null;
        try {
            out = new PrintWriter(jnlpFile);
            for (String l : lines) {
                out.println(l);
                out.flush();
            }
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] Error occurred while writing back to the file. " + e.getMessage());
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }

    }

    public static class JarFileFilter implements FilenameFilter {
        String ext;

        public JarFileFilter() {
            this.ext = ".jar";
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(ext);
        }
    }

}
