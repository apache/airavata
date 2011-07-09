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

package org.apache.airavata.xbaya.gpel;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.gpel.client.GcDefaultSupportedLinksFilter;
import org.gpel.client.GcLink;

public class GPELLinksFilter extends GcDefaultSupportedLinksFilter {

    /**
     * REL_XWF
     */
    public final static String REL_XWF = GraphSchema.NS_URI_XGR;

    /**
     * REL_IMAGE
     */
    public final static String REL_IMAGE = XBayaConstants.NS_URI_XBAYA + "image";

    /**
     * @see org.gpel.client.GcDefaultSupportedLinksFilter#accept(org.gpel.client.GcLink)
     */
    @Override
    public boolean accept(GcLink link) {
        String rel = link.getRel();
        if (rel.equals(REL_XWF)) {
            return true;
        } else if (rel.equals(REL_IMAGE)) {
            return true;
        } else {
            return super.accept(link);
        }
    }
}