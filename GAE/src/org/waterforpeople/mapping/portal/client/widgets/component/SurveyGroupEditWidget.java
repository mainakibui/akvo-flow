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

import java.util.HashMap;
import java.util.Map;

import org.waterforpeople.mapping.app.gwt.client.survey.SurveyGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyService;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;

import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.gallatinsystems.framework.gwt.util.client.ViewUtil;
import com.gallatinsystems.framework.gwt.wizard.client.ContextAware;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * edits/creates SurveyGroup objects
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyGroupEditWidget extends Composite implements ContextAware,
		ChangeHandler {

	private static TextConstants TEXT_CONSTANTS = GWT
	.create(TextConstants.class);
	private static final String FORM_LABEL_CSS = "form-label";
	private static final String TXT_BOX_CSS = "txt-box";
	private VerticalPanel panel;
	private Map<String, Object> bundle;
	private TextBox codeBox;
	private TextBox descriptionBox;
	private SurveyServiceAsync surveyService;
	private SurveyGroupDto currentDto;
	private boolean isChanged;

	public SurveyGroupEditWidget() {
		panel = new VerticalPanel();
		surveyService = GWT.create(SurveyService.class);
		codeBox = new TextBox();
		descriptionBox = new TextBox();
		panel.add(buildRow(TEXT_CONSTANTS.code(), codeBox));
		panel.add(buildRow(TEXT_CONSTANTS.description(), descriptionBox));
		currentDto = null;
		initWidget(panel);
	}

	private Widget buildRow(String label, TextBox box) {
		Label l = new Label();
		l.setText(label);
		l.setStylePrimaryName(FORM_LABEL_CSS);
		box.setStylePrimaryName(TXT_BOX_CSS);
		box.addChangeHandler(this);
		HorizontalPanel row = new HorizontalPanel();
		row.add(l);
		row.add(box);
		return row;
	}

	private void populateWidgets() {
		isChanged = false;
		if (currentDto != null) {
			codeBox.setText(currentDto.getCode());
			descriptionBox.setText(currentDto.getDescription());
		}
	}

	private boolean validateInput() {
		return ViewUtil.isTextPopulated(codeBox);
	}

	public void saveSurveyGroup(final CompletionListener listener) {
		if (validateInput()) {
			if (currentDto == null) {
				currentDto = new SurveyGroupDto();
			}
			currentDto.setCode(codeBox.getText().trim());
			currentDto
					.setDescription(descriptionBox.getText() != null ? descriptionBox
							.getText().trim()
							: null);
			surveyService.saveSurveyGroup(currentDto,
					new AsyncCallback<SurveyGroupDto>() {

						@Override
						public void onFailure(Throwable caught) {

							MessageDialog errDia = new MessageDialog(TEXT_CONSTANTS.error(),TEXT_CONSTANTS.errorTracePrefix()+" "+caught.getLocalizedMessage());									
							errDia.showRelativeTo(panel);
							if (listener != null) {
								listener.operationComplete(false,
										getContextBundle(true));
							}

						}

						@Override
						public void onSuccess(SurveyGroupDto result) {
							currentDto = result;
							if (listener != null) {
								listener.operationComplete(true,
										getContextBundle(true));
							}

						}
					});
		} else {
			MessageDialog validationDialog = new MessageDialog(TEXT_CONSTANTS.inputError(),TEXT_CONSTANTS.invalidSurveyGroup());										
			validationDialog.showRelativeTo(panel);
		}
	}

	@Override
	public void persistContext(String buttonText,CompletionListener listener) {
		if (isChanged) {
			saveSurveyGroup(listener);
		} else {
			listener.operationComplete(true, getContextBundle(true));
		}
	}

	@Override
	public void setContextBundle(Map<String, Object> bundle) {
		this.bundle = bundle;
		currentDto = (SurveyGroupDto) bundle
				.get(BundleConstants.SURVEY_GROUP_KEY);
		flushContext();
		populateWidgets();
	}

	@Override
	public Map<String, Object> getContextBundle(boolean doPopulation) {
		if (bundle == null) {
			bundle = new HashMap<String, Object>();
		}
		if (doPopulation) {
			bundle.put(BundleConstants.SURVEY_GROUP_KEY, currentDto);
		}
		return bundle;
	}
	
	@Override
	public void flushContext(){
		//no-op
	}

	@Override
	public void onChange(ChangeEvent event) {
		if (currentDto != null) {
			if (currentDto.getCode() != null
					&& !currentDto.getCode().equals(codeBox.getText())) {
				isChanged = true;
			} else if (currentDto.getDescription() != null
					&& !currentDto.getDescription().equals(
							descriptionBox.getText())) {
				isChanged = true;
			} else if ((ViewUtil.isTextPopulated(codeBox) && currentDto
					.getCode() == null)
					|| (ViewUtil.isTextPopulated(descriptionBox) && currentDto
							.getDescription() == null)) {
				isChanged = true;
			} else {
				isChanged = false;
			}
		} else {
			// if we haven't saved, set isChanged to true if either field is non
			// empty
			isChanged = (ViewUtil.isTextPopulated(codeBox) || ViewUtil
					.isTextPopulated(descriptionBox));
		}
	}
}
