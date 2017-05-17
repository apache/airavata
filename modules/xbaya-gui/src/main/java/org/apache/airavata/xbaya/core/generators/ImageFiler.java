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
package org.apache.airavata.xbaya.core.generators;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageFiler {

    private static Logger logger = LoggerFactory.getLogger(ImageFiler.class);

    private XBayaEngine engine;

    private JFileChooser pngFileChooser;

    private final FileFilter pngFileFilter = new FileFilter() {
        @Override
        public String getDescription() {
            return "PNG Files";
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String name = file.getName();
            if (name.endsWith(XBayaConstants.PNG_SUFFIX)) {
                return true;
            }
            return false;
        }
    };

    /**
     * Constructs an ImageFiler.
     * 
     * @param engine
     */
    public ImageFiler(XBayaEngine engine) {
        this.engine = engine;

        this.pngFileChooser = new JFileChooser();
        this.pngFileChooser.addChoosableFileFilter(this.pngFileFilter);
    }

    /**
     * Save the workflow image to the local file
     */
    public void saveWorkflowImage() {
        int returnVal = this.pngFileChooser.showSaveDialog(this.engine.getGUI().getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = this.pngFileChooser.getSelectedFile();
            logger.debug(file.getPath());

            // Put ".png" at the end of the file name
            String path = file.getPath();
            if (!path.endsWith(XBayaConstants.PNG_SUFFIX)) {
                file = new File(path + XBayaConstants.PNG_SUFFIX);
            }

            BufferedImage image;
            try {
                image = this.engine.getGUI().getWorkflow().getImage();
                ImageIO.write(image, XBayaConstants.PNG_FORMAT_NAME, file);
            } catch (IOException e) {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.WRITE_FILE_ERROR, e);
            } catch (RuntimeException e) {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (Error e) {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            }
        }
    }

}