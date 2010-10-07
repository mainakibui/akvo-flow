package org.waterforpeople.mapping.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import com.gallatinsystems.device.domain.DeviceFiles;
import com.gallatinsystems.framework.dao.BaseDAO;
import com.gallatinsystems.framework.servlet.PersistenceFilter;

public class DeviceFilesDao extends BaseDAO<DeviceFiles> {

	public DeviceFilesDao() {
		super(DeviceFiles.class);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	public List<DeviceFiles> listDeviceFilesByDate(Date startDate,
			String cursorString) {
		PersistenceManager pm = PersistenceFilter.getManager();
		javax.jdo.Query query = pm.newQuery(DeviceFiles.class);
		Map<String, Object> paramMap = null;

		StringBuilder filterString = new StringBuilder();
		StringBuilder paramString = new StringBuilder();
		paramMap = new HashMap<String, Object>();
		/*
		 * appendNonNullParam("createdDateTime", filterString, paramString,
		 * "Date", startDate, paramMap, GTE_OP); if (startDate != null) {
		 * query.declareImports("import java.util.Date"); }
		 */
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		java.util.Date date = new java.util.Date();
		String dateTime = dateFormat.format(date);
		appendNonNullParam("processDate", filterString, paramString, "String",
				dateTime, paramMap, GTE_OP);
		//query.setOrdering("createdDateTime desc");
		query.setFilter(filterString.toString());
		query.declareParameters(paramString.toString());

		prepareCursor(cursorString, query);

		List<DeviceFiles> results = (List<DeviceFiles>) query
				.executeWithMap(paramMap);

		return results;
	}

}