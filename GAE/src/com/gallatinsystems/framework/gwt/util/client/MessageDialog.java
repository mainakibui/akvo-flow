/*
 *  Copyright (C) 2010-2012 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package com.gallatinsystems.framework.gwt.util.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * simple utility class for showing a message dialog in GWT that consists of a
 * header and a body that is html
 * 
 * @author Christopher Fagiani
 * 
 */
public class MessageDialog extends DialogBox {
	private static FrameworkTextConstants TEXT_CONSTANTS = GWT
	.create(FrameworkTextConstants.class);
	private HTML content;
	private DockPanel dock;

	public MessageDialog(String title, String bodyHtml, boolean suppressButton,
			final ClickHandler okHandler) {
		this(title, bodyHtml, suppressButton, okHandler, null);
	}

	public MessageDialog(String title, String bodyHtml, boolean suppressButton,
			final ClickHandler okHandler, final ClickHandler cancelHandler) {
		setText(title);
		dock = new DockPanel();
		content = new HTML(bodyHtml);

		dock.add(content, DockPanel.CENTER);
		if (!suppressButton) {
			HorizontalPanel hp = new HorizontalPanel();
			Button ok = new Button(TEXT_CONSTANTS.ok());

			ok.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					MessageDialog.this.hide();
					if (okHandler != null) {
						okHandler.onClick(event);
					}
				}
			});
			hp.add(ok);
			// only add Cancel button if there is a click handler
			if (okHandler != null) {
				Button cancel = new Button(TEXT_CONSTANTS.cancel());
				hp.add(cancel);
				cancel.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						MessageDialog.this.hide();
						if (cancelHandler != null) {
							cancelHandler.onClick(event);
						}
					}
				});
			}
			dock.add(hp, DockPanel.SOUTH);
		}
		setWidget(dock);
	}

	public MessageDialog(String title, String bodyHtml, boolean suppressButton) {
		this(title, bodyHtml, suppressButton, null);
	}

	public MessageDialog(String title, String bodyHtml) {
		this(title, bodyHtml, false);
	}

	public void showCentered() {
		setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = ((Window.getScrollLeft()+Window.getClientWidth() - offsetWidth) / 2) >> 0;
				int top = ((Window.getScrollTop()+Window.getClientHeight() - offsetHeight) / 2) >> 0;
				setPopupPosition(left, top);
			}
		});
	}
}
