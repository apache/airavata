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

package org.apache.airavata.xbaya.streaming;

import java.net.MalformedURLException;

import javax.swing.table.AbstractTableModel;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.streaming.StreamServiceStub.StreamDescription;

public class StreamTableModel extends AbstractTableModel {

    private String[] columnNames = { "", "Stream", "Rate", "Last evetn Time", "message" };
    private Object[][] data;
    private XBayaEngine engine;
    private StreamDescription[] streams;

    public StreamTableModel(XBayaEngine engine) {
        this.engine = engine;

    }

    /**
     * @throws XregistryException
     * @throws MalformedURLException
     */
    public void init() {

        try {
            StreamServiceStub stub = new StreamServiceStub(XBayaConstants.STREAM_SERVER);
            streams = stub.getStreams(50);

            this.data = new Object[streams.length][columnNames.length];
            for (int i = 0; i < streams.length; i++) {
                this.data[i][0] = i;
                this.data[i][1] = streams[i].getStreamName();
                this.data[i][2] = streams[i].getRate().replace(':', '|');
                this.data[i][3] = streams[i].getLastEventTimestamp();
                this.data[i][4] = streams[i].getMessage();

            }
        } catch (Exception e) {
            engine.getErrorWindow().error(e);
        }

    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        // TODO Auto-generated method stub
        return columnNames.length;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        // TODO Auto-generated method stub
        return data.length;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // TODO Auto-generated method stub
        return data[rowIndex][columnIndex];
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * @param row
     */
    public void stopStream(int row) {
        // TODO Auto-generated method stub

    }

    /**
     * @param row
     * @return
     */
    public String getStream(int row) {
        return (String) this.data[row][1];
    }

    /**
     * @param row
     * @return
     */
    public String getStreamRate(int row) {
        return (String) this.data[row][2];
    }

    /**
     * @param newStreamName
     */
    public String getRate(String newStreamName) {
        for (int i = 0; i < data.length; i++) {
            if (this.data[i][1].equals(newStreamName)) {
                return (String) this.data[i][2];
            }
        }
        return "";

    }

}