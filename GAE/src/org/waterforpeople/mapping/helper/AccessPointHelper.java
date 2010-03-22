package org.waterforpeople.mapping.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.waterforpeople.mapping.dao.SurveyInstanceDAO;
import org.waterforpeople.mapping.domain.AccessPoint;
import org.waterforpeople.mapping.domain.GeoCoordinates;
import org.waterforpeople.mapping.domain.QuestionAnswerStore;
import org.waterforpeople.mapping.domain.SurveyInstance;
import org.waterforpeople.mapping.domain.AccessPoint.AccessPointType;

import com.gallatinsystems.framework.dao.BaseDAO;

public class AccessPointHelper {

	public void processSurveyInstance(String surveyId) {
		// Get the survey and QuestionAnswerStore
		// Get the surveyDefinition

		SurveyInstanceDAO sid = new SurveyInstanceDAO();
		SurveyInstance si = sid.getByKey(Long.parseLong(surveyId));
		ArrayList<QuestionAnswerStore> questionAnswerList = si
				.getQuestionAnswersStore();

		// Hardcoded for dev need to identify the map key between SurveyInstance
		// and Survey
		// Survey surveyDefinition = surveyDAO.get(39L);

		/*
		 * For Monday I am mapping questionIds from QuestionAnswer to values in
		 * mappingsurvey this needs to be replaced by a mapping between the two
		 * tables for Tuesday Lat/Lon/Alt from q4 geo WaterPointPhotoURL = qm2
		 * typeOfWaterPointTech = qm5 communityCode = qm1 constructionDate = qm4
		 * numberOfHouseholdsUsingWaterPoint = qm6 costPer = qm7
		 * farthestHouseholdfromWaterPoint = qm8
		 * CurrentManagementStructureWaterPoint = qm9 waterSystemStatus = qm10
		 * sanitationPointPhotoURL =q3 waterPointCaption = qm3
		 */

		AccessPoint ap;
		BaseDAO<AccessPoint> apDAO = new BaseDAO<AccessPoint>(AccessPoint.class);
		ap = parseAccessPoint(questionAnswerList,
				AccessPoint.AccessPointType.WATER_POINT);
		apDAO.save(ap);

	}

	private AccessPoint parseAccessPoint(
			ArrayList<QuestionAnswerStore> questionAnswerList,
			AccessPoint.AccessPointType accessPointType) {
		AccessPoint ap = null;
		if (accessPointType == AccessPointType.WATER_POINT) {
			ap = parseWaterPoint(questionAnswerList);
		} else if (accessPointType == AccessPointType.SANITATION_POINT) {

		}
		return ap;
	}

	private AccessPoint parseWaterPoint(
			ArrayList<QuestionAnswerStore> questionAnswerList) {
		AccessPoint ap = new AccessPoint();
		Properties props = System.getProperties();

		String photo_url_root = props.getProperty("photo_url_root");
		for (QuestionAnswerStore qas : questionAnswerList) {

			if (qas.getQuestionID().equals("qm1")) {
				ap.setCommunityCode(qas.getValue());
			} else if (qas.getQuestionID().equals("qm1a")) {
				GeoCoordinates geoC = new GeoCoordinates()
						.extractGeoCoordinate(qas.getValue());
				ap.setLatitude(geoC.getLatitude());
				ap.setLongitude(geoC.getLongitude());
				ap.setAltitude(geoC.getAltitude());
			} else if (qas.getQuestionID().equals("qm2")) {
				// Change photourl to s3 url
				String[] photoParts = qas.getValue().split("/");
				String newURL = photo_url_root + photoParts[2];
				ap.setPhotoURL(newURL);
			} else if (qas.getQuestionID().equals("qm3")) {
				// photo caption
				ap.setPointPhotoCaption(qas.getValue());
			} else if (qas.getQuestionID().equals("qm4")) {
				ap.setConstructionDate(qas.getValue());
			} else if (qas.getQuestionID().equals("qm5")) {
				ap.setTypeTechnology(qas.getValue());
			} else if (qas.getQuestionID().equals("qm6")) {
				ap.setNumberOfHouseholdsUsingPoint(qas.getValue());
			} else if (qas.getQuestionID().equals("qm7")) {
				ap.setCostPer(qas.getValue());
			} else if (qas.getQuestionID().equals("qm8")) {
				ap.setFarthestHouseholdfromPoint(qas.getValue());
			} else if (qas.getQuestionID().equals("qm9")) {
				// Current mgmt structure
				ap.setCurrentManagementStructurePoint(qas.getValue());
			} else if (qas.getQuestionID().equals("qm10")) {
				ap.setPointStatus(qas.getValue());
			}
			ap.setPointType(AccessPoint.AccessPointType.WATER_POINT);
			// for now hardcode the data to now
			ap.setCollectionDate(new Date());
		}

		return ap;
	}

}
