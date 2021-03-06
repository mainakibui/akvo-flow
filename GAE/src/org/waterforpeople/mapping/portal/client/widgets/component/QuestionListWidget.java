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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyService;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyServiceAsync;
import org.waterforpeople.mapping.app.gwt.client.util.TextConstants;
import org.waterforpeople.mapping.portal.client.widgets.component.SurveyNavigationWidget.MODE;

import com.gallatinsystems.framework.gwt.component.ListBasedWidget;
import com.gallatinsystems.framework.gwt.component.PageController;
import com.gallatinsystems.framework.gwt.util.client.CompletionListener;
import com.gallatinsystems.framework.gwt.util.client.MessageDialog;
import com.gallatinsystems.framework.gwt.wizard.client.ContextAware;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for viewing question lists
 * 
 * 
 * @author Christopher Fagiani
 * 
 */
public class QuestionListWidget extends ListBasedWidget implements ContextAware {

	private static TextConstants TEXT_CONSTANTS = GWT
			.create(TextConstants.class);
	private static Logger logger = Logger.getLogger("");
	private SurveyServiceAsync surveyService;
	private QuestionDto selectedQuestion;
	private QuestionGroupDto questionGroup;
	private Map<String, Object> bundle;
	private Map<Widget, Integer> widgetRowMap;
	private Map<Long, Integer> questionRowMap;
	private Button importQuestionsButton;
	private Grid dataGrid;
	private SurveyNavigationWidget surveyNavigationWidget;

	public QuestionListWidget(PageController controller) {
		super(controller);
		bundle = new HashMap<String, Object>();
		surveyService = GWT.create(SurveyService.class);
		selectedQuestion = null;
		importQuestionsButton = new Button(TEXT_CONSTANTS.importQuestions());
		importQuestionsButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				QuestionImportDialog importDia = new QuestionImportDialog(
						questionGroup);
				importDia.showCentered();
			}
		});

	}

	public void loadData(QuestionGroupDto questionGroupDto) {
		if (questionGroupDto != null) {
			if (questionGroupDto.getQuestionMap() != null
					&& questionGroupDto.getQuestionMap().size() > 0) {
				logger.log(Level.WARNING,
						"QuestionListWidget: Question list is populated already... has "
								+ questionGroupDto.getQuestionMap().size()
								+ " items");

				populateQuestionList(questionGroupDto.getQuestionMap().values());
			} else {
				surveyService.listQuestionsByQuestionGroup(questionGroupDto
						.getKeyId().toString(), false,false,
						new AsyncCallback<ArrayList<QuestionDto>>() {

							@Override
							public void onFailure(Throwable caught) {
								toggleLoading(false);
							}

							@Override
							public void onSuccess(ArrayList<QuestionDto> result) {
								if (result != null && result.size() > 0) {
									TreeMap<Integer, QuestionDto> questionTree = new TreeMap<Integer, QuestionDto>();

									for (int i = 0; i < result.size(); i++) {
										questionTree.put(result.get(i)
												.getOrder(), result.get(i));
									}
									logger.log(Level.WARNING,
											"QuestionListWidget: Question list fetched from server with "
													+ questionTree.size()
													+ " items");

									populateQuestionList(result);
									questionGroup.setQuestionMap(questionTree);
									bundle.put(
											BundleConstants.QUESTION_GROUP_KEY,
											questionGroup);

								} else {
									toggleLoading(false);
								}
							}
						});
			}
		}
	}

	private void populateQuestionList(Collection<QuestionDto> questionList) {
		toggleLoading(false);
		widgetRowMap = new HashMap<Widget, Integer>();
		questionRowMap = new HashMap<Long, Integer>();
		if (dataGrid != null) {
			dataGrid.removeFromParent();
		}
		if (importQuestionsButton.getParent() != null) {
			importQuestionsButton.removeFromParent();
		}
		dataGrid = new Grid(questionList.size(), 5);
		int i = 0;
		if (questionList != null) {

			for (QuestionDto q : questionList) {
				Label l = createListEntry((q.getOrder() != null ? q.getOrder()
						: "") + ": " + q.getText());
				HorizontalPanel bp = new HorizontalPanel();

				Image moveUp = new Image("/images/greenuparrow.png");
				if (i > 0) {
					createClickableWidget(ClickMode.MOVE_UP, moveUp);
				}
				Image moveDown = new Image("/images/greendownarrow.png");
				if (i < questionList.size() - 1) {
					createClickableWidget(ClickMode.MOVE_DOWN, moveDown);
				}
				Button deleteButton = createButton(ClickMode.DELETE,
						TEXT_CONSTANTS.delete());
				Button editButton = createButton(ClickMode.EDIT,
						TEXT_CONSTANTS.edit());

				Button insertAboveButton = createButton(ClickMode.INSERT,
						TEXT_CONSTANTS.insertQuestionAbove());

				bp.add(moveUp);
				bp.add(moveDown);

				widgetRowMap.put(l, i);
				dataGrid.setWidget(i, 0, l);
				dataGrid.setWidget(i, 1, bp);
				dataGrid.setWidget(i, 2, editButton);
				dataGrid.setWidget(i, 3, deleteButton);
				dataGrid.setWidget(i, 4, insertAboveButton);

				widgetRowMap.put(editButton, i);
				widgetRowMap.put(deleteButton, i);
				widgetRowMap.put(moveUp, i);
				widgetRowMap.put(moveDown, i);
				widgetRowMap.put(insertAboveButton, i);

				questionRowMap.put(q.getKeyId(), i);

				i++;
			}
		}
		addWidget(dataGrid);
		addWidget(importQuestionsButton);

	}

	@Override
	public void setContextBundle(Map<String, Object> bundle) {
		this.bundle = bundle;
		// remove the bundle item that this widget populates
		bundle.remove(BundleConstants.QUESTION_KEY);
		questionGroup = (QuestionGroupDto) bundle
				.get(BundleConstants.QUESTION_GROUP_KEY);
		surveyNavigationWidget = new SurveyNavigationWidget(
				(SurveyDto) bundle.get(BundleConstants.SURVEY_KEY), null,
				questionGroup.getOrder(), false, MODE.QUESTION_LIST,
				getPageController(), this);
		CaptionPanel cap = new CaptionPanel(TEXT_CONSTANTS.surveyNavigation());
		cap.add(surveyNavigationWidget);
		addWidget(cap);
		loadData(questionGroup);
	}

	@Override
	protected void handleItemClick(Object source, ClickMode mode) {
		int i = 0;
		for (QuestionDto q : questionGroup.getQuestionMap().values()) {
			if (i == widgetRowMap.get((Widget) source)) {
				selectedQuestion = q;
				break;
			}
			i++;
		}
		if (ClickMode.EDIT == mode || ClickMode.OPEN == mode) {
			openPage(QuestionEditWidget.class, getContextBundle(true));
		} else if (ClickMode.DELETE == mode) {
			deleteQuestion(selectedQuestion);
		} else if (ClickMode.MOVE_DOWN == mode) {
			moveQuestion(1, selectedQuestion);
		} else if (ClickMode.MOVE_UP == mode) {
			moveQuestion(-1, selectedQuestion);
		} else if (ClickMode.INSERT == mode) {
			Map<String, Object> bundle = getContextBundle(true);
			bundle.put(BundleConstants.INSERT_ABOVE_QUESTION, selectedQuestion);
			bundle.remove(BundleConstants.QUESTION_KEY);
			selectedQuestion = null;
			openPage(QuestionEditWidget.class, bundle);
		}
	}

	private void moveQuestion(int increment, QuestionDto question) {
		setWorking(true);
		final MessageDialog savingDialog = new MessageDialog(
				TEXT_CONSTANTS.saving(), TEXT_CONSTANTS.pleaseWait(), true);
		savingDialog.showCentered();
		Integer prevIdx = null;
		Integer nextIdx = null;
		Integer idx = null;
		for (Entry<Integer, QuestionDto> entry : questionGroup.getQuestionMap()
				.entrySet()) {
			if (idx != null) {
				nextIdx = entry.getKey();
				break;
			}
			if (entry.getValue().getKeyId().equals(question.getKeyId())) {
				idx = entry.getKey();
			} else {
				prevIdx = entry.getKey();
			}
		}
		List<QuestionDto> questionsToUpdate = new ArrayList<QuestionDto>();
		QuestionDto qToMove = questionGroup.getQuestionMap().get(idx);
		QuestionDto targetQuestion = null;
		if (increment > 0) {
			targetQuestion = questionGroup.getQuestionMap().get(nextIdx);
		} else {
			targetQuestion = questionGroup.getQuestionMap().get(prevIdx);
		}
		Integer tempOrder = qToMove.getOrder();
		qToMove.setOrder(targetQuestion.getOrder());
		targetQuestion.setOrder(tempOrder);
		questionGroup.getQuestionMap().put(targetQuestion.getOrder(),
				targetQuestion);
		questionGroup.getQuestionMap().put(qToMove.getOrder(), qToMove);

		questionsToUpdate.add(qToMove);
		questionsToUpdate.add(targetQuestion);
		Label widgetToMove = null;
		Label targetWidget = null;
		int rowToMove = questionRowMap.get(qToMove.getKeyId());
		int targetRow = questionRowMap.get(targetQuestion.getKeyId());

		for (Entry<Widget, Integer> entry : widgetRowMap.entrySet()) {
			if (entry.getKey() instanceof Label) {
				if (rowToMove == entry.getValue()) {
					widgetToMove = (Label) entry.getKey();
				}
				if (targetRow == entry.getValue()) {
					targetWidget = (Label) entry.getKey();
				}
			}

			if (widgetToMove != null && targetWidget != null) {
				break;
			}
		}
		widgetToMove
				.setText((targetQuestion.getOrder() != null ? targetQuestion
						.getOrder() : "") + ": " + targetQuestion.getText());
		targetWidget.setText((qToMove.getOrder() != null ? qToMove.getOrder()
				: "") + ": " + qToMove.getText());
		questionRowMap.put(qToMove.getKeyId(), targetRow);
		questionRowMap.put(targetQuestion.getKeyId(), rowToMove);

		surveyService.updateQuestionOrder(questionsToUpdate,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						setWorking(false);
						savingDialog.hide();
						selectedQuestion = null;
						MessageDialog errDia = new MessageDialog(TEXT_CONSTANTS
								.error(), TEXT_CONSTANTS.errorTracePrefix()
								+ " " + caught.getLocalizedMessage());
						errDia.showCentered();

					}

					@Override
					public void onSuccess(Void result) {
						setWorking(false);
						selectedQuestion = null;
						savingDialog.hide();
					}
				});

	}

	/**
	 * iterates over the questions in the current group to find the one that
	 * matches the id of the question passed in. If found, the map key is
	 * returned
	 * 
	 * @param question
	 * @return
	 */
	private Integer findKeyForQuestion(QuestionDto question) {
		Integer key = null;
		for (Entry<Integer, QuestionDto> entry : questionGroup.getQuestionMap()
				.entrySet()) {
			if (entry.getValue().getKeyId().equals(question.getKeyId())) {
				key = entry.getKey();
				break;
			}
		}
		return key;
	}

	private void deleteQuestion(QuestionDto question) {
		setWorking(true);
		final MessageDialog deletingDialog = new MessageDialog(
				TEXT_CONSTANTS.deleting(), TEXT_CONSTANTS.pleaseWait(), true);
		deletingDialog.showCentered();
		Integer key = findKeyForQuestion(question);
		if (key != null) {
			questionGroup.getQuestionMap().remove(key);
			// now fix the orders
			for (QuestionDto q : questionGroup.getQuestionMap().values()) {
				if (q.getOrder() >= question.getOrder()) {
					q.setOrder(q.getOrder() - 1);
				}
			}
			surveyService.deleteQuestion(question,
					question.getQuestionGroupId(), new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							setWorking(false);
							deletingDialog.hide();
							selectedQuestion = null;
							MessageDialog errDia = new MessageDialog(
									TEXT_CONSTANTS.error(), TEXT_CONSTANTS
											.errorTracePrefix()
											+ " "
											+ caught.getLocalizedMessage());
							errDia.showCentered();
						}

						@Override
						public void onSuccess(String result) {
							setWorking(false);
							deletingDialog.hide();
							if(result != null && result.trim().length()>0){
								MessageDialog errDia = new MessageDialog(
										TEXT_CONSTANTS.error(), TEXT_CONSTANTS
												.errorTracePrefix()
												+ " "
												+ result);
								errDia.showCentered();
							}
							selectedQuestion = null;
							loadData(questionGroup);
						}
					});
		}
	}

	@Override
	public Map<String, Object> getContextBundle(boolean doPopulation) {
		if (selectedQuestion != null && doPopulation) {
			bundle.put(BundleConstants.QUESTION_KEY, selectedQuestion);
		}
		return bundle;
	}

	@Override
	public void flushContext() {
		if (bundle != null) {
			bundle.remove(BundleConstants.QUESTION_KEY);
		}
	}

	@Override
	public void persistContext(String buttonText,CompletionListener listener) {
		if (listener != null) {
			listener.operationComplete(true, getContextBundle(true));
		}
	}
}