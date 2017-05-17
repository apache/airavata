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
package org.apache.airavata.xbaya.ui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JButton;

public class ToolbarButton extends JButton implements ActionListener, MouseListener{
	private ActionListener buttonClickListener;
	private static final long serialVersionUID = -8266744670729158206L;
	private boolean showCaption=false;
	private String caption;
	private int order;
	
	public ToolbarButton(Icon icon, String text, String description, int order) {
		super(icon);
		setOrder(order);
		setToolTipText(description);
		setCaption(text);
		setBorderPainted(false);
        setFocusable(false);
//        setBorder(BorderFactory.create);
        setRolloverEnabled(true);
        addMouseListener(this);
        addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getButtonClickListener()!=null){
			getButtonClickListener().actionPerformed(e);
		}
	}

	public ActionListener getButtonClickListener() {
		return buttonClickListener;
	}

	public void setButtonClickListener(ActionListener buttonClickListener) {
		this.buttonClickListener = buttonClickListener;
	}

	public boolean isShowCaption() {
		return showCaption;
	}

	public void setShowCaption(boolean showCaption) {
		this.showCaption = showCaption;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		if (isEnabled()) {
			setBorderPainted(true);
		}			
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setBorderPainted(false);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
