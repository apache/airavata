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


import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class AwsMetadata {
    private static final Logger log = LoggerFactory.getLogger(AwsMetadata.class);
    private static final String METADATA_URI_BASE = "http://169.254.169.254/";
    private static final String REGION_SUFFIX = "/latest/dynamic/instance-identity/document";
    private static final String ZONE_SUFFIX = "/latest/meta-data/placement/availability-zone";
    private static final String PUBLIC_IPV4_SUFFIX = "/latest/meta-data/public-ipv4";
    private static final String PRIVATE_IPV4_SUFFIX = "latest/meta-data/local-ipv4";
    private static final String HOSTNAME_SUFFIX = "/latest/meta-data/hostname";
    private static final String ID_SUFFIX = "/latest/meta-data/instance-id";

    private final URI baseUri;

    private String id;
    private String region;
    private String hostname;
    private String zone;
    private InetAddress publicIp;
    private InetAddress privateIp;

    public AwsMetadata() {
        try {
            baseUri = new URI(METADATA_URI_BASE);
        } catch (URISyntaxException e) {
            Preconditions.checkState(false, "Unexpected URI Syntax Exception: {}", e);
            throw new RuntimeException(e);
        }
    }

    public String getRegion() {
        if (region == null) {
            try {
                String dynamicData = getMetadataAt(REGION_SUFFIX);
                if (dynamicData != null) {
                    final JsonObject asJsonObject = new JsonParser().parse(dynamicData).getAsJsonObject();
                    region = asJsonObject.get("region").getAsString();
                }
            } catch (ClassCastException e) {
                log.error("Unable to get region, expecting a JSON Object", e);
            }
        }
        return region;
    }

    public String getZoneName() {
        if (zone == null) {
            zone = getMetadataAt(ZONE_SUFFIX);
        }
        return zone;
    }

    public String getId() {
        if (id == null) {
            id = getMetadataAt(ID_SUFFIX);
        }

        return id;
    }

    public String getHostname() {
        if (hostname == null) {
            hostname = getMetadataAt(HOSTNAME_SUFFIX);
        }
        return hostname;
    }

    public InetAddress getPublicIpAddress() {
        if (publicIp == null) {
            String ip = getMetadataAt(PUBLIC_IPV4_SUFFIX);
            if (ip != null) {
                publicIp = InetAddresses.forString(ip);
            }
        }
        return publicIp;
    }

    public InetAddress getInternalIpAddress() {
        if (privateIp == null) {
            String ip = getMetadataAt(PRIVATE_IPV4_SUFFIX);
            if (ip != null) {
                privateIp = InetAddresses.forString(ip);
            }
        }
        return privateIp;
    }

    private String getMetadataAt(String suffix) {
        try {
            URI resolved = baseUri.resolve(suffix);
            StringBuilder builder = new StringBuilder();
            String line = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resolved.toURL().openStream()))) {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (Exception e) {
            // ignore for now to make sure local servers don't go verbose
        }
        return null;
    }
}
