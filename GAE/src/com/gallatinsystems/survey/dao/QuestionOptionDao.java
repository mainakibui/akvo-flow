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

package com.gallatinsystems.survey.dao;

import java.util.List;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import com.gallatinsystems.framework.dao.BaseDAO;
import com.gallatinsystems.framework.servlet.PersistenceFilter;
import com.gallatinsystems.survey.domain.QuestionOption;
import com.gallatinsystems.survey.domain.Translation;

/**
 * Dao for manipulating questionOptions
 * 
 * @author Christopher Fagiani
 * 
 */
public class QuestionOptionDao extends BaseDAO<QuestionOption> {

	private TranslationDao translationDao;

	public QuestionOptionDao() {
		super(QuestionOption.class);
		translationDao = new TranslationDao();
	}

	/**
	 * lists all options for a given quesiton id, including the translations (if
	 * any)
	 * 
	 * @param questionId
	 * @return
	 */
	public TreeMap<Integer, QuestionOption> listOptionByQuestion(Long questionId) {
		List<QuestionOption> oList = listByProperty("questionId", questionId,
				"Long", "order", "asc");
		TreeMap<Integer, QuestionOption> map = new TreeMap<Integer, QuestionOption>();
		if (oList != null) {
			int i = 1;
			for (QuestionOption o : oList) {
				o.setTranslationMap(translationDao.findTranslations(
						Translation.ParentType.QUESTION_OPTION, o.getKey()
								.getId()));
				i++;
				map.put(o.getOrder() != null ? o.getOrder() : i, o);
			}
		}
		return map;
	}

	/**
	 * deletes all options associated with a given question
	 * 
	 * @param questionId
	 */
	public void deleteOptionsForQuestion(Long questionId) {
		List<QuestionOption> oList = listByProperty("questionId", questionId,
				"Long");
		if (oList != null) {
			PersistenceManager pm = PersistenceFilter.getManager();
			TranslationDao tDao = new TranslationDao();
			for (QuestionOption opt : oList) {
				tDao.deleteTranslationsForParent(opt.getKey().getId(),
						Translation.ParentType.QUESTION_OPTION);
				pm.deletePersistent(opt);
			}
		}
	}
}
