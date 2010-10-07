package org.waterforpeople.mapping.app.web.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.gallatinsystems.framework.rest.RestRequest;

/**
 * encapsulates requests to the SurveyManager apis
 * 
 * @author Christopher Fagiani
 * 
 */
public class SurveyManagerRequest extends RestRequest {

	private static final long serialVersionUID = -1914332708852551948L;

	public static final String GET_AVAIL_DEVICE_SURVEY_ACTION = "getAvailableSurveysDevice";
	public static final String GET_SURVEY_HEADER_ACTION  = "getSurveyHeader";
	public static final String GET_ZIP_FILE_URL_ACTION = "getZipFileUrls";
	

	private static final String SURVEY_INSTANCE_PARAM = "surveyInstanceId";
	private static final String SURVEY_ID_PARAM = "surveyId";
	private static final String SURVEY_DOC_PARAM = "surveyDocument";
	private static final String PHONE_NUM_PARAM = "devicePhoneNumber";
	private static final String FILE_START_DATE_PARAM = "startDate";
	
	private Long surveyId;
	private Long surveyInstanceId;
	private String surveyDoc;
	private String phoneNumber;
	private Date startDate;

	public Long getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(Long surveyId) {
		this.surveyId = surveyId;
	}

	public Long getSurveyInstanceId() {
		return surveyInstanceId;
	}

	public void setSurveyInstanceId(Long surveyInstanceId) {
		this.surveyInstanceId = surveyInstanceId;
	}

	public String getSurveyDoc() {
		return surveyDoc;
	}

	public void setSurveyDoc(String surveyDoc) {
		this.surveyDoc = surveyDoc;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public void setStartDate(String dateString) throws Exception{
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
			java.util.Date date = new java.util.Date();
			String dateTime = dateFormat.format(date);
			setStartDate(dateTime);
		}catch(Exception ex){
			throw new Exception("Could not parse date param");
		}
	}

	@Override
	protected void populateFields(HttpServletRequest req) throws Exception {
		surveyInstanceId = parseLong(req.getParameter(SURVEY_INSTANCE_PARAM),
				SURVEY_INSTANCE_PARAM);
		phoneNumber = req.getParameter(PHONE_NUM_PARAM);
		surveyId = parseLong(req.getParameter(SURVEY_ID_PARAM), SURVEY_ID_PARAM);
		surveyDoc = req.getParameter(SURVEY_DOC_PARAM);
		if(req.getParameter(FILE_START_DATE_PARAM)!=null)
			setStartDate(req.getParameter(FILE_START_DATE_PARAM));
	}

	@Override
	public void populateErrors() {
		// no-op right now?
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

}
