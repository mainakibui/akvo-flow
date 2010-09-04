package org.waterforpeople.mapping.analytics;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.List;

import org.waterforpeople.mapping.dao.SurveyInstanceDAO;
import org.waterforpeople.mapping.domain.QuestionAnswerStore;

import com.gallatinsystems.common.util.StringUtil;
import com.gallatinsystems.framework.analytics.summarization.DataSummarizationRequest;
import com.gallatinsystems.framework.analytics.summarization.DataSummarizer;
import com.gallatinsystems.framework.domain.DataChangeRecord;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;

/**
 * cleans data by fixing capitalization of Name field values. If the value
 * changed, it will fire a LCR message to the change listener queue
 * 
 * @author Christopher Fagiani
 * 
 */
public class NameQuestionDataCleanser implements DataSummarizer {

	private String currentCursor;
	private SurveyInstanceDAO dao;

	public NameQuestionDataCleanser() {
		dao = new SurveyInstanceDAO();
	}

	@Override
	public boolean performSummarization(String key, String type, String value,
			Integer offset, String cursor) {
		List<QuestionAnswerStore> answers = dao
				.listQuestionAnswerStoreForQuestion(key, cursor);
		if (answers != null && answers.size() > 0) {
			for (QuestionAnswerStore answer : answers) {
				if (answer.getValue() != null) {
					String newValue = StringUtil.capitalizeString(answer
							.getValue());
					if (!answer.getValue().equals(newValue)) {
						sendChangeMessage(new DataChangeRecord(
								QuestionAnswerStore.class.getName(), key,
								answer.getValue(), newValue));
						answer.setValue(newValue);
					}
				}
			}
			// now persist the changes
			dao.save(answers);
		}
		if (answers != null && answers.size() == 20) {
			currentCursor = SurveyInstanceDAO.getCursor(answers);
			return false;
		} else {
			currentCursor = null;
			return true;
		}
	}

	@Override
	public String getCursor() {
		return currentCursor;
	}

	/**
	 * sends a logical change record over the change task queue for the question
	 * given
	 * 
	 * @param value
	 */
	private void sendChangeMessage(DataChangeRecord value) {
		Queue queue = QueueFactory.getQueue("dataUpdate");
		queue.add(url("/app_worker/dataupdate").param(
				DataSummarizationRequest.OBJECT_KEY, value.getId()).param(
				DataSummarizationRequest.OBJECT_TYPE, "QuestionDataChange")
				.param(DataSummarizationRequest.VALUE_KEY, value.packString()));
	}

}