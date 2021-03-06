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

package org.waterforpeople.mapping.surveyentry.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationDto;
import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationService;
import org.waterforpeople.mapping.app.gwt.client.auth.WebActivityAuthorizationServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;
import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;
import org.waterforpeople.mapping.surveyentry.client.component.SurveyEntryWidget;
import org.waterforpeople.mapping.surveyentry.client.component.WebSurveySelectorDialog;

import com.gallatinsystems.framework.gwt.dto.client.BaseDto;
import com.gallatinsystems.framework.gwt.dto.client.ResponseDto;
import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Web-based client for responding to surveys. This page expects the surveyId as
 * a query parameter (sid).
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyEntryClient implements EntryPoint, CompletionListener {
	protected static TextConstants TEXT_CONSTANTS = GWT
			.create(TextConstants.class);
	private static final String MESSAGE_STYLE = "message";
	private static final String TOKEN_PARAM = "token";
	private static final String ACTIVITY_NAME = "WebSurvey";
	private SurveyEntryWidget entryWidget;
	private WebActivityAuthorizationServiceAsync authService;
	private WebActivityAuthorizationDto currentAuth;
	private Panel thankYouPanel;
	private Button submitAnotherButton;

	@Override
	public void onModuleLoad() {

		authService = GWT.create(WebActivityAuthorizationService.class);
		thankYouPanel = new VerticalPanel();
		Label thanks = new Label(TEXT_CONSTANTS.thankYouMessage());
		thanks.setStylePrimaryName(MESSAGE_STYLE);
		thankYouPanel.add(thanks);
		submitAnotherButton = new Button(TEXT_CONSTANTS.submitAnother());
		submitAnotherButton.setVisible(false);
		thankYouPanel.add(submitAnotherButton);
		submitAnotherButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				thankYouPanel.removeFromParent();
				entryWidget.setVisible(true);
				submitAnotherButton.setVisible(false);
				RootPanel.get().add(entryWidget);				
			}
		});
		String token = Window.Location.getParameter(TOKEN_PARAM);
		RootPanel.get().setPixelSize(1024, 768);
		RootPanel.get().getElement().getStyle()
				.setProperty("position", "relative");
		RootPanel.get().add(new Image("images/wfp-logo.gif"));
		final MessageDialog authDia = new MessageDialog(
				TEXT_CONSTANTS.authenticating(),
				TEXT_CONSTANTS.validatingAuth(), true);
		authDia.showCentered();
		if (token != null) {
			authService.isAuthorized(token, ACTIVITY_NAME,
					new AsyncCallback<WebActivityAuthorizationDto>() {

						@Override
						public void onSuccess(WebActivityAuthorizationDto result) {
							authDia.hide();
							if (result != null) {

								authorizationComplete(result);
							} else {
								authorizationFailed();
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							MessageDialog dia = new MessageDialog(
									TEXT_CONSTANTS.error(), TEXT_CONSTANTS
											.errorTracePrefix()
											+ " "
											+ caught.getLocalizedMessage());
							dia.showCentered();
						}
					});
		} else if (Window.Location.getPath() != null
				&& Window.Location.getPath().toLowerCase().contains("secure")) {
			authService
					.listUserAuthorizations(
							ACTIVITY_NAME,
							new AsyncCallback<ResponseDto<HashMap<BaseDto, WebActivityAuthorizationDto>>>() {

								@Override
								public void onFailure(Throwable caught) {
									MessageDialog dia = new MessageDialog(
											TEXT_CONSTANTS.error(),
											TEXT_CONSTANTS.errorTracePrefix()
													+ " "
													+ caught.getLocalizedMessage());
									dia.showCentered();
								}

								@Override
								public void onSuccess(
										final ResponseDto<HashMap<BaseDto, WebActivityAuthorizationDto>> result) {
									authDia.hide();
									if (result.getPayload() != null
											&& result.getPayload().size() > 0) {
										if (result.getPayload().size() > 1) {
											ArrayList<SurveyDto> surveys = new ArrayList<SurveyDto>();
											for (BaseDto d : result
													.getPayload().keySet()) {
												if (d instanceof SurveyDto) {
													surveys.add((SurveyDto) d);
												}
											}
											WebSurveySelectorDialog selector = new WebSurveySelectorDialog(
													surveys,
													new CompletionListener() {

														@Override
														public void operationComplete(
																boolean wasSuccessful,
																Map<String, Object> payload) {
															SurveyDto s = (SurveyDto) payload
																	.get(WebSurveySelectorDialog.SELECTED_SURVEY_KEY);
															if (s != null) {
																authorizationComplete(result
																		.getPayload()
																		.get(s));
															}

														}
													});
											selector.showCentered();
										} else {
											for (Entry<BaseDto, WebActivityAuthorizationDto> e : result
													.getPayload().entrySet()) {
												if (e.getValue() != null) {
													authorizationComplete(e
															.getValue());
													break;
												}
											}
										}
									} else {
										MessageDialog dia = new MessageDialog(
												TEXT_CONSTANTS.noSurveys(),
												TEXT_CONSTANTS.noSurveyAccess());
										dia.showCentered();
									}
								}
							});
		}
	}

	private void authorizationComplete(WebActivityAuthorizationDto auth) {
		currentAuth = auth;

		String submitter = null;
		if (currentAuth != null) {
			submitter = currentAuth.getUserName();
			if (submitter == null) {
				submitter = currentAuth.getToken();
			}
		}

		entryWidget = new SurveyEntryWidget(auth.getPayload(), submitter);
		entryWidget.setListener(this);
		RootPanel.get().add(entryWidget);
		entryWidget.initialize();
	}

	private void authorizationFailed() {
		Label l = new Label(TEXT_CONSTANTS.authFailed());
		RootPanel.get().add(l);
	}

	@Override
	public void operationComplete(boolean wasSuccessful,
			Map<String, Object> payload) {
		entryWidget.setVisible(false);
		entryWidget.removeFromParent();
		RootPanel.get().add(thankYouPanel);
		if (currentAuth != null) {
			Long count = currentAuth.getUsageCount();
			if (count == null) {
				count = new Long(1);
			} else {
				count = count + 1;
			}
			currentAuth.setUsageCount(count);
			authService.saveAuthorization(currentAuth,
					new AsyncCallback<WebActivityAuthorizationDto>() {

						@Override
						public void onFailure(Throwable caught) {
							// no-op
						}

						@Override
						public void onSuccess(WebActivityAuthorizationDto result) {
							if (result != null) {								
								if (result.isValidForAuth()) {
									submitAnotherButton.setVisible(true);
								}
							}
						}
					});
		}
	}
}
