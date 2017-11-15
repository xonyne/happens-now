package org.xonyne.events.model;

import java.lang.Long;

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
@Table(name="rating")
@SequenceGenerator(name="tating_ratingId_seq", sequenceName="rating_ratingId_seq", initialValue = 1, allocationSize = 1)
public class Rating {

	@GeneratedValue(generator="rating_ratingId_seq")
	@Id
	private Long id;
	private Integer rating;
	
	@OneToOne()
	@JoinColumn(name="eventId")
	private Event event;
	@OneToOne()
	@JoinColumn(name="userId")
	private User user;

	public Rating(){

	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	
}