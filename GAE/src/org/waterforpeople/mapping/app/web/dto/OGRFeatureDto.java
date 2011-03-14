package org.waterforpeople.mapping.app.web.dto;

import java.util.ArrayList;

import javax.jdo.annotations.Persistent;

import com.gallatinsystems.framework.gwt.dto.client.BaseDto;
import com.gallatinsystems.gis.map.domain.GeoMeasure;
import com.gallatinsystems.gis.map.domain.Geometry;


public class OGRFeatureDto extends BaseDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = -14513077811663164L;
	private String name = null;
	private String projectCoordinateSystemIdentifier = null;
	private String geoCoordinateSystemIdentifier = null;
	private String datumIdentifier = null;
	private Double spheroid = null;
	private String countryCode = null;
	private String unCode = null;
	private Integer pop2005 = null;
	private Double centroidLat = null;
	private Double centroidLon = null;
	private Double x1 = null;
	private Double x2 = null;
	private Double y1 = null;
	private Double y2 = null;
	private Double reciprocalOfFlattening = null;
	private FeatureType featureType = null;
	private String sub1 = null;
	private String sub2 = null;
	private String sub3 = null;
	private String sub4 = null;
	private String sub5 = null;
	private String sub6 = null;
	private Integer density = null;
	private Integer totalPopulation = null;
	private Integer femalePopulation = null;
	private Integer malePopulation = null;
	private Integer numberHouseholds = null;

	public Integer getDensity() {
		return density;
	}

	public void setDensity(Integer density) {
		this.density = density;
	}

	public Integer getTotalPopulation() {
		return totalPopulation;
	}

	public void setTotalPopulation(Integer totalPopulation) {
		this.totalPopulation = totalPopulation;
	}

	public Integer getFemalePopulation() {
		return femalePopulation;
	}

	public void setFemalePopulation(Integer femalePopulation) {
		this.femalePopulation = femalePopulation;
	}

	public Integer getMalePopulation() {
		return malePopulation;
	}

	public void setMalePopulation(Integer malePopulation) {
		this.malePopulation = malePopulation;
	}

	public Integer getNumberHouseholds() {
		return numberHouseholds;
	}

	public void setNumberHouseholds(Integer numberHouseholds) {
		this.numberHouseholds = numberHouseholds;
	}

	public enum FeatureType {
		COUNTRY, SUB_COUNTRY_OTHER
	};

	public String getSub1() {
		return sub1;
	}

	public void setSub1(String sub1) {
		this.sub1 = sub1;
	}

	public String getSub2() {
		return sub2;
	}

	public void setSub2(String sub2) {
		this.sub2 = sub2;
	}

	public String getSub3() {
		return sub3;
	}

	public void setSub3(String sub3) {
		this.sub3 = sub3;
	}

	public String getSub4() {
		return sub4;
	}

	public void setSub4(String sub4) {
		this.sub4 = sub4;
	}

	public String getSub5() {
		return sub5;
	}

	public void setSub5(String sub5) {
		this.sub5 = sub5;
	}

	public String getSub6() {
		return sub6;
	}

	public void setSub6(String sub6) {
		this.sub6 = sub6;
	}

	public String getUnCode() {
		return unCode;
	}

	public void setUnCode(String unCode) {
		this.unCode = unCode;
	}

	public Integer getPop2005() {
		return pop2005;
	}

	public void setPop2005(Integer pop2005) {
		this.pop2005 = pop2005;
	}

	public Double getCentroidLat() {
		return centroidLat;
	}

	public void setCentroidLat(Double centroidLat) {
		this.centroidLat = centroidLat;
	}

	public Double getCentroidLon() {
		return centroidLon;
	}

	public void setCentroidLon(Double centroidLon) {
		this.centroidLon = centroidLon;
	}

	public String getProjectCoordinateSystemIdentifier() {
		return projectCoordinateSystemIdentifier;
	}

	public void setProjectCoordinateSystemIdentifier(
			String projectCoordinateSystemIdentifier) {
		this.projectCoordinateSystemIdentifier = projectCoordinateSystemIdentifier;
	}

	public String getGeoCoordinateSystemIdentifier() {
		return geoCoordinateSystemIdentifier;
	}

	public void setGeoCoordinateSystemIdentifier(
			String geoCoordinateSystemIdentifier) {
		this.geoCoordinateSystemIdentifier = geoCoordinateSystemIdentifier;
	}

	public String getDatumIdentifier() {
		return datumIdentifier;
	}

	public void setDatumIdentifier(String datumIdentifier) {
		this.datumIdentifier = datumIdentifier;
	}

	public Double getSpheroid() {
		return spheroid;
	}

	public void setSpheroid(Double spheroid) {
		this.spheroid = spheroid;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public Double[] getBoundingBox() {
		return new Double[] { x1, y1, x2, y2 };
	}

	public void setBoundingBox(Double[] boundingBox) {
		x1 = boundingBox[0];
		y1 = boundingBox[1];
		x2 = boundingBox[2];
		y2 = boundingBox[3];
	}

	public void addBoundingBox(Double x1, Double y1, Double x2, Double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;

	}

	public ArrayList<GeoMeasure> getPropertyList() {
		return propertyList;
	}

	public void setPropertyList(ArrayList<GeoMeasure> propertyList) {
		this.propertyList = propertyList;
	}

	@Persistent
	private ArrayList<GeoMeasure> propertyList = null;

	private Geometry geometry = null;

	public ArrayList<GeoMeasure> getpropertyList() {
		return propertyList;
	}

	public void setpropertyList(ArrayList<GeoMeasure> propertyList) {
		this.propertyList = propertyList;
	}

	public Geometry getGeometry() {
		if (geometry == null)
			return new Geometry();
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public void addGeoMeasure(String name, String type, String value) {
		if (propertyList == null)
			propertyList = new ArrayList<GeoMeasure>();
		GeoMeasure geoMeasure = new GeoMeasure();
		geoMeasure.setName(name);
		geoMeasure.setType(type);
		geoMeasure.setValue(value);
		propertyList.add(geoMeasure);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setReciprocalOfFlattening(Double reciprocalOfFlattening) {
		this.reciprocalOfFlattening = reciprocalOfFlattening;
	}

	public Double getReciprocalOfFlattening() {
		return reciprocalOfFlattening;
	}

	public void setFeatureType(FeatureType featureType) {
		this.featureType = featureType;
	}

	public FeatureType getFeatureType() {
		return featureType;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CountryCode: " + countryCode + " ");
		sb.append("FeatureType: " + featureType + " ");
		sb.append("Sub1: " + sub1 + " ");
		sb.append("Sub2: " + sub2 + " ");
		sb.append("Sub3: " + sub3 + " ");
		sb.append("Sub4: " + sub4 + " ");
		sb.append("Sub5: " + sub5 + " ");
		sb.append("Sub6: " + sub6 + " ");
		return sb.toString();
	}
}
