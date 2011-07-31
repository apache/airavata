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

package org.apache.airavata.xbaya.util;

import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.xmlbeans.XmlOptions;

import xsul5.MLogger;
import edu.indiana.dde.metadata.catalog.types.ContentFilterType;
import edu.indiana.dde.metadata.catalog.types.ContentFilterType.Enum;

public class MyLEADUtil {

    private final static MLogger logger = MLogger.getLogger();

    public static XmlOptions PRETTY_PRINT_OPTS = new XmlOptions();
    static {
        PRETTY_PRINT_OPTS.setSavePrettyPrint();
    }

    public static final String C_FILTER_ID_ONLY = "ID_ONLY";
    public static final String C_FILTER_FULL_SCHEMA = "GUID_ONLY";

    public static final String H_FILTER_TARGET = "TARGET";
    public static final String H_FILTER_SUBTREE = "SUBTREE";
    public static final String H_FILTER_CHILDREN = "CHILDREN";

    /**
     * @param filter
     * @return
     * @throws MyLeadException
     */
    public static Enum getCFilter(String filter) throws MyLeadException {
        if (C_FILTER_FULL_SCHEMA.equals(filter)) {
            return ContentFilterType.FULL_SCHEMA;
        } else if (C_FILTER_ID_ONLY.equals(filter)) {
            return ContentFilterType.ID_ONLY;
        }

        throw new MyLeadException("UNknown c-filter type: it should be [" + C_FILTER_FULL_SCHEMA + " or "
                + C_FILTER_ID_ONLY + "] but got :" + filter);

    }

    /**
     * @param filter
     * @return
     * @throws MyLeadException
     */
    public static edu.indiana.dde.metadata.catalog.types.HierarchyFilterType.Enum getHFilter(String filter)
            throws MyLeadException {
        if (H_FILTER_CHILDREN.equals(filter)) {
            return edu.indiana.dde.metadata.catalog.types.HierarchyFilterType.CHILDREN;
        } else if (H_FILTER_SUBTREE.equals(filter)) {
            return edu.indiana.dde.metadata.catalog.types.HierarchyFilterType.SUBTREE;
        } else if (H_FILTER_TARGET.equals(filter)) {
            return edu.indiana.dde.metadata.catalog.types.HierarchyFilterType.TARGET;
        }
        throw new MyLeadException("UNknown h-filter type: it should be [" + H_FILTER_CHILDREN + " or "
                + H_FILTER_SUBTREE + " or" + H_FILTER_TARGET + "] but got :" + filter);

    }

}