package com.gallatinsystems.survey.domain.refactor;

import java.util.HashMap;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.gallatinsystems.framework.domain.BaseDomain;
import com.google.appengine.api.datastore.Key;
@PersistenceCapable
public class Question extends BaseDomain {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9123426646238761996L;

	public enum Type {
		GEO, TEXT, OPTION, VIDEO, PHOTO
	};

	private Type type = null;
	@Persistent(serialized="true") 
	private HashMap<String, String> tipMap;
	@Persistent(serialized="true") 
	private HashMap<String, String> textMap;
	private Boolean dependentFlag = null;
	private Boolean allowMultipleFlag = null;
	private Boolean allowOtherFlag = null;
	private Key dependentQuestionKey = null;
	@Persistent(serialized="true") 
	private HashMap<String, Key> questionOptionMap = null;
	private String validationRule = null;
	@Persistent(serialized="true") 
	private HashMap<Integer, Key> questionHelpMediaMap = null;

	public void addTip(String langCode, String tip) {
		if (tipMap == null)
			tipMap = new HashMap<String, String>();
		tipMap.put(langCode, tip);
	}

	public void addText(String langCode, String text) {
		if (textMap == null)
			textMap = new HashMap<String, String>();
		textMap.put(langCode, text);
	}

	public void addQuestionOption(String langCode, Key questionOptionKey) {
		if (questionOptionMap == null)
			questionOptionMap = new HashMap<String, Key>();
		questionOptionMap.put(langCode, questionOptionKey);
	}

	public void addHelpMedia(Integer order, Key key) {
		if (questionHelpMediaMap == null)
			questionHelpMediaMap = new HashMap<Integer, Key>();
		questionHelpMediaMap.put(order, key);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public HashMap<String, String> getTipMap() {
		return tipMap;
	}

	public void setTipMap(HashMap<String, String> tipMap) {
		this.tipMap = tipMap;
	}

	public HashMap<String, String> getTextMap() {
		return textMap;
	}

	public void setTextMap(HashMap<String, String> textMap) {
		this.textMap = textMap;
	}

	public Boolean getDependentFlag() {
		return dependentFlag;
	}

	public void setDependentFlag(Boolean dependentFlag) {
		this.dependentFlag = dependentFlag;
	}

	public Boolean getAllowMultipleFlag() {
		return allowMultipleFlag;
	}

	public void setAllowMultipleFlag(Boolean allowMultipleFlag) {
		this.allowMultipleFlag = allowMultipleFlag;
	}

	public Boolean getAllowOtherFlag() {
		return allowOtherFlag;
	}

	public void setAllowOtherFlag(Boolean allowOtherFlag) {
		this.allowOtherFlag = allowOtherFlag;
	}

	public Key getDependentQuestionKey() {
		return dependentQuestionKey;
	}

	public void setDependentQuestionKey(Key dependentQuestionKey) {
		this.dependentQuestionKey = dependentQuestionKey;
	}

	public HashMap<String, Key> getQuestionOptionMap() {
		return questionOptionMap;
	}

	public void setQuestionOptionMap(HashMap<String, Key> questionOptionMap) {
		this.questionOptionMap = questionOptionMap;
	}

	public String getValidationRule() {
		return validationRule;
	}

	public void setValidationRule(String validationRule) {
		this.validationRule = validationRule;
	}

	public void setQuestionHelpMediaMap(
			HashMap<Integer, Key> questionHelpMediaMap) {
		this.questionHelpMediaMap = questionHelpMediaMap;
	}

	public HashMap<Integer, Key> getQuestionHelpMediaMap() {
		return questionHelpMediaMap;
	}

}