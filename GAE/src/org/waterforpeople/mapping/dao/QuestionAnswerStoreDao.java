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

package org.waterforpeople.mapping.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;

import org.waterforpeople.mapping.domain.QuestionAnswerStore;

import com.gallatinsystems.framework.dao.BaseDAO;
import com.gallatinsystems.framework.servlet.PersistenceFilter;

public class QuestionAnswerStoreDao extends BaseDAO<QuestionAnswerStore> {

	public QuestionAnswerStoreDao() {
		super(QuestionAnswerStore.class);
	}

	public List<QuestionAnswerStore> listBySurvey(Long surveyId) {
		return super.listByProperty("surveyId", surveyId, "Long");

	}

	public List<QuestionAnswerStore> listByQuestion(Long questionId) {
		return super.listByProperty("questionID", questionId.toString(),
				"String");
	}

	/**
	 * lists all the QuestionAnswerStore objects that match the type passed in
	 * 
	 * @param type
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<QuestionAnswerStore> listByTypeAndDate(String type,
			Long surveyId, Date sinceDate, String cursor, Integer pageSize) {

		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

		Map<String, Object> paramMap = null;

		StringBuilder filterString = new StringBuilder();
		StringBuilder paramString = new StringBuilder();
		paramMap = new HashMap<String, Object>();

		appendNonNullParam("type", filterString, paramString, "String", type,
				paramMap);
		appendNonNullParam("surveyId", filterString, paramString, "Long",
				surveyId, paramMap);
		appendNonNullParam("lastUpdateDateTime", filterString, paramString,
				"Date", sinceDate, paramMap, GTE_OP);
		if (sinceDate != null) {
			query.declareImports("import java.util.Date");
		}
		query.setFilter(filterString.toString());
		query.declareParameters(paramString.toString());
		prepareCursor(cursor, pageSize, query);
		return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
	}

	public List<QuestionAnswerStore> listByTypeValue(String type, String value) {
		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

		Map<String, Object> paramMap = null;

		StringBuilder filterString = new StringBuilder();
		StringBuilder paramString = new StringBuilder();
		paramMap = new HashMap<String, Object>();

		appendNonNullParam("type", filterString, paramString, "String", type,
				paramMap);
		appendNonNullParam("value", filterString, paramString, "String", value,
				paramMap);
		query.setFilter(filterString.toString());
		query.declareParameters(paramString.toString());
		return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
	}

	/**
	 * lists all the QuestionAnswerStore objects that match the type passed in
	 * 
	 * @param sinceDate
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<QuestionAnswerStore> listByNotNullCollectionDateBefore(
			Date sinceDate, String cursor, Integer pageSize) {

		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

		Map<String, Object> paramMap = null;

		StringBuilder filterString = new StringBuilder();
		StringBuilder paramString = new StringBuilder();
		paramMap = new HashMap<String, Object>();
		appendNonNullParam("collectionDate", filterString, paramString, "Date",
				sinceDate, paramMap, LTE_OP);
		if (sinceDate != null) {
			query.declareImports("import java.util.Date");
		}
		query.setFilter(filterString.toString());
		query.declareParameters(paramString.toString());
		prepareCursor(cursor, pageSize, query);
		return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
	}

	
	@SuppressWarnings("unchecked")
	public List<QuestionAnswerStore> listByNotNullCollectionDateAfter(
			Date sinceDate, String cursor, Integer pageSize) {

		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(QuestionAnswerStore.class);

		Map<String, Object> paramMap = null;

		StringBuilder filterString = new StringBuilder();
		StringBuilder paramString = new StringBuilder();
		paramMap = new HashMap<String, Object>();
		appendNonNullParam("collectionDate", filterString, paramString, "Date",
				sinceDate, paramMap, GTE_OP);
		if (sinceDate != null) {
			query.declareImports("import java.util.Date");
		}
		query.setFilter(filterString.toString());
		query.declareParameters(paramString.toString());
		//prepareCursor(cursor, pageSize, query);
		log.log(Level.INFO,query.toString());
		return (List<QuestionAnswerStore>) query.executeWithMap(paramMap);
	}
	/**
	 * lists all the QuestionAnswerStore objects that match the type passed in
	 * 
	 * @param sinceDate
	 * @param cursor
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<QuestionAnswerStore> listByExactDateString() {

		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery("select from "
				+ QuestionAnswerStore.class.getName()
				+ " where collectionDate == 5674906531303000000");
		log.log(Level.INFO,query.toString());
		return (List<QuestionAnswerStore>) query.execute();
	}
}
