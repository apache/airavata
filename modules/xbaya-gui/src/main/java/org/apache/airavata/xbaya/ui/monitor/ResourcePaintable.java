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
package org.apache.airavata.xbaya.ui.monitor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.apache.airavata.xbaya.ui.graph.Paintable;

public class ResourcePaintable implements Paintable {

    private String resource;

    private String retryCount;

    private String displayText;

    /**
     * Constructs a ResourcePaintable.
     * 
     * @param resource
     * @param retryCount
     */
    public ResourcePaintable(String resource, String retryCount) {
        this.resource = resource;
        this.retryCount = retryCount;

        this.displayText = "";
        if (this.resource != null) {
            this.displayText += this.resource;
        }
        if (this.retryCount != null && this.retryCount.length() > 0 && !"0".equals(this.retryCount)) {
            this.displayText += "/" + this.retryCount;
        }
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.Paintable#paint(java.awt.Graphics2D, java.awt.Point)
     */
    public void paint(Graphics2D graphics, Point point) {
        graphics.setColor(Color.BLACK);
        graphics.drawString(this.displayText, point.x, point.y - 10);
    }

}