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

package org.waterforpeople.mapping.dataexport.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.waterforpeople.mapping.app.gwt.client.accesspoint.AccessPointDto;
import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesDto;
import org.waterforpeople.mapping.app.gwt.client.location.PlacemarkDto;
import org.waterforpeople.mapping.app.gwt.client.location.PlacemarkDtoResponse;
import org.waterforpeople.mapping.app.gwt.client.survey.OptionContainerDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDependencyDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.QuestionOptionDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;
import org.waterforpeople.mapping.app.gwt.client.survey.SurveyGroupDto;
import org.waterforpeople.mapping.app.gwt.client.survey.TranslationDto;
import org.waterforpeople.mapping.app.gwt.client.surveyinstance.SurveyInstanceDto;
import org.waterforpeople.mapping.app.web.dto.DataBackoutRequest;
import org.waterforpeople.mapping.app.web.dto.DeviceFileRestRequest;
import org.waterforpeople.mapping.app.web.dto.SurveyRestRequest;

import com.gallatinsystems.common.util.MD5Util;
import com.gallatinsystems.framework.rest.RestRequest;

/**
 * client code for calling the apis for data processing on the server
 * 
 * @author Christopher Fagiani
 * 
 */
public class BulkDataServiceClient {
	private static final String DATA_SERVLET_PATH = "/databackout?action=";
	public static final String RESPONSE_KEY = "dtoList";
	private static final String SURVEY_SERVLET_PATH = "/surveyrestapi?action=";
	private static final String DEVICE_FILES_SERVLET_PATH = "/devicefilesrestapi?action=";
	private static final String ACCESS_POINT_SERVLET_PATH = "/accesspoint?action=search";

	/**
	 * lists all responses from the server for a surveyInstance submission as a
	 * map of values keyed on questionId
	 * 
	 * @param instanceId
	 * @param serverBase
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> fetchQuestionResponses(String instanceId,
			String serverBase) throws Exception {
		String instanceValues = fetchDataFromServer(serverBase
				+ DATA_SERVLET_PATH
				+ DataBackoutRequest.LIST_INSTANCE_RESPONSE_ACTION + "&"
				+ DataBackoutRequest.SURVEY_INSTANCE_ID_PARAM + "="
				+ instanceId);
		return parseInstanceValues(instanceValues);
	}

	public static List<AccessPointDto> fetchAccessPoints(String cursor,
			String serverBase) throws Exception {
		String action = ACCESS_POINT_SERVLET_PATH
				+ (cursor != null ? "&cursor=" + cursor : "");
		String url = serverBase + action;

		String response = fetchDataFromServer(url);
		JSONObject jsonOuter = new JSONObject(response);
		if (jsonOuter.has("cursor")) {
			cursor = jsonOuter.getString("cursor");
		}
		List<AccessPointDto> apDtoList = RestAccessPointParser
				.parseAccessPoint(response);
		return apDtoList;
	}

	public static List<DeviceFilesDto> fetchDeviceFiles(String statusCode,
			String serverBase) throws Exception {
		return fetchData(null, serverBase, statusCode);
	}

	private static List<DeviceFilesDto> fetchData(String cursor,
			String serverBase, String statusCode) throws Exception {

		String queryString = null;
		String response = null;
		ArrayList<DeviceFilesDto> dfDto = new ArrayList<DeviceFilesDto>();
		queryString = serverBase + DEVICE_FILES_SERVLET_PATH
				+ DeviceFileRestRequest.LIST_DEVICE_FILES_ACTION + "&"
				+ DeviceFileRestRequest.PROCESSED_STATUS_PARAM + "="
				+ statusCode;
		if (cursor != null) {
			queryString = queryString + "&cursor=" + cursor;
		}
		response = fetchDataFromServer(queryString);
		List<DeviceFilesDto> list = parseDeviceFiles(response);
		if (list == null || list.size() == 0) {
			return null;
		}
		for (DeviceFilesDto dto : list) {
			dfDto.add(dto);
		}

		JSONObject jsonOuter = new JSONObject(response);
		if (jsonOuter.has("cursor")) {
			cursor = jsonOuter.getString("cursor");
			List<DeviceFilesDto> dfDtoTemp = fetchData(cursor, serverBase,
					statusCode);
			if (dfDtoTemp != null)
				for (DeviceFilesDto item : dfDtoTemp) {
					dfDto.add(item);
				}
		}

		return dfDto;
	}

	public static PlacemarkDtoResponse fetchPlacemarks(String countryCode,
			String serverBase, String cursor) throws Exception {
		try {
			return fetchPlacemarkData(cursor, serverBase, countryCode);
		} catch (Exception ex) {
			return fetchPlacemarkData(cursor, serverBase, countryCode);
		}
	}

	private static PlacemarkDtoResponse fetchPlacemarkData(String cursor,
			String serverBase, String countryCode) throws Exception {
		String queryString = null;
		String response = null;
		ArrayList<PlacemarkDto> pmDto = new ArrayList<PlacemarkDto>();
		queryString = serverBase + "/placemarkrestapi?"
				+ "needDetailsFlag=true" + "&country=" + countryCode
				+ "&display=googleearth&ignoreCache=true";
		if (cursor != null) {
			queryString = queryString + "&cursor=" + cursor;
		}
		response = fetchDataFromServer(queryString);
		List<PlacemarkDto> list = null;
		try {
			list = parsePlacemarks(response);
		} catch (Exception ex) {
			System.out.println("Caught Exception skipping this response");
		}
		if (list == null || list.size() == 0) {
			return null;
		}
		for (PlacemarkDto dto : list) {
			pmDto.add(dto);
		}

		PlacemarkDtoResponse pdr = new PlacemarkDtoResponse();
		pdr.setDtoList(pmDto);
		JSONObject jsonOuter = new JSONObject(response);
		if (jsonOuter.has("cursor")) {
			cursor = jsonOuter.getString("cursor");
			pdr.setCursor(cursor);
		} else {
			pdr.setCursor(null);
		}
		return pdr;
	}

	private static List<PlacemarkDto> parsePlacemarks(String response)
			throws Exception {
		JSONArray arr = null;
		if (response.startsWith("{")) {
			List<PlacemarkDto> dtoList = new ArrayList<PlacemarkDto>();
			if (response != null) {
				JSONObject json = new JSONObject(response);
				if (json != null) {
					if (json.has("placemarks")) {
						try {
							if (!json.getString("placemarks").equals("null"))
								arr = json.getJSONArray("placemarks");
						} catch (Exception ex) {
							ex.printStackTrace();
							arr = null;
						}
					} else
						return null;
				}
			}

			if (arr != null) {

				for (int i = 0; i < arr.length(); i++) {
					PlacemarkDto dto = new PlacemarkDto();
					JSONObject json = arr.getJSONObject(i);
					if (json != null) {
						if (json.has("communityCode")) {
							String x = json.getString("communityCode");
							dto.setCommunityCode(x);
						}
						if (json.has("markType")) {
							String x = json.getString("markType");
							dto.setMarkType(x);
						}
						if (json.has("iconUrl")) {
							String x = json.getString("iconUrl");
							dto.setIconUrl(x);
						}
						if (json.has("longitude")) {
							String x = json.getString("longitude");
							try {
								dto.setLongitude(new Double(x));
							} catch (NumberFormatException nex) {
								System.out
										.println("Couldn't parse Longitude for"
												+ dto.getCommunityCode());
								dto.setLongitude(null);
							}
						}
						if (json.has("latitude")) {
							String x = json.getString("latitude");
							try {
								dto.setLatitude(new Double(x));
							} catch (NumberFormatException nex) {
								System.out
										.println("Couldn't parse Latitude for"
												+ dto.getCommunityCode());
								dto.setLatitude(null);
							}
						}
						if (json.has("collectionDate")) {
							String x = json.getString("collectionDate");
							if (x != null) {
								try {
									dto.setCollectionDate(new Date(x));
								} catch (IllegalArgumentException iae) {
									// log it and ignore it
									System.out
											.println("Couldn't parse date for"
													+ dto.getCommunityCode());
									dto.setCollectionDate(null);
								}
							}
						}
						if (json.has("placemarkContents")) {
							String x = json.getString("placemarkContents");
							dto.setPlacemarkContents(x);
						}
						if (json.has("pinStyle")) {
							dto.setPinStyle(json.getString("pinStyle"));
						}
					}
					dtoList.add(dto);
				}
				return dtoList;
			}
			return null;
		}
		return null;
	}

	/**
	 * survey instance ids and their submission dates. Map keys are the
	 * instances and values are the dates.
	 * 
	 * @param surveyId
	 * @param serverBase
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> fetchInstanceIds(String surveyId,
			String serverBase) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		String instanceString = fetchDataFromServer(serverBase
				+ DATA_SERVLET_PATH + DataBackoutRequest.LIST_INSTANCE_ACTION
				+ "&" + DataBackoutRequest.SURVEY_ID_PARAM + "=" + surveyId
				+ "&" + DataBackoutRequest.INCLUDE_DATE_PARAM + "=true");
		if (instanceString != null) {
			StringTokenizer strTok = new StringTokenizer(instanceString, ",");
			while (strTok.hasMoreTokens()) {
				String instanceId = strTok.nextToken();
				String dateString = "";
				if (instanceId.contains("|")) {
					dateString = instanceId
							.substring(instanceId.indexOf("|") + 1);
					instanceId = instanceId.substring(0,
							instanceId.indexOf("|"));
				}
				values.put(instanceId, dateString.replaceAll("\n", " ").trim());
			}
		}
		return values;
	}

	public static void main(String[] args) {
		try {
			Map<String, String> results = BulkDataServiceClient
					.fetchInstanceIds(args[1], args[0]);
			if (results != null) {
				for (Entry<String, String> entry : results.entrySet()) {
					System.out
							.println(entry.getKey() + ", " + entry.getValue());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * method to parse SurveyInstance response values
	 * 
	 * @param data
	 * @return
	 */
	private static Map<String, String> parseInstanceValues(String data) {
		Map<String, String> responseMap = new HashMap<String, String>();
		if (data != null) {
			StringTokenizer lines = new StringTokenizer(data, "\n");
			if (lines != null) {
				while (lines.hasMoreTokens()) {
					StringTokenizer strTok = new StringTokenizer(
							lines.nextToken(), ",");
					String key = null;
					String val = "";
					if (strTok.hasMoreTokens()) {
						key = strTok.nextToken();
					}
					while (strTok.hasMoreTokens()) {
						if (val.length() > 0) {
							val += ",";
						}
						val += strTok.nextToken();
					}
					if (key != null && key.trim().length() > 0) {
						String oldVal = responseMap.get(key);
						if (oldVal != null) {
							if (val != null) {
								if (oldVal.trim().length() < val.trim()
										.length()) {
									responseMap.put(key, val);
								}
							}
						} else {
							responseMap.put(key, val);
						}
					}
				}
			}
		}
		return responseMap;
	}

	/**
	 * loads full details for a single question (options, translations, etc)
	 * 
	 * @param serverBase
	 * @param questionId
	 * @return
	 */
	public static QuestionDto loadQuestionDetails(String serverBase,
			Long questionId) throws Exception {

		List<QuestionDto> dtoList = null;

		dtoList = parseQuestions(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH
				+ SurveyRestRequest.GET_QUESTION_DETAILS_ACTION + "&"
				+ SurveyRestRequest.QUESTION_ID_PARAM + "=" + questionId));

		if (dtoList != null && dtoList.size() > 0) {
			return dtoList.get(0);
		} else {
			return null;
		}

	}

	/**
	 * returns an array containing 2 elements: the first is an ordered list of
	 * questionIds (in the order they appear in the survey) and the second
	 * element is a map of questions (keyed on id)
	 * 
	 * @param surveyId
	 * @param serverBase
	 * @return
	 * @throws Exception
	 */
	public static Object[] loadQuestions(String surveyId, String serverBase)
			throws Exception {
		Object[] results = new Object[2];
		Map<String, String> questions = new HashMap<String, String>();
		List<QuestionGroupDto> groups = fetchQuestionGroups(serverBase,
				surveyId);
		List<String> keyList = new ArrayList<String>();
		if (groups != null) {
			for (QuestionGroupDto group : groups) {
				List<QuestionDto> questionDtos = fetchQuestions(serverBase,
						group.getKeyId());
				if (questionDtos != null) {
					for (QuestionDto question : questionDtos) {
						keyList.add(question.getKeyId().toString());
						questions.put(question.getKeyId().toString(),
								question.getText());
					}
				}
			}
		}
		results[0] = keyList;
		results[1] = questions;
		return results;
	}

	/**
	 * gets questions from the server for a specific question group
	 * 
	 * @param serverBase
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public static List<QuestionDto> fetchQuestions(String serverBase,
			Long groupId) throws Exception {
		return parseQuestions(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH + SurveyRestRequest.LIST_QUESTION_ACTION
				+ "&" + SurveyRestRequest.QUESTION_GROUP_ID_PARAM + "="
				+ groupId));
	}

	/**
	 * gets a surveyInstance from the server for a specific id
	 * 
	 * @param id
	 * @param serverBase
	 * @return
	 * @throws Exception
	 */
	public static SurveyInstanceDto findSurveyInstance(Long id,
			String serverBase) throws Exception {
		return parseSurveyInstance(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH
				+ SurveyRestRequest.GET_SURVEY_INSTANCE_ACTION + "&"
				+ SurveyRestRequest.INSTANCE_PARAM + "=" + id));
	}

	/**
	 * gets question groups from the server for a specific survey
	 * 
	 * @param serverBase
	 * @param surveyId
	 * @return
	 * @throws Exception
	 */
	public static List<QuestionGroupDto> fetchQuestionGroups(String serverBase,
			String surveyId) throws Exception {
		return parseQuestionGroups(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH + SurveyRestRequest.LIST_GROUP_ACTION
				+ "&" + SurveyRestRequest.SURVEY_ID_PARAM + "=" + surveyId));
	}

	/**
	 * gets question groups from the server for a specific survey
	 * 
	 * @param serverBase
	 * @param surveyId
	 * @return
	 * @throws Exception
	 */
	public static List<SurveyGroupDto> fetchSurveyGroups(String serverBase)
			throws Exception {
		return parseSurveyGroups(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH
				+ SurveyRestRequest.LIST_SURVEY_GROUPS_ACTION));
	}

	/**
	 * gets survey list from the server for a specific survey
	 * 
	 * @param serverBase
	 * @param surveyId
	 * @return
	 * @throws Exception
	 */
	public static List<SurveyDto> fetchSurveys(Long surveyGroupId,
			String serverBase) throws Exception {
		return parseSurveys(fetchDataFromServer(serverBase
				+ SURVEY_SERVLET_PATH + SurveyRestRequest.LIST_SURVEYS_ACTION
				+ "&" + SurveyRestRequest.SURVEY_GROUP_ID_PARAM + "="
				+ surveyGroupId));
	}

	/**
	 * parses a single SurveyInstanceDto from a json response string
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static SurveyInstanceDto parseSurveyInstance(String response)
			throws Exception {
		SurveyInstanceDto dto = null;
		if (response != null) {
			JSONArray arr = getJsonArray(response);
			if (arr != null && arr.length() > 0) {
				JSONObject json = arr.getJSONObject(0);
				if (json != null) {
					dto = new SurveyInstanceDto();
					if (json.has("keyId")) {
						dto.setKeyId(json.getLong("keyId"));
					}
					if (json.has("surveyId")) {
						dto.setSurveyId(json.getLong("surveyId"));
					}
					if (json.has("userID")) {
						dto.setUserID(json.getLong("userID"));
					}
					if (json.has("submitterName")) {
						dto.setSubmitterName(json.getString("submitterName"));
					}
					if (json.has("approvedFlag")) {
						dto.setApprovedFlag(json.getString("approvedFlag"));
					}
					if (json.has("deviceIdentifier")) {
						dto.setDeviceIdentifier(json
								.getString("deviceIdentifier"));
					}
				}
			}
		}
		return dto;
	}

	/**
	 * parses the question group response and forms DTOs
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static List<QuestionGroupDto> parseQuestionGroups(String response)
			throws Exception {
		List<QuestionGroupDto> dtoList = new ArrayList<QuestionGroupDto>();
		JSONArray arr = getJsonArray(response);
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject json = arr.getJSONObject(i);
				if (json != null) {
					QuestionGroupDto dto = new QuestionGroupDto();
					try {
						if (json.has("code")) {
							dto.setCode(json.getString("code"));
						}
						if (json.has("keyId")) {
							dto.setKeyId(json.getLong("keyId"));
						}
						if (json.has("displayName")) {
							dto.setName(json.getString("displayName"));
						}
						if (json.has("description")) {
							dto.setDescription(json.getString("description"));
						}
						if (json.has("order")) {
							dto.setOrder(json.getInt("order"));
						}
						if (json.has("path")) {
							dto.setPath(json.getString("path"));
						}
						if (json.has("surveyId")) {
							dto.setSurveyId(json.getLong("surveyId"));
						}

						dtoList.add(dto);
					} catch (Exception e) {
						System.out.println("Error in json parsing: " + e);
						e.printStackTrace();
					}
				}
			}
		}
		return dtoList;
	}

	/**
	 * parses the survey group response and forms DTOs
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static List<SurveyGroupDto> parseSurveyGroups(String response)
			throws Exception {
		List<SurveyGroupDto> dtoList = new ArrayList<SurveyGroupDto>();
		JSONArray arr = getJsonArray(response);
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject json = arr.getJSONObject(i);
				if (json != null) {
					SurveyGroupDto dto = new SurveyGroupDto();
					try {
						if (json.has("code")) {
							dto.setCode(json.getString("code"));
						}
						if (json.has("keyId")) {
							dto.setKeyId(json.getLong("keyId"));
						}
						if (json.has("displayName")) {
							dto.setName(json.getString("displayName"));
						}
						if (json.has("description")) {
							dto.setDescription(json.getString("description"));
						}
						dtoList.add(dto);
					} catch (Exception e) {
						System.out.println("Error in json parsing: " + e);
						e.printStackTrace();
					}
				}
			}
		}
		return dtoList;
	}

	/**
	 * parses the survey group response and forms DTOs
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static List<SurveyDto> parseSurveys(String response)
			throws Exception {
		List<SurveyDto> dtoList = new ArrayList<SurveyDto>();
		JSONArray arr = getJsonArray(response);
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				JSONObject json = arr.getJSONObject(i);
				if (json != null) {
					SurveyDto dto = new SurveyDto();
					try {
						if (json.has("code")) {
							dto.setCode(json.getString("code"));
						}
						if (json.has("keyId")) {
							dto.setKeyId(json.getLong("keyId"));
						}
						if (json.has("name")) {
							dto.setName(json.getString("name"));
						}
						if (json.has("description")) {
							dto.setDescription(json.getString("description"));
						}
						if (json.has("status")) {
							dto.setStatus(json.getString("status"));
						}
						if (json.has("path")) {
							dto.setPath(json.getString("path"));
						}
						if (json.has("surveyGroupId")) {
							dto.setSurveyGroupId(json.getLong("surveyGroupId"));
						}
						if (json.has("version")) {
							dto.setVersion(json.getString("version"));
						}
						dtoList.add(dto);
					} catch (Exception e) {
						System.out.println("Error in json parsing: " + e);
						e.printStackTrace();
					}
				}
			}
		}
		return dtoList;
	}

	private static List<DeviceFilesDto> parseDeviceFiles(String response)
			throws Exception {
		if (response.startsWith("{")) {
			List<DeviceFilesDto> dtoList = new ArrayList<DeviceFilesDto>();
			JSONArray arr = getJsonArray(response);

			if (arr != null) {

				for (int i = 0; i < arr.length(); i++) {
					JSONObject json = arr.getJSONObject(i);
					dtoList.add(parseDeviceFile(json));
				}
				return dtoList;
			}
			return null;
		}
		return null;
	}

	public static DeviceFilesDto parseDeviceFile(JSONObject json)
			throws JSONException {
		DeviceFilesDto dto = new DeviceFilesDto();
		if (json != null) {
			if (json.has("processingMessage")) {
				String x = json.getString("processingMessage");
				dto.setProcessingMessage(x);
			}
			if (json.has("phoneNumber")) {
				String x = json.getString("phoneNumber");
				dto.setPhoneNumber(x);
			}
			if (json.has("processedStatus")) {
				String x = json.getString("processedStatus");
				dto.setProcessedStatus(x);
			}
			if (json.has("checksum")) {
				String x = json.getString("checksum");
				dto.setChecksum(x);
			}
			if (json.has("processDate")) {
				String x = json.getString("processDate");
				dto.setProcessDate(x);
			}
			if (json.has("URI")) {
				String x = json.getString("URI");
				dto.setURI(x);
			}
			if (json.has("surveyInstanceId")) {
				String x = json.getString("surveyInstanceId");
				dto.setSurveyInstanceId(Long.parseLong(x));
			}
		}
		return dto;
	}

	/**
	 * parses question responses into QuesitonDto objects
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static List<QuestionDto> parseQuestions(String response)
			throws Exception {
		if (response.startsWith("{")) {
			List<QuestionDto> dtoList = new ArrayList<QuestionDto>();
			JSONArray arr = getJsonArray(response);
			if (arr != null) {
				for (int i = 0; i < arr.length(); i++) {
					JSONObject json = arr.getJSONObject(i);
					if (json != null) {
						QuestionDto dto = new QuestionDto();
						try {
							if (json.has("surveyId")) {
								if (json.getString("surveyId") != null) {
									String numberC = json.getString("surveyId");
									try {
										dto.setSurveyId(Long.parseLong(numberC));
									} catch (NumberFormatException nex) {
										dto.setSurveyId(null);
									}
								}
							}

							if (json.has("mandatoryFlag")) {
								if (json.getString("mandatoryFlag") != null)
									dto.setMandatoryFlag(Boolean.parseBoolean(json
											.getString("mandatoryFlag")));
								else
									dto.setMandatoryFlag(false);
							}
							if (json.has("allowMultipleFlag")) {
								if (json.getString("allowMultipleFlag") != null)
									dto.setAllowMultipleFlag(Boolean.parseBoolean(json
											.getString("allowMultipleFlag")));
								else
									dto.setAllowMultipleFlag(false);
							}
							if (json.has("allowOtherFlag")) {
								if (json.getString("allowOtherFlag") != null)
									dto.setAllowOtherFlag(Boolean.parseBoolean(json
											.getString("allowOtherFlag")));
								else
									dto.setAllowOtherFlag(null);
							}
							if (json.has("order")) {
								dto.setOrder(json.getInt("order"));
							}
							if (json.has("questionGroupId")) {
								dto.setQuestionGroupId(json
										.getLong("questionGroupId"));
							}
							if (json.has("tip")) {
								dto.setTip(json.getString("tip"));
							}
							if (json.has("path")) {
								dto.setPath(json.getString("path"));
							}
							if (json.has("text")) {
								dto.setText(json.getString("text"));
							}
							if (json.has("keyId")) {
								dto.setKeyId(json.getLong("keyId"));
							}
							if (json.has("mandatoryFlag")) {
								dto.setMandatoryFlag(Boolean.parseBoolean(json
										.getString("mandatoryFlag")));
							}
							if (json.has("isName")) {
								dto.setIsName(Boolean.parseBoolean(json
										.getString("isName")));
							}
							if (json.has("allowDecimal")) {
								dto.setAllowDecimal(Boolean.parseBoolean(json
										.getString("allowDecimal")));
							}
							if (json.has("allowSign")) {
								dto.setAllowSign(Boolean.parseBoolean(json
										.getString("allowSign")));
							}
							if (json.has("minVal")) {
								try {
									dto.setMinVal(json.getDouble("minVal"));
								} catch (Exception e) {
									dto.setMinVal(null);
								}
							}
							if (json.has("maxVal")) {
								try {
									dto.setMaxVal(json.getDouble("maxVal"));
								} catch (Exception e) {
									dto.setMaxVal(null);
								}
							}

							if (json.has("translationMap")
									&& !JSONObject.NULL.equals(json
											.get("translationMap"))) {
								dto.setTranslationMap(parseTranslations(json
										.getJSONObject("translationMap")));
							}
							if (json.has("questionTypeString")
									&& json.getString("questionTypeString") != null) {
								dto.setType(QuestionDto.QuestionType.valueOf(json
										.getString("questionTypeString")));
							}

							if (json.has("optionContainerDto")
									&& !JSONObject.NULL.equals(json
											.get("optionContainerDto"))) {
								OptionContainerDto container = new OptionContainerDto();
								JSONObject contJson = json
										.getJSONObject("optionContainerDto");
								if (contJson.has("optionsList")
										&& !JSONObject.NULL.equals(contJson
												.get("optionsList"))) {
									JSONArray optArray = contJson
											.getJSONArray("optionsList");
									if (optArray != null) {
										for (int j = 0; j < optArray.length(); j++) {
											JSONObject optJson = optArray
													.getJSONObject(j);
											QuestionOptionDto opt = new QuestionOptionDto();
											opt.setKeyId(optJson
													.getLong("keyId"));
											opt.setText(optJson
													.getString("text"));
											opt.setOrder(optJson
													.getInt("order"));
											if (optJson.has("translationMap")
													&& !JSONObject.NULL
															.equals(optJson
																	.get("translationMap"))) {
												opt.setTranslationMap(parseTranslations(optJson
														.getJSONObject("translationMap")));
											}
											container.addQuestionOption(opt);
										}
									}
									dto.setOptionContainerDto(container);
								}
								if (json.has("questionDependency")
										&& !JSONObject.NULL.equals(json
												.get("questionDependency"))) {
									QuestionDependencyDto dep = new QuestionDependencyDto();
									JSONObject depJson = json
											.getJSONObject("questionDependency");
									dep.setQuestionId(depJson
											.getLong("questionId"));
									dep.setAnswerValue(depJson
											.getString("answerValue"));
									dto.setQuestionDependency(dep);
								}
							}
							dtoList.add(dto);
						} catch (Exception e) {
							System.out.println("Error in json parsing: " + e);
							e.printStackTrace();
						}
					}
				}
			}
			return dtoList;
		} else
			return null;
	}

	@SuppressWarnings("unchecked")
	private static TreeMap<String, TranslationDto> parseTranslations(
			JSONObject translationMapJson) throws Exception {
		Iterator<String> keyIter = translationMapJson.keys();
		TreeMap<String, TranslationDto> translationMap = null;
		if (keyIter != null) {
			translationMap = new TreeMap<String, TranslationDto>();
			String lang = keyIter.next();
			JSONObject transObj = translationMapJson.getJSONObject(lang);
			if (transObj != null) {
				TranslationDto tDto = new TranslationDto();
				tDto.setKeyId(transObj.getLong("keyId"));
				tDto.setParentId(transObj.getLong(("parentId")));
				tDto.setParentType(transObj.getString("parentType"));
				tDto.setLangCode(lang);
				tDto.setText(transObj.getString("text"));
				translationMap.put(lang, tDto);
			}
		}
		return translationMap;
	}

	/**
	 * invokes a remote REST api using the base and query string passed in. If
	 * shouldSign is true, the queryString will be augmented with a timestamp
	 * and hash parameter.
	 * 
	 * @param baseUrl
	 * @param queryString
	 * @param shouldSign
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String fetchDataFromServer(String baseUrl,
			String queryString, boolean shouldSign, String key)
			throws Exception {
		if (shouldSign) {
			if (queryString == null) {
				queryString = new String();
			} else {
				if (queryString.trim().startsWith("?")) {
					queryString = queryString.trim().substring(1);
				}
				queryString += "&";
			}
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			queryString += RestRequest.TIMESTAMP_PARAM + "="
					+ URLEncoder.encode(df.format(new Date()), "UTF-8");
			queryString = sortQueryString(queryString);
			queryString += "&" + RestRequest.HASH_PARAM + "="
					+ MD5Util.generateHMAC(queryString, key);
		}
		return fetchDataFromServer(baseUrl
				+ ((queryString != null && queryString.trim().length() > 0) ? "?"
						+ queryString
						: ""));

	}

	/**
	 * invokes a remote REST api. If the url is longer than 1900 characters,
	 * this method will use POST since that is too long for a GET
	 * 
	 * @param fullUrl
	 * @return
	 * @throws Exception
	 */
	public static String fetchDataFromServer(String fullUrl) throws Exception {
		if (fullUrl != null) {
			if (fullUrl.length() > 1900) {
				return fetchDataFromServerPOST(fullUrl);
			} else {
				return fetchDataFromServerGET(fullUrl);
			}
		} else {
			return null;
		}
	}

	/**
	 * executes a post to invoke a rest api
	 */
	private static String fetchDataFromServerPOST(String fullUrl)
			throws Exception {
		BufferedReader reader = null;
		String result = null;
		try {
			String baseUrl = fullUrl;
			String queryString = null;
			if (fullUrl.contains("?")) {
				baseUrl = fullUrl.substring(0, fullUrl.indexOf("?"));
				queryString = fullUrl.substring(fullUrl.indexOf("?") + 1);
			}
			URL url = new URL(baseUrl);
			System.out.println("Calling: " + baseUrl + " with params: "
					+ queryString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setConnectTimeout(30000);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Length",
					"" + Integer.toString(queryString.getBytes().length));
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Accept-Encoding", "gzip");
			conn.addRequestProperty("User-Agent", "gzip");

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(queryString);
			wr.flush();
			wr.close();
			InputStream instream = conn.getInputStream();
			String contentEncoding = conn.getHeaderField("Content-Encoding");

			if (contentEncoding != null
					&& contentEncoding.equalsIgnoreCase("gzip")) {
				reader = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(instream), "UTF-8"));
			} else {
				reader = new BufferedReader(new InputStreamReader(instream,
						"UTF-8"));
			}

			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result;
	}

	/**
	 * executes a GET to invoke a rest api
	 */
	private static String fetchDataFromServerGET(String fullUrl)
			throws Exception {
		BufferedReader reader = null;
		String result = null;
		try {
			URL url = new URL(fullUrl);
			System.out.println("Calling: " + url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setConnectTimeout(30000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.addRequestProperty("Accept-Encoding", "gzip");
			conn.addRequestProperty("User-Agent", "gzip");
			InputStream instream = conn.getInputStream();
			String contentEncoding = conn.getHeaderField("Content-Encoding");

			if (contentEncoding != null
					&& contentEncoding.equalsIgnoreCase("gzip")) {
				reader = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(instream), "UTF-8"));
			} else {
				reader = new BufferedReader(new InputStreamReader(instream,
						"UTF-8"));
			}

			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return result;
	}

	private static String sortQueryString(String queryString) {
		String[] parts = queryString.split("&");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (int i = 0; i < parts.length; i++) {
			String[] nvp = parts[i].split("=");
			if (nvp.length > 1) {
				if (nvp.length == 2) {
					pairs.add(new NameValuePair(nvp[0], nvp[1]));
				} else {
					// if we're here, we have multiple "=" so we need to merge
					// parts 1..n
					StringBuilder builder = new StringBuilder();
					for (int j = 1; j < nvp.length; j++) {
						if (builder.length() > 0) {
							builder.append("=");
						}
						builder.append(nvp[j]);
					}
					pairs.add(new NameValuePair(nvp[0], builder.toString()));
				}
			}
		}
		// now sort the names

		Collections.sort(pairs);
		StringBuilder result = new StringBuilder();
		for (NameValuePair nvp : pairs) {
			if (result.length() > 0) {
				result.append("&");
			}
			result.append(nvp.getName()).append("=").append(nvp.getValue());
		}
		return result.toString();
	}

	/**
	 * converts the string into a JSON array object.
	 */
	public static JSONArray getJsonArray(String response) throws Exception {
		System.out.println("response: " + response);
		if (response != null) {
			JSONObject json = new JSONObject(response);
			if (json != null) {
				return json.getJSONArray(RESPONSE_KEY);
			}
		}
		return null;
	}

}