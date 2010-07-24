package com.gallatinsystems.survey.device.view;

import java.util.ArrayList;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.gallatinsystems.survey.device.R;
import com.gallatinsystems.survey.device.activity.SurveyViewActivity;
import com.gallatinsystems.survey.device.dao.SurveyDbAdapter;
import com.gallatinsystems.survey.device.domain.Question;
import com.gallatinsystems.survey.device.util.ConstantUtil;
import com.gallatinsystems.survey.device.util.ViewUtil;

/**
 * this tab handles rendering of all validation errors so the user can see what
 * is is preventing submission of a survey.
 * 
 * @author Christopher Fagiani
 * 
 */
public class SubmitTabContentFactory extends SurveyTabContentFactory {

	private Button submitButton;
	private static final int DEFAULT_WIDTH = 200;
	private static final int HEADING_TEXT_SIZE = 20;
	private static final String HEADING_COLOR = "red";

	public SubmitTabContentFactory(SurveyViewActivity c,
			SurveyDbAdapter dbAdaptor, float textSize, String[] languageCodes) {
		super(c, dbAdaptor, textSize, languageCodes);

	}

	@Override
	public View createTabContent(String tag) {

		return refreshView();
	}

	/**
	 * checks completion status of questions and renders the view appropriately
	 * 
	 * @return
	 */
	public View refreshView() {
		submitButton = configureActionButton(R.string.submitbutton,
				new OnClickListener() {
					public void onClick(View v) {
						// if we have no missing responses, submit the survey
						databaseAdaptor.submitResponses(context
								.getRespondentId().toString());
						// send a broadcast message indicating new data is
						// available
						Intent i = new Intent(
								ConstantUtil.DATA_AVAILABLE_INTENT);
						context.sendBroadcast(i);
						ViewUtil.showConfirmDialog(
								R.string.submitcompletetitle,
								R.string.submitcompletetext, context);
						startNewSurvey();
					}
				});
		TableLayout table = new TableLayout(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		// get the list (across all tabs) of missing mandatory
		// responses
		ArrayList<Question> missingQuestions = context.checkMandatory();
		if (missingQuestions.size() == 0) {
			table.addView(constructHeadingRow(context
					.getString(R.string.submittext)));
			// display the "all ok" text and
			toggleButtons(true);
		} else {
			table.addView(constructHeadingRow(context
					.getString(R.string.mandatorywarning)));
			for (int i = 0; i < missingQuestions.size(); i++) {
				TableRow tr = new TableRow(context);
				tr.setLayoutParams(new ViewGroup.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				QuestionView qv = new QuestionView(context, missingQuestions
						.get(i), languageCodes, true);
				qv.suppressHelp(true);
				View ruler = new View(context);
				ruler.setBackgroundColor(0xFFFFFFFF);
				qv.addView(ruler, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT, 2));
				tr.addView(qv);
				table.addView(tr);
			}
			toggleButtons(false);
		}
		TableRow row = new TableRow(context);
		row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		if (submitButton.getParent() != null) {

		}
		row.addView(submitButton);
		table.addView(row);
		return replaceViewContent(table);
	}

	/**
	 * constructs a row in the table consisting of a single text view
	 * initialized with the text passed in
	 * 
	 * @param text
	 * @return
	 */
	private TableRow constructHeadingRow(String text) {
		TableRow tr = new TableRow(context);
		TextView heading = new TextView(context);
		heading.setWidth(DEFAULT_WIDTH);
		heading.setTextSize(HEADING_TEXT_SIZE);
		heading.setText(Html.fromHtml("<font color='" + HEADING_COLOR + "'>"
				+ text + "</font>"), BufferType.SPANNABLE);
		tr.addView(heading);
		return tr;
	}

	/**
	 * creates a new response object/record and sets the id in the context then
	 * resets the question view.
	 */
	private void startNewSurvey() {
		// create a new response object so we're ready for the
		// next instance
		context.setRespondentId(databaseAdaptor.createSurveyRespondent(context
				.getSurveyId(), context.getUserId()));
		context.resetAllQuestions();
	}

}