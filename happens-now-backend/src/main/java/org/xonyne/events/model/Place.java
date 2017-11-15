package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="place")
@SequenceGenerator(name="place_placeId_seq", sequenceName="place_placeId_seq", initialValue = 1, allocationSize = 1)
public class Place {

	@GeneratedValue(generator="place_placeId_seq")
	@Id
	private Long id;
	private String name;
	
	@OneToOne()
	@JoinColumn(name="locationId")
	public Location location;

	public Place(){

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	
}