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

package org.waterforpeople.mapping.portal.client.widgets.component;

import java.util.ArrayList;
import java.util.List;

import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;

import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.notification.app.gwt.client.NotificationSubscriptionDto;
import com.gallatinsystems.notification.app.gwt.client.NotificationSubscriptionService;
import com.gallatinsystems.notification.app.gwt.client.NotificationSubscriptionServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog box used for setting up notification subscriptions
 * 
 * @author Christopher Fagiani
 * 
 */
public class NotificationSubscriptionDialog extends DialogBox {

	private static TextConstants TEXT_CONSTANTS = GWT
			.create(TextConstants.class);
	private Long entityId;
	private NotificationSubscriptionServiceAsync notifService;
	private CompletionListener listener;
	private Label loadingLabel;
	private Label validationLabel;
	private DockPanel contentPane;
	private FlexTable subscriptionTable;
	private boolean enabled;
	private String type;
	private List<NotificationSubscriptionDto> currentDtoList;
	private Panel mainContent;

	/**
	 * instantiates and displays the dialog box and displays any existing
	 * subscription notifications
	 * 
	 * @param dto
	 * @param listener
	 */
	public NotificationSubscriptionDialog(Long entityId, String type,
			CompletionListener listener) {
		setText(TEXT_CONSTANTS.editSubscriptions());
		this.type = type;
		this.entityId = entityId;
		enabled = true;

		setAnimationEnabled(true);
		setGlassEnabled(true);

		this.listener = listener;
		notifService = GWT.create(NotificationSubscriptionService.class);
		loadingLabel = new Label(TEXT_CONSTANTS.loading());
		contentPane = new DockPanel();
		setPopupPosition(Window.getClientWidth() / 4,
				Window.getClientHeight() / 4);
		contentPane.add(loadingLabel, DockPanel.CENTER);

		HorizontalPanel buttonPanel = new HorizontalPanel();

		Button ok = new Button(TEXT_CONSTANTS.save());
		Button cancel = new Button(TEXT_CONSTANTS.close());
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		contentPane.add(buttonPanel, DockPanel.SOUTH);
		ok.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				saveSubscriptions();
			}
		});
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		setWidget(contentPane);
		loadData();
	}

	private void loadData() {
		notifService.listSubscriptions(entityId, type,
				new AsyncCallback<List<NotificationSubscriptionDto>>() {

					@Override
					public void onSuccess(
							List<NotificationSubscriptionDto> result) {
						currentDtoList = result;
						buildContent(result);
						contentPane.remove(loadingLabel);
						contentPane.add(mainContent, DockPanel.CENTER);
					}

					@Override
					public void onFailure(Throwable caught) {
						loadingLabel.setText(TEXT_CONSTANTS.errorTracePrefix()
								+ " " + caught.getLocalizedMessage());
					}
				});
	}

	private void saveSubscriptions() {
		enabled = false;
		boolean allValid = true;
		if (validationLabel != null) {
			validationLabel.removeFromParent();
		}
		if (subscriptionTable != null && subscriptionTable.getRowCount() > 0) {
			List<NotificationSubscriptionDto> subDtos = new ArrayList<NotificationSubscriptionDto>();
			for (int i = 0; i < subscriptionTable.getRowCount(); i++) {
				NotificationSubscriptionDto dto = ((NotificationSubscriptionWidget) subscriptionTable
						.getWidget(i, 0)).getValue();
				dto.setEntityId(entityId);
				if (type != null) {
					dto.setNotificationType(type);
				}
				subDtos.add(dto);
				if (!dto.validate()) {
					allValid = false;
				}
			}

			if (allValid) {
				notifService.saveSubscriptions(subDtos,
						new AsyncCallback<List<NotificationSubscriptionDto>>() {

							@Override
							public void onFailure(Throwable caught) {
								enabled = true;
							}

							@Override
							public void onSuccess(
									List<NotificationSubscriptionDto> result) {
								enabled = true;
								hide();
								notifyListeners();
							}
						});
			} else {
				validationLabel = new Label(
						TEXT_CONSTANTS.dateAndEmailMandatory());
				mainContent.add(validationLabel);
			}
		} else {
			hide();
		}

	}

	/**
	 * builds the UI.
	 * 
	 * @return
	 */
	private Widget buildContent(
			List<NotificationSubscriptionDto> notificationSubList) {
		if (mainContent == null) {
			mainContent = new VerticalPanel();
		} else {
			mainContent.clear();
		}

		if (subscriptionTable != null) {
			subscriptionTable.clear(true);
			subscriptionTable.removeAllRows();
		} else {
			subscriptionTable = new FlexTable();
		}
		if (notificationSubList != null) {
			int count = 0;
			for (NotificationSubscriptionDto dto : notificationSubList) {
				createRow(count, dto);
				count++;
			}
		}
		mainContent.add(subscriptionTable);
		HorizontalPanel buttonPanel = new HorizontalPanel();
		Button addButton = new Button(TEXT_CONSTANTS.add());
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				int idx = subscriptionTable.getRowCount();
				NotificationSubscriptionDto dto = new NotificationSubscriptionDto();
				currentDtoList.add(dto);
				dto.setEntityId(entityId);
				createRow(idx, dto);
			}
		});
		buttonPanel.add(addButton);
		mainContent.add(buttonPanel);

		return mainContent;
	}

	private void createRow(int idx, final NotificationSubscriptionDto dto) {
		subscriptionTable.insertRow(idx);
		subscriptionTable.setWidget(idx, 0, new NotificationSubscriptionWidget(
				dto));
		Button delButton = new Button(TEXT_CONSTANTS.remove());
		delButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (dto.getKeyId() == null) {
					// if null, we don't need to delete on the server, just
					// remove from the UI
					currentDtoList.remove(dto);
					buildContent(currentDtoList);
				} else {
					notifService.deleteSubscription(dto,
							new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									// no-op
								}

								@Override
								public void onSuccess(Void result) {
									currentDtoList.remove(dto);
									buildContent(currentDtoList);
								}
							});
				}
			}
		});
		subscriptionTable.setWidget(idx, 1, delButton);

	}

	private void notifyListeners() {
		if (listener != null) {
			listener.operationComplete(true, null);
		}
	}

	/**
	 * allow the user to press escape to close
	 */
	@Override
	public boolean onKeyDownPreview(char key, int modifiers) {
		if (enabled) {
			switch (key) {
			case KeyCodes.KEY_ESCAPE:
				hide();
				return true;
			}
		}
		return false;

	}

}
