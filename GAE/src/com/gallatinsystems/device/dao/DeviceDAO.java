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

package com.gallatinsystems.device.dao;

import java.util.Date;
import java.util.logging.Logger;

import com.gallatinsystems.device.domain.Device;
import com.gallatinsystems.device.domain.Device.DeviceType;
import com.gallatinsystems.framework.dao.BaseDAO;

/**
 * data access object for devices.
 * 
 * @author Christopher Fagiani
 * 
 */
public class DeviceDAO extends BaseDAO<Device> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DeviceDAO.class
			.getName());

	public DeviceDAO() {
		super(Device.class);
	}

	/**
	 * gets a single device by phoneNumber. If phone number is not unique (this
	 * shouldn't happen), it returns the first instance found.
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public Device get(String phoneNumber) {
		return super.findByProperty("phoneNumber", phoneNumber, STRING_TYPE);
	}

	/**
	 * updates the device's last known position
	 * 
	 * @param phoneNumber
	 * @param lat
	 * @param lon
	 * @param accuracy
	 * @param version
	 * @param deviceIdentifier
	 */
	public void updateDeviceLocation(String phoneNumber, Double lat,
			Double lon, Double accuracy, String version, String deviceIdentifier) {
		Device d = get(phoneNumber);
		if (d == null) {
			// if device is null, we have to create it
			d = new Device();
			d.setCreatedDateTime(new Date());
			d.setDeviceType(DeviceType.CELL_PHONE_ANDROID);
			d.setPhoneNumber(phoneNumber);
		}
		if (lat != null && lon != null) {
			d.setLastKnownLat(lat);
			d.setLastKnownLon(lon);
			d.setLastKnownAccuracy(accuracy);
		}
		if (deviceIdentifier != null) {
			d.setDeviceIdentifier(deviceIdentifier);
		}
		d.setLastLocationBeaconTime(new Date());
		d.setGallatinSoftwareManifest(version);
		// only save if d isn't already a persistent object
		if (d.getKey() == null) {
			save(d);
		}
	}
}
