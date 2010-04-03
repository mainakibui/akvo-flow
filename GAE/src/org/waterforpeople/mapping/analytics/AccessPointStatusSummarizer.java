package org.waterforpeople.mapping.analytics;

import org.waterforpeople.mapping.analytics.dao.AccessPointStatusSummaryDao;
import org.waterforpeople.mapping.dao.CommunityDao;
import org.waterforpeople.mapping.domain.AccessPoint;
import org.waterforpeople.mapping.domain.Country;

import com.gallatinsystems.framework.analytics.summarization.DataSummarizer;
import com.gallatinsystems.framework.dao.BaseDAO;

/**
 * This class will populate country/community lookup tables based on geo
 * information in the access piont.
 * 
 * @author Christopher Fagiani
 * 
 */
public class AccessPointStatusSummarizer implements DataSummarizer {

	@Override
	public boolean performSummarization(String key, String type) {
		if (key != null) {
			BaseDAO<AccessPoint> accessPointDao = new BaseDAO<AccessPoint>(
					AccessPoint.class);
			AccessPoint ap = accessPointDao.getByKey(Long.parseLong(key));
			if (ap != null) {
				CommunityDao commDao = new CommunityDao();
				Country c = commDao.findCountryByCommunity(ap
						.getCommunityCode());
				AccessPointStatusSummaryDao.incrementCount(ap, c);
			}
		}
		return true;
	}

}
