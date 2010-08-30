package org.waterforpeople.mapping.dataexport;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.waterforpeople.mapping.app.web.dto.DataBackoutRequest;

/**
 * simple applet to back out data from a single survey at a time
 * 
 * @author Christopher Fagiani
 * 
 */
public class DataBackoutApplet extends JApplet implements Runnable {

	private static final long serialVersionUID = 944163825066341210L;
	private static final String SERVER_PATH = "/databackout?action=";
	private JLabel statusLabel;
	private String surveyId;
	private String date;
	private String country;
	private String serverBase;

	public void init() {
		statusLabel = new JLabel();
		getContentPane().add(statusLabel);
		serverBase = getCodeBase().toString();
		if (serverBase.trim().endsWith("/")) {
			serverBase = serverBase.trim().substring(0,
					serverBase.lastIndexOf("/"));
		}
		InputDialog dia = new InputDialog();
		if (!dia.isCancelled()) {
			if (country != null && surveyId != null && date != null) {
				Thread worker = new Thread(this);
				worker.start();
			} else {
				statusLabel.setText("Applet misconfigured");
			}
		} else {
			statusLabel.setText("Cancelled");
		}
	}

	public void run() {

		try {
			SwingUtilities.invokeLater(new StatusUpdater("Deleting Raw Data"));
			deleteSurveyInstances();
			SwingUtilities.invokeLater(new StatusUpdater(
					"Deleting Question Summaries"));
			deleteQuestionSummaries();
			SwingUtilities.invokeLater(new StatusUpdater(
					"Deleting Access Points"));
			deleteAccessPoints();
			SwingUtilities.invokeLater(new StatusUpdater(
					"Deleting AP summaries"));
			deleteAccessPointSummary();
			SwingUtilities.invokeLater(new StatusUpdater("Backout Complete"));
		} catch (Exception e) {
			SwingUtilities
					.invokeLater(new StatusUpdater("Backout Failed: " + e));
		}
	}

	private void deleteAccessPoints() throws Exception {
		String resultString = "true";
		while (resultString != null
				&& "true".equalsIgnoreCase(resultString.trim())) {
			resultString = invokeRemoteMethod(DataBackoutRequest.DELETE_ACCESS_POINT_ACTION + "&"
					+ DataBackoutRequest.COUNTRY_PARAM + "=" + country + "&"
					+ DataBackoutRequest.DATE_PARAM + "=" + date);
		}
	}

	private void deleteAccessPointSummary() throws Exception {
		String resultString = "true";
		while (resultString != null
				&& "true".equalsIgnoreCase(resultString.trim())) {
			resultString = invokeRemoteMethod(DataBackoutRequest.DELETE_AP_SUMMARY_ACTION + "&"
					+ DataBackoutRequest.COUNTRY_PARAM + "=" + country + "&"
					+ DataBackoutRequest.DATE_PARAM + "=" + date);
		}
	}

	private void deleteQuestionSummaries() throws Exception {
		String summaryString = invokeRemoteMethod(DataBackoutRequest.GET_QUESTION_ACTION
				+ "&"
				+ DataBackoutRequest.SURVEY_ID_PARAM + "="+surveyId);
		if (summaryString != null) {
			StringTokenizer strTok = new StringTokenizer(summaryString, ",");
			while (strTok.hasMoreTokens()) {
				invokeRemoteMethod(DataBackoutRequest.DELETE_QUESTION_SUMMARY_ACTION
						+ "&" + DataBackoutRequest.QUESTION_ID_PARAM + "="
						+ strTok.nextToken());
			}
		}
	}

	private void deleteSurveyInstances() throws Exception {
		String instanceString = invokeRemoteMethod(DataBackoutRequest.LIST_INSTANCE_ACTION
				+ "&"
				+ DataBackoutRequest.SURVEY_ID_PARAM +"="+ surveyId);
		if (instanceString != null) {
			StringTokenizer strTok = new StringTokenizer(instanceString, ",");
			while (strTok.hasMoreTokens()) {
				invokeRemoteMethod(DataBackoutRequest.DELETE_SURVEY_INSTANCE_ACTION
						+ "&" + DataBackoutRequest.SURVEY_INSTANCE_ID_PARAM
						+ "=" + strTok.nextToken());
			}
		}
	}

	private String invokeRemoteMethod(String queryString) throws Exception {
		URL url = new URL(serverBase + SERVER_PATH + queryString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

	/**
	 * Private class to handle updating of the UI thread from our worker thread
	 */
	private class StatusUpdater implements Runnable {

		private String status;

		public StatusUpdater(String val) {
			status = val;
		}

		public void run() {
			statusLabel.setText(status);
		}
	}

	private class InputDialog extends JDialog implements ActionListener {
		
		private static final long serialVersionUID = -2875321125734363515L;
		private JTextField surveyField;
		private JTextField countryField;
		private JTextField dateField;
		private JButton okButton;
		private JButton cancelButton;
		private JLabel status;
		private boolean cancelled;

		public InputDialog() {
			super();
			surveyField = new JTextField();
			countryField = new JTextField();
			dateField = new JTextField();
			okButton = new JButton("Ok");
			cancelButton = new JButton("Cancel");
			status = new JLabel();

			JPanel contentPane = new JPanel(new GridLayout(5, 2, 10, 10));
			contentPane.add(new JLabel("Survey: "));
			contentPane.add(surveyField);
			contentPane.add(new JLabel("Country Code: "));
			contentPane.add(countryField);
			contentPane.add(new JLabel("Date (yyyy-MM-dd)"));
			contentPane.add(dateField);
			contentPane.add(okButton);
			contentPane.add(cancelButton);
			contentPane.add(status);
			setContentPane(contentPane);
			cancelButton.addActionListener(this);
			okButton.addActionListener(this);
			setSize(300, 200);
			setTitle("Enter Backout Parameters");
			setModal(true);
			setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			boolean isValid = true;
			if (e.getSource() == cancelButton) {
				cancelled = true;
			} else {
				surveyId = surveyField.getText().trim();
				country = countryField.getText().trim();

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					df.parse(dateField.getText().trim());
					date = dateField.getText().trim();
				} catch (Exception ex) {
					status.setText("Date is invalid");
					isValid = false;
				}

				if (surveyId == null || surveyId.length() == 0) {
					status.setText("SurveyId is missing");
					isValid = false;
				}

				if (country == null || country.length() == 0) {
					status.setText("Country is missing");
					isValid = false;
				}
			}
			if (isValid) {
				setVisible(false);
			}
		}

		public boolean isCancelled() {
			return cancelled;
		}
	}

}