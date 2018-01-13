package org.xonyne.events.model;

import java.lang.Long;
import java.lang.String;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author ridwann
 * @version 1.0
 * @created 13-Nov-2017 10:10:15 PM
 */
@Entity
@Table(name="place")
public class Place {

	@Id
	@Column(name="place_id")
	private Long id;
	private String name;
	
	@OneToOne(targetEntity=Location.class, cascade={CascadeType.ALL})
	@JoinColumn(name="location_Id")
	public Location location;
        
        @Transient
        private boolean isStale;

	public Place(){

	}

	public Place(Long id, String name, Location location) {
		super();
		this.id = id;
		this.name = name;
		this.location = location;
                this.isStale = false;
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
        
        public boolean getIsStale() {
		return this.isStale;
	}

	public void setIsStale(boolean isStale) {
		this.isStale = isStale;
	}

	
}