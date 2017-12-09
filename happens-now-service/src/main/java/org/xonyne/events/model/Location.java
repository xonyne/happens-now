package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;
import java.lang.Double;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="location")
@SequenceGenerator(name="location_id_seq", sequenceName="location_id_seq", initialValue = 1, allocationSize = 1)
public class Location {

	@GeneratedValue(generator="location_id_seq")
	@Id
	@Column(name="location_id")
	private Long id;
	public Location(Long id, String city, String country, String street,
			String zip, Double latitude, Double longitude) {
		super();
		this.id = id;
		this.city = city;
		this.country = country;
		this.street = street;
		this.zip = zip;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	private String city;
	private String country;
	private String street;
	private String zip;
	private Double latitude;
	private Double longitude;

	public Location(){

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}