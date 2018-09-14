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
 */
package org.apache.airavata.helix.impl.task.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class for {@link CatalogEntry} related tasks
 *
 * @since 1.0.0-SNAPSHOT
 */
public class CatalogUtil {

    private final static Logger logger = LoggerFactory.getLogger(CatalogUtil.class);

    /**
     * Creates list of {@link CatalogEntry}s using the catalog DB
     * which has the details of Docker parsers
     *
     * @return a list of {@link CatalogEntry}s
     * @throws FileNotFoundException if Catalog could not be found
     */
    public static List<CatalogEntry> catalogLookup(String catalogPath) throws FileNotFoundException {
        List<CatalogEntry> entries = new ArrayList<>();
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(new FileReader(catalogPath), JsonArray.class);
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();

            CatalogEntry entry = new CatalogEntry.Builder(
                    obj.get("dockerImageName").getAsString(), obj.get("dockerWorkingDirPath").getAsString(),
                    obj.get("executableBinary").getAsString(), obj.get("executingFile").getAsString(),
                    obj.get("inputFileExtension").getAsString(), obj.get("outputFileName").getAsString())
                    .applicationType(obj.get("applicationType").getAsString())
                    .operation(obj.get("operation").getAsString())
                    .runInDetachedMode(obj.get("runInDetachedMode").getAsString())
                    .automaticallyRmContainer(obj.get("automaticallyRmContainer").getAsString())
                    .runInDetachedMode(obj.get("runInDetachedMode").getAsString())
                    .securityOpt(obj.get("securityOpt").getAsString())
                    .envVariables(!obj.get("envVariables").isJsonNull()
                            ? gson.fromJson(obj.get("envVariables").getAsJsonArray(), String[].class)
                            : null)
                    .cpus(obj.get("cpus").getAsString())
                    .label(obj.get("label").getAsString())
                    .build();

            // Catalog entry should hold either an operation or application
            if (entry.getOperation().isEmpty() || entry.getApplicationType().isEmpty()) {
                entries.add(entry);

            } else {
                logger.warn("Avoid adding the Docker entry: " + entry.getDockerImageName() +
                        " as the catalog entry contains both operation and application");
            }
        }
        return entries;
    }

    /**
     * Converts the given <code>entry</code> to a JSON String
     *
     * @param entry {@link CatalogEntry} should be converted to JSON string
     * @return JSON string of the <code>entry</code>
     */
    public static String catalogEntryToJSONString(CatalogEntry entry) {
        return new Gson().toJson(entry);
    }

    /**
     * Converts the given <code>strEntry</code> to a {@link CatalogEntry}
     *
     * @param strEntry which should be used to generate the relevant {@link CatalogEntry}
     * @return the {@link CatalogEntry} corresponding to the <code>strEntry</code>
     */
    public static CatalogEntry jsonStringToCatalogEntry(String strEntry) {
        return new Gson().fromJson(strEntry, CatalogEntry.class);
    }
}
