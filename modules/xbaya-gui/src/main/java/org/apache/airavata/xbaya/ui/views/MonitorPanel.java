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
package org.apache.airavata.xbaya.ui.views;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.airavata.common.utils.BrowserLauncher;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.messaging.EventData;
import org.apache.airavata.xbaya.messaging.EventDataRepository;
import org.apache.airavata.xbaya.messaging.Monitor;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.monitor.MonitorWindow;
import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler;
import org.apache.airavata.xbaya.ui.widgets.XBayaComponent;
import org.xmlpull.infoset.XmlElement;

public class MonitorPanel implements XBayaComponent, TableModelListener {

    /**
     * The title.
     */
    public static final String TITLE = "Monitoring";

    private XBayaGUI xbayaGUI;

    private EventDataRepository tableSliderModel;

    private JTable table;

    private JScrollPane scrollPane;

    private JSlider slider;

    private JPanel panel;

    /**
     * 
     * Constructs a MonitorPanel.
     * 
     * @param xbayaGUI
     * @param monitor
     */
    public MonitorPanel(XBayaGUI xbayaGUI, Monitor monitor) {
        this(xbayaGUI, null, monitor);
    }

    /**
     * Constructs a NotificationPane.
     * 
     * @param xbayaGUI The XBayaEngine.
     */
    public MonitorPanel(XBayaGUI xbayaGUI, String nodeID, Monitor monitor) {
        this.xbayaGUI=xbayaGUI;
        if (null == nodeID) {
            this.tableSliderModel = monitor.getEventDataRepository();
        } else {
            this.tableSliderModel = monitor.getEventData(nodeID);
        }
        init();
        this.tableSliderModel.addTableModelListener(this);

        // Also create a handler to change colors of graphs here.
        MonitorEventHandler eventHandler = new MonitorEventHandler(this.xbayaGUI);
        this.tableSliderModel.addChangeListener(eventHandler);
    }

    /**
     * Returns the scroll pane.
     * 
     * @return The scroll pane
     */
    public JPanel getSwingComponent() {
        return this.panel;
    }

    /**
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(final TableModelEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tableChangedInSwingThread(event);
            }
        });

    }

    private void tableChangedInSwingThread(TableModelEvent event) {
        // logger.entering(event);
        // logger.entering("firstRow: " + event.getFirstRow());
        // logger.entering("lastRow: " + event.getLastRow());
        int type = event.getType();
        if (type == TableModelEvent.INSERT) {
            int firstRow = event.getFirstRow();

            // check if we need to scroll down the scroll bar.
            boolean move = false;

            Rectangle visibleRect = this.table.getVisibleRect();
            // logger.info("visibleRect: " + visibleRect);
            Rectangle bottomCellRect = this.table.getCellRect(firstRow - 1, 0, true);
            // logger.info("bottomCellRect: " + bottomCellRect);
            move = bottomCellRect.intersects(visibleRect);
            // logger.info("move: " + move);

            if (move) {
                // When the scroll bar is at the bottom and the slider is at the
                // right, scroll down.
                // If the slider is not at the right, this method won't be
                // called.
                Rectangle newBottomCellRect = this.table.getCellRect(this.table.getRowCount() - 1, 0, true);
                // logger.info("cellRect: " + newBottomCellRect);
                this.table.scrollRectToVisible(newBottomCellRect);
            }

            // Sometimes, it fails to reprint without this.
            this.scrollPane.repaint();

        } else if (type == TableModelEvent.DELETE) {
            // Nothing
        } else {
            // No other cases.
        }

        int size = this.tableSliderModel.getEventSize();
        boolean empty = (size == 0);
        this.slider.setEnabled(!empty);
    }

    private void init() {
        this.table = new JTable(this.tableSliderModel) {
            @Override
            public String getToolTipText(MouseEvent event) {
                String tip = null;
                Point point = event.getPoint();
                int colIndex = columnAtPoint(point);
                if (colIndex == EventDataRepository.Column.MESSAGE.ordinal()) {
                    tip = "Double click here to see the full message.";
                }
                return tip;
            }
        };

        this.table.getTableHeader().setReorderingAllowed(false);

        this.table.addMouseListener(new MouseInputAdapter() {
            private MonitorWindow window;

            @Override
            public void mouseClicked(MouseEvent event) {
                Point point = event.getPoint();
                int row = MonitorPanel.this.table.rowAtPoint(point);
                if (row >= 0 && row < MonitorPanel.this.table.getRowCount()) {
                    EventData message = MonitorPanel.this.tableSliderModel.getEvent(row);

                    int clickCount = event.getClickCount();
                    if (clickCount == 1) {
   /*                     if (MonitorUtil.getType(message) == MonitorUtil.EventType.PUBLISH_URL) {
                            int column = MonitorPanel.this.table.columnAtPoint(point);
                            if (column == EventDataRepository.Column.MESSAGE.ordinal()) {
                                String url = MonitorUtil.getLocation(message);
                                try {
                                    BrowserLauncher.openURL(url);
                                } catch (Exception e) {
                                    MonitorPanel.this.xbayaGUI.getErrorWindow().error(e.getMessage(), e);
                                }
                            }
                        } else if (MonitorUtil.getType(message) == MonitorUtil.EventType.SENDING_RESULT) {
                            if (null != message && null != message.element("result")
                                    && null != message.element("result").element("body")
                                    && null != message.element("result").element("body").element("Body")) {
                                XmlElement body = message.element("result").element("body").element("Body");
                                Iterator bodyItr = body.children().iterator();
                                // find the first body Element
                                findAndLaunchBrowser(bodyItr);
                                // XmlElement output = message.element("result").element("body").
                                // element("Body").element("Visualize_OutputParams").element("Visualized_Output");
                                // Iterator children = output.children().iterator();
                                // while (children.hasNext()) {
                                // Object object = (Object) children.next();
                                // if(object instanceof String){
                                // try {
                                // new URL(((String)object).trim());
                                // try {
                                // BrowserLauncher.openURL(((String)object).trim());
                                // } catch (Throwable e) {
                                // //do nothing
                                // }
                                // } catch (MalformedURLException e) {
                                // //do nothing
                                // }
                                // }
                                //
                                // }
                            }
                        }*/
                    } else if (clickCount >= 2) {
                        // Handle double clicks to pop up a window.
                        if (this.window == null) {
                            this.window = new MonitorWindow(MonitorPanel.this.xbayaGUI);
                        }
                        this.window.show(message);
                    }
                }
            }

            /**
             * @param bodyItr
             */
            private void findAndLaunchBrowser(Iterator bodyItr) {
                if (bodyItr.hasNext()) {
                    Object firstElement = bodyItr.next();
                    if (firstElement instanceof XmlElement) {
                        findAndLuanchBrowser((XmlElement) firstElement);
                    }
                }
            }

            /**
             * @param firstElement
             */
            private void findAndLuanchBrowser(XmlElement firstElement) {
                Iterator children = ((XmlElement) firstElement).children().iterator();
                while (children.hasNext()) {
                    Object object = (Object) children.next();
                    if (object instanceof String) {
                        try {
                            new URL(((String) object).trim());
                            try {
                                BrowserLauncher.openURL(((String) object).trim());
                            } catch (Throwable e) {
                                // do nothing
                            }
                        } catch (MalformedURLException e) {
                            // do nothing
                        }
                    } else if (object instanceof XmlElement) {
                        findAndLuanchBrowser((XmlElement) object);
                    }
                }
            }
        });

        // Adjust size of columns
        TableColumnModel columnModel = this.table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            if (i == columnCount - 1) {
                column.setPreferredWidth(500);
            } else {
                column.setPreferredWidth(50);
            }
        }
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        this.scrollPane = new JScrollPane(this.table);
        this.scrollPane.setMinimumSize(SwingUtil.MINIMUM_SIZE);
        this.scrollPane.setDoubleBuffered(true);

        this.slider = new JSlider(this.tableSliderModel);
        this.slider.setSnapToTicks(true);
        this.slider.setEnabled(false);

        this.panel = new JPanel();
        this.panel.setLayout(new BorderLayout());
        this.panel.add(this.scrollPane, BorderLayout.CENTER);
        this.panel.add(this.slider, BorderLayout.SOUTH);
    }
}