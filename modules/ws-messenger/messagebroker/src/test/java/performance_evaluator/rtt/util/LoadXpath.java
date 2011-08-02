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

package performance_evaluator.rtt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

public class LoadXpath {
    private static LoadXpath xpath = null;
    LinkedList<String> xpathList = null;

    public static LoadXpath getInstace() {
        if (xpath == null)
            xpath = new LoadXpath();
        return xpath;
    }

    public LinkedList<String> getXpathList(String fileName) throws IOException {
        URL url = ClassLoader.getSystemResource(fileName);
        if (url != null && xpathList == null)
            return convertStreamToString(url.openStream());
        return xpathList;
    }

    private LinkedList<String> convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            xpathList = new LinkedList<String>();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    xpathList.add(line);
                }
            } finally {
                is.close();
            }
            return xpathList;
        } else {
            return null;
        }
    }
}
