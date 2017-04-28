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
package org.apache.airavata.xbaya.messaging;

import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EventDataRepository implements TableModel, BoundedRangeModel {

    /**
     * Column
     */
    public static enum Column {
        /**
         * TIME
         */
        TIME("Time"),
        /**
         * ID
         */
        ID("Component"),
        /**
         * STATUS
         */
        STATUS("Status"),
        /**
         * MESSAGE
         */
        MESSAGE("Message");

        private String name;

        private Column(String name) {
            this.name = name;
        }

        /**
         * @return The name.
         */
        public String getName() {
            return this.name;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(EventDataRepository.class);

    private List<TableModelListener> tableModelListeners;

    private List<ChangeListener> sliderModelListeners;

    private List<EventData> events;

    private int sliderValue;

    private boolean sliderAdjusting;

    private ChangeEvent tableModelChangeEvent;

    private List<EventDataListener> monitorEventListerners;

    /**
     *
     * Constructs a MonitorEventData.
     *
     */
    public EventDataRepository() {
        this.tableModelListeners = new LinkedList<TableModelListener>();
        this.sliderModelListeners = new LinkedList<ChangeListener>();
        this.tableModelChangeEvent = new ChangeEvent(this); // We only need one.
        this.events = new ArrayList<EventData>();
    }

    /**
     * @param event
     */
    public void addEvent(EventData event) {
        // no need the check for not null because second clause is evaluated only if
        // not null
            boolean sliderMax = (this.sliderValue == this.events.size());

            this.events.add(event);

            if (sliderMax) {
                // Move the slider to the max
                this.sliderValue = this.events.size();

                // The new event shows up on the table only when the slider is
                // max.
                TableModelEvent tableEvent = new TableModelEvent(this, this.sliderValue - 1, this.sliderValue,
                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
                fireTableChanged(tableEvent);
            }

            // The muxmum of the slider changed regardless whether we move the
            // slider or not.
            if (event.getType() == MessageType.WORKFLOWNODE) {
                this.tableModelChangeEvent = new ChangeEvent(event);
                fireSliderChanged();
            }
            triggerListenerForMonitorEvent(event);

    }
    public void triggerListenerForPreMonitorStart() {
		for (EventDataListener listener : getMonitorEventListerners()) {
			try {
				listener.monitoringPreStart();
			} catch (Exception e) {
                logger.error(e.getMessage(), e);
			}
		}
	}

    public void triggerListenerForPostMonitorStart() {
		for (EventDataListener listener : getMonitorEventListerners()) {
			try {
				listener.monitoringPostStart();
			} catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
		}
	}

    public void triggerListenerForPreMonitorStop() {
		for (EventDataListener listener : getMonitorEventListerners()) {
			try {
				listener.monitoringPreStop();
			} catch (Exception e) {
                logger.error(e.getMessage(), e);
			}
		}
	}

    public void triggerListenerForPostMonitorStop() {
		for (EventDataListener listener : getMonitorEventListerners()) {
			try {
				listener.monitoringPostStop();
			} catch (Exception e) {
                logger.error(e.getMessage(), e);
			}
		}
	}
	private void triggerListenerForMonitorEvent(EventData event) {
//		for (EventDataListener listener : getMonitorEventListerners()) {
//			try {
//				listener.notify(this, event);
//				if (event.getType()==EventType.WORKFLOW_TERMINATED){
//					listener.onCompletion(event);
//				}else if (event.getType()==EventType.SENDING_FAULT){
//					listener.onFail(event);
//				}
//			} catch (Exception e) {
//				//just in case
//				e.printStackTrace();
//			}
//		}
	}

    /**
     * @return All events.
     */
    public List<EventData> getEvents() {
        return this.events;
    }

    /**
     * Returns a notification at a specified row.
     *
     * @param index
     *            The specified row.
     * @return The notification at the specified row
     */
    public EventData getEvent(int index) {
        return this.events.get(index);
    }

    /**
     * @return The number of events.
     */
    public int getEventSize() {
        return this.events.size();
    }

    /**
     * Clears the notifications.
     */
    public void removeAllEvents() {
        int size = this.events.size();
        this.events.clear();

        this.sliderValue = 0;

        TableModelEvent event = new TableModelEvent(this, 0, Math.max(0, size - 1), TableModelEvent.ALL_COLUMNS,
                TableModelEvent.DELETE);
        fireTableChanged(event);

        // The muxmum of the slider changed.
        fireSliderChanged();
    }

    // methods implementing TableModel interface.

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        // Only show the events up to the slider value.
        return this.sliderValue;
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return Column.values().length;
    }

    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
        Column[] columns = Column.values();
        if (columnIndex < 0 || columnIndex >= columns.length) {
            // Should not happen.
            throw new IllegalArgumentException("columnIndex has be be between 0 to " + columns.length);
        }
        return columns[columnIndex].getName();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        String value;
        try {
            EventData event = this.events.get(rowIndex);
            value = getTextAt(event, columnIndex);
        } catch (RuntimeException e) {
            // This should not happen, but if it happens it blocks the UI.
            // That's why catching it.
            logger.error(e.getMessage(), e);
            value = "Error";
        }
        return value;
    }

    /**
     * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
     */
    public void addTableModelListener(TableModelListener listener) {
        this.tableModelListeners.add(listener);
    }

    /**
     * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
     */
    public void removeTableModelListener(TableModelListener listener) {
        this.tableModelListeners.remove(listener);
    }

    // methods implementing BoundedRangeModel interface.

    /**
     * @see javax.swing.BoundedRangeModel#getExtent()
     */
    public int getExtent() {
        return 0;
    }

    /**
     * @see javax.swing.BoundedRangeModel#setExtent(int)
     */
    public void setExtent(int newExtent) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.BoundedRangeModel#getMaximum()
     */
    public int getMaximum() {
        return getEventSize();
    }

    /**
     * @see javax.swing.BoundedRangeModel#setMaximum(int)
     */
    public void setMaximum(int newMaximum) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.BoundedRangeModel#getMinimum()
     */
    public int getMinimum() {
        return 0;
    }

    /**
     * @see javax.swing.BoundedRangeModel#setMinimum(int)
     */
    public void setMinimum(int newMinimum) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.BoundedRangeModel#getValue()
     */
    public int getValue() {
        return this.sliderValue;
    }

    /**
     * @see javax.swing.BoundedRangeModel#setValue(int)
     */
    public void setValue(int newValue) {
        if (this.sliderValue == newValue) {
            return;
        }

        // Correct the value to be withing the range.
        if (newValue < 0) {
            newValue = 0;
        }
        if (newValue > this.events.size()) {
            newValue = this.events.size();
        }

        int oldRowCount = this.sliderValue;
        this.sliderValue = newValue;

        TableModelEvent event;
        if (oldRowCount < this.sliderValue) {
            event = new TableModelEvent(this, oldRowCount, this.sliderValue, TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.INSERT);
        } else {
            event = new TableModelEvent(this, this.sliderValue, oldRowCount, TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.DELETE);
        }
        fireTableChanged(event);
        fireSliderChanged();
    }

    /**
     * @see javax.swing.BoundedRangeModel#getValueIsAdjusting()
     */
    public boolean getValueIsAdjusting() {
        return this.sliderAdjusting;
    }

    /**
     * @see javax.swing.BoundedRangeModel#setValueIsAdjusting(boolean)
     */
    public void setValueIsAdjusting(boolean adjusting) {
        this.sliderAdjusting = adjusting;
        fireSliderChanged();
    }

    /**
     * @see javax.swing.BoundedRangeModel#setRangeProperties(int, int, int, int, boolean)
     */
    public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.swing.BoundedRangeModel#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener listener) {
        this.sliderModelListeners.add(listener);
    }

    /**
     * @see javax.swing.BoundedRangeModel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener) {
        this.sliderModelListeners.remove(listener);
    }

    private String getTextAt(EventData event, int columnIndex) {
        Column[] columns = Column.values();
        if (columnIndex < 0 || columnIndex >= columns.length) {
            // Should not happen.
            throw new IllegalArgumentException("columnIndex has be be between 0 to " + columns.length);
        }
        Column column = columns[columnIndex];
        String value;
        switch (column) {
        case TIME:
            value = event.getUpdateTime().toString();
            break;
        case ID:
            value = event.getMessageId();
            break;
        case STATUS:
            value = event.getStatus();
            break;
        case MESSAGE:
            value = event.getMessage();
            break;
        default:
            // Should not happen.
            throw new IllegalArgumentException("columnIndex has be be between 0 to " + columns.length);
        }
        return value;
    }

    private void fireTableChanged(TableModelEvent event) {
        for (TableModelListener listener : this.tableModelListeners) {
            listener.tableChanged(event);
        }
    }

    private void fireSliderChanged() {
        for (ChangeListener listener : this.sliderModelListeners) {
            listener.stateChanged(this.tableModelChangeEvent);
        }
    }
	private List<EventDataListener> getMonitorEventListerners() {
		if (monitorEventListerners==null){
			monitorEventListerners=new ArrayList<EventDataListener>();
		}
		return monitorEventListerners;
	}

	public void registerEventListener(EventDataListener listener){
		if (listener!=null) {
			getMonitorEventListerners().add(listener);
		}
	}
	
	public void unregisterEventListener(EventDataListener listener){
		if (getMonitorEventListerners().contains(listener)) {
			getMonitorEventListerners().remove(listener);
		}
	}


    public void fireNewWorkflowStart(String workflowId) {
        for (ChangeListener sliderModelListener : sliderModelListeners) {
            sliderModelListener.stateChanged(new ChangeEvent(workflowId));
        }
    }
}